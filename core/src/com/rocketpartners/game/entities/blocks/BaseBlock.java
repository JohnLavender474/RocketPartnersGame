package com.rocketpartners.game.entities.blocks;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.engine.IGame2D;
import com.engine.common.ClassInstanceUtils;
import com.engine.common.objects.Properties;
import com.engine.common.shapes.GameRectangle;
import com.engine.cullables.CullableOnUncontained;
import com.engine.cullables.CullablesComponent;
import com.engine.cullables.ICullable;
import com.engine.drawables.shapes.DrawableShapesComponent;
import com.engine.entities.GameEntity;
import com.engine.entities.contracts.IBodyEntity;
import com.engine.entities.contracts.IDrawableShapesEntity;
import com.engine.world.Body;
import com.engine.world.BodyComponent;
import com.engine.world.BodyType;
import com.engine.world.IFixture;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.cullables.CullablesUtils;
import com.rocketpartners.game.world.BodyComponentCreator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class BaseBlock extends GameEntity implements IBodyEntity, IDrawableShapesEntity {

    public static final String TAG = "Block";

    private final BodyType bodyType;
    private final boolean isAbstract;

    public BaseBlock(@NotNull IGame2D game, @NotNull BodyType bodyType, boolean isAbstract) {
        super(game);
        this.bodyType = bodyType;
        this.isAbstract = isAbstract;
    }

    @Override
    public void init() {
        addComponent(defineBodyComponent());
        addComponent(new CullablesComponent(this, new ObjectMap<>()));
        addComponent(new DrawableShapesComponent(this, new Array<>(), new Array<>(), true));
        addDebugShapeSupplier(this::getBody);
    }

    @Override
    public void spawn(@NotNull Properties props) {
        super.spawn(props);
        GameRectangle bounds = props.get(Constants.ConstKeys.BOUNDS, GameRectangle.class);
        Body body =
                Objects.requireNonNull(getComponent(ClassInstanceUtils.convertToKClass(BodyComponent.class))).getBody();
        assert bounds != null;
        body.set(bounds);

        CullablesComponent cullablesComponent =
                getComponent(ClassInstanceUtils.convertToKClass(CullablesComponent.class));
        assert cullablesComponent != null;

        ObjectMap<String, ICullable> cullables = cullablesComponent.getCullables();
        boolean cullOutOfBounds = props.getOrDefault(Constants.ConstKeys.CULL_OUT_OF_BOUNDS, false, Boolean.class);
        if (cullOutOfBounds) {
            Camera gameCamera = getGame().getViewports().get(Constants.ConstKeys.GAME).getCamera();
            CullableOnUncontained<GameRectangle> cullableOnUncontained =
                    CullablesUtils.getCameraCullingLogic(gameCamera, body, Constants.ConstVals.STANDARD_TIME_TO_CULL);

            cullables.put(Constants.ConstKeys.CULL_OUT_OF_BOUNDS, cullableOnUncontained);
        } else {
            cullables.remove(Constants.ConstKeys.CULL_OUT_OF_BOUNDS);
        }
    }

    protected BodyComponent defineBodyComponent() {
        Body body = Body.Companion.createBody(bodyType);
        return BodyComponentCreator.create(this, body);
    }

    public void hitByHead(@NotNull IFixture headFixture) {
        // Do nothing
    }

    public void hitBySide(@NotNull IFixture sideFixture) {
        // Do nothing
    }

    public void hitByFeet(@NotNull IFixture feetFixture) {
        // Do nothing
    }
}
