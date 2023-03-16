# ProtocolSidebar

[![Build Status](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml/badge.svg?branch=dev)](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/me.catcoder/bukkit-sidebar?server=https%3A%2F%2Foss.sonatype.org)

Unleash the power of your Minecraft server's scoreboard with ProtocolSidebar - the ultimate non-flickering, feature-packed sidebar library.

Requires ProtocolLib. Optionally supports ViaVersion.

Supported Minecraft versions: 1.12.2 - 1.19.3

* [Features](#features)
* [Adding to your project](#adding-to-your-project)
  * [Maven](#maven)
  * [Gradle](#gradle)
  * [Gradle (Kotlin DSL)](#gradle-kotlin-dsl)
* [Basic usage](#basic-usage)
* [Sidebar title animations](#sidebar-title-animations)
* [Sidebar Pager](#sidebar-pager)

![Sidebar](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/sidebar.gif)

## Features

* No flickering (without using a buffer)
* Easy to use
* Optionally supports [Adventure API](https://docs.advntr.dev/text.html), [MiniMessage](https://docs.advntr.dev/minimessage/index.html)
* Extremely fast, can be used asynchronously
* Cool inbuilt animations
* Inbuilt pager for showing multiple sidebars to the player
* Automatic score management system: sidebar reorders lines automatically
* Everything is at the packet level, so it works with other plugins using scoreboard and/or teams
* Supports up to 30 characters per line on 1.12.2 and below
* No character limit on 1.13 and higher
* Supports hex colors on 1.16 and higher

## Adding to your project

### Maven

```xml
<repository>
    <id>sonatype-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
```
```xml
<dependency>
    <groupId>me.catcoder</groupId>
    <artifactId>bukkit-sidebar</artifactId>
    <version>6.0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
}
```
```groovy
dependencies {
    implementation 'me.catcoder:bukkit-sidebar:6.0.1-SNAPSHOT'
}
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}
```
```kotlin
dependencies {
    implementation("me.catcoder:bukkit-sidebar:6.0.1-SNAPSHOT")
}
```

## Basic usage

```java
// create sidebar which uses Adventure API
// you can also use other methods from ProtocolSidebar class
// for another text providers such as BungeeCord Chat, MiniMessage...
Sidebar<Component> sidebar = ProtocolSidebar.newAdventureSidebar(
        TextIterators.textFadeHypixel("SIDEBAR"), this);

// let's add some lines
sidebar.addLine(
    Component
        .text("Just a static line")
        .color(NamedTextColor.GREEN));
// add an empty line
sidebar.addBlankLine();
// also you can add updatable lines which applies to all players
sidebar.addUpdatableLine(
    player -> Component
        .text("Your Hunger: ")
        .append(Component.text(
                player.getFoodLevel())
        .color(NamedTextColor.GREEN))
    );

sidebar.addBlankLine();
sidebar.addUpdatableLine(
    player -> Component
        .text("Your Health: ")
        .append(Component.text(
                player.getHealth())
        .color(NamedTextColor.GREEN))
);
sidebar.addBlankLine();
sidebar.addLine(
    Component
        .text("https://github.com/CatCoderr/ProtocolSidebar")
        .color(NamedTextColor.YELLOW
));

// update all lines every 10 ticks
sidebar.updateLinesPeriodically(0, 10);

// ...

// show to the player
sidebar.addViewer(player);
// ...hide from the player
sidebar.removeViewer(player);
```

More examples available [here.](https://github.com/CatCoderr/ProtocolSidebar/tree/master/test-plugin/src/main/java/me/catcoder/sidebar)

![Example](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/nice_example.gif)

## Sidebar title animations

Library has built-in title animations, but you can also create your [own](https://github.com/CatCoderr/ProtocolSidebar/blob/master/src/main/java/me/catcoder/sidebar/text/TextIterator.java).
![Hypixel-like animation](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/animation_example.gif)

Animations also can be used in updatable lines:

```java
TextIterator animation = TextIterators.textFadeHypixel("Hello World!");
SidebarLine<?> line = sidebar.addUpdatableLine(sidebar.asLineUpdater(animation));

line.updatePeriodically(0, 1, sidebar);
```

## Sidebar Pager

You can also use sidebar pager, which allows you to show player multiple pages of information.
```java
Sidebar<Component> anotherSidebar = ProtocolSidebar.newAdventureSidebar(
        TextIterators.textFadeHypixel("ANOTHER SIDEBAR"), this);

Sidebar<Component> firstSidebar = ProtocolSidebar.newAdventureSidebar(
        TextIterators.textFadeHypixel("SIDEBAR"), this);

SidebarPager<Component> pager = new SidebarPager<>(
        Arrays.asList(firstSidebar, anotherSidebar), 20 * 5, this);

// add page status line to all sidebars in pager
pager.addPageLine((page, maxPage, sidebar) ->
        sidebar.addLine(Component
            .text("Page " + page + "/" + maxPage)
            .color(NamedTextColor.GREEN)));

pager.applyToAll(Sidebar::addBlankLine);

// ...add some lines
        
// show to player
pager.show(player);

// ...
// hide from the player
pager.hide(player);
```

![Pager example](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/pager_example.gif)
