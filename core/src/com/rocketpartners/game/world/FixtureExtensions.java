package com.rocketpartners.game.world;

import com.engine.entities.IGameEntity;
import com.engine.world.IFixture;
import com.rocketpartners.game.Constants;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class FixtureExtensions {

    public static IGameEntity getEntity(IFixture fixture) {
        return (IGameEntity) fixture.getProperty(Constants.ConstKeys.ENTITY);
    }
}
