package com.rocketpartners.game.screens.levels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.engine.IGame2D;
import com.engine.IGameEngine;
import com.engine.audio.IAudioManager;
import com.engine.common.objects.Properties;
import com.engine.controller.polling.IControllerPoller;
import com.engine.drawables.shapes.IDrawableShape;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sorting.IComparableDrawable;
import com.engine.events.IEventsManager;
import com.engine.graph.SimpleNodeGraphMap;
import com.engine.screens.levels.tiledmap.TiledMapLevelScreen;
import com.engine.screens.levels.tiledmap.TiledMapLoadResult;
import com.engine.screens.levels.tiledmap.builders.TiledMapLayerBuilders;
import com.engine.spawns.SpawnsManager;
import com.rocketpartners.game.Constants;
import com.rocketpartners.game.RocketPartnersGame;
import com.rocketpartners.game.assets.MusicAsset;
import com.rocketpartners.game.controllers.ControllerButton;
import com.rocketpartners.game.drawables.Background;
import com.rocketpartners.game.screens.levels.map.MapBuilder;
import com.rocketpartners.game.screens.levels.spawns.PlayerSpawnsManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;

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
    private Stage uiStage;
    private IGameEngine engine;
    private IEventsManager eventsMan;
    private IAudioManager audioMan;
    private MusicAsset musicAsset;
    private PlayerSpawnsManager playerSpawnsMan;
    private IControllerPoller controllerPoller;
    private Array<Background> backgrounds;

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
        backgroundCamera = getGame().getViewports().get(Constants.ConstKeys.BACKGROUND).getCamera();
        gameCamera = getGame().getViewports().get(Constants.ConstKeys.GAME).getCamera();
        uiStage = ((RocketPartnersGame) getGame()).getUiStage();
        engine = getGame().getEngine();
        eventsMan = getGame().getEventsMan();
        audioMan = ((RocketPartnersGame) getGame()).getAudioMan();
        playerSpawnsMan = new PlayerSpawnsManager(gameCamera);
        controllerPoller = getGame().getControllerPoller();
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
        SimpleNodeGraphMap graphMap = new SimpleNodeGraphMap(0, 0, worldWidth, worldHeight, Constants.ConstVals.PPM);
        ((RocketPartnersGame) getGame()).setGraphMap(graphMap);
        // TODO: set background and foreground parallax
    }

    @NotNull
    @Override
    protected TiledMapLayerBuilders getLayerBuilders() {
        return new MapBuilder((RocketPartnersGame) getGame(), spawnsMan);
    }

    @Override
    protected void buildLevel(@NotNull Properties properties) {
        backgrounds = (Array<Background>) properties.get(Constants.ConstKeys.BACKGROUNDS);
        String playerSpawnsKey = Constants.ConstKeys.PLAYER + "_" + Constants.ConstKeys.SPAWNS;
        Array<RectangleMapObject> playerSpawnObjs = (Array<RectangleMapObject>) properties.get(playerSpawnsKey);
        if (playerSpawnObjs != null) {
            playerSpawnsMan.setSpawnObjs(playerSpawnObjs);
        }
        // TODO: set game rooms, disposables, backgrounds, spawners, and camera positions
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
            // TODO: render backgrounds
            // TODO: update camera manager
            // TODO: update spawns managers

        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getInitialized()) {
            eventsMan.removeListener(this);
            disposables.forEach(Disposable::dispose);
            disposables.clear();
        }
    }
}
