package com.rocketpartners.game.assets;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SpriteSheetAsset implements IAsset {
    PLAYER_8BIT_SPRITE_SHEET("player-8bit.txt");

    public static final String SPRITE_SHEET_ASSET_PREFIX = "sprites/sprite_sheets/";

    private final String src;

    public static Array<IAsset> asAssetArray() {
        Array<IAsset> array = new Array<>();
        for (SpriteSheetAsset asset : SpriteSheetAsset.values()) {
            array.add(asset);
        }
        return array;
    }

    @NotNull
    @Override
    public String getSource() {
        return SPRITE_SHEET_ASSET_PREFIX + src;
    }

    @NotNull
    @Override
    public Class<?> getAssClass() {
        return TextureAtlas.class;
    }
}
