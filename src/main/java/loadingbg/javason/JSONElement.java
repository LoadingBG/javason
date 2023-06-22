package loadingbg.javason;

import java.util.*;

public sealed interface JSONElement permits JSONNull, JSONBoolean, JSONNumber, JSONString, JSONArray, JSONObject {
    boolean isNull();
    boolean isBoolean();
    boolean isNumber();
    boolean isString();
    boolean isArray();
    boolean isObject();

    Optional<JSONNull> asNull();
    Optional<JSONBoolean> asBoolean();
    Optional<JSONNumber> asNumber();
    Optional<JSONString> asString();
    Optional<JSONArray> asArray();
    Optional<JSONObject> asObject();

    String toJSONString();
}
