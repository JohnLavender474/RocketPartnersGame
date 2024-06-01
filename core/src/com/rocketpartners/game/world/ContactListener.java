package com.rocketpartners.game.world;

import com.badlogic.gdx.math.Vector2;
import com.engine.common.enums.ProcessState;
import com.engine.entities.IGameEntity;
import com.engine.world.Contact;
import com.engine.world.IContactListener;
import com.engine.world.IFixture;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.entities.Player;
import com.rocketpartners.game.entities.blocks.BaseBlock;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ContactListener implements IContactListener {

    private final RocketPartnersGame game;

    @Override
    public void beginContact(@NotNull Contact contact, float delta) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            handleFeetWorldBlockContact(contact, ProcessState.BEGIN);
        } else if (contact.fixturesMatch(FixtureType.SIDE, FixtureType.WORLD_BLOCK)) {
            handleSideWorldBlockContact(contact, ProcessState.BEGIN);
        } else if (contact.fixturesMatch(FixtureType.HEAD, FixtureType.WORLD_BLOCK)) {
            handleHeadWorldBlockContact(contact, ProcessState.BEGIN);
        }
    }

    @Override
    public void continueContact(@NotNull Contact contact, float v) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            handleFeetWorldBlockContact(contact, ProcessState.CONTINUE);
        } else if (contact.fixturesMatch(FixtureType.SIDE, FixtureType.WORLD_BLOCK)) {
            handleSideWorldBlockContact(contact, ProcessState.CONTINUE);
        } else if (contact.fixturesMatch(FixtureType.HEAD, FixtureType.WORLD_BLOCK)) {
            handleHeadWorldBlockContact(contact, ProcessState.CONTINUE);
        }
    }

    @Override
    public void endContact(@NotNull Contact contact, float v) {
        if (contact.fixturesMatch(FixtureType.FEET, FixtureType.WORLD_BLOCK)) {
            handleFeetWorldBlockContact(contact, ProcessState.END);
        } else if (contact.fixturesMatch(FixtureType.SIDE, FixtureType.WORLD_BLOCK)) {
            handleSideWorldBlockContact(contact, ProcessState.END);
        } else if (contact.fixturesMatch(FixtureType.HEAD, FixtureType.WORLD_BLOCK)) {
            handleHeadWorldBlockContact(contact, ProcessState.END);
        }
    }

    private void handleFeetWorldBlockContact(@NotNull Contact contact, @NotNull ProcessState processState) {
        Pair<IFixture, IFixture> fixtures = contact.getFixturesInOrder(FixtureType.FEET, FixtureType.WORLD_BLOCK);
        if (fixtures == null) {
            return;
        }
        IFixture feetFixture = fixtures.component1();
        IFixture blockFixture = fixtures.component2();

        BodyExtensions.setBodySense(feetFixture.getBody(), BodySense.FEET_ON_GROUND, processState != ProcessState.END);

        Vector2 posDelta = BodyExtensions.getPosDelta(blockFixture.getBody());
        feetFixture.getBody().translation(posDelta);

        IGameEntity entity = FixtureExtensions.getEntity(feetFixture);
        if (entity instanceof Player player) {
            player.setAButtonTask(processState == ProcessState.END ? Player.AButtonTask.JETPACK :
                    Player.AButtonTask.JUMP);
        }

        if (processState == ProcessState.BEGIN) {
            ((BaseBlock) FixtureExtensions.getEntity(blockFixture)).hitByFeet(feetFixture);
        }
    }

    private void handleHeadWorldBlockContact(@NotNull Contact contact, @NotNull ProcessState processState) {
        Pair<IFixture, IFixture> fixtures = contact.getFixturesInOrder(FixtureType.HEAD, FixtureType.WORLD_BLOCK);
        if (fixtures == null) {
            return;
        }
        IFixture headFixture = fixtures.component1();
        IFixture blockFixture = fixtures.component2();

        BodyExtensions.setBodySense(headFixture.getBody(), BodySense.HEAD_TOUCHING_BLOCK,
                processState != ProcessState.END);
        if (processState == ProcessState.BEGIN) {
            ((BaseBlock) FixtureExtensions.getEntity(blockFixture)).hitByHead(headFixture);
        }
    }

    private void handleSideWorldBlockContact(@NotNull Contact contact, @NotNull ProcessState processState) {
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
            BodyExtensions.setBodySense(sideFixture.getBody(), BodySense.SIDE_TOUCHING_BLOCK_LEFT,
                    processState != ProcessState.END);
        } else if (sideType.equals(Constants.ConstKeys.RIGHT)) {
            BodyExtensions.setBodySense(sideFixture.getBody(), BodySense.SIDE_TOUCHING_BLOCK_RIGHT,
                    processState != ProcessState.END);
        }

        if (processState == ProcessState.BEGIN) {
            ((BaseBlock) FixtureExtensions.getEntity(fixtures.component2())).hitBySide(sideFixture);
        }
    }
}
