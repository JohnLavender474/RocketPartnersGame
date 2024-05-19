package com.rocketpartners.game.entities.blocks;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.ObjectMap;
import com.engine.IGame2D;
import com.engine.common.ClassInstanceUtils;
import com.engine.common.objects.Properties;
import com.engine.common.shapes.GameRectangle;
import com.engine.cullables.CullableOnUncontained;
import com.engine.cullables.CullablesComponent;
import com.engine.cullables.ICullable;
import com.engine.entities.GameEntity;
import com.engine.world.Body;
import com.engine.world.BodyComponent;
import com.engine.world.BodyType;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.cullables.CullablesUtils;
import com.rocketpartners.game.world.BodyComponentCreator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class Block extends GameEntity {

    public static final String TAG = "Block";

    private final BodyType bodyType;
    private final boolean isAbstract;

    public Block(@NotNull IGame2D game, BodyType bodyType, boolean isAbstract) {
        super(game);
        this.bodyType = bodyType;
        this.isAbstract = isAbstract;
    }

    @Override
    public void init() {
        addComponent(defineBodyComponent());
        addComponent(new CullablesComponent(this, new ObjectMap<>()));
    }

    @Override
    public void spawn(@NotNull Properties props) {
        super.spawn(props);
        GameRectangle bounds = props.get(Constants.ConstKeys.BOUNDS,
                ClassInstanceUtils.convertToKClass(GameRectangle.class));
        Body body =
                Objects.requireNonNull(getComponent(ClassInstanceUtils.convertToKClass(BodyComponent.class))).getBody();
        assert bounds != null;
        body.set(bounds);

        CullablesComponent cullablesComponent =
                getComponent(ClassInstanceUtils.convertToKClass(CullablesComponent.class));
        assert cullablesComponent != null;
        ObjectMap<String, ICullable> cullables = cullablesComponent.getCullables();
        boolean cullOutOfBounds = props.getOrDefault(Constants.ConstKeys.CULL_OUT_OF_BOUNDS, true,
                ClassInstanceUtils.convertToKClass(Boolean.class));
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
}
