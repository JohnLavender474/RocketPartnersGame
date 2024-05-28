package com.rocketpartners.game.world;

import com.engine.world.Body;
import com.rocketpartners.game.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyExtensions {

    public static boolean isBodySensing(@NotNull Body body, @NotNull BodySense bodySense) {
        Set<BodySense> bodySenses = getBodySenses(body);
        return bodySenses.contains(bodySense);
    }

    public static Set<BodySense> getBodySenses(@NotNull Body body) {
        if (!body.hasProperty(Constants.ConstKeys.BODY_SENSES)) {
            body.putProperty(Constants.ConstKeys.BODY_SENSES, EnumSet.noneOf(BodySense.class));
        }
        return (Set<BodySense>) body.getProperty(Constants.ConstKeys.BODY_SENSES);
    }

    public static void putBodySense(@NotNull Body body, @NotNull BodySense bodySense) {
        Set<BodySense> bodySenses = getBodySenses(body);
        bodySenses.add(bodySense);
    }
}
