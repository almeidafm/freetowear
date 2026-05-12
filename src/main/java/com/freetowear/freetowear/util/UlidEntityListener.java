package com.freetowear.freetowear.util;

import jakarta.persistence.PrePersist;
import java.lang.reflect.Field;

public class UlidEntityListener {

    @PrePersist
    public void generateUlid(Object entity) {
        try {
            Field idField = findIdField(entity.getClass());
            if (idField == null) return;

            idField.setAccessible(true);
            if (idField.get(entity) == null) {
                idField.set(entity, UlidGenerator.generate());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to assign ULID to: "
                    + entity.getClass().getSimpleName(), e);
        }
    }

    private Field findIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                return field;
            }
        }
        return clazz.getSuperclass() != null
                ? findIdField(clazz.getSuperclass())
                : null;
    }
}