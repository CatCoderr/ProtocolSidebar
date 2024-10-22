<h1 align="center">
  ProtocolSidebar
</h1>
  <p align="center">Powerful feature-packed Minecraft scoreboard library</p>
    <p align="center">

<p align="center">
<a target="_blank"><img src="https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/build.yaml/badge.svg" alt="Build" /></a>
<a target="_blank"><img src="https://img.shields.io/github/license/CatCoderr/ProtocolSidebar" alt="License" /></a>
<a target="_blank"><img src="https://img.shields.io/badge/Minecraft%20Versions-1.12.2--1.21.2-blue?style=flat" alt="Minecraft Versions" /></a>
</p>

* [Features](#features)
* [Adding to your project](#adding-to-your-project)
    * [Maven](#maven)
    * [Gradle](#gradle)
    * [Gradle (Kotlin DSL)](#gradle-kotlin-dsl)
* [Basic usage](#basic-usage)
* [Conditional lines](#conditional-lines)
* [Score number formatting](#score-number-formatting)
* [Sidebar title animations](#sidebar-title-animations)
* [Sidebar Pager](#sidebar-pager)

![Sidebar](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/sidebar.gif)

⚠️ **Note**: starting from **6.2.7-SNAPSHOT** version, the repository has been moved to https://catcoder.pl.ua/snapshots. 
You can find URL for Maven and Gradle in the [Adding to your project](#adding-to-your-project) section.


## Donations
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-Donate%20Now-yellow?style=for-the-badge&logo=buy-me-a-coffee)](https://www.buymeacoffee.com/catcoderr)


## Features

* No flickering (without using a buffer)
* Does not require any additional libraries/plugins on the server
* Easy to use
* Optionally supports [Adventure API](https://docs.advntr.dev/text.html), [MiniMessage](https://docs.advntr.dev/minimessage/index.html), [MiniPlaceholders](https://github.com/MiniPlaceholders/MiniPlaceholders)
* Extremely fast, can be used asynchronously
* Cool inbuilt animations
* Inbuilt pager for showing multiple sidebars to the player
* Automatic score management system: sidebar reorders lines automatically
* Everything is at the packet level, so it works with other plugins using scoreboard and/or teams
* Supports up to 30 characters per line on 1.12.2 and below
* No character limit on 1.13 and higher
* Supports hex colors on 1.16 and higher
* Minimized NMS interaction, means that packets are constructed at the byte buffer level and then sent directly to the player's channel.

## Adding To Your Project

Instead of manually bundling the library into your JAR file, you can
use [the standalone plugin](https://github.com/CatCoderr/ProtocolSidebar/tree/master/standalone-plugin).

Simply run `./gradlew clean shadowJar` and put the resulting JAR file located in `bin` folder into your plugins folder.

In other cases, you must use something like [shadow](https://imperceptiblethoughts.com/shadow/) (for Gradle)
or [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) (for Maven).

### Maven

```xml
<repository>
    <id>catcoder-snapshots</id>
    <url>https://catcoder.pl.ua/snapshots</url>
</repository>
```
```xml
<dependency>
    <groupId>me.catcoder</groupId>
    <artifactId>bukkit-sidebar</artifactId>
    <version>6.2.7-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://catcoder.pl.ua/snapshots' }
}
```
```groovy
dependencies {
    implementation 'me.catcoder:bukkit-sidebar:6.2.7-SNAPSHOT'
}
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://catcoder.pl.ua/snapshots")
}
```
```kotlin
dependencies {
    implementation("me.catcoder:bukkit-sidebar:6.2.7-SNAPSHOT")
}
```

## Basic Usage

```java
// create sidebar which uses Adventure API
// you can also use other methods from ProtocolSidebar class
// for another text providers such as BungeeCord Chat, MiniMessage...
Sidebar<Component> sidebar = ProtocolSidebar.newAdventureSidebar(
        TextIterators.textFadeHypixel("SIDEBAR"), this);

// let's add some lines
sidebar.addLine(
    Component.text("Just a static line").color(NamedTextColor.GREEN));
// add an empty line
sidebar.addBlankLine();
// also you can add updatable lines which applies to all players receiving this sidebar
sidebar.addUpdatableLine(
    player -> Component.text("Your Hunger: ")
        .append(Component.text(player.getFoodLevel())
        .color(NamedTextColor.GREEN))
    );

sidebar.addBlankLine();
sidebar.addUpdatableLine(
    player -> Component.text("Your Health: ")
        .append(Component.text(player.getHealth())
        .color(NamedTextColor.GREEN))
);
sidebar.addBlankLine();
sidebar.addLine(
    Component.text("https://github.com/CatCoderr/ProtocolSidebar")
        .color(NamedTextColor.YELLOW
));

// update all lines except static ones every 10 ticks
sidebar.updateLinesPeriodically(0, 10);

// ...

// show to the player
sidebar.addViewer(player);
// ...hide from the player
sidebar.removeViewer(player);
```
![Example](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/nice_example.gif)

## Conditional Lines
The visibility of these lines depends on the condition you set.
If the condition is true, the line will be shown, otherwise it will be hidden.
It's an updatable line, so it will update along with other updatable lines.

## Score number formatting
You can use `scoreNumberFormat` method in both `ScoreboardObjective` and `SidebarLine` classes to format score numbers.

This feature is available starting from 1.20.4 version.
```java
// removes scores completely for all lines
sidebar.getObjective().scoreNumberFormatBlank();
// set's custom fixed text for all lines
sidebar.getObjective().scoreNumberFormatFixed(player -> Component.text("Test").color(NamedTextColor.BLUE));

// set's score number format for specific line (overrides objective's format)
var line = sidebar.addLine(Component.text("Some line").color(NamedTextColor.YELLOW));
line.scoreNumberFormatFixed(player -> Component.text("Test").color(NamedTextColor.BLUE));
```

## Sidebar Title Animations

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
        sidebar.addLine(Component.text("Page " + page + "/" + maxPage)
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
