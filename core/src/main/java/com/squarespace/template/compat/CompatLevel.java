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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;


/**
 * Compatibility level for one compile. Level 0 keeps every legacy behavior
 * active, the released surface. At level N every patch with a threshold at
 * most N is fixed. The highest level is the fully fixed compiler.
 *
 * Per-site overrides force a legacy behavior on regardless of level, for the
 * rare site the ladder cannot express.
 */
public final class CompatLevel {

  /**
   * The fully fixed compiler. Every fix is applied and no legacy behavior is
   * active.
   */
  public static CompatLevel fixed() {
    return new CompatLevel(Patch.maxThreshold(), EnumSet.noneOf(Patch.class));
  }

  /**
   * The default level. Every legacy behavior is active, which preserves
   * released behavior for unmigrated sites.
   */
  public static CompatLevel defaultLevel() {
    return new CompatLevel(0, EnumSet.noneOf(Patch.class));
  }

  /**
   * A specific ladder position.
   */
  public static CompatLevel at(int level) {
    if (level < 0) {
      throw new IllegalArgumentException("compat level must be >= 0, got " + level);
    }
    return new CompatLevel(level, EnumSet.noneOf(Patch.class));
  }

  private final int level;

  private final Set<Patch> overrides;

  private CompatLevel(int level, Set<Patch> overrides) {
    this.level = level;
    this.overrides = overrides;
  }

  /**
   * True when the legacy behavior for the patch is active at this level.
   * That is when the level is below the patch threshold, or when an
   * override forces the patch on.
   */
  public boolean enabled(Patch patch) {
    return overrides.contains(patch) || level < patch.threshold();
  }

  /**
   * Level value. 0 keeps every legacy behavior active.
   */
  public int level() {
    return level;
  }

  /**
   * Patches whose legacy behavior is forced on regardless of level.
   */
  public Set<Patch> overrides() {
    return Collections.unmodifiableSet(overrides);
  }

  /**
   * A copy with the patch's legacy behavior forced on.
   */
  public CompatLevel withPatch(Patch patch) {
    Set<Patch> copy = overrides.isEmpty() ? EnumSet.noneOf(Patch.class) : EnumSet.copyOf(overrides);
    copy.add(patch);
    return new CompatLevel(level, copy);
  }

  /**
   * A copy at a different ladder position. The override set is kept so a
   * level change never drops a per-site override.
   */
  public CompatLevel withLevel(int level) {
    if (level < 0) {
      throw new IllegalArgumentException("compat level must be >= 0, got " + level);
    }
    return new CompatLevel(level, overrides);
  }

  /**
   * A copy at the base's level. The override set is the union of this
   * level's and the base's, so setting a full level never drops a
   * per-site override applied before it.
   */
  public CompatLevel withBase(CompatLevel base) {
    Set<Patch> merged = overrides;
    if (!base.overrides().isEmpty()) {
      merged = merged.isEmpty() ? EnumSet.copyOf(base.overrides()) : EnumSet.copyOf(merged);
      merged.addAll(base.overrides());
    }
    return new CompatLevel(base.level(), merged);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CompatLevel)) {
      return false;
    }
    CompatLevel other = (CompatLevel)obj;
    return level == other.level && overrides.equals(other.overrides);
  }

  @Override
  public int hashCode() {
    return level * 31 + overrides.hashCode();
  }

  @Override
  public String toString() {
    return "CompatLevel(level=" + level + ", overrides=" + overrides + ")";
  }
}
