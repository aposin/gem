# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning (SemVer)](http://semver.org/).

## [Unreleased]

## [0.4.1]

### Added

* Feature Branch Provider for different JIRA instances
* Support for customize sorting og Feature Branch Providers
* Obsolete Environment cleanup dialog have a checkbox to select/unselect all of them

### Changed

* Unquoted system properties in OOMPH/Eclipse configuration is not supported anymore (only HOCON-based substitution is allowed)
* Command-line details on progress dialogs is shown now with tabs for each command instead of of pileup consoles

### Fixed

* GEM configuration failing on startup/refresh crash the application as expected
* Obsolete Environment cleanup dialog is updated after deletion

## [0.4.0]

### Added

* Plug-in interface to create several services based on configuration
* New menu and dialog to detect and clean obsolete environments
* Extension to provide sorting of projects/environments
* Log Eclipse launcher command-line to INFO

### Changed

* Improved misconfigured launchers on UI
* Improved 'Remove Branch' workflow: if the branch is checkout already, clean and then checkout to internal branch before branch removal
* User preferences created by the framework is now HOCON
* Change button focus for dialog to input parameters to OK instead of Cancel (e.g., manual branch creation dialog)

### Fixed

* Fix bug on switching to not completely cloned environments
* Refresh crashes the application when configuration is broken (same as on startup)
* Fix message for wrong repository configuration

## [0.3.0]

First pre-release.


[Unreleased]: https://github.com/aposin/gem/tree/main
[0.4.1]: https://github.com/aposin/gem/releases/tag/v0.4.1
[0.4.0]: https://github.com/aposin/gem/releases/tag/v0.4.0
[0.3.0]: https://github.com/aposin/gem/releases/tag/v0.3.0
