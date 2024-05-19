package com.rocketpartners.game.entities.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.engine.audio.AudioComponent;
import com.engine.common.extensions.CameraExtensionsKt;
import com.engine.components.IGameComponent;
import com.engine.cullables.CullableOnEvent;
import com.engine.cullables.CullableOnUncontained;
import com.engine.cullables.CullablesComponent;
import com.engine.cullables.ICullable;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.entities.contracts.IProjectileEntity;
import com.rocketpartners.game.events.EventType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectileComponents {

    public static Array<IGameComponent> defineProjectileComponents(IProjectileEntity projectile) {
        Array<IGameComponent> components = new Array<>();
        components.add(new AudioComponent(projectile));
        CullablesComponent cullablesComponent = new CullablesComponent(projectile, getCullables(projectile));
        components.add(cullablesComponent);
        return components;
    }

    private static @NotNull ObjectMap<String, ICullable> getCullables(IProjectileEntity projectile) {
        ObjectSet<Object> cullEvents = new ObjectSet<>();
        cullEvents.addAll(EventType.PLAYER_SPAWN, EventType.BEGIN_ROOM_TRANS, EventType.GATE_INIT_OPENING);
        CullableOnEvent cullOnEvent = new CullableOnEvent(it -> cullEvents.contains(it.getKey()), cullEvents);
        CullableOnUncontained<Camera> cullOnOutOfGameCam = new CullableOnUncontained<>(
                () -> projectile.getGame().getViewports().get(Constants.ConstKeys.GAME).getCamera(),
                it -> CameraExtensionsKt.overlaps(it, projectile.getBody()),
                Constants.ConstVals.STANDARD_TIME_TO_CULL
        );
        ObjectMap<String, ICullable> cullables = new ObjectMap<>();
        cullables.put(Constants.ConstKeys.CULL_EVENTS, cullOnEvent);
        cullables.put(Constants.ConstKeys.CULL_OUT_OF_BOUNDS, cullOnOutOfGameCam);
        return cullables;
    }
}