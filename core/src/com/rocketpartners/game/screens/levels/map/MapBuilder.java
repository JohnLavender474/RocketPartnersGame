package com.rocketpartners.game.screens.levels.map;

import com.badlogic.gdx.utils.OrderedMap;
import com.engine.screens.levels.tiledmap.builders.ITiledMapLayerBuilder;
import com.engine.screens.levels.tiledmap.builders.TiledMapLayerBuilders;
import com.engine.spawns.SpawnsManager;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.screens.levels.camera.CameraManagerForRooms;
import com.rocketpartners.game.screens.levels.map.layers.AbstractBoundsLayer;
import com.rocketpartners.game.screens.levels.map.layers.PlayerSpawnsLayer;
import com.rocketpartners.game.screens.levels.map.layers.RoomsLayer;
import com.rocketpartners.game.screens.levels.map.layers.WorldBlocksLayer;
import com.rocketpartners.game.screens.levels.spawns.PlayerSpawnsManager;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MapBuilder extends TiledMapLayerBuilders {

    private static final String ROOMS_LAYER = "rooms";
    private static final String ABSTRACT_BOUNDS_LAYER = "abstract_bounds";
    private static final String WORLD_BLOCKS_LAYER = "world_blocks";
    private static final String PLAYER_SPAWNS_LAYER = "player_spawns";

    private final RocketPartnersGame game;
    private final CameraManagerForRooms cameraManagerForRooms;
    private final SpawnsManager spawnsMan;
    private final PlayerSpawnsManager playerSpawnsMan;

    @NotNull
    @Override
    public OrderedMap<String, ITiledMapLayerBuilder> getLayerBuilders() {
        OrderedMap<String, ITiledMapLayerBuilder> layerBuildersMap = new OrderedMap<>();
        layerBuildersMap.put(ROOMS_LAYER, new RoomsLayer(cameraManagerForRooms));
        layerBuildersMap.put(ABSTRACT_BOUNDS_LAYER, new AbstractBoundsLayer(game));
        layerBuildersMap.put(WORLD_BLOCKS_LAYER, new WorldBlocksLayer(game));
        layerBuildersMap.put(PLAYER_SPAWNS_LAYER, new PlayerSpawnsLayer(playerSpawnsMan));
        return layerBuildersMap;
    }
}
