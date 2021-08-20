package me.catcoder.sidebar.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {

    private static Unsafe unsafe;

    public static Unsafe getUnsafe() {
        if (unsafe == null) {
            try {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                unsafe = (Unsafe) theUnsafe.get(null);
            } catch(Exception ex) {
                throw new RuntimeException("Should never happens", ex);
            }
        }
        return unsafe;
    }

}
