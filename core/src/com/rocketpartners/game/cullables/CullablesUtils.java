package com.rocketpartners.game.cullables;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.engine.common.extensions.CameraExtensionsKt;
import com.engine.common.shapes.GameRectangle;
import com.engine.cullables.CullableOnUncontained;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CullablesUtils {

    public static CullableOnUncontained<GameRectangle> getCameraCullingLogic(Camera camera, GameRectangle bounds,
                                                                             float timeToCull) {
        return new CullableOnUncontained<>(() -> CameraExtensionsKt.toGameRectangle(camera),
                o -> bounds.overlaps((Rectangle) o), timeToCull);
    }
}
