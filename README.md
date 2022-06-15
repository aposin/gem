# Git Environment Manager (GEM)
[![Build](https://github.com/allianz/gem/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/allianz/gem/actions/workflows/build.yml?query=branch%3Amain+event%3Apush)
[![CodeQL](https://github.com/allianz/gem/actions/workflows/codeql-analysis.yml/badge.svg?branch=main)](https://github.com/allianz/gem/actions/workflows/codeql-analysis.yml?query=branch%3Amain+event%3Apush)
![GitHub top language](https://img.shields.io/github/languages/top/aposin/gem.svg)
[![CLA assistant](https://cla-assistant.io/readme/badge/aposin/gem)](https://cla-assistant.io/aposin/gem)
[![GitHub](https://img.shields.io/github/license/aposin/gem.svg)](https://github.com/allianz/gem/blob/main/LICENSE)

GEM (Git Environment Manager) is a git branch manager which:

* Defines environments based on one or several shared repositories for protected branches,
  using worktrees to minimize number of clones.
* Enforces a feature-branch workflow to work with different providers of issues,
  including branch creation, and local/remote handling.
* Is extensible by plug-ins.

## Building

To build a released version, checkout the version tag beforehand.

To build the executables for Windows and Linux, run on the root folder:

```bash
mvn verify
```

Both linux and windows distributions are located under `products\org.aposin.gem.product\target\products`.

This builds the `alpha_` version of the product and plug-ins on the repository.
Check the "Releasing" section for how to build a release.

## Versioning

This project adheres to [Semantic Versioning (SemVer)](http://semver.org/).
Maintainers are in charge of updating the version and add to the [CHANGELOG.md](CHANGELOG.md) any relevant information.

Currently, except the `org.aposin.gem.parent` project, the rest have the same SemVer (with different build number).
This ensures that a product with all `org.aposin.gem` plug-ins on the same version is fully functional.

:information_source: As a configuration project, the `org.aposin.gem.parent` is in the same version unless required for the process to identify major changes.

To help updating project versions, the `tycho-versions-plugin` is included in the parent-pom.
Run `mvn -DnewVersion=${nextVersion}.qualifier tycho-versions:set-version` with `${newVersion}` being the new version.

### Releasing a new version

For preparing a new release,:

* Update the version if relevant to the SemVer specs and it is not up-to-date.
  If some changes are not included yet,
  update also the [CHANGELOG.md](CHANGELOG.md) with them.
* Update the `Unreleased` header with the new version (maintaining this title)
  and add the link to the (still not existing) release page at the bottom.
* Update the product file to point to a frozen version branch of the [gem-config] repository.
  You should create the branch on that repository from the `main` version and set it as protected.
* Open a PR (or merge if you have permissions).
* Once it is merged, tag the version and publish the release on GitHub.

To build the release version (and attach to the GitHub release if required),
you should add the `-Drelease` property to the build command.

After this is done, no more PR should be accepted until the versions are updated to the next patch version.
In addition to update the version numbers, the product file should point again to the `main` [gem-config] branch to allow changes in the configuration format.



[gem-config]: https://github.com/allianz/gem-config
