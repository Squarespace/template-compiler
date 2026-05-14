/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template.compat;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.template.Compiler;
import com.squarespace.template.Context;
import com.squarespace.template.FormatterTable;
import com.squarespace.template.PredicateTable;


/**
 * Enforces compatibility level invariants. Levels are defined by the
 * patch thresholds and must behave the same at every level in between.
 */
public class CompatLevelTest {

  @Test
  public void testFixedLevel() {
    // The fully fixed compiler is the highest level.
    CompatLevel compat = CompatLevel.fixed();
    assertEquals(compat.level(), Patch.maxThreshold());
    for (Patch patch : Patch.values()) {
      assertFalse(compat.enabled(patch), patch.name());
    }
  }

  @Test
  public void testDefaultLevel() {
    // The default keeps every legacy behavior active, the released surface.
    CompatLevel compat = CompatLevel.defaultLevel();
    assertEquals(compat.level(), 0);
    for (Patch patch : Patch.values()) {
      assertTrue(compat.enabled(patch), patch.name());
    }
  }

  @Test
  public void testLevelThresholds() {
    // The legacy active set at level L is the patches with threshold above L.
    // A fix applies at its threshold level and above.
    for (int level = 0; level <= Patch.maxThreshold(); level++) {
      CompatLevel compat = CompatLevel.at(level);
      for (Patch patch : Patch.values()) {
        assertEquals(compat.enabled(patch), patch.threshold() > level, patch.name() + " at level " + level);
      }
    }
  }

  @Test
  public void testMonotonic() {
    // Raising a site level can only fix behaviors, never re-enable legacy.
    for (int level = 0; level < Patch.maxThreshold(); level++) {
      for (Patch patch : Patch.values()) {
        if (CompatLevel.at(level + 1).enabled(patch)) {
          assertTrue(CompatLevel.at(level).enabled(patch), patch.name());
        }
      }
    }
  }

  @Test
  public void testOverrides() {
    CompatLevel base = CompatLevel.fixed();
    CompatLevel patched = base.withPatch(Patch.MOD_ZERO);
    for (Patch patch : Patch.values()) {
      assertEquals(patched.enabled(patch), patch == Patch.MOD_ZERO, patch.name());
    }
    // The original level is untouched.
    for (Patch patch : Patch.values()) {
      assertFalse(base.enabled(patch), patch.name());
    }
    // An override on an already active patch is a no-op for that patch.
    CompatLevel defaulted = CompatLevel.defaultLevel().withPatch(Patch.MOD_ZERO);
    assertTrue(defaulted.enabled(Patch.MOD_ZERO));
    assertEquals(defaulted.level(), 0);
  }

  @Test
  public void testWithLevelPreservesOverrides() throws Exception {
    // A level change must not drop the override set.
    CompatLevel patched = CompatLevel.at(0).withPatch(Patch.MOD_ZERO).withLevel(Patch.maxThreshold());
    assertEquals(patched.level(), Patch.maxThreshold());
    for (Patch patch : Patch.values()) {
      assertEquals(patched.enabled(patch), patch == Patch.MOD_ZERO, patch.name());
    }
    // The same guarantee at the executor level, in both setter orders.
    // At level 2 the threshold-3 patches are still legacy active.
    Compiler compiler = new Compiler(new FormatterTable(), new PredicateTable());
    Context ctx = compiler.newExecutor()
        .template("x")
        .json("{}")
        .compatPatch(Patch.MOD_ZERO)
        .compatLevel(2)
        .execute();
    assertTrue(ctx.compatEnabled(Patch.MOD_ZERO), "override must survive a later compatLevel");
    assertFalse(ctx.compatEnabled(Patch.JSON_START_KEYWORD));
    assertTrue(ctx.compatEnabled(Patch.TIMEZONE_NULL_LITERAL));
    assertEquals(ctx.getCompat().level(), 2);
    ctx = compiler.newExecutor()
        .template("x")
        .json("{}")
        .compatPatch(Patch.MOD_ZERO)
        .compat(CompatLevel.at(2))
        .execute();
    assertTrue(ctx.compatEnabled(Patch.MOD_ZERO), "override must survive a later compat(...)");
    assertEquals(ctx.getCompat(), CompatLevel.at(2).withPatch(Patch.MOD_ZERO));
    ctx = compiler.newExecutor()
        .template("x")
        .json("{}")
        .compatLevel(2)
        .compatPatch(Patch.MOD_ZERO)
        .execute();
    assertTrue(ctx.compatEnabled(Patch.MOD_ZERO), "override must apply after compatLevel");
    assertEquals(ctx.getCompat().level(), 2);
  }

  @Test
  public void testWithBase() {
    // The level moves to the base's; overrides are the union.
    CompatLevel receiver = CompatLevel.at(0).withPatch(Patch.MOD_ZERO);
    CompatLevel merged = receiver.withBase(CompatLevel.at(2));
    assertEquals(merged.level(), 2);
    for (Patch patch : Patch.values()) {
      assertEquals(merged.enabled(patch), patch == Patch.MOD_ZERO || patch.threshold() > 2, patch.name());
    }
    assertEquals(merged, CompatLevel.at(2).withPatch(Patch.MOD_ZERO));
    // The receiver is untouched.
    assertEquals(receiver, CompatLevel.at(0).withPatch(Patch.MOD_ZERO));
  }

  @Test
  public void testNegativeLevel() {
    try {
      CompatLevel.at(-1);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      CompatLevel.at(0).withLevel(-1);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testEquals() {
    assertEquals(CompatLevel.at(2), CompatLevel.at(2));
    assertEquals(CompatLevel.at(2).hashCode(), CompatLevel.at(2).hashCode());
    assertEquals(CompatLevel.defaultLevel(), CompatLevel.at(0));
    assertFalse(CompatLevel.at(1).equals(CompatLevel.at(2)));
    CompatLevel a = CompatLevel.at(2).withPatch(Patch.MOD_ZERO);
    CompatLevel b = CompatLevel.at(2).withPatch(Patch.MOD_ZERO);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertFalse(a.equals(CompatLevel.at(2)));
  }

  @Test
  public void testLadderShape() {
    // Level 1 fixes the P1 batch, level 2 adds P2, level 3 adds P3.
    // Thresholds freeze once a release ships, so pin the shape here.
    assertEquals(Patch.maxThreshold(), 3);
    for (Patch patch : Patch.values()) {
      assertTrue(patch.threshold() >= 1 && patch.threshold() <= 3, patch.name());
    }
    assertEquals(Patch.MOD_ZERO.threshold(), 1);
    assertEquals(Patch.JSON_START_KEYWORD.threshold(), 2);
    assertEquals(Patch.PRODUCT_PRICE_TRUE_SLOT.threshold(), 3);
  }
}
