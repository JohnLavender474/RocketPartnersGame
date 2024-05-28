package com.rocketpartners.game.entities.blocks;

import com.engine.IGame2D;
import com.engine.common.shapes.GameRectangle;
import com.engine.world.Body;
import com.engine.world.BodyComponent;
import com.engine.world.BodyType;
import com.engine.world.Fixture;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.world.BodyComponentCreator;
import com.rocketpartners.game.world.FixtureType;
import org.jetbrains.annotations.NotNull;

public class WorldBlock extends BaseBlock {

    public static final float STANDARD_FRICTION_X = 0.075f;
    public static final float STANDARD_FRICTION_Y = 0f;

    public WorldBlock(@NotNull IGame2D game) {
        super(game, BodyType.STATIC, false);
    }

    @Override
    protected BodyComponent defineBodyComponent() {
        BodyComponent bodyComponent = super.defineBodyComponent();
        Body body = bodyComponent.getBody();
        body.getPhysics().getFrictionToApply().set(STANDARD_FRICTION_X, STANDARD_FRICTION_Y);

        GameRectangle worldBlockBounds = new GameRectangle();
        Fixture worldBlockFixture = Fixture.Companion.createFixture(body, FixtureType.WORLD_BLOCK, worldBlockBounds);
        body.addFixture(worldBlockFixture);

        body.getPreProcess().put(Constants.ConstKeys.DEFAULT,
                delta -> worldBlockBounds.set(body.getBoundingRectangle()));

        return BodyComponentCreator.append(this, bodyComponent);
    }
}
