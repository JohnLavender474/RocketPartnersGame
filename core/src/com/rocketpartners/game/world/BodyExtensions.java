package com.rocketpartners.game.world;

import com.badlogic.gdx.math.Vector2;
import com.engine.world.Body;
import com.rocketpartners.game.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

import static com.rocketpartners.game.Constants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyExtensions {

    public static boolean isBodySensing(@NotNull Body body, @NotNull BodySense bodySense) {
        Set<BodySense> bodySenses = getBodySenses(body);
        return bodySenses.contains(bodySense);
    }

    public static Set<BodySense> getBodySenses(@NotNull Body body) {
        if (!body.hasProperty(ConstKeys.BODY_SENSES)) {
            body.putProperty(ConstKeys.BODY_SENSES, EnumSet.noneOf(BodySense.class));
        }
        return (Set<BodySense>) body.getProperty(ConstKeys.BODY_SENSES);
    }

    public static void setBodySense(@NotNull Body body, @NotNull BodySense bodySense, boolean value) {
        Set<BodySense> bodySenses = getBodySenses(body);
        if (value) {
            bodySenses.add(bodySense);
        } else {
            bodySenses.remove(bodySense);
        }
    }

    public static Vector2 getPriorPosition(@NotNull Body body) {
        return (Vector2) body.getProperty(ConstKeys.PRIOR);
    }
}
