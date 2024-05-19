package com.rocketpartners.game.screens.levels;

import com.rocketpartners.game.assets.MusicAsset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LevelEnum {
    TEST1("Test1.tmx", MusicAsset.MMX2_X_HUNTER_MUSIC);

    private final String tmxSourceFile;
    private final MusicAsset musicAsset;
}
