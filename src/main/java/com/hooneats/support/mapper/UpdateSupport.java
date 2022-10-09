package com.hooneats.support.mapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hooneats
 */
public interface UpdateSupport {

    default Optional<?> updateObject(
        Class<?> resourceClass, Optional<?> resourceObject, Optional<?> targetObject
    ) {
        Map<String, Optional<?>> updateFieldValueMap = getUpdateMapper(resourceClass,
            resourceObject);
        readMapAndUpdateObject(targetObject, updateFieldValueMap);
        return targetObject;
    }

    private Map<String, Optional<?>> getUpdateMapper(Class<?> resourceClass, Optional<?> obj) {
        Field[] fields = resourceClass.getDeclaredFields();
        return Arrays.stream(fields)
            .collect(
                Collectors.toMap(getEntityFieldName(), getResourceFieldValue(obj)));
    }

    private Function<Field, String> getEntityFieldName() {
        return field -> {
            UpdateColumn updateColumn = field.getAnnotation(UpdateColumn.class);
            return updateColumn.updateFieldName();
        };
    }

    private Function<Field, Optional<?>> getResourceFieldValue(Optional<?> obj) {
        return field -> {
            try {
                field.setAccessible(true);
                return Optional.ofNullable(field.get(obj.get()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void readMapAndUpdateObject(Optional<?> targetObject,
        Map<String, Optional<?>> updateFieldAndValueMap) {
        updateFieldAndValueMap.forEach(updateObjectField(targetObject));
    }

    private static BiConsumer<String, Optional<?>> updateObjectField(
        Optional<?> targetObject) {
        var obj =
            targetObject.orElseThrow(
                () -> new RuntimeException("Could not update, because targetObject is null"));
        return (key, value) -> {
            value.ifPresent(v -> {
                try {
                    Field field = obj.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(obj, v);
                } catch (Exception e) {
                    throw new RuntimeException("Could not update, because trouble reflection");
                }
            });
        };
    }
}
