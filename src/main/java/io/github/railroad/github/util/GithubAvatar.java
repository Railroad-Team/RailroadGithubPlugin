package io.github.railroad.github.util;

import io.github.railroad.github.data.GithubAccount;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GithubAvatar {
    private static final Map<Integer, Image> AVATAR_CACHE = new ConcurrentHashMap<>();

    public static Image getAvatar(GithubAccount account) {
        return AVATAR_CACHE.computeIfAbsent(account.getUserId(),
                $ -> new Image(account.getOrRequestUser().avatarUrl(), true));
    }
}