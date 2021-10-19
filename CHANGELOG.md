# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning (SemVer)](http://semver.org/).

## [Unreleased]

### Added

* Plug-in interface to create several services based on configuration
* New menu and dialog to detect and clean obsolete environments
* Extension to provide sorting of projects/environments

### Changed

* Improved misconfigured launchers on UI
* Improved 'Remove Branch' workflow: if the branch is checkout already, clean and then checkout to internal branch before branch removal
* User preferences created by the framework is now HOCON

### Fixed

* Fix bug on switching to not completely cloned environments

## [0.3.0]

First pre-release.


[Unreleased]: https://github.com/aposin/gem/tree/main
[0.3.0]: https://github.com/aposin/gem/releases/tag/v0.3.0
