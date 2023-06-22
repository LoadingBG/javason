package loadingbg.javason;

import java.util.*;
import java.util.stream.*;

public record JSONObject(Map<String, JSONElement> value) implements JSONElement {
    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public Optional<JSONNull> asNull() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONBoolean> asBoolean() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONNumber> asNumber() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONString> asString() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONArray> asArray() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONObject> asObject() {
        return Optional.of(this);
    }

    @Override
    public String toJSONString() {
        return value.entrySet()
            .stream()
            .map(e -> "\"" + e.getKey() + "\":" + e.getValue().toJSONString())
            .collect(Collectors.joining(",", "{", "}"));
    }

    public Optional<JSONElement> get(final String key) {
        return value.containsKey(key) ? Optional.of(value.get(key)) : Optional.empty();
    }

    public Optional<JSONElement> dig(final String key, final Object... subkeys) {
        return digInternal(0, key, subkeys);
    }

    public Optional<JSONNull> digNull(final String key, final Object... subkeys) {
        return dig(key, subkeys).flatMap(JSONElement::asNull);
    }

    public Optional<JSONBoolean> digBoolean(final String key, final Object... subkeys) {
        return dig(key, subkeys).flatMap(JSONElement::asBoolean);
    }

    public Optional<JSONNumber> digNumber(final String key, final Object... subkeys) {
        return dig(key, subkeys).flatMap(JSONElement::asNumber);
    }

    public Optional<JSONString> digString(final String key, final Object... subkeys) {
        return dig(key, subkeys).flatMap(JSONElement::asString);
    }

    public Optional<JSONArray> digArray(final String key, final Object... subkeys) {
        return dig(key, subkeys).flatMap(JSONElement::asArray);
    }

    public Optional<JSONObject> digObject(final String key, final Object... subkeys) {
        return dig(key, subkeys).flatMap(JSONElement::asObject);
    }

    Optional<JSONElement> digInternal(final int subkeysIndex, final String key, final Object... subkeys) {
        if (!value.containsKey(key)) {
            return Optional.empty();
        }

        final var subvalue = value.get(key);
        if (subkeysIndex >= subkeys.length) {
            return Optional.of(subvalue);
        }

        if (subkeys[subkeysIndex] instanceof Integer nextIndex && subvalue instanceof JSONArray array) {
            return array.digInternal(subkeysIndex + 1, nextIndex, subkeys);
        }
        if (subkeys[subkeysIndex] instanceof String nextKey && subvalue instanceof JSONObject object) {
            return object.digInternal(subkeysIndex + 1, nextKey, subkeys);
        }
        return Optional.empty();
    }
}
