# Releasing

Releases follow the same version-specific changelog pattern used by the other NsTut mod repositories.

## Release inputs

A publishable release requires:

- `mod_version` in the root `gradle.properties` set to a non-SNAPSHOT value such as `0.1.0`.
- A matching `changelog/<version>.md` file.
- Passing Forge 1.20.1 and NeoForge 1.21.1 builds/tests.
- `CURSEFORGE_API_TOKEN` configured as a GitHub Actions secret before CurseForge publishing is enabled.
- The real CurseForge project ID replacing `TODO_PROJECT_ID` in `.github/workflows/release.yml`.

The root `CHANGELOG.md` is the human-readable history. Files under `changelog/` are the authoritative release bodies consumed by publishing automation.

## Snapshot behavior

Development versions such as `0.1.0-SNAPSHOT` are intentionally ignored by the release workflow. Pushing normal development commits to `main` must not create tags or releases.

## Automatic release behavior

On a push to `main`, `.github/workflows/release.yml`:

1. Reads `mod_version`.
2. Stops cleanly for `*-SNAPSHOT` versions.
3. Detects a version bump or an unpublished non-SNAPSHOT version.
4. Requires `changelog/<version>.md`.
5. Builds/tests Forge 1.20.1 under Java 17.
6. Builds/tests NeoForge 1.21.1 under Java 21.
7. Verifies the expected release JARs exist and uploads them as a workflow artifact.
8. Publishes both JARs to CurseForge when a real project ID is configured.
9. Creates/reuses the `v<version>` tag.
10. Creates the GitHub Release using the version-specific changelog and both JARs.

The workflow can also be run manually. Manual runs still refuse SNAPSHOT versions.

## CurseForge placeholder

The workflow currently contains:

```text
CURSEFORGE_PROJECT_ID: 'TODO_PROJECT_ID'
```

While that placeholder remains, the CurseForge step prints a notice and skips publication instead of failing or uploading to an unintended project. GitHub artifact/release behavior remains available.

When the project ID is known, replace only the placeholder value and ensure `CURSEFORGE_API_TOKEN` is configured.

## Preparing a release

1. Update `CHANGELOG.md` so the version is no longer marked Unreleased and record the release date.
2. Review `changelog/<version>.md` as public release notes.
3. Change `mod_version` from the snapshot to the exact release version.
4. Update `README.md`, `CURSEFORGE.md`, and compatibility docs if support changed.
5. Merge/push to `main` and let the release workflow publish.
6. Start the next development cycle by bumping `mod_version` to the next `-SNAPSHOT` value.
