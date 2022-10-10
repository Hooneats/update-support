package com.hooneats.support.mapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hooneats
 */
public class ObjectSupport {

    public static Object of(Object resource, Class<?> resultClass) {
        final var fieldValueMap = getFieldValueMap(resource, null);
        return createAndSetValue(resultClass, fieldValueMap);
    }

    public static Object of(Object resource, Class<?> resultClass, String... excludeField) {
        final var fieldValueMap = getFieldValueMap(resource, excludeField);
        return createAndSetValue(resultClass, fieldValueMap);
    }

    private static Object createAndSetValue(Class<?> resultClass,
        Map<String, Optional<Object>> fieldValueMap) {
        final var newInstance = createNewInstance(resultClass);
        setNewInstanceValue(resultClass, fieldValueMap, newInstance);
        return resultClass.cast(newInstance);
    }

    private static Map<String, Optional<Object>> getFieldValueMap(Object resource,
        String... excludeField) {
        final var fields = resource.getClass().getDeclaredFields();
        final var fieldStream = Arrays.stream(fields);
        return predicateFieldStream(resource, fieldStream, excludeField);
    }

    private static Map<String, Optional<Object>> predicateFieldStream(Object resource,
        Stream<Field> fieldStream, String[] excludeField) {
        return Objects.isNull(excludeField) ?
            fieldStream.
                collect(Collectors.toMap(Field::getName, readValue(resource))) :
            fieldStream
                .filter(field ->
                    !Arrays.stream(excludeField)
                        .collect(Collectors.toList())
                        .contains(field.getName())
                )
                .collect(Collectors.toMap(Field::getName, readValue(resource)));
    }

    private static Function<Field, Optional<Object>> readValue(Object resource) {
        return field -> {
            try {
                field.setAccessible(true);
                return Optional.ofNullable(field.get(resource));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access field");
            }
        };
    }

    private static Object createNewInstance(Class<?> resultClass) {
        try {
            final var constructor = resultClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create new Instance.");
        }
    }

    private static void setNewInstanceValue(Class<?> resultClass,
        Map<String, Optional<Object>> fieldValueMap,
        Object newInstance) {
        final var resultFields = resultClass.getDeclaredFields();
        Arrays.stream(resultFields)
            .forEach(setValue(fieldValueMap, newInstance));
    }

    private static Consumer<Field> setValue(Map<String, Optional<Object>> fieldValueMap,
        Object newInstance) {
        return field -> {
            final var object = fieldValueMap.get(field.getName());
            if (Objects.isNull(object)) {
                return;
            }
            object.ifPresent(obj -> {
                try {
                    field.setAccessible(true);
                    field.set(newInstance, object.get());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not set field");
                }
            });
        };
    }
}
