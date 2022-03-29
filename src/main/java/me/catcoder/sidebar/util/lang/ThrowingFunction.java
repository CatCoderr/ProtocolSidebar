package me.catcoder.sidebar.util.lang;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;   
}
