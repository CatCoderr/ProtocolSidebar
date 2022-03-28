# ProtocolSidebar
[![Build Status](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml/badge.svg?branch=dev)](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml)
![Maven Central](https://img.shields.io/maven-central/v/me.catcoder/bukkit-sidebar)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/me.catcoder/bukkit-sidebar?server=https%3A%2F%2Foss.sonatype.org)

Non-flickering scoreboard (sidebar) implementation using ProtocolLib.
Also supports ViaVersion.

POM snippet:
```xml
<dependency>
  <groupId>me.catcoder</groupId>
  <artifactId>bukkit-sidebar</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

## Example usage

```java
Sidebar sidebar = new Sidebar(owner, "objective", "§2title");
        
sidebar.addLine("§aStatic line");
sidebar.addLine("Lines longer than 32 characters will be automatically truncated if player version is < 1.13");
sidebar.addLine(player -> "EXP: " + player.getExp());

sidebar.addViewer(player);
        
//...
// update all dynamic lines 
sidebar.update();
```