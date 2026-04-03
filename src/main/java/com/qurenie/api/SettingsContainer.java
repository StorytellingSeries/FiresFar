package com.qurenie.api;

import lombok.Builder;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public class SettingsContainer<T> implements Iterable<T> {

    @Singular
    List<T> settings;

    @Override
    public @NotNull Iterator<T> iterator() {
        return settings.iterator();
    }

    public @NotNull NamedSettingsContainer<T> named(Function<T, String> getter) {
        return new NamedSettingsContainer<>(settings.stream().collect(Collectors.toUnmodifiableMap(getter, t -> t)));
    }

    /**
     * Возвращает новый {@link SettingsContainer}, в который добавлены элементы
     * из {@code other}, которых нет в {@code this}.
     *
     * @param other другой контейнер
     * @return новый SettingsContainer с объединёнными элементами
     */
    public @NotNull SettingsContainer<T> merge(@NotNull SettingsContainer<T> other) {
        if (other.settings.isEmpty()) {
            return this;
        }

        List<T> newSettings = new ArrayList<>(this.settings);

        // добавляем только те элементы, которые ещё не содержатся в this
        for (T otherSetting : other.settings) {
            if (newSettings.stream().noneMatch(s -> Objects.equals(s, otherSetting))) {
                newSettings.add(otherSetting);
            }
        }

        return SettingsContainer.<T>builder()
                .settings(newSettings)
                .build();
    }
}
