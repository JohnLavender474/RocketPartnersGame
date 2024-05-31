package com.rocketpartners.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.engine.Game2D;
import com.engine.GameEngine;
import com.engine.IGameEngine;
import com.engine.animations.AnimationsSystem;
import com.engine.audio.AudioSystem;
import com.engine.behaviors.BehaviorsSystem;
import com.engine.common.extensions.AssetManagerExtensionsKt;
import com.engine.common.extensions.ObjectSetExtensionsKt;
import com.engine.common.objects.MultiCollectionIterable;
import com.engine.controller.ControllerSystem;
import com.engine.controller.buttons.Buttons;
import com.engine.controller.polling.ControllerPoller;
import com.engine.controller.polling.IControllerPoller;
import com.engine.cullables.CullablesSystem;
import com.engine.drawables.fonts.BitmapFontHandle;
import com.engine.drawables.fonts.FontsSystem;
import com.engine.drawables.shapes.DrawableShapesSystem;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sorting.IComparableDrawable;
import com.engine.drawables.sprites.GameSprite;
import com.engine.drawables.sprites.SpritesSystem;
import com.engine.events.Event;
import com.engine.events.EventsManager;
import com.engine.events.IEventListener;
import com.engine.events.IEventsManager;
import com.engine.graph.IGraphMap;
import com.engine.pathfinding.Pathfinder;
import com.engine.pathfinding.PathfindingSystem;
import com.engine.points.PointsSystem;
import com.engine.screens.IScreen;
import com.engine.systems.IGameSystem;
import com.engine.updatables.UpdatablesSystem;
import com.engine.world.WorldSystem;
import com.rocketpartners.game.assets.IAsset;
import com.rocketpartners.game.assets.MusicAsset;
import com.rocketpartners.game.assets.SoundAsset;
import com.rocketpartners.game.assets.SpriteSheetAsset;
import com.rocketpartners.game.audio.AudioManager;
import com.rocketpartners.game.controllers.ControllerUtils;
import com.rocketpartners.game.entities.Player;
import com.rocketpartners.game.events.EventType;
import com.rocketpartners.game.screens.ScreenEnum;
import com.rocketpartners.game.screens.levels.LevelEnum;
import com.rocketpartners.game.screens.levels.LevelScreen;
import com.rocketpartners.game.utils.BitmapFontHandleUtils;
import com.rocketpartners.game.world.CollisionHandler;
import com.rocketpartners.game.world.ContactListener;
import com.rocketpartners.game.world.FixtureType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.rocketpartners.game.Constants.*;

@Getter
@Setter
public final class RocketPartnersGame extends Game2D implements IEventListener {

    public static final boolean DEBUG_SHAPES = true;
    public static final boolean DEBUG_TEXT = true;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private Buttons buttons;
    private IControllerPoller controllerPoller;
    private AssetManager assMan;
    private IEventsManager eventsMan;
    private IGameEngine engine;
    private ObjectSet<Object> eventKeyMask;
    private AudioManager audioMan;
    private IGraphMap graphMap;
    private ObjectMap<DrawingSection, PriorityQueue<IComparableDrawable<Batch>>> drawables;
    private PriorityQueue<IDrawableShape> shapes;
    private Player player;
    private ObjectMap<String, IGameSystem> systemsMap;
    private BitmapFontHandle debugText;

    public void create() {
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        batch = new SpriteBatch();
        buttons = ControllerUtils.getInstance().loadButtons();
        controllerPoller = new ControllerPoller(buttons);
        assMan = new AssetManager();
        eventsMan = new EventsManager();
        eventKeyMask = ObjectSetExtensionsKt.objectSetOf(EventType.TURN_CONTROLLER_ON, EventType.TURN_CONTROLLER_OFF);

        loadAssets(assMan);
        // TODO: instead of immediately finishing loading, should show progress bar and lock game until
        //  loading is complete
        assMan.finishLoading();

        OrderedMap<SoundAsset, Sound> sounds = new OrderedMap<>();
        for (SoundAsset ass : SoundAsset.values()) {
            sounds.put(ass, AssetManagerExtensionsKt.getSound(assMan, ass.getSource()));
        }
        OrderedMap<MusicAsset, Music> music = new OrderedMap<>();
        for (MusicAsset ass : MusicAsset.values()) {
            music.put(ass, AssetManagerExtensionsKt.getMusic(assMan, ass.getSource()));
        }
        audioMan = new AudioManager(sounds, music);

        int screenWidth = ConstVals.VIEW_WIDTH * ConstVals.PPM;
        int screenHeight = ConstVals.VIEW_HEIGHT * ConstVals.PPM;
        Viewport backgroundViewport = new FitViewport(screenWidth, screenHeight);
        getViewports().put(ConstKeys.BACKGROUND, backgroundViewport);
        Viewport gameViewport = new FitViewport(screenWidth, screenHeight);
        getViewports().put(ConstKeys.GAME, gameViewport);
        Viewport uiViewport = new FitViewport(screenWidth, screenHeight);
        getViewports().put(ConstKeys.UI, uiViewport);

        drawables = new ObjectMap<>();
        for (DrawingSection section : DrawingSection.values()) {
            drawables.put(section, new PriorityQueue<>());
        }
        shapes = new PriorityQueue<>(Comparator.comparingInt(o -> o.getShapeType().ordinal()));

        engine = createEngine(this);
        systemsMap = new ObjectMap<>();
        engine.getSystems().forEach(system -> systemsMap.put(system.getClass().getSimpleName(), system));

        player = new Player(this);
        player.init();
        player.setInitialized(true);

        debugText = BitmapFontHandleUtils.create("Debug Text");

        // TODO: add screens
        ObjectMap<String, IScreen> screens = getScreens();
        screens.put(ScreenEnum.LEVEL_SCREEN.name(), new LevelScreen(this));

        startLevelScreen(LevelEnum.TEST1);
    }

    public void setDebugText(@NotNull String text) {
        debugText.setTextSupplier(() -> text);
    }

    private static void loadAssets(AssetManager assetManager) {
        Array<Iterable<IAsset>> assetArrays = new Array<>();
        assetArrays.add(MusicAsset.asAssetArray());
        assetArrays.add(SoundAsset.asAssetArray());
        assetArrays.add(SpriteSheetAsset.asAssetArray());
        for (IAsset asset : new MultiCollectionIterable<>(assetArrays)) {
            assetManager.load(asset.getSource(), asset.getAssClass());
        }
    }

    private static IGameEngine createEngine(RocketPartnersGame game) {
        ObjectMap<Object, ObjectSet<Object>> worldFilterMap = new ObjectMap<>();
        worldFilterMap.put(FixtureType.FEET, ObjectSetExtensionsKt.objectSetOf(FixtureType.WORLD_BLOCK));
        worldFilterMap.put(FixtureType.PLAYER, ObjectSetExtensionsKt.objectSetOf(FixtureType.ITEM));
        worldFilterMap.put(FixtureType.DAMAGEABLE, ObjectSetExtensionsKt.objectSetOf(FixtureType.DAMAGER));

        return new GameEngine(
                new ControllerSystem(game.getControllerPoller()),
                new BehaviorsSystem(),
                new WorldSystem(
                        new ContactListener(game),
                        (Supplier<IGraphMap>) game::getGraphMap,
                        ConstVals.WORLD_TIME_STEP,
                        new CollisionHandler(game),
                        worldFilterMap,
                        true),
                new CullablesSystem(),
                new PathfindingSystem(
                        (component) -> new Pathfinder(game.getGraphMap(), component.getParams()),
                        ConstVals.PATHFINDER_TIMEOUT,
                        ConstVals.PATHFINDER_TIMEOUT_UNIT),
                new PointsSystem(),
                new UpdatablesSystem(),
                new FontsSystem((Consumer<BitmapFontHandle>) (font) ->
                        game.getDrawables().get(font.getPriority().getSection()).add(font)),
                new AnimationsSystem(),
                new SpritesSystem((Consumer<GameSprite>) (sprite) ->
                        game.getDrawables().get(sprite.getPriority().getSection()).add(sprite)),
                new DrawableShapesSystem((Consumer<IDrawableShape>) (shape) ->
                        game.getShapes().add(shape),
                        DEBUG_SHAPES),
                new AudioSystem(
                        (request) -> game.getAudioMan().playSound(request.getSource(), request.getLoop()),
                        (request) -> game.getAudioMan().playMusic(request.getSource(), request.getLoop()),
                        (request) -> game.getAudioMan().stopSound(request),
                        game.getAudioMan()::stopMusic,
                        false,
                        false,
                        true,
                        true
                )
        );
    }

    public void startLevelScreen(LevelEnum level) {
        LevelScreen levelScreen = (LevelScreen) getScreens().get(ScreenEnum.LEVEL_SCREEN.name());
        levelScreen.setTmxMapSource(level.getTmxSourceFile());
        levelScreen.setMusicAsset(level.getMusicAsset());
        setCurrentScreen(ScreenEnum.LEVEL_SCREEN.name());
    }

    public void onEvent(@NotNull Event event) {
        if (eventKeyMask.contains(event.getKey())) {
            switch ((EventType) event.getKey()) {
                case TURN_CONTROLLER_OFF -> controllerPoller.setOn(false);
                case TURN_CONTROLLER_ON -> controllerPoller.setOn(true);
            }
        }
    }

    public void render() {
        super.render();
        float delta = Gdx.graphics.getDeltaTime();
        audioMan.update(delta);
        if (DEBUG_TEXT) {
            batch.setProjectionMatrix(getViewports().get(ConstKeys.UI).getCamera().combined);
            batch.begin();
            debugText.draw(batch);
            batch.end();
        }
    }
}
