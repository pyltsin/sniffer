# sniffer

![Build](https://github.com/pyltsin/sniffer/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16305.svg)](https://plugins.jetbrains.com/plugin/16305)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16305.svg)](https://plugins.jetbrains.com/plugin/16305)

<!-- Plugin description -->
Basic simple static analysis. It uses IDEA API.

See inspections: "Settings->Editor/Inspections/Sniffer"

This plugin adds a few simple inspections:

- detect "An object is used as an argument to its own method"
- detect key in HashSet, HashMap, where hashcode and equals is not overrided


<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "sniffer"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/pyltsin/sniffer/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
