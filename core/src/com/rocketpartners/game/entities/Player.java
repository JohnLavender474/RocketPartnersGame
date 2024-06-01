package com.rocketpartners.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.engine.IGame2D;
import com.engine.animations.*;
import com.engine.audio.AudioComponent;
import com.engine.behaviors.Behavior;
import com.engine.behaviors.BehaviorsComponent;
import com.engine.common.enums.Direction;
import com.engine.common.enums.Facing;
import com.engine.common.enums.Position;
import com.engine.common.extensions.ObjectSetExtensionsKt;
import com.engine.common.interfaces.IBoundsSupplier;
import com.engine.common.interfaces.IFaceable;
import com.engine.common.interfaces.Resettable;
import com.engine.common.interfaces.Updatable;
import com.engine.common.objects.Properties;
import com.engine.common.shapes.GameRectangle;
import com.engine.common.time.Timer;
import com.engine.controller.ControllerComponent;
import com.engine.controller.buttons.ButtonActuator;
import com.engine.controller.polling.IControllerPoller;
import com.engine.damage.IDamageable;
import com.engine.damage.IDamager;
import com.engine.drawables.shapes.DrawableShapesComponent;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingPriority;
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
import com.engine.world.*;
import com.rocketpartners.game.RocketPartnersGame;
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

    public enum AButtonTask {
        JUMP,
        JETPACK
    }

    public static class JetpackStamina implements Updatable, Resettable {

        public static final float JETPACK_THRUST_DURATION = 5f;
        public static final float JETPACK_RECOVERY_DELAY = 0.1f;
        public static final float JETPACK_RECOVERY_RATE = 4f;

        private final Timer jetpackRecoveryDelayTimer;

        private float thrustTime;
        private boolean jetpacking;

        public JetpackStamina() {
            jetpackRecoveryDelayTimer = new Timer(JETPACK_RECOVERY_DELAY);
            thrustTime = 0f;
        }

        public boolean hasStamina() {
            return thrustTime < JETPACK_THRUST_DURATION;
        }

        public float getImpulseRatio() {
            return thrustTime / JETPACK_THRUST_DURATION;
        }

        public void setJetpacking(boolean jetpacking) {
            this.jetpacking = jetpacking;
            if (jetpacking) {
                jetpackRecoveryDelayTimer.reset();
            }
        }

        @Override
        public void update(float delta) {
            if (jetpacking) {
                thrustTime += delta;
                thrustTime = Math.min(JETPACK_THRUST_DURATION, thrustTime);
            } else {
                jetpackRecoveryDelayTimer.update(delta);
                if (jetpackRecoveryDelayTimer.isFinished()) {
                    thrustTime = Math.max(0f, thrustTime - JETPACK_RECOVERY_RATE * delta);
                }
            }
        }

        @Override
        public void reset() {
            thrustTime = 0f;
            jetpacking = false;
            jetpackRecoveryDelayTimer.reset();
        }
    }

    private static final float SHOOT_ANIMATION_DURATION = 0.3f;

    private static final float DAMAGE_DURATION = 0.75f;
    private static final float DAMAGE_RECOVERY_TIME = 1.5f;
    private static final float DAMAGE_FLASH_DURATION = 0.05f;

    private static final float CLAMP_VEL_X = 25f;
    private static final float CLAMP_VEL_Y = 50f;

    private static final float JETPACK_IMPULSE = 2f;

    private static final float JETDASH_IMPULSE = 8f;
    private static final float JETDASH_DELTA_SCALAR = 2.5f;

    private static final float BRAKE_DURATION = 0.2f;

    private static final float MAX_GROUND_RUN_SPEED = 9f;
    private static final float MAX_JUMP_RUN_SPEED = 4f;
    private static final float MAX_JETPACK_RUN_SPEED = 2f;

    private static final float RUN_IMPULSE = 1.5f;
    private static final float GROUND_JUMP_IMPULSE = 15f;


    private static final float WALL_JUMP_VERTICAL = 30f;
    private static final float WALL_JUMP_HORIZONTAL = 7f;

    private static final float SLIP_ANIMATION_THRESHOLD = 0.3f;

    private static ObjectMap<Class<? extends IDamager>, DamageNegotation> damageNegotiations;
    private static Map<String, TextureRegion> regions;

    private final ObjectSet<Object> eventKeyMask;
    private final Map<String, Timer> timers;

    private boolean canMove;
    private boolean running;
    private boolean invincible;
    private boolean damageFlash;

    private Facing facing;
    private JetpackStamina jetpackStamina;
    private Direction directionRotation;
    private GravityType gravityType;
    private AButtonTask aButtonTask;

    public Player(@NotNull IGame2D game) {
        super(game);
        eventKeyMask = new ObjectSet<>();
        jetpackStamina = new JetpackStamina();

        timers = new HashMap<>();
        timers.put("shoot_anim", new Timer(SHOOT_ANIMATION_DURATION));
        timers.put("damage", new Timer(DAMAGE_DURATION));
        timers.put("damage_recovery", new Timer(DAMAGE_RECOVERY_TIME));
        timers.put("damage_flash", new Timer(DAMAGE_FLASH_DURATION));
        timers.put("brake", new Timer(BRAKE_DURATION));

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
                    SpriteSheetAsset.PLAYER_8BIT_SPRITE_SHEET.getSource(), TextureAtlas.class);
            regions.put("stand", atlas.findRegion("stand"));
            regions.put("stand-shoot", atlas.findRegion("stand-shoot"));
            regions.put("run", atlas.findRegion("run"));
            regions.put("slip", atlas.findRegion("slip"));
            regions.put("jump", atlas.findRegion("jump"));
            regions.put("brake", atlas.findRegion("brake"));
            regions.put("shoot", atlas.findRegion("shoot"));
            regions.put("jetpack", atlas.findRegion("jetpack"));
            regions.put("jetdash", atlas.findRegion("thrust"));
            regions.put("damaged", atlas.findRegion("damaged"));
            regions.put("jetpackFlame", atlas.findRegion("jetpackFlame"));
            regions.put("wallslide", atlas.findRegion("wallslide"));
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

        ObjectSet<String> timersToReset = ObjectSetExtensionsKt.objectSetOf("jetdash");
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            String key = entry.getKey();
            Timer timer = entry.getValue();
            if (timersToReset.contains(key)) {
                timer.reset();
            } else {
                timer.setToEnd();
            }
        }

        canMove = true;
        running = false;

        invincible = false;
        damageFlash = false;

        facing = Facing.RIGHT;
        directionRotation = Direction.UP;
        aButtonTask = AButtonTask.JUMP;

        jetpackStamina.reset();
    }

    @Override
    public void onEvent(@NotNull Event event) {
    }

    @Override
    public boolean getInvincible() {
        return invincible || !timers.get("damage").isFinished() || !timers.get("damage_recovery").isFinished();
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
        timers.get("damage").reset();
        return true;
    }

    public boolean isDamaged() {
        return !timers.get("damage").isFinished();
    }

    public boolean isShooting() {
        return !timers.get("shoot_animation").isFinished();
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
        body.width = 0.85f * ConstVals.PPM;
        body.height = 1.25f * ConstVals.PPM;
        PhysicsData physicsData = body.getPhysics();
        physicsData.setVelocityClamp(new Vector2(CLAMP_VEL_X, CLAMP_VEL_Y).scl(ConstVals.PPM));
        physicsData.setTakeFrictionFromOthers(true);

        Array<Function0<IDrawableShape>> debugShapesSupplier = new Array<>();

        Fixture bodyFixture = Fixture.Companion.createFixture(body, FixtureType.BODY, new GameRectangle().set(body));
        body.addFixture(bodyFixture);
        bodyFixture.getRawShape().setColor(Color.GRAY);
        debugShapesSupplier.add(bodyFixture::getShape);

        Fixture feetFixture = Fixture.Companion.createFixture(body, FixtureType.FEET,
                new GameRectangle().setSize(0.6f * ConstVals.PPM, 0.1f * ConstVals.PPM));
        feetFixture.getOffsetFromBodyCenter().y = -0.625f * ConstVals.PPM;
        body.addFixture(feetFixture);
        feetFixture.getRawShape().setColor(Color.GREEN);
        debugShapesSupplier.add(feetFixture::getShape);

        Fixture leftFixture = Fixture.Companion.createFixture(body, FixtureType.SIDE,
                new GameRectangle().setSize(0.1f * ConstVals.PPM, 0.5f * ConstVals.PPM));
        leftFixture.putProperty(ConstKeys.SIDE_TYPE, ConstKeys.LEFT);
        leftFixture.getOffsetFromBodyCenter().x = -0.425f * ConstVals.PPM;
        body.addFixture(leftFixture);
        leftFixture.getRawShape().setColor(Color.YELLOW);
        debugShapesSupplier.add(leftFixture::getShape);

        Fixture rightFixture = Fixture.Companion.createFixture(body, FixtureType.SIDE,
                new GameRectangle().setSize(0.1f * ConstVals.PPM, 0.5f * ConstVals.PPM));
        rightFixture.putProperty(ConstKeys.SIDE_TYPE, ConstKeys.RIGHT);
        rightFixture.getOffsetFromBodyCenter().x = 0.425f * ConstVals.PPM;
        body.addFixture(rightFixture);
        rightFixture.getRawShape().setColor(Color.YELLOW);
        debugShapesSupplier.add(rightFixture::getShape);

        // TODO: define other fixtures

        body.getPreProcess().put(ConstKeys.DEFAULT, delta -> {
            float gravity;
            if (gravityType != GravityType.NO_GRAVITY && BodyExtensions.isBodySensing(body, BodySense.FEET_ON_GROUND)) {
                gravity = ConstVals.GROUND_GRAVITY;
            } else {
                gravity = switch (gravityType) {
                    case NORMAL_GRAVITY -> ConstVals.NORMAL_GRAVITY;
                    case LOW_GRAVITY -> ConstVals.LOW_GRAVITY;
                    case NO_GRAVITY -> 0f;
                };
            }

            Vector2 gravityVector2 = new Vector2();
            switch (directionRotation) {
                case UP -> gravityVector2.set(0, gravity);
                case DOWN -> gravityVector2.set(0, -gravity);
                case LEFT -> gravityVector2.set(gravity, 0);
                case RIGHT -> gravityVector2.set(-gravity, 0);
            }
            body.getPhysics().getGravity().set(gravityVector2.scl(ConstVals.PPM));
        });

        DrawableShapesComponent drawableShapesComponent = new DrawableShapesComponent(this, new Array<>(),
                debugShapesSupplier, true);
        addComponent(drawableShapesComponent);

        return BodyComponentCreator.create(this, body);
    }

    private UpdatablesComponent defineUpdatablesComponent() {
        return new UpdatablesComponent(this, delta -> {
            jetpackStamina.update(delta);
            float jetpackStaminaRatio = jetpackStamina.getImpulseRatio();
            ((RocketPartnersGame) getGame()).setDebugText("Jetpack Stamina: " + jetpackStaminaRatio);

            Timer brakeTimer = timers.get("brake");
            brakeTimer.update(delta);

            Timer damageTimer = timers.get("damage");
            Timer damageRecoveryTimer = timers.get("damage_recovery");

            if (damageTimer.isFinished() && !damageRecoveryTimer.isFinished()) {
                damageRecoveryTimer.update(delta);

                Timer damageFlashTimer = timers.get("damage_flash");
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

        Behavior wallslideBehavior = new Behavior(
                (delta) -> {
                    if (isDamaged() || BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND) ||
                            isAnyBehaviorActive(BehaviorType.JUMPING, BehaviorType.JETDASHING,
                                    BehaviorType.JETPACKING)) {
                        return false;
                    }

                    IControllerPoller controllerPoller = getGame().getControllerPoller();
                    boolean left = BodyExtensions.isBodySensing(getBody(), BodySense.SIDE_TOUCHING_BLOCK_LEFT) &&
                            controllerPoller.isPressed(ControllerButton.LEFT);
                    boolean right = BodyExtensions.isBodySensing(getBody(), BodySense.SIDE_TOUCHING_BLOCK_RIGHT) &&
                            controllerPoller.isPressed(ControllerButton.RIGHT);
                    return left || right;
                },
                () -> {
                    aButtonTask = AButtonTask.JUMP;
                },
                (delta) -> getBody().getPhysics().getFrictionOnSelf().y += 1.2f,
                () -> aButtonTask = AButtonTask.JETPACK
        );
        behaviorsComponent.addBehavior(BehaviorType.WALL_SLIDING, wallslideBehavior);

        Behavior jumpBehavior = new Behavior(
                (delta) -> {
                    IControllerPoller controllerPoller = getGame().getControllerPoller();
                    if (isDamaged() || !controllerPoller.isPressed(ControllerButton.A) ||
                            isBehaviorActive(BehaviorType.JETDASHING)) {
                        return false;
                    }
                    if (isBehaviorActive(BehaviorType.JUMPING)) {
                        Vector2 velocity = getBody().getPhysics().getVelocity();
                        return switch (directionRotation) {
                            case UP -> velocity.y > 0f;
                            case DOWN -> velocity.y < 0f;
                            case LEFT -> velocity.x < 0f;
                            case RIGHT -> velocity.x > 0f;
                        };
                    } else {
                        return aButtonTask == AButtonTask.JUMP &&
                                controllerPoller.isJustPressed(ControllerButton.A) &&
                                (BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND) ||
                                        isBehaviorActive(BehaviorType.WALL_SLIDING));
                    }
                },
                () -> {
                    Vector2 velocity = getBody().getPhysics().getVelocity();
                    if (isBehaviorActive(BehaviorType.WALL_SLIDING)) {
                        velocity.x = WALL_JUMP_HORIZONTAL * ConstVals.PPM * facing.getValue();
                        velocity.y = WALL_JUMP_VERTICAL * ConstVals.PPM;
                    } else {
                        if (velocity.x > MAX_JUMP_RUN_SPEED * ConstVals.PPM) {
                            velocity.x = MAX_JUMP_RUN_SPEED * ConstVals.PPM;
                        } else if (velocity.x < -MAX_JUMP_RUN_SPEED * ConstVals.PPM) {
                            velocity.x = -MAX_JUMP_RUN_SPEED * ConstVals.PPM;
                        }
                        velocity.y = GROUND_JUMP_IMPULSE * ConstVals.PPM;
                    }
                },
                null,
                () -> getBody().getPhysics().getVelocity().y = 0f);
        behaviorsComponent.addBehavior(BehaviorType.JUMPING, jumpBehavior);

        Behavior jetpackBehavior = new Behavior(
                (delta) -> {
                    IControllerPoller controllerPoller = getGame().getControllerPoller();
                    if (isDamaged() || !jetpackStamina.hasStamina() ||
                            !controllerPoller.isPressed(ControllerButton.A) ||
                            BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND) ||
                            isAnyBehaviorActive(BehaviorType.WALL_SLIDING, BehaviorType.JETDASHING)) {
                        return false;
                    }

                    if (isBehaviorActive(BehaviorType.JETPACKING)) {
                        return controllerPoller.isPressed(ControllerButton.A);
                    } else {
                        return controllerPoller.isJustPressed(ControllerButton.A) &&
                                aButtonTask == AButtonTask.JETPACK;
                    }
                },
                () -> {
                    requestToPlaySound(SoundAsset.JETPACK_SOUND, true);

                    jetpackStamina.setJetpacking(true);

                    PhysicsData physicsData = getBody().getPhysics();
                    physicsData.setGravityOn(false);
                    Vector2 velocity = physicsData.getVelocity();
                    if (velocity.x > MAX_JETPACK_RUN_SPEED * ConstVals.PPM) {
                        velocity.x = MAX_JETPACK_RUN_SPEED * ConstVals.PPM;
                    } else if (velocity.x < -MAX_JETPACK_RUN_SPEED * ConstVals.PPM) {
                        velocity.x = -MAX_JETPACK_RUN_SPEED * ConstVals.PPM;
                    }
                },
                (delta) -> {
                    jetpackStamina.update(delta);

                    PhysicsData physicsData = getBody().getPhysics();
                    physicsData.getVelocity().y = JETPACK_IMPULSE * ConstVals.PPM;
                },
                () -> {
                    ((RocketPartnersGame) getGame()).getAudioMan().stopSound(SoundAsset.JETPACK_SOUND);

                    jetpackStamina.setJetpacking(false);

                    getBody().getPhysics().setGravityOn(true);
                }
        );
        behaviorsComponent.addBehavior(BehaviorType.JETPACKING, jetpackBehavior);

        Behavior jetdashBehavior = new Behavior(
                (delta) -> {
                    IControllerPoller controllerPoller = getGame().getControllerPoller();
                    if (!jetpackStamina.hasStamina() || isDamaged() ||
                            !controllerPoller.isPressed(ControllerButton.Y) ||
                            BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND) ||
                            isBehaviorActive(BehaviorType.WALL_SLIDING)) {
                        return false;
                    }
                    return isBehaviorActive(BehaviorType.JETDASHING) ||
                            controllerPoller.isJustPressed(ControllerButton.Y);
                },
                () -> {
                    requestToPlaySound(SoundAsset.JETDASH_SOUND, false);

                    jetpackStamina.setJetpacking(true);

                    PhysicsData physicsData = getBody().getPhysics();
                    physicsData.setGravityOn(false);
                    physicsData.setVelocity(new Vector2(JETDASH_IMPULSE * ConstVals.PPM * facing.getValue(), 0f));
                },
                (delta) -> {
                    jetpackStamina.update(JETDASH_DELTA_SCALAR * delta);

                    PhysicsData physicsData = getBody().getPhysics();
                    physicsData.setVelocity(new Vector2(JETDASH_IMPULSE * ConstVals.PPM * facing.getValue(), 0f));
                },
                () -> {
                    jetpackStamina.setJetpacking(false);

                    PhysicsData physicsData = getBody().getPhysics();
                    physicsData.setGravityOn(true);

                    Timer brakeTimer = timers.get("brake");
                    brakeTimer.reset();
                }
        );
        behaviorsComponent.addBehavior(BehaviorType.JETDASHING, jetdashBehavior);

        return behaviorsComponent;
    }

    private SpritesComponent defineSpritesComponent() {
        GameSprite playerSprite = new GameSprite(new DrawingPriority(DrawingSection.FOREGROUND, 1), false);
        playerSprite.setSize(2.475f * ConstVals.PPM, 1.875f * ConstVals.PPM);

        GameSprite jetpackFlameSprite = new GameSprite(new DrawingPriority(DrawingSection.FOREGROUND, 0), false);
        jetpackFlameSprite.setSize(1f * ConstVals.PPM, 1f * ConstVals.PPM);

        SpritesComponent spritesComponent = new SpritesComponent(this,
                new Pair<>("player", playerSprite), new Pair<>("jetpackFlame", jetpackFlameSprite));

        spritesComponent.putUpdateFunction("player", (delta, gameSprite) -> {
            gameSprite.setFlip(facing == Facing.LEFT, false);
            gameSprite.setAlpha(damageFlash ? 0f : 1f);

            float rotation = directionRotation.getRotation();
            gameSprite.setOriginCenter();
            gameSprite.setRotation(rotation);

            Position position = switch (directionRotation) {
                case UP -> Position.BOTTOM_CENTER;
                case DOWN -> Position.TOP_CENTER;
                case LEFT -> Position.CENTER_RIGHT;
                case RIGHT -> Position.CENTER_LEFT;
            };
            Vector2 bodyPosition = getBounds().getPositionPoint(position);
            SpriteExtensionsKt.setPosition(gameSprite, bodyPosition, position);

            if (isBehaviorActive(BehaviorType.WALL_SLIDING)) {
                float offsetX = -0.2f * ConstVals.PPM * facing.getValue();
                gameSprite.translate(offsetX, 0f);
            }
        });

        spritesComponent.putUpdateFunction("jetpackFlame", (delta, gameSprite) -> {
            gameSprite.setHidden(!isAnyBehaviorActive(BehaviorType.JETPACKING, BehaviorType.JETDASHING));
            gameSprite.setOriginCenter();

            float facingOffset;
            float verticalOffset;
            if (isBehaviorActive(BehaviorType.JETPACKING)) {
                float rotation = directionRotation.getRotation();
                gameSprite.setRotation(rotation);
                gameSprite.setFlip(false, false);
                facingOffset = -0.5f;
                verticalOffset = -0.25f;
            } else {
                gameSprite.setRotation(270f);
                gameSprite.setFlip(facing == Facing.LEFT, false);
                facingOffset = -0.75f;
                verticalOffset = -0.1f;
            }
            facingOffset *= facing.getValue();

            Vector2 offset = (switch (directionRotation) {
                case UP -> new Vector2(facingOffset, verticalOffset);
                case DOWN -> new Vector2(facingOffset, -verticalOffset);
                case LEFT -> new Vector2(verticalOffset, facingOffset);
                case RIGHT -> new Vector2(-verticalOffset, -facingOffset);
            }).scl(ConstVals.PPM);
            Vector2 position = getBounds().getPositionPoint(Position.CENTER).add(offset);

            SpriteExtensionsKt.setPosition(gameSprite, position, Position.CENTER);
        });
        return spritesComponent;
    }

    private AnimationsComponent defineAnimationsComponent() {
        Array<Pair<Function0<GameSprite>, IAnimator>> spriteAnimators = new Array<>();
        spriteAnimators.add(definePlayerSpriteAnimator());
        spriteAnimators.add(defineJetpackFlameSpriteAnimator());
        return new AnimationsComponent(this, spriteAnimators);
    }

    private Pair<Function0<GameSprite>, IAnimator> definePlayerSpriteAnimator() {
        GameSprite playerSprite = getSprites().get("player");

        Supplier<String> keySupplier = () -> {
            if (!BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND)) {
                if (isBehaviorActive(BehaviorType.WALL_SLIDING)) {
                    return "wallslide";
                }
                if (isBehaviorActive(BehaviorType.JETDASHING)) {
                    return "jetdash";
                }
                Timer brakeTimer = timers.get("brake");
                if (brakeTimer.isFinished()) {
                    return "jump";
                } else {
                    return "brake";
                }
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
        animations.put("jump", new Animation(regions.get("jump")));
        animations.put("run", new Animation(regions.get("run"), 2, 2, 0.175f, true));
        animations.put("wallslide", new Animation(regions.get("wallslide")));
        animations.put("jetdash", new Animation(regions.get("jetdash"), 1, 3, 0.05f, false));
        animations.put("brake", new Animation(regions.get("brake")));
        animations.put("slip", new Animation(regions.get("slip")));
        animations.put("stand-shoot", new Animation(regions.get("stand-shoot")));

        Animator animator = new Animator(keySupplier, animations);

        return new Pair<>(() -> playerSprite, animator);
    }

    private Pair<Function0<GameSprite>, IAnimator> defineJetpackFlameSpriteAnimator() {
        GameSprite jetpackFlameSprite = getSprites().get("jetpackFlame");
        Animation animation = new Animation(regions.get("jetpackFlame"), 1, 3, 0.1f, true);
        Animator animator = new Animator(animation);
        return new Pair<>(() -> jetpackFlameSprite, animator);
    }

    private PointsComponent definePointsComponent() {
        PointsComponent pointsComponent = new PointsComponent(this);
        pointsComponent.putPoints(ConstKeys.HEALTH, ConstVals.MIN_HEALTH, ConstVals.MAX_HEALTH, ConstVals.MAX_HEALTH);
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

                    facing = isBehaviorActive(BehaviorType.WALL_SLIDING) ? Facing.RIGHT : Facing.LEFT;
                    running = !isBehaviorActive(BehaviorType.WALL_SLIDING);

                    // TODO: threshold and impulse should be dynamic based on the current player state
                    float threshold;
                    if (BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND)) {
                        threshold = MAX_GROUND_RUN_SPEED * ConstVals.PPM;
                    } else if (isBehaviorActive(BehaviorType.JETPACKING)) {
                        threshold = MAX_JETPACK_RUN_SPEED * ConstVals.PPM;
                    } else {
                        threshold = MAX_JUMP_RUN_SPEED * ConstVals.PPM;
                    }
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

                    facing = isBehaviorActive(BehaviorType.WALL_SLIDING) ? Facing.LEFT : Facing.RIGHT;
                    running = !isBehaviorActive(BehaviorType.WALL_SLIDING);

                    // TODO: threshold and impulse should be dynamic based on the current player state
                    float threshold;
                    if (BodyExtensions.isBodySensing(getBody(), BodySense.FEET_ON_GROUND)) {
                        threshold = MAX_GROUND_RUN_SPEED * ConstVals.PPM;
                    } else if (isBehaviorActive(BehaviorType.JETPACKING)) {
                        threshold = MAX_JETPACK_RUN_SPEED * ConstVals.PPM;
                    } else {
                        threshold = MAX_JUMP_RUN_SPEED * ConstVals.PPM;
                    }
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
