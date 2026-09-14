# Releasing

Releases follow the same version-specific changelog pattern used by the other NsTut mod repositories.

Current release target: **0.1.1**.

## Release inputs

A publishable release requires:

- `mod_version` in the root `gradle.properties` set to a non-SNAPSHOT value such as `0.1.1`.
- A matching `changelog/<version>.md` file.
- Passing Forge 1.20.1 and NeoForge 1.21.1 builds/tests.
- `CURSEFORGE_API_TOKEN` configured as a GitHub Actions secret before CurseForge publishing is enabled.
- CurseForge project `1696007` configured in `.github/workflows/release.yml`.

The root `CHANGELOG.md` is the human-readable history. Files under `changelog/` are the authoritative release bodies consumed by publishing automation.

## Snapshot behavior

Development versions such as `0.1.2-SNAPSHOT` are intentionally ignored by the release workflow. Pushing normal development commits to `main` must not create tags or releases.

## Automatic release behavior

On a push to `main`, `.github/workflows/release.yml`:

1. Reads `mod_version`.
2. Stops cleanly for `*-SNAPSHOT` versions.
3. Detects a version bump or an unpublished non-SNAPSHOT version.
4. Requires `changelog/<version>.md`.
5. Builds/tests Forge 1.20.1 under Java 17.
6. Builds/tests NeoForge 1.21.1 under Java 21.
7. Uploads each exact release JAR as its own workflow artifact.
8. Publishes Forge 1.20.1 and NeoForge 1.21.1 independently to CurseForge project `1696007` from those artifacts.
9. Creates the GitHub Release from the same verified artifacts and `changelog/<version>.md`.

The workflow can also be run manually from `main` with the `release` input explicitly enabled. Manual runs still refuse SNAPSHOT versions.

## CurseForge publishing

CurseForge publishing targets project `1696007`. Each supported loader/version is a separate publish job that downloads the exact JAR produced by the release build, validates CurseForge game-version tags, declares Create as a required dependency, and treats duplicate-upload responses as successful retries.

`CURSEFORGE_API_TOKEN` must be configured as a GitHub Actions secret. A missing token fails publishing instead of silently skipping it.

## Preparing a release

1. Update `CHANGELOG.md` so the version is no longer marked Unreleased and record the release date when publishing.
2. Review `changelog/<version>.md` as public release notes.
3. Change `mod_version` from the snapshot to the exact release version.
4. Update `README.md`, `CURSEFORGE.md`, and compatibility docs if support changed.
5. Complete required manual runtime verification for supported addon screens.
6. Merge/push to `main` and let the release workflow publish.
7. Start the next development cycle by bumping `mod_version` to the next `-SNAPSHOT` value.
