/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FeaturePropertiesMap {
  private final ConcurrentHashMap<String, Set<String>> map = new ConcurrentHashMap<>();

  public void putValue(String key, String value) {
    map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
  }

  public void putValues(String key, Set<String> values) {
    map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).addAll(values);
  }

  public Set<String> getValues(String key) {
    return map.getOrDefault(key, Collections.emptySet());
  }

  public Map<String, Set<String>> getMap() {
    return map.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey, entry -> Collections.unmodifiableSet(entry.getValue())));
  }
}
