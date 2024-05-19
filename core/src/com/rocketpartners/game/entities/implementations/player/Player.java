package com.rocketpartners.game.entities.implementations.player;

import com.badlogic.gdx.utils.ObjectSet;
import com.engine.IGame2D;
import com.engine.common.enums.Facing;
import com.engine.common.interfaces.IFaceable;
import com.engine.common.time.Timer;
import com.engine.damage.IDamageable;
import com.engine.damage.IDamager;
import com.engine.entities.GameEntity;
import com.engine.events.Event;
import com.engine.events.IEventListener;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class Player extends GameEntity implements IEventListener, IDamageable, IFaceable {

    private static final float SHOOT_ANIMATION_DURATION = 0.3f;
    private static final float DAMAGE_DURATION = 0.75f;
    private static final float DAMAGE_RECOVERY_TIME = 1.5f;
    private static final float DAMAGE_FLASH_DURATION = 0.05f;

    private final ObjectSet<Object> eventKeyMask;
    private final Timer shootAnimationTimer;
    private final Timer damageTimer;
    private final Timer damageRecoveryTimer;
    private final Timer damageFlashTimer;

    private boolean invincible;
    private Facing facing;

    public Player(@NotNull IGame2D game) {
        super(game);
        eventKeyMask = new ObjectSet<>();
        shootAnimationTimer = new Timer(SHOOT_ANIMATION_DURATION);
        damageTimer = new Timer(DAMAGE_DURATION);
        damageRecoveryTimer = new Timer(DAMAGE_RECOVERY_TIME);
        damageFlashTimer = new Timer(DAMAGE_FLASH_DURATION);
    }

    @Override
    public void onEvent(@NotNull Event event) {
    }

    @Override
    public boolean getInvincible() {
        return invincible || !damageTimer.isFinished() || !damageRecoveryTimer.isFinished();
    }

    @Override
    public boolean canBeDamagedBy(@NotNull IDamager iDamager) {
        return false;
    }

    @Override
    public boolean takeDamageFrom(@NotNull IDamager iDamager) {
        return false;
    }
}
