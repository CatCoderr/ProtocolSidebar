# ProtocolSidebar
[![Build Status](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml/badge.svg?branch=dev)](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/me.catcoder/bukkit-sidebar?server=https%3A%2F%2Foss.sonatype.org)

Non-flickering scoreboard (sidebar) implementation using ProtocolLib.
Also supports ViaVersion.

Supported Minecraft versions: 1.12.2 - 1.19.3

![Sidebar](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/sidebar.gif)

POM snippet:
```xml
<dependency>
  <groupId>me.catcoder</groupId>
  <artifactId>bukkit-sidebar</artifactId>
  <version>5.0.1-SNAPSHOT</version>
</dependency>
```

## How to use it?

```java
TextIterator animation = new TextPrintAnimation("Hello World!", "_", 10);

sidebar = new Sidebar(animation, this);

sidebar.addLine("Test Static Line");
sidebar.addBlankLine();

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

SidebarLine line = sidebar.addUpdatableLine(animation.asLineUpdater());

line.updatePeriodically(0, 1, this, sidebar);

sidebar.addBlankLine();
sidebar.addLine("§ehttps://github.com/CatCoderr/ProtocolSidebar");

sidebar.updateLinesPeriodically(0, 20, this);
```

## Sidebar title animations

Library has built-in title animations (like Hypixel), but you can also create your [own](https://github.com/CatCoderr/ProtocolSidebar/blob/master/src/main/java/me/catcoder/sidebar/text/TextIterator.java).

![Hypixel-like animation](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/animation_example.gif)

Animations also can be used in updatable lines:

```java
TextIterator animation = TextIterators.textFadeHypixel("Hello World!");
SidebarLine line = sidebar.addUpdatableLine(animation.asLineUpdater());

line.updatePeriodically(0, 1, this, sidebar);

```