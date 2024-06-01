package com.rocketpartners.game.screens.levels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.engine.IGame2D;
import com.engine.IGameEngine;
import com.engine.animations.AnimationsSystem;
import com.engine.audio.AudioSystem;
import com.engine.behaviors.BehaviorsSystem;
import com.engine.common.objects.Properties;
import com.engine.controller.ControllerSystem;
import com.engine.controller.polling.IControllerPoller;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sorting.IComparableDrawable;
import com.engine.drawables.sprites.SpritesSystem;
import com.engine.events.Event;
import com.engine.events.IEventsManager;
import com.engine.graph.SimpleNodeGraphMap;
import com.engine.motion.MotionSystem;
import com.engine.screens.levels.tiledmap.TiledMapLevelScreen;
import com.engine.screens.levels.tiledmap.TiledMapLoadResult;
import com.engine.screens.levels.tiledmap.builders.TiledMapLayerBuilders;
import com.engine.spawns.Spawn;
import com.engine.spawns.SpawnsManager;
import com.engine.systems.IGameSystem;
import com.engine.updatables.UpdatablesSystem;
import com.engine.world.WorldSystem;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.assets.MusicAsset;
import com.rocketpartners.game.assets.SoundAsset;
import com.rocketpartners.game.audio.AudioManager;
import com.rocketpartners.game.controllers.ControllerButton;
import com.rocketpartners.game.drawables.Background;
import com.rocketpartners.game.entities.Player;
import com.rocketpartners.game.events.EventType;
import com.rocketpartners.game.screens.levels.camera.CameraManagerForRooms;
import com.rocketpartners.game.screens.levels.events.PlayerSpawnEventHandler;
import com.rocketpartners.game.screens.levels.map.MapBuilder;
import com.rocketpartners.game.screens.levels.spawns.PlayerSpawnsManager;
import com.rocketpartners.game.utils.ConstFuncs;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;

import static com.rocketpartners.game.Constants.ConstKeys;
import static com.rocketpartners.game.Constants.ConstVals;

@Getter
@Setter
public class LevelScreen extends TiledMapLevelScreen {

    private ObjectSet<Object> eventKeyMask;
    private SpawnsManager spawnsMan;
    private Array<Disposable> disposables;
    private ObjectMap<DrawingSection, PriorityQueue<IComparableDrawable<Batch>>> drawables;
    private PriorityQueue<IDrawableShape> shapes;
    private Camera backgroundCamera;
    private Camera gameCamera;
    private Camera uiCamera;
    private Player player;
    private IGameEngine engine;
    private IEventsManager eventsMan;
    private AudioManager audioMan;
    private MusicAsset musicAsset;
    private PlayerSpawnsManager playerSpawnsMan;
    private PlayerSpawnEventHandler playerSpawnEventHandler;
    private IControllerPoller controllerPoller;
    private Array<Background> backgrounds;
    private CameraManagerForRooms cameraManagerForRooms;
    private Vector3 gameCamPriorPos;
    private OrderedMap<IGameSystem, Boolean> systemsOnPause;

    public LevelScreen(@NotNull IGame2D game) {
        super(game, new Properties());
    }

    @Override
    public void init() {
        super.init();
        eventKeyMask = new ObjectSet<>();
        spawnsMan = new SpawnsManager();
        disposables = new Array<>();
        drawables = ((RocketPartnersGame) getGame()).getDrawables();
        shapes = ((RocketPartnersGame) getGame()).getShapes();
        backgroundCamera = getGame().getViewports().get(ConstKeys.BACKGROUND).getCamera();
        gameCamera = getGame().getViewports().get(ConstKeys.GAME).getCamera();
        uiCamera = getGame().getViewports().get(ConstKeys.UI).getCamera();
        player = ((RocketPartnersGame) getGame()).getPlayer();
        engine = getGame().getEngine();
        eventsMan = getGame().getEventsMan();
        audioMan = ((RocketPartnersGame) getGame()).getAudioMan();
        playerSpawnsMan = new PlayerSpawnsManager(gameCamera);
        playerSpawnEventHandler = new PlayerSpawnEventHandler((RocketPartnersGame) getGame());
        controllerPoller = getGame().getControllerPoller();
        backgrounds = new Array<>();
        gameCamPriorPos = new Vector3();

        ObjectMap<String, IGameSystem> systemsMap = ((RocketPartnersGame) getGame()).getSystemsMap();
        Array<IGameSystem> systemsToSwitch = new Array<>();
        systemsToSwitch.add(systemsMap.get(AnimationsSystem.class.getSimpleName()));
        systemsToSwitch.add(systemsMap.get(ControllerSystem.class.getSimpleName()));
        systemsToSwitch.add(systemsMap.get(MotionSystem.class.getSimpleName()));
        systemsToSwitch.add(systemsMap.get(UpdatablesSystem.class.getSimpleName()));
        systemsToSwitch.add(systemsMap.get(BehaviorsSystem.class.getSimpleName()));
        systemsToSwitch.add(systemsMap.get(WorldSystem.class.getSimpleName()));
        systemsToSwitch.add(systemsMap.get(AudioSystem.class.getSimpleName()));

        cameraManagerForRooms = new CameraManagerForRooms(gameCamera);
        cameraManagerForRooms.setFocus(((RocketPartnersGame) getGame()).getPlayer());
        cameraManagerForRooms.setOnBeginTransition(() -> {
            systemsToSwitch.forEach(system -> system.setOn(false));

            Properties properties = new Properties();
            properties.put(ConstKeys.POSITION, cameraManagerForRooms.getTransitionInterpolation());
            properties.put(ConstKeys.CURRENT, cameraManagerForRooms.getCurrentRoom());
            properties.put(ConstKeys.PRIOR, cameraManagerForRooms.getPriorRoom());
            eventsMan.submitEvent(new Event(EventType.BEGIN_ROOM_TRANS, new Properties()));
        });
        cameraManagerForRooms.setOnContinueTransition(delta -> {
            if (cameraManagerForRooms.isDelayJustFinished()) {
                AnimationsSystem animationsSystem =
                        (AnimationsSystem) systemsMap.get(AnimationsSystem.class.getSimpleName());
                animationsSystem.setOn(true);
            }

            Properties eventProps = new Properties();
            eventProps.put(ConstKeys.POSITION, cameraManagerForRooms.getTransitionInterpolation());
            eventsMan.submitEvent(new Event(EventType.CONTINUE_ROOM_TRANS, eventProps));
        });
        cameraManagerForRooms.setOnEndTransition(() -> {
            RectangleMapObject currentRoom = cameraManagerForRooms.getCurrentRoom();
            Properties eventProps = new Properties();
            eventProps.put(ConstKeys.ROOM, currentRoom);

            eventsMan.submitEvent(new Event(EventType.END_ROOM_TRANS, eventProps));

            MapProperties currentRoomProps = currentRoom.getProperties();
            if (currentRoomProps.containsKey(ConstKeys.EVENT)) {
                String eventString = currentRoomProps.get(ConstKeys.EVENT, String.class);
                EventType eventType = EventType.valueOf(eventString.toUpperCase());
                eventsMan.submitEvent(new Event(eventType, eventProps));
            } else {
                systemsToSwitch.forEach(system -> system.setOn(true));
            }
        });
    }

    @Override
    public void show() {
        dispose();
        super.show();
        eventsMan.addListener(this);
        engine.getSystems().forEach(system -> system.setOn(true));
        if (musicAsset != null) {
            audioMan.playMusic(musicAsset, true);
        }

        TiledMapLoadResult tiledMapLoadResult = getTiledMapLoadResult();
        assert tiledMapLoadResult != null;
        int worldWidth = tiledMapLoadResult.getWorldWidth();
        int worldHeight = tiledMapLoadResult.getWorldHeight();

        SimpleNodeGraphMap graphMap = new SimpleNodeGraphMap(0, 0, worldWidth, worldHeight, ConstVals.PPM);
        ((RocketPartnersGame) getGame()).setGraphMap(graphMap);

        gameCamPriorPos.set(gameCamera.position);

        playerSpawnEventHandler.init();

        // TODO: set background and foreground parallax

        backgroundCamera.position.set(ConstFuncs.getCamInitPos());
        gameCamera.position.set(ConstFuncs.getCamInitPos());
    }

    @NotNull
    @Override
    protected TiledMapLayerBuilders getLayerBuilders() {
        return new MapBuilder((RocketPartnersGame) getGame(), cameraManagerForRooms, spawnsMan, playerSpawnsMan);
    }

    @Override
    protected void buildLevel(@NotNull Properties properties) {
        backgrounds = (Array<Background>) properties.get(ConstKeys.BACKGROUNDS);
        if (backgrounds == null) {
            backgrounds = new Array<>();
        }
        // TODO:
        /*
        Array<RectangleMapObject> rooms = (Array<RectangleMapObject>) properties.get(Constants.ConstKeys.ROOMS);
        cameraManagerForRooms.setRooms(rooms);

        Array<ISpawner> spawns = (Array<ISpawner>) properties.get(Constants.ConstKeys.SPAWNERS);
        assert spawns != null;
        spawnsMan.setSpawners(spawns);

        Array<Disposable> disposables = (Array<Disposable>) properties.get(Constants.ConstKeys.DISPOSABLES);
        this.disposables.addAll(disposables);
         */
    }

    @Override
    public void onEvent(@NotNull Event event) {
        switch ((EventType) event.getKey()) {
            case GAME_PAUSE:
                getGame().pause();
                break;
            case GAME_RESUME:
                getGame().resume();
                break;
            case PLAYER_SPAWN:
                cameraManagerForRooms.reset();
                engine.getSystems().forEach(system -> system.setOn(true));
                engine.spawn(player, playerSpawnsMan.getCurrentSpawnProps());
                // TODO: unset entity stats handler
                break;
            case PLAYER_READY:
                eventsMan.submitEvent(new Event(EventType.TURN_CONTROLLER_ON, new Properties()));
                break;
            case PLAYER_JUST_DIED:
                audioMan.stopMusic(null);
                // TODO: init player death event
                break;
            case PLAYER_DONE_DYIN:
                audioMan.playMusic(musicAsset, true);
                // TODO: init player spawn event or present game over screen
                break;
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        if (controllerPoller.isJustPressed(ControllerButton.START)) {
            if (getGame().getPaused()) {
                getGame().resume();
            } else {
                getGame().pause();
            }
        }

        if (!getGame().getPaused()) {
            backgrounds.forEach(background -> background.update(delta));
            cameraManagerForRooms.update(delta);

            if (!cameraManagerForRooms.isTransitioning() /* TODO: && player spawn event is finished */) {
                playerSpawnsMan.run();
                spawnsMan.update(delta);
                Array<Spawn> newSpawns = spawnsMan.getSpawnsAndClear();
                newSpawns.forEach(spawn -> engine.spawn(spawn.getEntity(), spawn.getProperties()));
            }

            /*
            TODO: implement the following

            if (!bossSpawnEventHandler.finished) bossSpawnEventHandler.update(delta)

            if (!playerSpawnEventHandler.finished) playerSpawnEventHandler.update(delta)
            else if (!playerDeathEventHandler.finished) playerDeathEventHandler.update(delta)
            else if (!endLevelEventHandler.finished) endLevelEventHandler.update(delta)

            playerStatsHandler.update(delta)
            */
        }

        float gameCamDeltaX = gameCamera.position.x - gameCamPriorPos.x;
        backgroundCamera.position.x += gameCamDeltaX * 0.5f;
        gameCamPriorPos.set(gameCamera.position);

        engine.update(delta);

        Batch batch = getGame().getBatch();
        batch.begin();

        batch.setProjectionMatrix(backgroundCamera.combined);
        backgrounds.forEach(background -> background.draw(batch));

        PriorityQueue<IComparableDrawable<Batch>> backgroundDrawables = drawables.get(DrawingSection.BACKGROUND);
        while (!backgroundDrawables.isEmpty()) {
            IComparableDrawable<Batch> backgroundSprite = backgroundDrawables.poll();
            backgroundSprite.draw(batch);
        }

        batch.setProjectionMatrix(gameCamera.combined);

        assert getTiledMapLevelRenderer() != null;
        getTiledMapLevelRenderer().render((OrthographicCamera) gameCamera);

        PriorityQueue<IComparableDrawable<Batch>> gameGroundDrawables = drawables.get(DrawingSection.PLAYGROUND);
        while (!gameGroundDrawables.isEmpty()) {
            IComparableDrawable<Batch> gameGroundSprite = gameGroundDrawables.poll();
            gameGroundSprite.draw(batch);
        }

        batch.setProjectionMatrix(uiCamera.combined);

        PriorityQueue<IComparableDrawable<Batch>> foregroundDrawables = drawables.get(DrawingSection.FOREGROUND);
        while (!foregroundDrawables.isEmpty()) {
            IComparableDrawable<Batch> gameSprite = foregroundDrawables.poll();
            gameSprite.draw(batch);
        }

        batch.end();

        // batch.setProjectionMatrix(uiStage.getCamera().combined);

        /*
        TODO:
        entityStatsHandler.draw(batch)
        playerStatsHandler.draw(batch)
        */

        /*
        TODO:
        if (!playerSpawnEventHandler.finished) playerSpawnEventHandler.draw(batch)
        else if (!endLevelEventHandler.finished) endLevelEventHandler.draw(batch)
        */

        ShapeRenderer shapeRenderer = getGame().getShapeRenderer();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        while (!shapes.isEmpty()) {
            IDrawableShape shape = shapes.poll();
            shape.draw(shapeRenderer);
        }
        shapeRenderer.end();

        // TODO: if (!cameraShaker.isFinished) cameraShaker.update(delta)
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getInitialized()) {
            disposables.forEach(Disposable::dispose);
            disposables.clear();
            engine.reset();
            audioMan.stopMusic(null);
            eventsMan.removeListener(this);
            spawnsMan.reset();
            playerSpawnsMan.reset();
            cameraManagerForRooms.reset();
        }
    }

    @Override
    public void pause() {
        systemsOnPause.clear();

        engine.getSystems().forEach(system -> {
            systemsOnPause.put(system, system.getOn());
            if (!(system instanceof SpritesSystem)) {
                system.setOn(false);
            }
        });

        audioMan.pauseAllSound();
        audioMan.pauseMusic(null);
        audioMan.playSound(SoundAsset.PAUSE_SOUND, false);
    }
}
