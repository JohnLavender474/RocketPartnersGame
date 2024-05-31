package com.rocketpartners.game.screens.levels.map.layers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.engine.common.extensions.AssetManagerExtensionsKt;
import com.engine.common.objects.Properties;
import com.engine.screens.levels.tiledmap.builders.ITiledMapLayerBuilder;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.assets.SpriteSheetAsset;
import com.rocketpartners.game.drawables.Background;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import static com.rocketpartners.game.Constants.*;

@RequiredArgsConstructor
public class BackgroundsLayer implements ITiledMapLayerBuilder {

    private final RocketPartnersGame game;

    @Override
    public void build(@NotNull MapLayer mapLayer, @NotNull Properties properties) {
        Array<Background> backgrounds = new Array<>();

        for (MapObject mapObject : mapLayer.getObjects()) {
            if (mapObject instanceof RectangleMapObject r) {
                String atlasKey = SpriteSheetAsset.SPRITE_SHEET_ASSET_PREFIX + r.getProperties().get(ConstKeys.ATLAS);
                String regionKey = (String) r.getProperties().get(ConstKeys.REGION);
                int rows = (int) r.getProperties().get(ConstKeys.ROWS);
                int columns = (int) r.getProperties().get(ConstKeys.COLUMNS);
                TextureRegion backgroundRegion = AssetManagerExtensionsKt.getTextureRegion(
                        game.getAssMan(), atlasKey, regionKey);
                Rectangle bounds = r.getRectangle();
                Background background = new Background(bounds.getX(), bounds.getY(), backgroundRegion,
                        bounds.getWidth(), bounds.getHeight(), rows, columns);
                backgrounds.add(background);
            }
        }

        properties.put(ConstKeys.BACKGROUNDS, backgrounds);
    }
}
