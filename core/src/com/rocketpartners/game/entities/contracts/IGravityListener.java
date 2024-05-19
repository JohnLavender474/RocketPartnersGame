package com.rocketpartners.game.entities.contracts;

import com.engine.common.enums.Direction;
import com.rocketpartners.game.world.GravityType;

public interface IGravityListener {

    void enterAreaOfGravityType(GravityType gravityType);

    void enterAreaOfGravityDirection(Direction gravityDirection);
}
