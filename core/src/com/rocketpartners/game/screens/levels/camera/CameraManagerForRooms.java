package com.rocketpartners.game.screens.levels.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.engine.common.enums.Direction;
import com.engine.common.enums.ProcessState;
import com.engine.common.extensions.Vector3ExtensionsKt;
import com.engine.common.interfaces.IBoundsSupplier;
import com.engine.common.interfaces.Resettable;
import com.engine.common.interfaces.Updatable;
import com.engine.common.shapes.GameRectangle;
import com.engine.common.shapes.RectangleExtensionsKt;
import com.engine.common.time.Timer;
import com.rocketpartners.game.Constants;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.function.Consumer;

import static com.engine.common.UtilMethodsKt.getOverlapPushDirection;
import static com.engine.common.UtilMethodsKt.interpolate;

@Getter
public class CameraManagerForRooms implements Updatable, Resettable {

    private static final float DELAY_DURATION = 0.35f;
    private static final float TRANS_DURATION = 1f;
    private static final float DISTANCE_ON_TRANS = 1.5f;
    private static final float INTERPOLATION_SCALAR = 5f;

    private final Camera camera;
    private final Timer delayTimer;
    private final Timer transTimer;
    private final Vector2 transitionStart;
    private final Vector2 transitionTarget;
    private final Vector2 focusStart;
    private final Vector2 focusTarget;

    @Setter
    private Array<RectangleMapObject> rooms;
    private IBoundsSupplier focus;

    private RectangleMapObject priorRoom;
    private RectangleMapObject currentRoom;

    private Direction transitionDirection;
    private ProcessState transitionState;

    @Setter
    private Runnable onBeginTransition;
    @Setter
    private Consumer<Float> onContinueTransition;
    @Setter
    private Runnable onEndTransition;

    private boolean reset;

    public CameraManagerForRooms(Camera camera) {
        this.camera = camera;
        delayTimer = new Timer(DELAY_DURATION);
        transTimer = new Timer(TRANS_DURATION);
        transitionStart = new Vector2();
        transitionTarget = new Vector2();
        focusStart = new Vector2();
        focusTarget = new Vector2();
    }

    public String getCurrentRoomKey() {
        return currentRoom == null ? null : currentRoom.getName();
    }

    public boolean isTransitioning() {
        return transitionState != null;
    }

    public boolean isDelayJustFinished() {
        return delayTimer.isJustFinished();
    }

    public float getTransitionTimerRatio() {
        return transTimer.getRatio();
    }

    public Vector2 getTransitionInterpolation() {
        if (transitionState == null) {
            return null;
        }
        Vector2 startCopy = focusStart.cpy();
        Vector2 targetCopy = focusTarget.cpy();
        return interpolate(startCopy, targetCopy, getTransitionTimerRatio());
    }

    public void setFocus(IBoundsSupplier focus) {
        this.focus = focus;
        reset = true;
        if (focus == null) {
            return;
        }
        Vector2 center = focus.getBounds().getCenter();
        camera.position.x = center.x;
        camera.position.y = center.y;
    }

    @Override
    public void update(float v) {
        if (reset) {
            reset = false;
            priorRoom = null;
            currentRoom = null;
            transitionDirection = null;
            transitionState = null;
            setCameraToFocusable(v);
            currentRoom = getNextRoom();
        } else if (isTransitioning()) {
            onTransition(v);
        } else {
            onNoTransition(v);
        }
    }

    @Override
    public void reset() {
        reset = true;
    }

    private void setTransitionValues(Rectangle rectangle) {
        transitionState = ProcessState.BEGIN;
        transitionStart.set(camera.position.x, camera.position.y);
        transitionTarget.set(transitionStart);
        focusStart.set(focus.getBounds().getCenter());
        focusTarget.set(focusStart);
        switch (transitionDirection) {
            case LEFT:
                transitionTarget.x = (rectangle.x + rectangle.width) - Math.min(rectangle.width / 2f,
                        camera.viewportWidth / 2f);
                focusTarget.x = (rectangle.x + rectangle.width - DISTANCE_ON_TRANS * Constants.ConstVals.PPM);
                break;
            case RIGHT:
                transitionTarget.x = rectangle.x + Math.min(rectangle.width / 2f, camera.viewportWidth / 2f);
                focusTarget.x = rectangle.x + DISTANCE_ON_TRANS * Constants.ConstVals.PPM;
                break;
            case UP:
                transitionTarget.y = rectangle.y + Math.min(rectangle.height / 2f, camera.viewportHeight / 2f);
                focusTarget.y = rectangle.y + DISTANCE_ON_TRANS * Constants.ConstVals.PPM;
                break;
            case DOWN:
                transitionTarget.y = (rectangle.y + rectangle.height) - Math.min(rectangle.height / 2f,
                        camera.viewportHeight / 2f);
                focusTarget.y = (rectangle.y + rectangle.height - DISTANCE_ON_TRANS * Constants.ConstVals.PPM);
                break;
        }
    }

    private void onNoTransition(float v) {
        if (currentRoom == null) {
            RectangleMapObject nextRoom = getNextRoom();
            if (nextRoom != null) {
                priorRoom = currentRoom;
                currentRoom = nextRoom;
            }
            camera.position.x = focus.getBounds().getCenter().x;
            return;
        }

        if (focus == null) {
            return;
        }
        if (!currentRoom.getRectangle().overlaps(focus.getBounds())) {
            currentRoom = null;
        }
        if (currentRoom == null) {
            return;
        }
        GameRectangle currentRoomBounds = RectangleExtensionsKt.toGameRectangle(currentRoom.getRectangle());
        if (currentRoomBounds.overlaps((Rectangle) focus.getBounds())) {
            setCameraToFocusable(v);
            if (camera.position.y > (currentRoomBounds.y + currentRoomBounds.height) - camera.viewportHeight / 2f) {
                camera.position.y = (currentRoomBounds.y + currentRoomBounds.height) - camera.viewportHeight / 2f;
            }
            if (camera.position.y < currentRoomBounds.y + camera.viewportHeight / 2f) {
                camera.position.y = currentRoomBounds.y + camera.viewportHeight / 2f;
            }
            if (camera.position.x > (currentRoomBounds.x + currentRoomBounds.width) - camera.viewportWidth / 2f) {
                camera.position.x = (currentRoomBounds.x + currentRoomBounds.width) - camera.viewportWidth / 2f;
            }
            if (camera.position.x < currentRoomBounds.x + camera.viewportWidth / 2f) {
                camera.position.x = currentRoomBounds.x + camera.viewportWidth / 2f;
            }
        }

        for (RectangleMapObject room : rooms) {
            if (room.getRectangle().overlaps(focus.getBounds()) && !Objects.equals(room.getName(),
                    getCurrentRoomKey())) {
                float width = 5f * Constants.ConstVals.PPM;
                float height = 5f * Constants.ConstVals.PPM;
                GameRectangle boundingBox =
                        new GameRectangle().setSize(width, height).setCenter(focus.getBounds().getCenter());
                transitionDirection = getOverlapPushDirection(boundingBox, currentRoomBounds, new Rectangle());
                priorRoom = currentRoom;
                currentRoom = room;
                setTransitionValues(room.getRectangle());
                break;
            }
        }
    }

    private void onTransition(float delta) {
        switch (transitionState) {
            case END:
                transitionDirection = null;
                transitionState = null;
                delayTimer.reset();
                transTimer.reset();
                transitionStart.setZero();
                transitionTarget.setZero();
                onEndTransition.run();
                break;
            case BEGIN:
            case CONTINUE:
                if (transitionState == ProcessState.BEGIN) {
                    onBeginTransition.run();
                } else {
                    onContinueTransition.accept(delta);
                }
                transitionState = ProcessState.CONTINUE;
                delayTimer.update(delta);
                if (!delayTimer.isFinished()) {
                    return;
                }
                transTimer.update(delta);
                Vector2 pos = interpolate(transitionStart, transitionTarget, getTransitionTimerRatio());
                camera.position.x = pos.x;
                camera.position.y = pos.y;
                transitionState = transTimer.isFinished() ? ProcessState.END : ProcessState.CONTINUE;
                break;
        }
    }

    private RectangleMapObject getNextRoom() {
        if (focus == null || rooms == null) {
            return null;
        }
        RectangleMapObject nextRoom = null;
        for (RectangleMapObject room : rooms) {
            if (room.getRectangle().contains(focus.getBounds().getCenter())) {
                nextRoom = room;
                break;
            }
        }
        return nextRoom;
    }

    private void setCameraToFocusable(float v) {
        if (focus == null) {
            return;
        }
        Vector2 focusPos = focus.getBounds().getCenter();
        Vector2 cameraPos = interpolate(Vector3ExtensionsKt.toVector2(camera.position), focusPos,
                v * INTERPOLATION_SCALAR);
        camera.position.x = cameraPos.x;
        camera.position.y = cameraPos.y;
    }
}
