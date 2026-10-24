package com.berkay.restaurant.service.domain.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class LocalTimeOrEmptyDeserializer extends JsonDeserializer<LocalTime> implements ContextualDeserializer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern STRICT_TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$");

    private final String propertyName;

    public LocalTimeOrEmptyDeserializer() {
        this(null);
    }

    public LocalTimeOrEmptyDeserializer(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        String name = property != null ? property.getName() : null;
        return new LocalTimeOrEmptyDeserializer(name);
    }

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            Object target = p.getCurrentValue();
            if (target == null && p.getParsingContext() != null) {
                target = p.getParsingContext().getCurrentValue();
            }
            if (target != null) {
                String fieldName = this.propertyName != null ? this.propertyName : p.currentName();
                if (fieldName == null && p.getParsingContext() != null) {
                    fieldName = p.getParsingContext().getCurrentName();
                }
                setClearFlag(target, fieldName);
            }
            return null;
        }
        String trimmed = value.trim();
        if (!STRICT_TIME_PATTERN.matcher(trimmed).matches()) {
            throw InvalidFormatException.from(
                    p,
                    "Cannot deserialize value of type `java.time.LocalTime` from String \"" + value + "\"",
                    value,
                    LocalTime.class
            );
        }
        try {
            return LocalTime.parse(trimmed, FORMATTER);
        } catch (DateTimeParseException e) {
            throw InvalidFormatException.from(
                    p,
                    "Cannot deserialize value of type `java.time.LocalTime` from String \"" + value + "\"",
                    value,
                    LocalTime.class
            );
        }
    }

    private void setClearFlag(Object target, String fieldName) {
        if (target == null || fieldName == null) {
            return;
        }
        String methodName = null;
        if ("openingTime".equals(fieldName)) {
            methodName = "setClearOpeningTime";
        } else if ("closingTime".equals(fieldName)) {
            methodName = "setClearClosingTime";
        }

        if (methodName != null) {
            try {
                try {
                    target.getClass().getMethod(methodName, Boolean.class).invoke(target, Boolean.TRUE);
                    return;
                } catch (NoSuchMethodException e) {
                    target.getClass().getMethod(methodName, boolean.class).invoke(target, true);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
    }
}
