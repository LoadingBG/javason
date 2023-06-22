package loadingbg.javason;

import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import java.util.stream.*;

public final class JavaSON {
    private JavaSON() {}

    public static JSONElement toJSON(final Object obj) {
        return switch (obj) {
            case null -> JSONNull.INSTANCE;
            case Boolean bool -> new JSONBoolean(bool);
            case Number num -> new JSONNumber(num);
            case CharSequence str -> new JSONString(str.toString());
            case JSON json -> json.toJSON();
            case Object[] arr -> new JSONArray(Arrays.stream(arr).map(JavaSON::toJSON).toList());
            case List<?> list -> new JSONArray(list.stream().map(JavaSON::toJSON).toList());
            case Map<?, ?> map -> new JSONObject(
                    map.entrySet()
                        .stream()
                        .collect(Collectors.toMap(e -> escapeString(e.getKey().toString()), e -> toJSON(e.getValue()))));
            default -> {
                final var seenKeys = new HashMap<String, Integer>();
                final var methods = obj.getClass().getDeclaredMethods();
                for (final var method : methods) {
                    if (method.getAnnotation(JSONField.class) == null || Modifier.isPrivate(method.getModifiers())) {
                        continue;
                    }
                    seenKeys.compute(method.getAnnotation(JSONField.class).value(), (k, v) -> v == null ? 1 : v + 1);
                }
                yield new JSONObject(Arrays.stream(methods)
                    .filter(method -> seenKeys.get(method.getAnnotation(JSONField.class).value()) == 1)
                    .collect(Collectors.toMap(
                        method -> escapeString(method.getAnnotation(JSONField.class).value()),
                        method -> {
                            try {
                                return toJSON(method.invoke(obj));
                            } catch (final Exception e) {
                                return null;
                            }
                        })));
            }
        };
    }

    public static Optional<Map.Entry<JSONElement, String>> parseJSON(final String json) {
        final var stripped = json.stripLeading();
        return parseNull(stripped)
            .or(() -> parseBoolean(stripped))
            .or(() -> parseNumber(stripped))
            .or(() -> parseString(stripped))
            .or(() -> parseArray(stripped))
            .or(() -> parseObject(stripped));
    }

    private static Optional<Map.Entry<JSONElement, String>> parseNull(final String json) {
        if (json.startsWith("null")) {
            return Optional.of(Map.entry(JSONNull.INSTANCE, json.substring(4)));
        }
        return Optional.empty();
    }

    private static Optional<Map.Entry<JSONElement, String>> parseBoolean(final String json) {
        if (json.startsWith("true")) {
            return Optional.of(Map.entry(new JSONBoolean(true), json.substring(4)));
        } else if (json.startsWith("false")) {
            return Optional.of(Map.entry(new JSONBoolean(false), json.substring(5)));
        }
        return Optional.empty();
    }

    private static Optional<Map.Entry<JSONElement, String>> parseNumber(final String json) {
        if (json.isEmpty()) {
            return Optional.empty();
        }
        final var len = json.length();

        final var isNegative = json.charAt(0) == '-';
        var idx = isNegative ? 1 : 0;
        if (idx >= len) {
            return Optional.empty();
        }

        final var whole = new StringBuilder();
        if (json.charAt(1) == '0') {
            whole.append('0');
        } else {
            while (idx < len && Character.isDigit(json.charAt(idx))) {
                whole.append(json.charAt(idx));
                idx++;
            }
        }
        if (whole.isEmpty()) {
            return Optional.empty();
        }
        if (idx >= len) {
            return Optional.of(Map.entry(parseNum(isNegative, whole.toString(), "", ""), ""));
        }

        final var frac = new StringBuilder();
        final var exp = new StringBuilder();

        if (json.charAt(idx) == '.') {
            idx++;
            while (idx < len && Character.isDigit(json.charAt(idx))) {
                frac.append(json.charAt(idx));
                idx++;
            }
            if (frac.isEmpty()) {
                return Optional.empty();
            }
        }
        if (idx >= len) {
            return Optional.of(Map.entry(parseNum(isNegative, whole.toString(), frac.toString(), ""), ""));
        }

        if (json.charAt(idx) == 'e' || json.charAt(idx) == 'E') {
            idx++;
            if (idx >= len) {
                return Optional.empty();
            }
            if (json.charAt(idx) == '-') {
                exp.append('-');
                idx++;
            } else if (json.charAt(idx) == '+') {
                idx++;
            }

            while (idx < len && Character.isDigit(json.charAt(idx))) {
                exp.append(json.charAt(idx));
                idx++;
            }
            if (exp.isEmpty()) {
                return Optional.empty();
            }
        }
        return Optional.of(Map.entry(parseNum(isNegative, whole.toString(), frac.toString(), exp.toString()), json.substring(idx)));
    }

    private static JSONNumber parseNum(final boolean isNegative, final String whole, final String frac, final String exp) {
        final var wholePart = (isNegative ? "-" : "") + whole;
        if (frac.isEmpty()) {
            var bigInt = new BigInteger(wholePart);
            if (!exp.isEmpty()) {
                // try-catch parseInt?
                bigInt = bigInt.pow(Integer.parseInt(exp));
            }

            final var longValue = bigInt.longValue();
            if (!bigInt.equals(BigInteger.valueOf(longValue))) {
                return new JSONNumber(bigInt);
            }

            final var byteValue = bigInt.byteValue();
            if (byteValue == longValue) {
                return new JSONNumber(byteValue);
            }

            final var shortValue = bigInt.shortValue();
            if (shortValue == longValue) {
                return new JSONNumber(shortValue);
            }

            final var intValue = bigInt.intValue();
            if (intValue == longValue) {
                return new JSONNumber(intValue);
            }

            return new JSONNumber(longValue);
        }

        var bigDecimal = new BigDecimal(wholePart + frac + exp);

        final var doubleValue = bigDecimal.doubleValue();
        if (!bigDecimal.equals(BigDecimal.valueOf(doubleValue))) {
            return new JSONNumber(bigDecimal);
        }

        final var floatValue = bigDecimal.floatValue();
        if (floatValue == doubleValue) {
            return new JSONNumber(floatValue);
        }

        return new JSONNumber(doubleValue);
    }

    private static Optional<Map.Entry<JSONElement, String>> parseString(final String json) {
        if (json.isEmpty() || json.charAt(0) != '"') {
            return Optional.empty();
        }

        final var len = json.length();
        var idx = 1;
        final var valueBuilder = new StringBuilder();
        while (idx < len) {
            final var currChar = json.charAt(idx);
            if (currChar == '"') {
                return Optional.of(Map.entry(new JSONString(valueBuilder.toString()), json.substring(idx + 1)));
            } else if (currChar == '\\') {
                if (idx + 1 >= len) {
                    return Optional.empty();
                }
                final var escaped = json.charAt(idx + 1);
                switch (escaped) {
                    case '"', '\'', '/' -> valueBuilder.append(escaped);
                    case 'b' -> valueBuilder.append('\b');
                    case 'f' -> valueBuilder.append('\f');
                    case 'n' -> valueBuilder.append('\n');
                    case 'r' -> valueBuilder.append('\r');
                    case 't' -> valueBuilder.append('\t');
                    case 'u' -> {
                        if (idx + 5 >= len) {
                            return Optional.empty();
                        }
                        // hexIdx includes the 'u' symbol
                        var unicodeValue = 0L;
                        for (var hexIdx = 1; hexIdx < 5; hexIdx++) {
                            final var hexChar = json.charAt(idx + hexIdx);
                            switch (hexChar) {
                                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> unicodeValue = (unicodeValue << 4) | (hexChar - '0');
                                case 'A', 'B', 'C', 'D', 'E', 'F' -> unicodeValue = (unicodeValue << 4) | (hexChar - 'A' + 10);
                                case 'a', 'b', 'c', 'd', 'e', 'f' -> unicodeValue = (unicodeValue << 4) | (hexChar - 'a' + 10);
                                default -> {
                                    return Optional.empty();
                                }
                            }
                        }
                        valueBuilder.append((char) unicodeValue);
                    }
                    default -> {
                        return Optional.empty();
                    }
                }
            } else {
                valueBuilder.append(currChar);
            }
            idx++;
        }
        return Optional.empty();
    }

    private static Optional<Map.Entry<JSONElement, String>> parseArray(String json) {
        if (json.isEmpty() || json.charAt(0) != '[') {
            return Optional.empty();
        }

        json = json.substring(1).stripLeading();
        final var valueList = new ArrayList<JSONElement>();
        var expectsEnd = false;
        var expectsValue = false;
        while (!json.isEmpty()) {
            if (json.charAt(0) == ']') {
                return expectsValue ? Optional.empty() : Optional.of(Map.entry(new JSONArray(valueList), json.substring(1)));
            }

            if (expectsEnd) {
                return Optional.empty();
            }

            final var parsedValue = parseJSON(json);
            if (parsedValue.isEmpty()) {
                return Optional.empty();
            }
            final var currValue = parsedValue.get();

            valueList.add(currValue.getKey());
            json = currValue.getValue().stripLeading();
            if (json.charAt(0) == ',') {
                json = json.substring(1).stripLeading();
                expectsValue = true;
            } else {
                expectsEnd = true;
                expectsValue = false;
            }
        }
        return Optional.empty();
    }

    private static Optional<Map.Entry<JSONElement, String>> parseObject(String json) {
        if (json.isEmpty() || json.charAt(0) != '{') {
            return Optional.empty();
        }

        json = json.substring(1).stripLeading();
        final var valueMap = new HashMap<String, JSONElement>();
        var expectsEnd = false;
        var expectsValue = false;
        while (!json.isEmpty()) {
            if (json.charAt(0) == '}') {
                return expectsValue ? Optional.empty() : Optional.of(Map.entry(new JSONObject(valueMap), json.substring(1)));
            }

            if (expectsEnd) {
                return Optional.empty();
            }

            final var parsedKey = parseString(json);
            if (parsedKey.isEmpty()) {
                return Optional.empty();
            }
            final var currKey = parsedKey.get();

            json = currKey.getValue().stripLeading();
            if (json.charAt(0) != ':') {
                return Optional.empty();
            }
            json = json.substring(1).stripLeading();

            final var parsedValue = parseJSON(json);
            if (parsedValue.isEmpty()) {
                return Optional.empty();
            }
            final var currValue = parsedValue.get();

            valueMap.put(currKey.getKey().asString().get().value(), currValue.getKey());
            json = currValue.getValue().stripLeading();
            if (json.charAt(0) == ',') {
                json = json.substring(1).stripLeading();
                expectsValue = true;
            } else {
                expectsEnd = true;
                expectsValue = false;
            }
        }
        return Optional.empty();
    }

    static String escapeString(final String str) {
        return str
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\f", "\\f")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\\", "\\\\");
    }
}
