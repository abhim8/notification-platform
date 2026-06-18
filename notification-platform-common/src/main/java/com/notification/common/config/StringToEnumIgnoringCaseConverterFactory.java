package com.notification.common.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

public class StringToEnumIgnoringCaseConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumIgnoringCaseConverter<>(targetType);
    }
   // Testing pr flow
    private static class StringToEnumIgnoringCaseConverter<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;

        StringToEnumIgnoringCaseConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            String trimmed = source.trim();
            for (T constant : enumType.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(trimmed)) {
                    return constant;
                }
            }
            for (T constant : enumType.getEnumConstants()) {
                try {
                    var method = enumType.getMethod("getValue");
                    Object val = method.invoke(constant);
                    if (val instanceof String s && s.equalsIgnoreCase(trimmed)) {
                        return constant;
                    }
                } catch (Exception ignored) {
                }
            }
            throw new IllegalArgumentException(
                    "Unknown " + enumType.getSimpleName() + ": " + source);
        }
    }
}
