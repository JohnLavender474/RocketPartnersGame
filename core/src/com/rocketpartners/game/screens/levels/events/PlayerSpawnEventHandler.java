package com.rocketpartners.game.screens.levels.events;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.engine.common.interfaces.Initializable;
import com.engine.common.interfaces.Updatable;
import com.engine.common.objects.Properties;
import com.engine.drawables.IDrawable;
import com.engine.events.Event;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.events.EventType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSpawnEventHandler implements Initializable, Updatable, IDrawable<Batch> {

    private final RocketPartnersGame game;

    @Override
    public void init() {
        game.getEventsMan().submitEvent(new Event(EventType.PLAYER_SPAWN, new Properties()));
    }

    @Override
    public void update(float v) {

    }

    @Override
    public void draw(Batch batch) {

    }

    public boolean isFinished() {
        return true;
    }
}
