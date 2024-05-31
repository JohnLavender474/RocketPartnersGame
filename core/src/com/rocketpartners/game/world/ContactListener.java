package com.rocketpartners.game.world;

import com.badlogic.gdx.math.Vector2;
import com.engine.entities.IGameEntity;
import com.engine.world.Contact;
import com.engine.world.IContactListener;
import com.engine.world.IFixture;
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
            handleFeetWorldBlockContact(contact);
        }
    }

    @Override
    public void continueContact(@NotNull Contact contact, float v) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            handleFeetWorldBlockContact(contact);
        }
    }

    @Override
    public void endContact(@NotNull Contact contact, float v) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            Pair<IFixture, IFixture> fixtures = contact.getFixturesInOrder(FixtureType.FEET, FixtureType.WORLD_BLOCK);
            if (fixtures == null) {
                return;
            }
            IFixture feetFixture = fixtures.component1();

            BodyExtensions.setBodySense(feetFixture.getBody(), BodySense.FEET_ON_GROUND, false);

            IGameEntity entity = FixtureExtensions.getEntity(feetFixture);
            if (entity instanceof Player player) {
                player.setAButtonTask(Player.AButtonTask.JETPACK);
            }
        }
    }

    private void handleFeetWorldBlockContact(@NotNull Contact contact) {
        Pair<IFixture, IFixture> fixtures = contact.getFixturesInOrder(FixtureType.FEET, FixtureType.WORLD_BLOCK);
        if (fixtures == null) {
            return;
        }
        IFixture feetFixture = fixtures.component1();
        IFixture blockFixture = fixtures.component2();

        BodyExtensions.setBodySense(feetFixture.getBody(), BodySense.FEET_ON_GROUND, true);
/*
        Vector2 posDelta = BodyExtensions.getPriorPosition(blockFixture.getBody());
        feetFixture.getBody().translation(posDelta);

 */

        IGameEntity entity = FixtureExtensions.getEntity(feetFixture);
        if (entity instanceof Player player) {
            player.setAButtonTask(Player.AButtonTask.JUMP);
            player.setAllowedToJetDash(true);
        }
    }
}
