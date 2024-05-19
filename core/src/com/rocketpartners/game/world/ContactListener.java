package com.rocketpartners.game.world;

import com.engine.world.Contact;
import com.engine.world.IContactListener;
import com.rocketpartners.game.RocketPartnersGame;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ContactListener implements IContactListener {

    private final RocketPartnersGame game;

    @Override
    public void beginContact(@NotNull Contact contact, float v) {

    }

    @Override
    public void continueContact(@NotNull Contact contact, float v) {

    }

    @Override
    public void endContact(@NotNull Contact contact, float v) {

    }
}
