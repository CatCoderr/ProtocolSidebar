# ProtocolSidebar
[![Build Status](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml/badge.svg?branch=dev)](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/me.catcoder/bukkit-sidebar?server=https%3A%2F%2Foss.sonatype.org)

Non-flickering scoreboard (sidebar) implementation using ProtocolLib.
Also supports ViaVersion.

Supported Minecraft versions: 1.12.2 - 1.19.3

POM snippet:
```xml
<dependency>
  <groupId>me.catcoder</groupId>
  <artifactId>bukkit-sidebar</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

## How to use it?

```java
Sidebar sidebar = new Sidebar(TextIterators.textFadeHypixel("Hello World!"), this);

sidebar.addLine("Test Static Line"); // supports legacy color codes
sidebar.addBlankLine();

// Supports modern chat components
sidebar.addUpdatableLine(player -> new ComponentBuilder("Your Health: ")
    .append(player.getHealth() + "")
    .color(ChatColor.GREEN)
    .create());

sidebar.addBlankLine();
sidebar.addUpdatableLine(player -> new ComponentBuilder("Your Hunger: ")
    .append(player.getFoodLevel() + "")
    .color(ChatColor.GREEN)
    .create());
sidebar.addBlankLine();

// Long lines will be truncated if player version < 1.13
sidebar.addLine("§ehttps://github.com/CatCoderr/ProtocolSidebar");

// Update all dynamic lines every 20 ticks
sidebar.updateLinesPeriodically(0L, 20L, this);
```

## Sidebar title animations

Library has built-in title animations (like Hypixel), but you can also create your [own](https://github.com/CatCoderr/ProtocolSidebar/blob/master/src/main/java/me/catcoder/sidebar/text/TextIterator.java).

![Hypixel-like animation](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/animation_example.gif)
