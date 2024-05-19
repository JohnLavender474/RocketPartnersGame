package com.rocketpartners.game.world;

import com.engine.world.Body;
import com.engine.world.ICollisionHandler;
import com.engine.world.StandardCollisionHandler;
import com.rocketpartners.game.RocketPartnersGame;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class CollisionHandler implements ICollisionHandler {

    private final RocketPartnersGame game;

    private boolean trySpecialCollision(@NotNull Body body1, @NotNull Body body2) {
        return false;
    }

    @Override
    public boolean handleCollision(@NotNull Body body1, @NotNull Body body2) {
        return trySpecialCollision(body1, body2) || StandardCollisionHandler.INSTANCE.handleCollision(body1, body2);
    }
}
