package me.catcoder.sidebar.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

    /**
     * Sets the value of a field on the object.
     * @param obj           The object
     * @param fieldName     The name of the field
     * @param fieldValue    The value to set
     * @since 1.9
     */
    public static void setField(Object obj, String fieldName, Object fieldValue) {
        try {
            Field declaredField = getFieldInternal(obj, fieldName);
            boolean wasAccessible = declaredField.isAccessible();
            declaredField.setAccessible(true);
            try {
                declaredField.set(obj, fieldValue);
            } finally {
                declaredField.setAccessible(wasAccessible);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getFieldInternal(Object obj, String fieldName) throws NoSuchFieldException {
        return getFieldFromClass(obj.getClass(), fieldName);
    }

    private static Field getFieldFromClass(Class<?> aClass, String fieldName) throws NoSuchFieldException {
        if (aClass == null) {
            throw new NoSuchFieldException("Unable to locate field " + fieldName);
        }
        try {
            return aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Ignored
        }
        try {
            return aClass.getField(fieldName);
        } catch (NoSuchFieldException e) {
            // Ignore
        }
        return getFieldFromClass(aClass.getSuperclass(), fieldName);
    }

}
