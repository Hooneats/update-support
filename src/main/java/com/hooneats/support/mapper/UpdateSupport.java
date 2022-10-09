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
        final Class<?> resourceClass, final Optional<?> resourceObject,
        final Optional<?> targetObject
    ) {
        final var updateFieldValueMap = getUpdateMapper(resourceClass,
            resourceObject);
        readMapAndUpdateObject(targetObject, updateFieldValueMap);
        return targetObject;
    }

    private Map<String, Optional<?>> getUpdateMapper(final Class<?> resourceClass,
        final Optional<?> obj) {
        final var fields = resourceClass.getDeclaredFields();
        return Arrays.stream(fields)
            .collect(
                Collectors.toMap(getEntityFieldName(), getResourceFieldValue(obj)));
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

    private Function<Field, Optional<?>> getResourceFieldValue(final Optional<?> obj) {
        return field -> {
            try {
                field.setAccessible(true);
                return Optional.ofNullable(field.get(obj.get()));
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
            value.ifPresent(v -> {
                try {
                    if (key.isBlank()) {
                        return;
                    }
                    final var field = obj.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(obj, v);
                } catch (Exception e) {
                    throw new RuntimeException("Could not update, because trouble reflection");
                }
            });
        };
    }
}
