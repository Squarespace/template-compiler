# Compatibility levels

This document explains how the compat level ladder works, why each bug fix
sits at the tier it does, and what the threshold freeze means for future
releases.

## Why the ladder exists

The compiler accumulated a long list of confirmed bugs: crashes on unusual
input, values that are plainly wrong, and output quirks that customer sites
may have come to rely on. Shipping every fix at once would change the output
of live sites without their consent. Some of those "bugs" are defensible
interpretations of the input: a site that renders a bare number for an
unknown currency, or that shows the text `true` in a localized price slot,
may have templates built around what it sees today.

The compat level splits the fixes into reviewed batches. Level 0 matches
the legacy behavior, unchanged byte for byte, and it is the default.

There are several ways the compatibility levels could be used to verify
a group of patches are stable. A request cookie could set a given compat
level and bypass the page cache, performing an A/B test of a given
website. A template could be pinned to a given compat level, so that all
websites using that template render using the verified set of patches.

This live patching mechanism allowed the backlog of bug fixes to finally
be released, providing a patch forward to stabilize and verify these
bug fixes in production in a safe way.


## How it works

- Each fix is gated by an entry in the `Patch` enum The
  entry names the legacy behavior and carries a threshold, the lowest level
  where the fix is active.
- A `CompatLevel` is an integer plus an optional override set. For a given
  patch, the legacy behavior is active when the level is below the patch's
  threshold, or when an override forces it on. `CompatLevel.enabled(patch)`
  returns true when the legacy behavior is active; code guards the fixed
  path on `!ctx.compatEnabled(patch)`.
- Levels are monotonic: every level is a strict superset of the fixes at the
  level below it. `CompatLevelTest` pins this.
- `CompatLevel.fixed()` is the fully fixed compiler (the current maximum
  threshold). It is what tests use for the fixed-level side of each pair.
- API surface: `Compiler.compile(source, safe, preprocess, compat)`, the
  executor's `.compat(...)`, `.compatLevel(n)`, and `.compatPatch(patch)`,
  and the CLI flags `--compat-level` and `--compat-patch` (repeatable).

## The threshold freeze

`Patch` carries the comment: *the threshold freezes once a release ships.*
Here is what that means and why it is true.

A wesite's rendered output is a function of three things: its templates, its
data, and the compatibility level of the template renderer. The level is 
part of the public contract, because adopting a level is a deliberate act.
A template developer reviewed what the level changes, migrated and tested 
their site.  Once that review is done and the compatibility level set,
the set of fixes behind the level can no longer move without the operator's
knowledge.

Once a bug is part of a patch it must be frozen. It can never move between
patches. Suppose that in a later release, someone changes the
threshold of a bug from 3 to 2 to "include the fix earlier". This could 
inadvertently break wesites using templates that have not verified that
bug fix in development mode. That is exactly the silent change the ladder 
exists to prevent. The symmetric case is worse: raising a threshold 
removes a fix from a level the release notes promised.

So the rule is:

- **Before the first release that ships the feature**, the thresholds are
  design inputs. Re-tiering is a one-line change in the enum plus a test
  run, and it costs nothing, because no release contains the table yet.
- **Once a release ships**, the threshold table of that release is fixed.
  Sites pin levels against it, and moving an entry changes live output.
- **Future releases grow the ladder, not rewrite it.** New fixes get new
  patches at `maxThreshold() + 1`, a new top tier. Every existing level
  keeps exactly the fixes it had, because the new patch's legacy behavior
  is active below its threshold.
- **Patches retire, they do not shift.** When no site still needs a legacy
  behavior, the entry, the legacy code path, and its tests are removed
  together, as the enum javadoc says.
- **Overrides cover the rest.** A rare site that must keep one specific
  legacy behavior regardless of level forces it on with a per-site
  override, and the ladder does not need to bend.

**NOTE** Before the compatibility level feature is wired into site-server,
there is a window where the compat level for a bug can be changed. Once
this feature is deployed and being actively used in site-server the
bug pins for any compat levels in use must never be changed.

