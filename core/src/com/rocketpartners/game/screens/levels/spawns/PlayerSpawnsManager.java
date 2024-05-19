package com.rocketpartners.game.screens.levels.spawns;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.engine.common.extensions.CameraExtensionsKt;
import com.engine.common.interfaces.Resettable;
import com.engine.common.objects.Properties;
import com.rocketpartners.game.utils.MapObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;

public class PlayerSpawnsManager implements Runnable, Resettable {

    private final Camera camera;
    private final PriorityQueue<RectangleMapObject> spawnQueue;

    private RectangleMapObject currentSpawnObj;

    public PlayerSpawnsManager(@NotNull Camera camera) {
        this.camera = camera;
        spawnQueue = new PriorityQueue<>((o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o1.getName().compareTo(o2.getName());
        });
    }

    public void setSpawnObjs(Iterable<RectangleMapObject> objs) {
        for (RectangleMapObject obj : objs) {
            spawnQueue.add(obj);
        }
        currentSpawnObj = spawnQueue.poll();
    }

    public Properties getCurrentSpawnProps() {
        if (currentSpawnObj == null) {
            return null;
        }
        return MapObjectUtils.convertToProps(currentSpawnObj);
    }

    @Override
    public void run() {
        if (spawnQueue.isEmpty()) {
            return;
        }
        RectangleMapObject peekObj = spawnQueue.peek();
        if (CameraExtensionsKt.overlaps(camera, peekObj.getRectangle())) {
            currentSpawnObj = spawnQueue.poll();
        }
    }

    @Override
    public void reset() {
        spawnQueue.clear();
        currentSpawnObj = null;
    }
}
