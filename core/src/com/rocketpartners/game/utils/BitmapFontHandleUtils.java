package com.rocketpartners.game.utils;

import com.badlogic.gdx.math.Vector2;
import com.engine.drawables.fonts.BitmapFontHandle;
import com.engine.drawables.sorting.DrawingPriority;
import com.engine.drawables.sorting.DrawingSection;
import org.jetbrains.annotations.NotNull;

import static com.rocketpartners.game.Constants.*;

public class BitmapFontHandleUtils {

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
