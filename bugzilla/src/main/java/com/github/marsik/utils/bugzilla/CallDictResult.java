package com.github.marsik.utils.bugzilla;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Value;

public class CallDictResult extends HashMap<String, Object> {
    public CallDictResult(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CallDictResult(int initialCapacity) {
        super(initialCapacity);
    }

    public CallDictResult() {
    }

    public CallDictResult(Map<? extends String, ?> m) {
        super(m);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, Class<T> cls) {
        return (T)get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        T[] val = (T[])get(key);
        return Optional.ofNullable(val).map(Arrays::asList).orElse(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public List<CallDictResult> getDictList(String key) {
        List<Map<String, Object>> val = getList(key);
        if (val == null) {
            return Collections.emptyList();
        }
        return val.stream().map(CallDictResult::new).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public CallDictResult getDict(String key) {
        return new CallDictResult((Map<String, Object>)get(key));
    }
}
