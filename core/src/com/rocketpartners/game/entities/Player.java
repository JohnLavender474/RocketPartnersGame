package com.rocketpartners.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.engine.IGame2D;
import com.engine.animations.Animation;
import com.engine.animations.AnimationsComponent;
import com.engine.animations.Animator;
import com.engine.animations.IAnimation;
import com.engine.audio.AudioComponent;
import com.engine.behaviors.BehaviorsComponent;
import com.engine.common.enums.Direction;
import com.engine.common.enums.Facing;
import com.engine.common.enums.Position;
import com.engine.common.interfaces.IBoundsSupplier;
import com.engine.common.interfaces.IFaceable;
import com.engine.common.objects.Properties;
import com.engine.common.shapes.GameRectangle;
import com.engine.common.time.Timer;
import com.engine.controller.ControllerComponent;
import com.engine.controller.buttons.ButtonActuator;
import com.engine.damage.IDamageable;
import com.engine.damage.IDamager;
import com.engine.drawables.shapes.DrawableShapesComponent;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sprites.GameSprite;
import com.engine.drawables.sprites.SpriteExtensionsKt;
import com.engine.drawables.sprites.SpritesComponent;
import com.engine.entities.GameEntity;
import com.engine.entities.contracts.*;
import com.engine.events.Event;
import com.engine.events.IEventListener;
import com.engine.points.PointsComponent;
import com.engine.updatables.UpdatablesComponent;
import com.engine.world.Body;
import com.engine.world.BodyComponent;
import com.engine.world.BodyType;
import com.engine.world.Fixture;
import com.rocketpartners.game.assets.SoundAsset;
import com.rocketpartners.game.assets.SpriteSheetAsset;
import com.rocketpartners.game.behaviors.BehaviorType;
import com.rocketpartners.game.controllers.ControllerButton;
import com.rocketpartners.game.damage.DamageNegotation;
import com.rocketpartners.game.entities.contracts.IDirectionRotatable;
import com.rocketpartners.game.entities.contracts.IGravityListener;
import com.rocketpartners.game.entities.contracts.IHealthEntity;
import com.rocketpartners.game.world.*;
import kotlin.Pair;
import kotlin.jvm.functions.Function0;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.rocketpartners.game.Constants.ConstKeys;
import static com.rocketpartners.game.Constants.ConstVals;

@Getter
@Setter
public class Player extends GameEntity implements IBodyEntity, IHealthEntity, IAudioEntity, ISpritesEntity,
        IAnimatedEntity, IBehaviorsEntity, IEventListener, IDamageable, IFaceable, IBoundsSupplier, IDirectionRotatable,
        IGravityListener {

    private static final float SHOOT_ANIMATION_DURATION = 0.3f;
    private static final float DAMAGE_DURATION = 0.75f;
    private static final float DAMAGE_RECOVERY_TIME = 1.5f;
    private static final float DAMAGE_FLASH_DURATION = 0.05f;

    private static final float MAX_RUN_SPEED = 9f;
    private static final float RUN_IMPULSE = 1.5f;

    private static final float SLIP_ANIMATION_THRESHOLD = 0.3f;

    private static ObjectMap<Class<? extends IDamager>, DamageNegotation> damageNegotiations;

    private static Map<String, TextureRegion> regions;

    private final ObjectSet<Object> eventKeyMask;
    private final Timer shootAnimationTimer;
    private final Timer damageTimer;
    private final Timer damageRecoveryTimer;
    private final Timer damageFlashTimer;

    private Facing facing;
    private Direction directionRotation;

    private boolean canMove;
    private boolean running;

    private boolean invincible;
    private boolean damageFlash;

    private GravityType gravityType;

    public Player(@NotNull IGame2D game) {
        super(game);
        eventKeyMask = new ObjectSet<>();
        shootAnimationTimer = new Timer(SHOOT_ANIMATION_DURATION);
        damageTimer = new Timer(DAMAGE_DURATION);
        damageRecoveryTimer = new Timer(DAMAGE_RECOVERY_TIME);
        damageFlashTimer = new Timer(DAMAGE_FLASH_DURATION);

        if (damageNegotiations == null) {
            damageNegotiations = new ObjectMap<>();
            // TODO: put entries here
        }
    }

    @Override
    public void init() {
        if (regions == null) {
            regions = new HashMap<>();
            TextureAtlas atlas = getGame().getAssMan().get(
                    SpriteSheetAsset.PLAYER_SPRITE_SHEET.getSource(), TextureAtlas.class);
            regions.put("stand", atlas.findRegion("stand"));
            regions.put("stand-shoot", atlas.findRegion("stand-shoot"));
            regions.put("run", atlas.findRegion("run"));
            regions.put("slip", atlas.findRegion("slip"));
            regions.put("jump", atlas.findRegion("jump"));
            regions.put("shoot", atlas.findRegion("shoot"));
            regions.put("jetpack", atlas.findRegion("jetpack"));
            regions.put("damaged", atlas.findRegion("damaged"));
        }
        addComponent(new AudioComponent(this));
        addComponent(defineBodyComponent());
        addComponent(defineBehaviorsComponent());
        addComponent(defineUpdatablesComponent());
        addComponent(defineSpritesComponent());
        addComponent(defineAnimationsComponent());
        addComponent(definePointsComponent());
        addComponent(defineControllerComponent());
    }

    @Override
    public void spawn(@NotNull Properties props) {
        super.spawn(props);

        GameRectangle spawnBounds = props.get(ConstKeys.BOUNDS, GameRectangle.class);
        assert spawnBounds != null;
        Vector2 spawnPoint = spawnBounds.getBottomCenterPoint();
        getBody().setBottomCenterToPoint(spawnPoint);

        GravityType gravityType = GravityType.valueOf(props.getOrDefault(ConstKeys.GRAVITY_TYPE,
                "normal_gravity", String.class).toUpperCase());
        enterAreaOfGravityType(gravityType);

        Direction gravityDirection = Direction.valueOf(props.getOrDefault(ConstKeys.GRAVITY_DIRECTION,
                "up", String.class).toUpperCase());
        enterAreaOfGravityDirection(gravityDirection);

        shootAnimationTimer.setToEnd();
        damageTimer.setToEnd();
        damageRecoveryTimer.setToEnd();
        damageFlashTimer.setToEnd();

        canMove = true;
        running = false;

        invincible = false;
        damageFlash = false;

        facing = Facing.RIGHT;
        directionRotation = Direction.UP;
    }

    @Override
    public void onEvent(@NotNull Event event) {
    }

    @Override
    public boolean getInvincible() {
        return invincible || !damageTimer.isFinished() || !damageRecoveryTimer.isFinished();
    }

    @Override
    public boolean canBeDamagedBy(@NotNull IDamager damager) {
        return !invincible && damageNegotiations.containsKey(damager.getClass());
    }

    @Override
    public boolean takeDamageFrom(@NotNull IDamager damager) {
        int damage = damageNegotiations.get(damager.getClass()).get(damager);
        getHealthPoints().translate(-damage);
        requestToPlaySound(SoundAsset.PLAYER_DAMAGE_SOUND, false);
        damageTimer.reset();
        return true;
    }

    public boolean isDamaged() {
        return !damageTimer.isFinished();
    }

    public boolean isShooting() {
        return !shootAnimationTimer.isFinished();
    }

    @NotNull
    @Override
    public GameRectangle getBounds() {
        return getBody();
    }

    @Override
    public void enterAreaOfGravityType(@NotNull GravityType gravityType) {
        this.gravityType = gravityType;
    }

    @Override
    public void enterAreaOfGravityDirection(@NotNull Direction gravityDirection) {
        directionRotation = gravityDirection;
    }

    private BodyComponent defineBodyComponent() {
        Body body = Body.Companion.createBody(BodyType.DYNAMIC);
        body.setColor(Color.BROWN);
        body.width = 0.75f * ConstVals.PPM;
        body.height = 0.95f * ConstVals.PPM;
        body.getPhysics().setTakeFrictionFromOthers(true);

        Array<Function0<IDrawableShape>> debugShapesSupplier = new Array<>();

        Fixture bodyFixture = Fixture.Companion.createFixture(body, FixtureType.BODY, new GameRectangle().set(body));
        body.addFixture(bodyFixture);
        bodyFixture.getRawShape().setColor(Color.GRAY);
        debugShapesSupplier.add(bodyFixture::getShape);

        Fixture feetFixture = Fixture.Companion.createFixture(body, FixtureType.FEET,
                new GameRectangle().setSize(0.6f * ConstVals.PPM, 0.15f * ConstVals.PPM));
        feetFixture.getOffsetFromBodyCenter().y = -0.5f * ConstVals.PPM;
        body.addFixture(feetFixture);
        feetFixture.getRawShape().setColor(Color.GREEN);
        debugShapesSupplier.add(feetFixture::getShape);

        // TODO: define other fixtures

        body.getPreProcess().put(ConstKeys.DEFAULT, delta -> {
            float gravity;
            if (gravityType != GravityType.NO_GRAVITY && BodyExtensions.isBodySensing(body, BodySense.FEET_ON_GROUND)) {
                gravity = ConstVals.GROUND_GRAVITY;
            } else {
                switch (gravityType) {
                    case NORMAL_GRAVITY:
                        gravity = ConstVals.NORMAL_GRAVITY;
                        break;
                    case LOW_GRAVITY:
                        gravity = ConstVals.LOW_GRAVITY;
                        break;
                    case NO_GRAVITY:
                        gravity = 0f;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + gravityType);
                }
            }

            Vector2 gravityVector2 = new Vector2();
            switch (directionRotation) {
                case UP:
                    gravityVector2.set(0, gravity);
                    break;
                case DOWN:
                    gravityVector2.set(0, -gravity);
                    break;
                case LEFT:
                    gravityVector2.set(gravity, 0);
                    break;
                case RIGHT:
                    gravityVector2.set(-gravity, 0);
                    break;
            }
            body.getPhysics().getGravity().set(gravityVector2);
        });

        DrawableShapesComponent drawableShapesComponent = new DrawableShapesComponent(this, new Array<>(),
                debugShapesSupplier, true);
        addComponent(drawableShapesComponent);

        return BodyComponentCreator.create(this, body);
    }

    private UpdatablesComponent defineUpdatablesComponent() {
        return new UpdatablesComponent(this, delta -> {
            if (damageTimer.isFinished() && !damageRecoveryTimer.isFinished()) {
                damageRecoveryTimer.update(delta);
                damageFlashTimer.update(delta);
                if (damageFlashTimer.isFinished()) {
                    damageFlashTimer.reset();
                    damageFlash = !damageFlash;
                }
            }
            if (damageRecoveryTimer.isJustFinished()) {
                damageFlash = false;
            }
        });
    }

    private BehaviorsComponent defineBehaviorsComponent() {
        BehaviorsComponent behaviorsComponent = new BehaviorsComponent(this);

        // TODO: create behaviors

        return behaviorsComponent;
    }

    private SpritesComponent defineSpritesComponent() {
        GameSprite sprite = new GameSprite();
        sprite.setSize(2.475f * ConstVals.PPM, 1.875f * ConstVals.PPM);
        sprite.getPriority().setSection(DrawingSection.FOREGROUND);
        sprite.getPriority().setPriority(1);

        SpritesComponent spritesComponent = new SpritesComponent(this, new Pair<>(SpritesComponent.SPRITE, sprite));
        spritesComponent.putUpdateFunction(SpritesComponent.SPRITE, (delta, gameSprite) -> {
            gameSprite.setFlip(facing == Facing.LEFT, false);
            gameSprite.setAlpha(damageFlash ? 0f : 1f);

            float rotation = directionRotation.getRotation();
            gameSprite.setOriginCenter();
            gameSprite.setRotation(rotation);

            Position position;
            switch (directionRotation) {
                case UP:
                    position = Position.BOTTOM_CENTER;
                    break;
                case DOWN:
                    position = Position.TOP_CENTER;
                    break;
                case LEFT:
                    position = Position.CENTER_RIGHT;
                    break;
                case RIGHT:
                    position = Position.CENTER_LEFT;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + directionRotation);
            }
            Vector2 bodyPosition = getBounds().getPositionPoint(position);
            SpriteExtensionsKt.setPosition(gameSprite, bodyPosition, position);
        });
        return spritesComponent;
    }

    private AnimationsComponent defineAnimationsComponent() {
        Supplier<String> keySupplier = () -> {
            if (isDamaged()) {
                return "damaged";
            }
            if (isShooting()) {
                return "stand-shoot";
            }
            if (running) {
                return "run";
            }
            float xSpeed = Math.abs(getBody().getPhysics().getVelocity().x);
            if (xSpeed >= SLIP_ANIMATION_THRESHOLD) {
                return "slip";
            }
            return "stand";
        };

        ObjectMap<String, IAnimation> animations = new ObjectMap<>();
        animations.put("stand", new Animation(regions.get("stand")));
        animations.put("run", new Animation(regions.get("run"), 2, 2, 0.175f, true));
        animations.put("slip", new Animation(regions.get("slip")));
        animations.put("stand-shoot", new Animation(regions.get("stand-shoot")));

        Animator animator = new Animator(keySupplier, animations);
        return new AnimationsComponent(this, animator);
    }

    private PointsComponent definePointsComponent() {
        PointsComponent pointsComponent = new PointsComponent(this);
        pointsComponent.putPoints(ConstKeys.HEALTH, ConstVals.MIN_HEALTH,
                ConstVals.MAX_HEALTH, ConstVals.MAX_HEALTH);
        pointsComponent.putListener(ConstKeys.HEALTH, it -> {
            if (it.getCurrent() <= ConstVals.MIN_HEALTH) {
                kill(null);
            }
            return null;
        });
        return pointsComponent;
    }

    private ControllerComponent defineControllerComponent() {
        ControllerComponent controllerComponent = new ControllerComponent(this);

        controllerComponent.putActuator(ControllerButton.LEFT, new ButtonActuator(
                null,
                (poller, delta) -> {
                    if (isDamaged()) {
                        if (!poller.isPressed(ControllerButton.RIGHT)) {
                            running = false;
                        }
                        return;
                    }

                    facing = Facing.LEFT;
                    // facing = isBehaviorActive(BehaviorType.WALL_SLIDE) ? Facing.RIGHT : Facing.LEFT;
                    running = !isBehaviorActive(BehaviorType.WALL_SLIDE);

                    // TODO: threshold and impulse should be dynamic based on the current player state
                    float threshold = MAX_RUN_SPEED * ConstVals.PPM;
                    float impulse = RUN_IMPULSE * ConstVals.PPM;
                    Vector2 velocity = getBody().getPhysics().getVelocity();
                    if (velocity.x > -threshold) {
                        getBody().getPhysics().getVelocity().x -= impulse * delta * ConstVals.PPM;
                    }
                },
                poller -> {
                    if (!poller.isPressed(ControllerButton.RIGHT)) {
                        running = false;
                    }
                },
                null
        ));

        controllerComponent.putActuator(ControllerButton.RIGHT, new ButtonActuator(
                null,
                (poller, delta) -> {
                    if (isDamaged()) {
                        if (!poller.isPressed(ControllerButton.LEFT)) {
                            running = false;
                        }
                        return;
                    }

                    facing = Facing.RIGHT;
                    // facing = isBehaviorActive(BehaviorType.WALL_SLIDE) ? Facing.LEFT : Facing.RIGHT;
                    running = !isBehaviorActive(BehaviorType.WALL_SLIDE);

                    // TODO: threshold and impulse should be dynamic based on the current player state
                    float threshold = MAX_RUN_SPEED * ConstVals.PPM;
                    float impulse = RUN_IMPULSE * ConstVals.PPM;
                    Vector2 velocity = getBody().getPhysics().getVelocity();
                    if (velocity.x < threshold) {
                        getBody().getPhysics().getVelocity().x += impulse * delta * ConstVals.PPM;
                    }
                },
                poller -> {
                    if (!poller.isPressed(ControllerButton.LEFT)) {
                        running = false;
                    }
                },
                null
        ));

        return controllerComponent;
    }
}
