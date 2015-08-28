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

package com.squarespace.template;


/**
 * Enforces both a soft and hard limit on the number of instructions
 * executed, and calls the {@link Handler#onLimit(Limit, HardSoftCodeLimiter)}
 * method when a limit is reached.
 */
public class HardSoftCodeLimiter implements CodeLimiter {

  private static final int SOFT_FLAG = 0x01;

  private static final int HARD_FLAG = 0x02;

  private static final int DEFAULT_RESOLUTION = 64;

  private static final Handler DUMMY_HANDLER = new Handler() {
    public void onLimit(Limit limit, HardSoftCodeLimiter limiter) throws CodeExecuteException {
    }
  };

  private final int softLimit;

  private final int hardLimit;

  // Number of instructions to execute between checks
  private final int resolution;

  // Indicates which limits have already been crossed, so we don't
  // check them more than once.
  private int flags;

  private Handler handler;

  private int instructionCount;

  private HardSoftCodeLimiter(int softLimit, int hardLimit, int resolution, Handler handler) {
    this.softLimit = Math.max(0, softLimit);
    this.hardLimit = Math.max(0, hardLimit);
    this.resolution = Math.max(1, resolution);
    this.handler = handler == null ? DUMMY_HANDLER : handler;
    if (softLimit == Integer.MAX_VALUE) {
      this.flags |= SOFT_FLAG;
    }
    if (hardLimit == Integer.MAX_VALUE) {
      this.flags |= HARD_FLAG;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public int instructionCount() {
    return instructionCount;
  }

  public int softLimit() {
    return softLimit;
  }

  public int hardLimit() {
    return hardLimit;
  }

  public void check() throws CodeExecuteException {
    this.instructionCount++;
    if (instructionCount % resolution != 0) {
      return;
    }
    if ((flags & SOFT_FLAG) == 0) {
      if (instructionCount > softLimit) {
        flags |= SOFT_FLAG;
        handler.onLimit(Limit.SOFT, this);
      }
    }
    if ((flags & HARD_FLAG) == 0) {
      if (instructionCount > hardLimit) {
        flags |= HARD_FLAG;
        handler.onLimit(Limit.HARD, this);
      }
    }
  }

  public static enum Limit {
    SOFT,
    HARD
  }

  public interface Handler {
    void onLimit(Limit limit, HardSoftCodeLimiter limiter) throws CodeExecuteException;
  }

  public static class Builder {

    private int softLimit = Integer.MAX_VALUE;

    private int hardLimit = Integer.MAX_VALUE;

    private int resolution = DEFAULT_RESOLUTION;

    private Handler handler = DUMMY_HANDLER;

    public Builder setSoftLimit(int limit) {
      this.softLimit = limit;
      return this;
    }

    public Builder setHardLimit(int limit) {
      this.hardLimit = limit;
      return this;
    }

    public Builder setResolution(int resolution) {
      this.resolution = resolution;
      return this;
    }

    public Builder setHandler(Handler handler) {
      this.handler = handler;
      return this;
    }

    public HardSoftCodeLimiter build() {
      return new HardSoftCodeLimiter(softLimit, hardLimit, resolution, handler);
    }
  }

}
