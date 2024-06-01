package com.rocketpartners.game.drawables;

import com.badlogic.gdx.math.Vector2;
import com.engine.drawables.fonts.BitmapFontHandle;
import com.engine.drawables.sorting.DrawingPriority;
import com.engine.drawables.sorting.DrawingSection;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.rocketpartners.game.Constants.ConstVals;

public class BitmapFontHandleUtils {

    public static BitmapFontHandle create(@NotNull Supplier<String> textSupplier) {
        return new BitmapFontHandle(
                (Function0<String>) textSupplier::get,
                ConstVals.FONT_SIZE,
                new Vector2(),
                true,
                true,
                false,
                null,
                new DrawingPriority(DrawingSection.FOREGROUND, 1)
        );
    }

    public static BitmapFontHandle create(@NotNull String text) {
        return new BitmapFontHandle(
                text,
                ConstVals.FONT_SIZE,
                new Vector2(),
                true,
                true,
                false,
                null,
                new DrawingPriority(DrawingSection.FOREGROUND, 1)
        );
    }
}
