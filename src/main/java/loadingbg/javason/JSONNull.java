package loadingbg.javason;

import java.util.*;

public final class JSONNull implements JSONElement {
    static final JSONNull INSTANCE = new JSONNull();

    private JSONNull() {}

    @Override
    public boolean isNull() {
        return true;
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
        return false;
    }

    @Override
    public Optional<JSONNull> asNull() {
        return Optional.of(this);
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
        return Optional.empty();
    }

    @Override
    public String toJSONString() {
        return "null";
    }

    @Override
    public String toString() {
        return "JSONNull";
    }
}
