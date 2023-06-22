package loadingbg.javason;

import java.util.*;
import java.util.stream.*;

public record JSONArray(List<JSONElement> value) implements JSONElement {
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
        return true;
    }

    @Override
    public boolean isObject() {
        return false;
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
        return Optional.of(this);
    }

    @Override
    public Optional<JSONObject> asObject() {
        return Optional.empty();
    }

    @Override
    public String toJSONString() {
        return value.stream()
            .map(JSONElement::toJSONString)
            .collect(Collectors.joining(",", "[", "]"));
    }

    public Optional<JSONElement> get(final int index) {
        return index >= value.size() ? Optional.empty() : Optional.of(value.get(index));
    }

    public Optional<JSONElement> dig(final int index, final Object... subkeys) {
        return digInternal(0, index, subkeys);
    }

    public Optional<JSONNull> digNull(final int index, final Object... subkeys) {
        return dig(index, subkeys).flatMap(JSONElement::asNull);
    }

    public Optional<JSONBoolean> digBoolean(final int index, final Object... subkeys) {
        return dig(index, subkeys).flatMap(JSONElement::asBoolean);
    }

    public Optional<JSONNumber> digNumber(final int index, final Object... subkeys) {
        return dig(index, subkeys).flatMap(JSONElement::asNumber);
    }

    public Optional<JSONString> digString(final int index, final Object... subkeys) {
        return dig(index, subkeys).flatMap(JSONElement::asString);
    }

    public Optional<JSONArray> digArray(final int index, final Object... subkeys) {
        return dig(index, subkeys).flatMap(JSONElement::asArray);
    }

    public Optional<JSONObject> digObject(final int index, final Object... subkeys) {
        return dig(index, subkeys).flatMap(JSONElement::asObject);
    }

    Optional<JSONElement> digInternal(final int subkeysIndex, final int index, final Object... subkeys) {
        if (index >= value.size()) {
            return Optional.empty();
        }

        final var subvalue = value.get(index);
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
