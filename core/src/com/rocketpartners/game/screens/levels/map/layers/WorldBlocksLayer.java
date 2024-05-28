package com.rocketpartners.game.screens.levels.map.layers;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.engine.common.objects.Properties;
import com.engine.screens.levels.tiledmap.builders.ITiledMapLayerBuilder;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.entities.blocks.WorldBlock;
import com.rocketpartners.game.utils.MapObjectUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class WorldBlocksLayer implements ITiledMapLayerBuilder {

    private final RocketPartnersGame game;

    @Override
    public void build(@NotNull MapLayer mapLayer, @NotNull Properties properties) {
        mapLayer.getObjects().forEach(object -> {
            if (object instanceof RectangleMapObject) {
                Properties spawnProps = MapObjectUtils.convertToProps(object);
                assert spawnProps != null;
                WorldBlock worldBlock = new WorldBlock(game);
                game.getEngine().spawn(worldBlock, spawnProps);
            }
        });
    }
}
