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
        public static final String SPAWNERS = "spawners";
        public static final String NAME = "name";
        public static final String BOUNDS = "bounds";
        public static final String POLYGON = "polygon";
        public static final String CIRCLE = "circle";
        public static final String LINES = "lines";
        public static final String POSITION = "position";
        public static final String ROOM = "room";
        public static final String ROOMS = "rooms";
        public static final String EVENT = "event";
        public static final String DISPOSABLES = "disposables";
        public static final String ENTITY = "entity";
        public static final String DELTA = "delta";
        public static final String PRIOR = "prior";
        public static final String CULL_EVENTS = "cull_events";
        public static final String CULL_OUT_OF_BOUNDS = "cull_out_of_bounds";
        public static final String HEALTH = "health";
        public static final String DEFAULT = "default";
        public static final String CURRENT = "current";
        public static final String GRAVITY_TYPE = "gravity_type";
        public static final String GRAVITY_DIRECTION = "gravity_direction";
        public static final String BODY_SENSES = "body_senses";
    }

    public static class ConstVals {
        public static final int VIEW_WIDTH = 12;
        public static final int VIEW_HEIGHT = 10;
        public static final int PPM = 32;
        public static final float WORLD_TIME_STEP = 1 / 150f;
        public static final float STANDARD_TIME_TO_CULL = 1f;
        public static final float NORMAL_GRAVITY = -0.375f;
        public static final float LOW_GRAVITY = -0.25f;
        public static final float GROUND_GRAVITY = -0.0015f;
        public static final int MAX_HEALTH = 30;
        public static final int MIN_HEALTH = 0;
        public static final long PATHFINDER_TIMEOUT = 10;
        public static final TimeUnit PATHFINDER_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    }

}
