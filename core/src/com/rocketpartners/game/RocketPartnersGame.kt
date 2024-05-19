package com.rocketpartners.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.engine.Game2D;
import com.engine.GameEngine;
import com.engine.IGameEngine;
import com.engine.animations.AnimationsSystem;
import com.engine.audio.AudioSystem;
import com.engine.audio.IAudioManager;
import com.engine.behaviors.BehaviorsSystem;
import com.engine.common.extensions.ObjectSetExtensionsKt;
import com.engine.common.objects.MultiCollectionIterable;
import com.engine.controller.ControllerSystem;
import com.engine.controller.buttons.Buttons;
import com.engine.controller.polling.ControllerPoller;
import com.engine.controller.polling.IControllerPoller;
import com.engine.cullables.CullablesSystem;
import com.engine.drawables.fonts.FontsSystem;
import com.engine.drawables.shapes.DrawableShapesSystem;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sorting.IComparableDrawable;
import com.engine.drawables.sprites.SpritesSystem;
import com.engine.events.Event;
import com.engine.events.IEventListener;
import com.engine.events.IEventsManager;
import com.engine.graph.IGraphMap;
import com.engine.pathfinding.Pathfinder;
import com.engine.pathfinding.PathfindingSystem;
import com.engine.points.PointsSystem;
import com.engine.updatables.UpdatablesSystem;
import com.engine.world.WorldSystem;
import com.rocketpartners.game.assets.IAsset;
import com.rocketpartners.game.assets.MusicAsset;
import com.rocketpartners.game.assets.SoundAsset;
import com.rocketpartners.game.assets.TextureAsset;
import com.rocketpartners.game.controllers.ControllerUtils;
import com.rocketpartners.game.events.EventType;
import com.rocketpartners.game.world.CollisionHandler;
import com.rocketpartners.game.world.ContactListener;
import com.rocketpartners.game.world.FixtureType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;

@Getter
@Setter
public final class RocketPartnersGame extends Game2D implements IEventListener {

    public static final boolean DEBUG_SHAPES = true;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private Buttons buttons;
    private IControllerPoller controllerPoller;
    private AssetManager assMan;
    private IEventsManager eventsMan;
    private IGameEngine engine;
    private ObjectSet<Object> eventKeyMask;
    private Stage uiStage;
    private IAudioManager audioMan;
    private IGraphMap graphMap;
    private ObjectMap<DrawingSection, PriorityQueue<IComparableDrawable<Batch>>> drawables;
    private PriorityQueue<IDrawableShape> shapes;

    public void create() {
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        batch = new SpriteBatch();
        buttons = ControllerUtils.getInstance().loadButtons();
        controllerPoller = new ControllerPoller(buttons);
        assMan = new AssetManager();
        eventKeyMask = ObjectSetExtensionsKt.objectSetOf(EventType.TURN_CONTROLLER_ON, EventType.TURN_CONTROLLER_OFF);

        loadAssets(assMan);
        // TODO: instead of immediately finishing loading, should show progress bar and lock game until
        //  loading is complete
        assMan.finishLoading();

        int screenWidth = Constants.ConstVals.VIEW_WIDTH * Constants.ConstVals.PPM;
        int screenHeight = Constants.ConstVals.VIEW_HEIGHT * Constants.ConstVals.PPM;
        Viewport backgroundViewport = new FitViewport(screenWidth, screenHeight);
        getViewports().put(Constants.ConstKeys.BACKGROUND, backgroundViewport);
        Viewport gameViewport = new FitViewport(screenWidth, screenHeight);
        getViewports().put(Constants.ConstKeys.GAME, gameViewport);

        drawables = new ObjectMap<>();
        for (DrawingSection section : DrawingSection.values()) {
            drawables.put(section, new PriorityQueue<>());
        }
        shapes = new PriorityQueue<>(Comparator.comparingInt(o -> o.getShapeType().ordinal()));

        engine = createEngine(this);
    }

    private static void loadAssets(AssetManager assetManager) {
        Array<Iterable<IAsset>> assetArrays = new Array<>();
        assetArrays.add(MusicAsset.asAssetArray());
        assetArrays.add(SoundAsset.asAssetArray());
        assetArrays.add(TextureAsset.asAssetArray());
        for (IAsset asset : new MultiCollectionIterable<>(assetArrays)) {
            assetManager.load(asset.getSource(), asset.getAssClass());
        }
    }

    private static IGameEngine createEngine(RocketPartnersGame game) {
        ObjectMap<Object, ObjectSet<Object>> worldFilterMap = new ObjectMap<>();
        worldFilterMap.put(FixtureType.PLAYER, ObjectSetExtensionsKt.objectSetOf(FixtureType.ITEM));
        worldFilterMap.put(FixtureType.DAMAGEABLE, ObjectSetExtensionsKt.objectSetOf(FixtureType.DAMAGER));

        return new GameEngine(
                new ControllerSystem(game.getControllerPoller()),
                new AnimationsSystem(),
                new BehaviorsSystem(),
                new WorldSystem(
                        new ContactListener(game),
                        game::getGraphMap,
                        Constants.ConstVals.WORLD_TIME_STEP,
                        new CollisionHandler(game),
                        worldFilterMap,
                        true
                ),
                new CullablesSystem(),
                new PathfindingSystem(
                        (component) -> new Pathfinder(game.getGraphMap(), component.getParams()),
                        Constants.ConstVals.PATHFINDER_TIMEOUT,
                        Constants.ConstVals.PATHFINDER_TIMEOUT_UNIT
                ),
                new PointsSystem(),
                new UpdatablesSystem(),
                new FontsSystem((font) -> {
                    game.getDrawables().get(font.getPriority().getSection()).add(font);
                    return null;
                }),
                new SpritesSystem((sprite) -> {
                    game.getDrawables().get(sprite.getPriority().getSection()).add(sprite);
                    return null;
                }),
                new AnimationsSystem(),
                new DrawableShapesSystem((shape) -> {
                    game.getShapes().add(shape);
                    return null;
                }, DEBUG_SHAPES),
                new AudioSystem(
                        (request) -> {
                            game.getAudioMan().playSound(request.getSource(), request.getLoop());
                            return null;
                        },
                        (request) -> {
                            game.getAudioMan().playMusic(request.getSource(), request.getLoop());
                            return null;
                        },
                        (request) -> {
                            game.getAudioMan().stopSound(request);
                            return null;
                        },
                        (request) -> {
                            game.getAudioMan().stopMusic(request);
                            return null;
                        },
                        false,
                        false,
                        true,
                        true
                )
        );
    }

    public void onEvent(@NotNull Event event) {
        if (eventKeyMask.contains(event.getKey())) {
            // TODO: implement event handling
        }
    }

    public void render() {
    }

    public void dispose() {
    }
}
