package com.rocketpartners.game.assets;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SoundAsset implements IAsset {
    ;

    public static final String SOUND_ASSET_PREFIX = "sound/";

    private final String src;

    public static Array<IAsset> asAssetArray() {
        Array<IAsset> array = new Array<>();
        for (SoundAsset asset : SoundAsset.values()) {
            array.add(asset);
        }
        return array;
    }

    @NotNull
    @Override
    public String getSource() {
        return SOUND_ASSET_PREFIX + src;
    }

    @NotNull
    @Override
    public Class<?> getAssClass() {
        return Music.class;
    }
}
