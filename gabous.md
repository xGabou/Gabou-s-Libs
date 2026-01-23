# Gabou's Libs - Changelog

### Added
- PA/SSP host election utilities in CompatUtils (PaSspHost enum, host selection, and per-mod hosting check).
- JVM override for PA/SSP host election (gaboulibs.pasphost) with tolerant values and explicit override logging.
- InitGuards utility to prevent double initialization via one-time keys.
### Fixed / Behavior
- PA/SSP status logging now includes the elected host when at least one is present.
### Changed
