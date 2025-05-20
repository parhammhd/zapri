# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [1.2.1] - 2025-05-20
### Added
- Support for opening http/https links also when typed inside the address bar. (contributed by [BardiyaFeili](https://github.com/BardiyaFeili))
- Support for opening links by pressing enter. (contributed by [BardiyaFeili](https://github.com/BardiyaFeili))
### Changed
- Several edits to fix the Text Protocol(still not working correctly).
### Removed
- The "Go" button on the address bar. (contributed by [BardiyaFeili](https://github.com/BardiyaFeili))

## [1.2.0] - 2025-05-04
### Added
- Support for the Finger Protocol.
- Support for the Text Protocol.
- Support for opening http/https links in default browser.
- Different colors for current protocol links, supported by Zapri (including future supported) links and other (like http/https/ftp) links.
- New protocol icons for local:// and browser:// links.
- A globe protocol icon for the web.
### Changed
- Rewrote UrlParser in UrlUtils.
- Changed Gemini protocol icon to a rocket.
- Cleaned up all Gemini Specific code from BrowserScreen.
- Fixed some small bugs here and there.

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
