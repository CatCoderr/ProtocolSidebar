# ProtocolSidebar
[![Build Status](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml/badge.svg?branch=dev)](https://github.com/CatCoderr/ProtocolSidebar/actions/workflows/maven-publish.yaml)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/me.catcoder/bukkit-sidebar?server=https%3A%2F%2Foss.sonatype.org)

Non-flickering scoreboard (sidebar) implementation. Requires ProtocolLib. Optionally supports ViaVersion.

Supported Minecraft versions: 1.12.2 - 1.19.3

## Features
* No flickering (without using a buffer)
* Easy to use
* Cool inbuilt animations
* Inbuilt pager for showing multiple sidebars to the player
* Automatic score management system: sidebar reorders lines automatically
* Everything is at the packet level, so it works with other plugins using scoreboard and/or teams
* Can be used asynchronously
* Supports up to 30 characters per line on 1.12.2 and below
* No character limit on 1.13 and higher
* Supports hex colors on 1.16 and higher

![Sidebar](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/sidebar.gif)

POM snippet:
```xml
<dependency>
  <groupId>me.catcoder</groupId>
  <artifactId>bukkit-sidebar</artifactId>
  <version>5.1.2-SNAPSHOT</version>
</dependency>
```

## How to use it?

```java
// some cool inbuilt animations
TextIterator typingAnimation = TextIterators.textTypingOldSchool("Hello World! It's a test plugin for ProtocolSidebar!");
TextIterator lineFade = TextIterators.textFadeHypixel("https://github.com/CatCoderr/ProtocolSidebar");
TextIterator title = TextIterators.textFadeHypixel("Hello World!");

// create sidebar
Sidebar sidebar = new Sidebar(title, plugin);

// let's add some lines
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

// animations also available for lines
sidebar.addUpdatableLine(typingAnimation.asLineUpdater())
    .updatePeriodically(0, 1, sidebar);

sidebar.addBlankLine();
sidebar.addUpdatableLine(lineFade.asLineUpdater())
    .updatePeriodically(0, 1, sidebar);

sidebar.updateLinesPeriodically(0L, 20L);
```

![Example](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/nice_example.gif)

## Sidebar title animations

Library has built-in title animations (like Hypixel), but you can also create your [own](https://github.com/CatCoderr/ProtocolSidebar/blob/master/src/main/java/me/catcoder/sidebar/text/TextIterator.java).

![Hypixel-like animation](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/animation_example.gif)

Animations also can be used in updatable lines:

```java
TextIterator animation = TextIterators.textFadeHypixel("Hello World!");
SidebarLine line = sidebar.addUpdatableLine(animation.asLineUpdater());

line.updatePeriodically(0, 1, sidebar);

```

## Sidebar Pager

You can also use sidebar pager, which allows you to show player multiple pages of information.
```java
Sidebar sidebar = new Sidebar(title, this);
Sidebar testSidebar = new Sidebar(TextIterators.textFadeHypixel("Test"), this);

// create a new sidebar pager
SidebarPager pager = new SidebarPager(
        Arrays.asList(sidebar, testSidebar), 3 * 20L, this);

// ... populate sidebar with lines
// first page was taken from code above
        
// add page status line to all sidebars in pager
pager.addPageLine(new ComponentBuilder("Page: ")
                    .color(ChatColor.YELLOW),
        "%d/%d", // currentPage/totalPages
        builder -> builder.bold(true).color(ChatColor.GREEN));

// show to the player
pager.show(player);

// hide from the player
pager.hide(player);
```

![Pager example](https://github.com/CatCoderr/ProtocolSidebar/raw/master/assets/pager_example.gif)
