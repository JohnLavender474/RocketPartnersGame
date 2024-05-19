package com.rocketpartners.game.screens.levels.map;

import com.badlogic.gdx.utils.OrderedMap;
import com.engine.screens.levels.tiledmap.builders.ITiledMapLayerBuilder;
import com.engine.screens.levels.tiledmap.builders.TiledMapLayerBuilders;
import com.engine.spawns.SpawnsManager;
import com.rocketpartners.game.RocketPartnersGame;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MapBuilder extends TiledMapLayerBuilders {

    private final RocketPartnersGame game;
    private final SpawnsManager spawnsMan;

    @NotNull
    @Override
    public OrderedMap<String, ITiledMapLayerBuilder> getLayerBuilders() {
        return null;
    }
}
