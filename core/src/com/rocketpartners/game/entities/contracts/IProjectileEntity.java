package com.rocketpartners.game.entities.contracts;

import com.engine.damage.IDamageable;
import com.engine.damage.IDamager;
import com.engine.entities.contracts.IBodyEntity;
import com.engine.world.IFixture;
import org.jetbrains.annotations.NotNull;

public interface IProjectileEntity extends IBodyEntity, IOwnable, IDamager {

    void explodeAndDie();

    default boolean canDamage(@NotNull IDamageable damageable) {
        return damageable != getOwner();
    }

    default void hitBody(IFixture bodyFixture) {
    }

    default void hitBlock(IFixture blockFixture) {
    }

    default void hitShield(IFixture shieldFixture) {
    }

    default void hitWater(IFixture waterFixture) {
    }

    default void hitProjectile(IFixture projectileFixture) {
    }
}