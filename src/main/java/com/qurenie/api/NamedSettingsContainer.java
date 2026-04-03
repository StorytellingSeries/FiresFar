package com.qurenie.api;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class NamedSettingsContainer<T> {

    Map<String, T> map;

    public Map<String, T> getValues() {
        return map;
    }

    public T get(String key) {
        return map.get(key);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    NamedSettingsContainer(Map<String, T> map) {
        this.map = map;
    }

    public NamedSettingsContainerBuilder<T> builder() {
        return new NamedSettingsContainerBuilder<>();
    }

    public static class NamedSettingsContainerBuilder<T> {

        HashMap<String, T> m;

        public NamedSettingsContainerBuilder<T> addSetting(String key, T value) {
            m.put(key, value);
            return this;
        }

        public NamedSettingsContainer<T> build() {
            return new NamedSettingsContainer<>(ImmutableMap.copyOf(m));
        }

    }

}
