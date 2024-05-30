package com.rocketpartners.game.world;

import com.engine.common.interfaces.Updatable;
import com.engine.entities.IGameEntity;
import com.engine.world.Body;
import com.engine.world.BodyComponent;
import com.engine.world.IFixture;
import com.rocketpartners.game.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.rocketpartners.game.Constants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyComponentCreator {

    public static BodyComponent create(IGameEntity entity, Body body) {
        define(entity, body);
        return new BodyComponent(entity, body);
    }

    public static BodyComponent append(IGameEntity entity, BodyComponent bodyComponent) {
        Body body = bodyComponent.getBody();
        define(entity, body);
        return bodyComponent;
    }

    private static void define(IGameEntity entity, Body body) {
        body.putProperty(ConstKeys.ENTITY, entity);
        Updatable preProcessDelta = delta -> body.putProperty(ConstKeys.PRIOR, body.getPosition());
        body.getPreProcess().put(ConstKeys.DELTA, preProcessDelta);
        body.getFixtures().forEach(e -> {
            IFixture fixture = e.getSecond();
            fixture.putProperty(ConstKeys.ENTITY, entity);
        });
    }
}
