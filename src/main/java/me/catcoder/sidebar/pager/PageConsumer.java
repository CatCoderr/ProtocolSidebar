package me.catcoder.sidebar.pager;


import me.catcoder.sidebar.Sidebar;

@FunctionalInterface
public interface PageConsumer<R> {

    void accept(int page, int maxPage, Sidebar<R> sidebar);
}
