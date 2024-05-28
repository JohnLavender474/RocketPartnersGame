package com.rocketpartners.game.screens.levels.map.layers;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;
import com.engine.common.objects.Properties;
import com.engine.screens.levels.tiledmap.builders.ITiledMapLayerBuilder;
import com.rocketpartners.game.screens.levels.camera.CameraManagerForRooms;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class RoomsLayer implements ITiledMapLayerBuilder {

    private final CameraManagerForRooms cameraManagerForRooms;

    @Override
    public void build(@NotNull MapLayer mapLayer, @NotNull Properties properties) {
        Array<RectangleMapObject> rooms = new Array<>();
        mapLayer.getObjects().forEach(object -> {
            if (object instanceof RectangleMapObject) {
                rooms.add((RectangleMapObject) object);
            }
        });
        cameraManagerForRooms.setRooms(rooms);
    }
}
