package com.hooneats.support.mapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hooneats
 */
public interface UpdateSupport {

    default Optional<?> updateObject(
        final Optional<?> resourceObject,
        final Optional<?> targetObject
    ) {
        final var updateFieldValueMap = getUpdateMapper(resourceObject);
        readMapAndUpdateObject(targetObject, updateFieldValueMap);
        return targetObject;
    }

    private Map<String, Optional<?>> getUpdateMapper(
        final Optional<?> resourceObject) {
        final var fields = resourceObject.orElseThrow(
                () -> new RuntimeException("Could not update, because resourceObject is null"))
            .getClass().getDeclaredFields();
        return Arrays.stream(fields)
            .collect(
                Collectors.toMap(getEntityFieldName(), getResourceFieldValue(resourceObject)));
    }

    private Function<Field, String> getEntityFieldName() {
        return field -> {
            final var updateColumn = field.getAnnotation(UpdateColumn.class);
            if (Objects.isNull(updateColumn)) {
                return "";
            }
            return updateColumn.name();
        };
    }

    private Function<Field, Optional<?>> getResourceFieldValue(final Optional<?> resourceObject) {
        return field -> {
            try {
                field.setAccessible(true);
                return Optional.ofNullable(field.get(resourceObject.get()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void readMapAndUpdateObject(final Optional<?> targetObject,
        final Map<String, Optional<?>> updateFieldAndValueMap) {
        updateFieldAndValueMap.forEach(updateObjectField(targetObject));
    }

    private BiConsumer<String, Optional<?>> updateObjectField(
        final Optional<?> targetObject) {
        final var obj =
            targetObject.orElseThrow(
                () -> new RuntimeException("Could not update, because targetObject is null"));
        return (key, value) -> {
            if (key.isBlank()) {
                return;
            }
            value.ifPresent(v -> {
                try {
                    final var field = obj.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(obj, v);
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Could not update, maybe problem is updateColumn name");
                }
            });
        };
    }
}
