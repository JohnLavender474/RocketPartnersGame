package com.rocketpartners.game.screens.levels.map.layers;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;
import com.engine.common.objects.Properties;
import com.engine.screens.levels.tiledmap.builders.ITiledMapLayerBuilder;
import com.rocketpartners.game.screens.levels.spawns.PlayerSpawnsManager;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PlayerSpawnsLayer implements ITiledMapLayerBuilder {

    private final PlayerSpawnsManager playerSpawnsMan;

    @Override
    public void build(@NotNull MapLayer mapLayer, @NotNull Properties properties) {
        Array<RectangleMapObject> spawnObjs = new Array<>();
        mapLayer.getObjects().forEach(object -> {
            if (object instanceof RectangleMapObject) {
                spawnObjs.add((RectangleMapObject) object);
            }
        });
        playerSpawnsMan.setSpawnObjs(spawnObjs);
    }
}
