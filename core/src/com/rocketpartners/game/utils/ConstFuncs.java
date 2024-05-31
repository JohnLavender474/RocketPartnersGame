package com.rocketpartners.game.utils;

import com.badlogic.gdx.math.Vector3;

import static com.rocketpartners.game.Constants.*;

public class ConstFuncs {

    public static Vector3 getCamInitPos() {
        Vector3 v = new Vector3();
        v.x = ConstVals.VIEW_WIDTH * ConstVals.PPM / 2f;
        v.y = ConstVals.VIEW_HEIGHT * ConstVals.PPM / 2f;
        return v;
    }
}
