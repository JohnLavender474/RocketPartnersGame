package com.rocketpartners.game.world;

import com.engine.entities.IGameEntity;
import com.engine.world.Contact;
import com.engine.world.IContactListener;
import com.engine.world.IFixture;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.entities.Player;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ContactListener implements IContactListener {

    private final RocketPartnersGame game;

    @Override
    public void beginContact(@NotNull Contact contact, float delta) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            handleFeetWorldBlockContact(contact, true);
        } else if (contact.fixturesMatch(FixtureType.SIDE, FixtureType.WORLD_BLOCK)) {
            handleSideWorldBlockContact(contact, true);
        }
    }

    @Override
    public void continueContact(@NotNull Contact contact, float v) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            handleFeetWorldBlockContact(contact, true);
        } else if (contact.fixturesMatch(FixtureType.SIDE, FixtureType.WORLD_BLOCK)) {
            handleSideWorldBlockContact(contact, true);
        }
    }

    @Override
    public void endContact(@NotNull Contact contact, float v) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
           handleFeetWorldBlockContact(contact, false);
        } else if (contact.fixturesMatch(FixtureType.SIDE, FixtureType.WORLD_BLOCK)) {
            handleSideWorldBlockContact(contact, false);
        }
    }

    private void handleFeetWorldBlockContact(@NotNull Contact contact, boolean begin) {
        Pair<IFixture, IFixture> fixtures = contact.getFixturesInOrder(FixtureType.FEET, FixtureType.WORLD_BLOCK);
        if (fixtures == null) {
            return;
        }
        IFixture feetFixture = fixtures.component1();
        IFixture blockFixture = fixtures.component2();

        BodyExtensions.setBodySense(feetFixture.getBody(), BodySense.FEET_ON_GROUND, begin);

        // TODO
        /*
        Vector2 posDelta = BodyExtensions.getPriorPosition(blockFixture.getBody());
        feetFixture.getBody().translation(posDelta);
        */

        IGameEntity entity = FixtureExtensions.getEntity(feetFixture);
        if (entity instanceof Player player) {
            player.setAButtonTask(begin ? Player.AButtonTask.JUMP : Player.AButtonTask.JETPACK);
        }
    }

    private void handleSideWorldBlockContact(@NotNull Contact contact, boolean begin) {
        Pair<IFixture, IFixture> fixtures = contact.getFixturesInOrder(FixtureType.SIDE, FixtureType.WORLD_BLOCK);
        if (fixtures == null) {
            return;
        }
        IFixture sideFixture = fixtures.component1();
        String sideType = sideFixture.getProperty(Constants.ConstKeys.SIDE_TYPE, String.class);
        if (sideType == null) {
            return;
        }
        if (sideType.equals(Constants.ConstKeys.LEFT)) {
            BodyExtensions.setBodySense(sideFixture.getBody(), BodySense.SIDE_TOUCHING_BLOCK_LEFT, begin);
        } else if (sideType.equals(Constants.ConstKeys.RIGHT)) {
            BodyExtensions.setBodySense(sideFixture.getBody(), BodySense.SIDE_TOUCHING_BLOCK_RIGHT, begin);
        }
    }
}
