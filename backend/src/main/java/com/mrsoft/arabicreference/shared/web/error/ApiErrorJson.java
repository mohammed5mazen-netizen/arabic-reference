package com.mrsoft.arabicreference.shared.web.error;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;

/**
 * Small JSON writer for servlet entry points that run outside Spring MVC message conversion.
 */
public final class ApiErrorJson {

    private ApiErrorJson() {
    }

    public static String write(ApiErrorResponse error) {
        StringBuilder builder = new StringBuilder(256);
        builder.append('{');
        appendField(builder, "code", error.code());
        builder.append(',');
        appendField(builder, "message", error.message());
        builder.append(",\"details\":[");
        appendStringList(builder, error.details());
        builder.append("],\"fieldErrors\":[");
        for (int index = 0; index < error.fieldErrors().size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            FieldErrorDetail fieldError = error.fieldErrors().get(index);
            builder.append('{');
            appendField(builder, "field", fieldError.field());
            builder.append(',');
            appendField(builder, "message", fieldError.message());
            builder.append('}');
        }
        builder.append("],");
        appendField(builder, "traceId", error.traceId());
        builder.append(',');
        appendField(builder, "timestamp", error.timestamp().toString());
        builder.append('}');
        return builder.toString();
    }

    private static void appendStringList(StringBuilder builder, Iterable<String> values) {
        boolean first = true;
        for (String value : values) {
            if (!first) {
                builder.append(',');
            }
            builder.append('"').append(escape(value)).append('"');
            first = false;
        }
    }

    private static void appendField(StringBuilder builder, String name, String value) {
        builder.append('"').append(name).append("\":\"").append(escape(value)).append('"');
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (current < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) current));
                    } else {
                        escaped.append(current);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
