![java 16](https://img.shields.io/badge/java-16-brightgreen.svg)
---
# POM Update GUI
A tiny JavaFX GUI that allows loading a POM file in order to update plugin and dependency versions.
It automatically fetches all the possible versions that could be chosen from Maven Central.
Furthermore, it supports the usage of version-variables.

<p align="center">
	<a href="https://raw.githubusercontent.com/FoelliX/POMUpdaterGUI/master/screenshot.jpg" target="_blank"><img src="https://raw.githubusercontent.com/FoelliX/POMUpdaterGUI/master/screenshot.jpg" width="500px"/></a>
</p>

## Running 
To run the GUI:
- download the latest release
- run: `java -jar POMUpdaterGUI-X.X.X.jar /path/to/pom.xml`

## Building
If you want to build the tool by yourself:
- clone the repository
- run: `mvn`  
- check `target/build`

### Notes
- **Tested on:** Windows 10, Oracle Java 16.0.1  
However, should run on any system that has Java 16 and JavaFX installed.

- **Why needed?**  
With `mvn versions:display-plugin-updates` and `mvn versions:display-dependency-updates` we can only display information about existing plugin and dependency versions.
With `mvn versions:use-latest-versions` we can update all versions only to the latest.
However, to comfortably select a mixture of different versions there was no satisfactory possibility until now!

---

By [FoelliX.de](https://FoelliX.de)