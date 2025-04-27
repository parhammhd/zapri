# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [1.1.1] - 2025-04-27
### Added
- Gemtext support for preformatted toggle lines, quote lines, list items, lang parameter.
- Support for more Gemini status codes, and laying groundwork for more.
- Initial support for Gemini redirection.
### Changed
- Moved some Gemini specific code to the dev.parham.zapri.protocol.gemini package.
- Fixed some bugs related to URLs not being rendered correctly.
- Changed nex protocol icon in the address bar.

## [1.1.0] - 2025-04-25
### Changed
- Automated release test via GitHub Actions. No functional changes. Cleaned up the repo.

## [1.0.0] - 2025-04-11
### Added
- Initial release of Zapri
- Gemini protocol support
- Basic Gemtext rendering (headings, links, preformatted text)
- Self-signed certificate handling
- Simple browser UI with navigation
