package com.github.marsik.utils.bugzilla;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Value;

@Value
public class CallDictResult {
    Map<String, Object> map;

    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, Class<T> cls) {
        return (T)map.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        T[] val = (T[])map.get(key);
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
    public CallDictResult get(String key) {
        return new CallDictResult((Map<String, Object>)map.get(key));
    }

    public Set<String> keySet() {
        return map.keySet();
    }
}
