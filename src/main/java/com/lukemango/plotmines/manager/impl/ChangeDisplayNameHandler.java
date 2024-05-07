package com.lukemango.plotmines.manager.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChangeDisplayNameHandler {

    private static final Cache<UUID, Mine> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS)
            .build();

    public static boolean isChangingDisplayName(UUID uuid) {
        return cache.getIfPresent(uuid) != null;
    }

    public static void setChangingDisplayName(UUID uuid, Mine mine) {
        cache.put(uuid, mine);
    }

    public static void removeChangingDisplayName(UUID uuid) {
        cache.invalidate(uuid);
    }

    public static Mine getChangingDisplayName(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

}
