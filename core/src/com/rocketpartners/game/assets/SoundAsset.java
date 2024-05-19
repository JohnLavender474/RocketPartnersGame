package com.rocketpartners.game.assets;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SoundAsset implements IAsset {
    PAUSE_SOUND("pause_menu.mp3", 1),
    PLAYER_DAMAGE_SOUND("player_damage.mp3", 1);

    public static final String SOUND_ASSET_PREFIX = "sounds/";

    private final String src;
    @Getter
    private final int seconds;

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
        return Sound.class;
    }
}
