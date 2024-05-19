package com.rocketpartners.game.entities.contracts;

import com.engine.common.enums.Direction;

public interface IDirectionRotatable {

    Direction getDirectionRotation();

    void setDirectionRotation(Direction direction);
}
