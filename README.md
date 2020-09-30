# ProtocolSidebar
[![Build Status](https://travis-ci.org/CatCoderr/ProtocolSidebar.svg?branch=master)](https://travis-ci.org/CatCoderr/ProtocolSidebar) 

Non-flickering scoreboard (sidebar) implementation using ProtocolLib.
Also supports ViaVersion.

> Minecraft version: [1.8-1.16]

Latest releases: https://jitpack.io/#CatCoderr/ProtocolSidebar/

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