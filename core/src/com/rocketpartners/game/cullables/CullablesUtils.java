package com.rocketpartners.game.cullables;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.engine.common.extensions.CameraExtensionsKt;
import com.engine.common.shapes.GameRectangle;
import com.engine.cullables.CullableOnUncontained;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CullablesUtils {

    public static CullableOnUncontained<GameRectangle> getCameraCullingLogic(
            @NotNull Camera camera, @NotNull GameRectangle bounds, float timeToCull) {
        return new CullableOnUncontained<>(
                (Supplier<GameRectangle>) () -> CameraExtensionsKt.toGameRectangle(camera),
                o -> bounds.overlaps((Rectangle) o), timeToCull);
    }
}
