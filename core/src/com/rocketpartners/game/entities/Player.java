package com.rocketpartners.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.engine.IGame2D;
import com.engine.animations.AnimationsComponent;
import com.engine.audio.AudioComponent;
import com.engine.behaviors.BehaviorsComponent;
import com.engine.common.ClassInstanceUtils;
import com.engine.common.enums.Direction;
import com.engine.common.enums.Facing;
import com.engine.common.enums.Position;
import com.engine.common.interfaces.IBoundsSupplier;
import com.engine.common.interfaces.IFaceable;
import com.engine.common.objects.Properties;
import com.engine.common.shapes.GameRectangle;
import com.engine.common.time.Timer;
import com.engine.controller.ControllerComponent;
import com.engine.damage.IDamageable;
import com.engine.damage.IDamager;
import com.engine.drawables.shapes.DrawableShapesComponent;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sprites.GameSprite;
import com.engine.drawables.sprites.SpriteExtensionsKt;
import com.engine.drawables.sprites.SpritesComponent;
import com.engine.entities.GameEntity;
import com.engine.events.Event;
import com.engine.events.IEventListener;
import com.engine.points.Points;
import com.engine.points.PointsComponent;
import com.engine.updatables.UpdatablesComponent;
import com.engine.world.Body;
import com.engine.world.BodyComponent;
import com.engine.world.BodyType;
import com.engine.world.Fixture;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.assets.SoundAsset;
import com.rocketpartners.game.damage.DamageNegotation;
import com.rocketpartners.game.entities.contracts.IDirectionRotatable;
import com.rocketpartners.game.entities.contracts.IGravityListener;
import com.rocketpartners.game.world.BodyComponentCreator;
import com.rocketpartners.game.world.FixtureType;
import com.rocketpartners.game.world.GravityType;
import kotlin.Pair;
import kotlin.jvm.functions.Function0;
import kotlin.reflect.KClass;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
@Setter
public class Player extends GameEntity implements IEventListener, IDamageable, IFaceable, IBoundsSupplier,
        IDirectionRotatable, IGravityListener {

    private static final float SHOOT_ANIMATION_DURATION = 0.3f;
    private static final float DAMAGE_DURATION = 0.75f;
    private static final float DAMAGE_RECOVERY_TIME = 1.5f;
    private static final float DAMAGE_FLASH_DURATION = 0.05f;

    private static ObjectMap<Class<? extends IDamager>, DamageNegotation> damageNegotiations;

    private final ObjectSet<Object> eventKeyMask;
    private final Timer shootAnimationTimer;
    private final Timer damageTimer;
    private final Timer damageRecoveryTimer;
    private final Timer damageFlashTimer;

    private boolean invincible;
    private boolean canMove;
    private boolean damageFlash;
    private float gravity;
    private Facing facing;
    private Direction directionRotation;

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
        addComponent(new AudioComponent(this));
        addComponent(defineBodyComponent());
        addComponent(defineBehaviorsComponent());
        addComponent(defineUpdatablesComponent());
        addComponent(defineSpritesComponent());
        // TODO: addComponent(defineAnimationsComponent());
        // TODO: addComponent(defineControllerComponent());
        // TODO: addComponent(definePointsComponent());
    }

    @Override
    public void spawn(@NotNull Properties props) {
        super.spawn(props);

        GameRectangle spawnBounds = props.get(Constants.ConstKeys.BOUNDS,
                ClassInstanceUtils.convertToKClass(GameRectangle.class));
        assert spawnBounds != null;
        Vector2 spawnPoint = spawnBounds.getBottomCenterPoint();
        Body body =
                Objects.requireNonNull(getComponent(ClassInstanceUtils.convertToKClass(BodyComponent.class))).getBody();
        body.setBottomCenterToPoint(spawnPoint);

        GravityType gravityType = GravityType.valueOf(props.getOrDefault(Constants.ConstKeys.GRAVITY_TYPE,
                "normal_gravity", ClassInstanceUtils.convertToKClass(String.class)).toUpperCase());
        enterAreaOfGravityType(gravityType);

        Direction gravityDirection = Direction.valueOf(props.getOrDefault(Constants.ConstKeys.GRAVITY_DIRECTION,
                "up", ClassInstanceUtils.convertToKClass(String.class)).toUpperCase());
        enterAreaOfGravityDirection(gravityDirection);

        shootAnimationTimer.setToEnd();
        damageTimer.setToEnd();
        damageRecoveryTimer.setToEnd();
        damageFlashTimer.setToEnd();

        invincible = false;
        canMove = true;
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
        PointsComponent pointsComponent = getComponent(ClassInstanceUtils.convertToKClass(PointsComponent.class));
        assert pointsComponent != null;
        Points healthPoints = pointsComponent.getPoints(Constants.ConstKeys.HEALTH);
        healthPoints.translate(damage);

        AudioComponent audioComponent = getComponent(ClassInstanceUtils.convertToKClass(AudioComponent.class));
        assert audioComponent != null;
        audioComponent.requestToPlaySound(SoundAsset.PLAYER_DAMAGE_SOUND, false);

        damageTimer.reset();
        return true;
    }

    @NotNull
    @Override
    public GameRectangle getBounds() {
        KClass<BodyComponent> key = ClassInstanceUtils.convertToKClass(BodyComponent.class);
        return Objects.requireNonNull(getComponent(key)).getBody();
    }

    @Override
    public void enterAreaOfGravityType(GravityType gravityType) {
        switch (gravityType) {
            case NORMAL_GRAVITY -> gravity = Constants.ConstVals.NORMAL_GRAVITY;
            case LOW_GRAVITY -> gravity = Constants.ConstVals.LOW_GRAVITY;
        }
    }

    @Override
    public void enterAreaOfGravityDirection(Direction gravityDirection) {
        directionRotation = gravityDirection;
    }

    private BodyComponent defineBodyComponent() {
        Body body = Body.Companion.createBody(BodyType.DYNAMIC);
        body.setColor(Color.BROWN);
        body.width = 0.75f * Constants.ConstVals.PPM;
        body.getPhysics().setTakeFrictionFromOthers(true);

        Array<Function0<IDrawableShape>> debugShapesSupplier = new Array<>();

        Fixture bodyFixture = Fixture.Companion.createFixture(body, FixtureType.BODY, new GameRectangle().set(body));
        body.addFixture(bodyFixture);
        bodyFixture.getRawShape().setColor(Color.GRAY);
        debugShapesSupplier.add(bodyFixture::getShape);

        Fixture feetFixture = Fixture.Companion.createFixture(body, FixtureType.FEET,
                new GameRectangle().setSize(0.6f * Constants.ConstVals.PPM, 0.15f * Constants.ConstVals.PPM));
        feetFixture.getOffsetFromBodyCenter().y = -0.5f * Constants.ConstVals.PPM;
        body.addFixture(feetFixture);
        feetFixture.getRawShape().setColor(Color.GREEN);
        debugShapesSupplier.add(feetFixture::getShape);

        // TODO: define other fixtures

        body.getPreProcess().put(Constants.ConstKeys.DEFAULT, delta -> {
            Vector2 gravityVector2 = new Vector2();
            switch (directionRotation) {
                case UP -> gravityVector2.set(0, -gravity);
                case DOWN -> gravityVector2.set(0, gravity);
                case LEFT -> gravityVector2.set(-gravity, 0);
                case RIGHT -> gravityVector2.set(gravity, 0);
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
        sprite.setSize(2.475f * Constants.ConstVals.PPM, 1.875f * Constants.ConstVals.PPM);
        sprite.getPriority().setSection(DrawingSection.FOREGROUND);
        sprite.getPriority().setPriority(1);

        SpritesComponent spritesComponent = new SpritesComponent(this, new Pair<>(SpritesComponent.SPRITE, sprite));
        spritesComponent.putUpdateFunction(SpritesComponent.SPRITE, (delta, gameSprite) -> {
            float rotation = directionRotation.getRotation();
            gameSprite.setOriginCenter();
            gameSprite.setRotation(rotation);

            Position position;
            switch (directionRotation) {
                case UP -> position = Position.BOTTOM_CENTER;
                case DOWN -> position = Position.TOP_CENTER;
                case LEFT -> position = Position.CENTER_RIGHT;
                case RIGHT -> position = Position.CENTER_LEFT;
                default -> throw new IllegalStateException("Unexpected value: " + directionRotation);
            }
            Vector2 bodyPosition = getBounds().getPositionPoint(position);
            SpriteExtensionsKt.setPosition(gameSprite, bodyPosition, position);

            gameSprite.setAlpha(damageFlash ? 0f : 1f);
        });
        return spritesComponent;
    }

    private AnimationsComponent defineAnimationsComponent() {
        return null;
    }

    private ControllerComponent defineControllerComponent() {
        return null;
    }

    private PointsComponent definePointsComponent() {
        return null;
    }
}
