package com.rocketpartners.game.entities.contracts;

import com.engine.entities.IGameEntity;

public interface IOwnable {

    void setOwner(IGameEntity owner);

    IGameEntity getOwner();
}
