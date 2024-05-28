package com.rocketpartners.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.engine.audio.IAudioManager;
import com.engine.audio.SoundRequest;
import com.engine.common.interfaces.Updatable;
import com.engine.common.time.Timer;
import com.rocketpartners.game.assets.MusicAsset;
import com.rocketpartners.game.assets.SoundAsset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class AudioManager implements Updatable, IAudioManager {

    public static final float MIN_VOLUME = 0f;
    public static final float MAX_VOLUME = 1f;
    public static final float DEFAULT_VOLUME = 0.5f;

    @AllArgsConstructor
    private static final class SoundEntry {

        private long id;
        private SoundAsset ass;
        private boolean loop;
        private float time;

        private SoundEntry(long id, SoundAsset ass) {
            this(id, ass, false, 0f);
        }

        @Override
        public int hashCode() {
            return ass.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SoundEntry && ((SoundEntry) o).ass == ass;
        }
    }

    private final OrderedMap<SoundAsset, Sound> sounds;
    private final OrderedMap<MusicAsset, Music> music;
    private final OrderedSet<SoundRequest> soundsToPlay;
    private final Array<SoundEntry> playingSounds;

    private Music currentMusic;
    private Timer fadeOutTimer;

    @Getter
    private boolean musicPaused;
    @Getter
    private float soundVolume;
    @Getter
    private float musicVolume;

    public AudioManager(OrderedMap<SoundAsset, Sound> sounds, OrderedMap<MusicAsset, Music> music) {
        this.sounds = sounds;
        this.music = music;
        soundsToPlay = new OrderedSet<>();
        playingSounds = new Array<>();
        soundVolume = DEFAULT_VOLUME;
        musicVolume = DEFAULT_VOLUME;
    }

    @Override
    public void setSoundVolume(float soundVolume) {
        if (soundVolume > MAX_VOLUME) {
            soundVolume = MAX_VOLUME;
        } else if (soundVolume < MIN_VOLUME) {
            soundVolume = MIN_VOLUME;
        }
        this.soundVolume = soundVolume;

        for (SoundEntry entry : playingSounds) {
            Sound sound = sounds.get(entry.ass);
            sound.setVolume(entry.id, soundVolume);
        }
    }

    @Override
    public void setMusicVolume(float musicVolume) {
        if (musicVolume > MAX_VOLUME) {
            musicVolume = MAX_VOLUME;
        } else if (musicVolume < MIN_VOLUME) {
            musicVolume = MIN_VOLUME;
        }
        this.musicVolume = musicVolume;

        for (Music m : music.values()) {
            m.setVolume(musicVolume);
        }
    }

    @Override
    public void playMusic(@Nullable Object key, boolean loop) {
        if (key == null) {
            if (currentMusic != null) {
                currentMusic.play();
            }
            return;
        }

        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = music.get((MusicAsset) key);
        fadeOutTimer = null;
        currentMusic.setLooping(loop);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
        musicPaused = false;
    }

    @Override
    public void stopMusic(@Nullable Object key) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        fadeOutTimer = null;
        musicPaused = true;
    }

    @Override
    public void pauseMusic(@Nullable Object key) {
        if (currentMusic != null) {
            currentMusic.pause();
        }
        musicPaused = true;
    }

    @Override
    public void playSound(@Nullable Object key, boolean loop) {
        if (key == null) {
            return;
        }
        playSound(new SoundRequest(key, loop));
    }

    public void playSound(SoundRequest request) {
        soundsToPlay.add(request);
    }

    @Override
    public void stopSound(@Nullable Object key) {
        sounds.get((SoundAsset) key).stop();
    }

    @Override
    public void pauseSound(@Nullable Object o) {
        sounds.get((SoundAsset) o).pause();
    }

    public void pauseAllSound() {
        sounds.values().forEach(Sound::pause);
    }

    public void resumeAllSound() {
        sounds.values().forEach(Sound::resume);
    }

    public boolean isSoundPlaying(SoundAsset sound) {
        for (SoundEntry entry : playingSounds) {
            if (entry.ass == sound) {
                return true;
            }
        }
        return false;
    }

    public void fadeOutMusic(float time) {
        fadeOutTimer = new Timer(time);
        fadeOutTimer.setRunOnFinished(() -> {
            if (currentMusic != null) {
                currentMusic.setVolume(musicVolume);
            }
            return null;
        });
    }

    public void stopFadingOutMusic() {
        fadeOutTimer = null;
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    @Override
    public void update(float delta) {
        soundsToPlay.forEach(request -> {
            SoundAsset key = (SoundAsset) request.getSource();
            Sound sound = sounds.get(key);
            long id = request.getLoop() ? sound.loop(soundVolume) : sound.play(soundVolume);
            playingSounds.add(new SoundEntry(id, key, request.getLoop(), 0f));
        });
        soundsToPlay.clear();

        Iterator<SoundEntry> playingSoundsIter = playingSounds.iterator();
        while (playingSoundsIter.hasNext()) {
            SoundEntry entry = playingSoundsIter.next();
            if (entry.loop) {
                continue;
            }
            entry.time += delta;
            if (entry.time > entry.ass.getSeconds()) {
                playingSoundsIter.remove();
            }
        }

        if (!musicPaused && fadeOutTimer != null) {
            fadeOutTimer.update(delta);
            if (currentMusic != null) {
                currentMusic.setVolume(musicVolume - (musicVolume * fadeOutTimer.getRatio()));
            }
            if (fadeOutTimer.isFinished()) {
                fadeOutTimer = null;
                stopMusic(null);
            }
        }
    }
}
