# ProtocolSidebar
[![Build Status](https://travis-ci.org/CatCoderr/ProtocolSidebar.svg?branch=master)](https://travis-ci.org/CatCoderr/ProtocolSidebar) 

Non-flickering scoreboard (sidebar) implementation with ProtocolLib.


Requires you to install ViaVersion on your server: https://www.spigotmc.org/resources/viaversion.19254/

Dependencies: [ProtocolLib, ViaVersion]

> Minecraft version: 1.8-1.16

Latest release: https://jitpack.io/#CatCoderr/ProtocolSidebar/3.0.0

## Example usage

```java
    Sidebar sidebar = new Sidebar("objective-name");
    sidebar.setDisplayName("Test");
       
    sidebar.setLine(1, "First static line");
    sidebar.setLine(2, "Second static line");
    sidebar.setLine(3, (player) -> "Your name is " + player.getName());
    sidebar.setLine(5, (player) -> "Total EXP: " + player.getTotalExperience())
    sidebar.setLine(4, "Lines longer than 32 characters will be automatically truncated if player version is < 1.13")
     
    //...
    sidebar.send(event.getPlayer());
    //...
    // update the line displaying total experience of player
    sidebar.getLine(5).update().accept(event.getPlayer());
```