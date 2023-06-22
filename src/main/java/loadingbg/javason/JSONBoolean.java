package loadingbg.javason;

import java.util.*;

public record JSONBoolean(boolean value) implements JSONElement {
    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return true;
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
        return false;
    }

    @Override
    public Optional<JSONNull> asNull() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONBoolean> asBoolean() {
        return Optional.of(this);
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
        return Optional.empty();
    }

    @Override
    public String toJSONString() {
        return Boolean.toString(value);
    }
}
