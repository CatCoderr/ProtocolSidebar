package me.catcoder.sidebar.util.lang;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
   
}
