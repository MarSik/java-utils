package com.github.marsik.utils.bugzilla;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Bug extends CallDictResult {
    public Bug(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Bug(int initialCapacity) {
        super(initialCapacity);
    }

    public Bug() {
        super();
    }

    public Bug(Map<? extends String, ?> m) {
        super(m);
    }

    public void applyChange(String field, Object oldValue, Object newValue) {
        computeIfAbsent(field, key -> new ArrayList<>());
        compute(field, (_f, value) -> {
           if (value instanceof Collection) {
               if (oldValue != null) {
                   ((Collection) value).remove(oldValue);
               }
               if (newValue != null) {
                   ((Collection) value).add(newValue);
               }
               return value;
           } else {
               return newValue;
           }
        });
    }
}
