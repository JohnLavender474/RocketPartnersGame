package com.rocketpartners.game.screens.levels;

import com.rocketpartners.game.assets.MusicAsset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LevelEnum {
    TEST1("Test1.tmx", MusicAsset.INTO_THE_LAVA_PIT);

    private static final String TMX_PATH = "tiled_maps/tmx/";

    private final String tmxSourceFile;
    @Getter
    private final MusicAsset musicAsset;

    public String getTmxSourceFile() {
        return TMX_PATH + tmxSourceFile;
    }
}
