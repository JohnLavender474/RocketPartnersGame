package com.rocketpartners.game.screens.levels;

import com.rocketpartners.game.screens.ScreenEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreenOnLevelCompletionMap {

    public static ScreenEnum getScreen(LevelEnum level) {
        return switch (level) {
            case TEST1 -> ScreenEnum.SAVE_GAME_SCREEN;
        };
    }
}
