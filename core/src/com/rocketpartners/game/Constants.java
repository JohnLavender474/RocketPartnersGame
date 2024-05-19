package com.rocketpartners.game;

import java.util.concurrent.TimeUnit;

public class Constants {

    public static class PreferenceFiles {
        public static final String ROCKET_PARTNERS_KEYBOARD_PREFERENCES = "rocket_partners_keyboard_preferences";
    }

    public static class ConstKeys {
        public static final String GAME = "game";
        public static final String BACKGROUND = "background";
        public static final String BACKGROUNDS = "backgrounds";
        public static final String PLAYER = "player";
        public static final String SPAWNS = "spawns";
        public static final String NAME = "name";
        public static final String BOUNDS = "bounds";
        public static final String POLYGON = "polygon";
        public static final String CIRCLE = "circle";
        public static final String LINES = "lines";
    }

    public static class ConstVals {
        public static final int VIEW_WIDTH = 16;
        public static final int VIEW_HEIGHT = 12;
        public static final int PPM = 32;
        public static final float WORLD_TIME_STEP = 1 / 150f;
        public static final int MAX_HEALTH = 30;
        public static final int MIN_HEALTH = 0;
        public static final long PATHFINDER_TIMEOUT = 10;
        public static final TimeUnit PATHFINDER_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    }

}
