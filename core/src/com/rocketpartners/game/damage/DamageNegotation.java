package com.rocketpartners.game.damage;

import com.engine.damage.IDamager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
@Setter
@AllArgsConstructor
public class DamageNegotation {

    @NotNull
    private Function<IDamager, Integer> negotation;

    public DamageNegotation(int damage) {
        this(damager -> damage);
    }

    public int get(IDamager damager) {
        return negotation.apply(damager);
    }

    public static DamageNegotation create(int damage) {
        return new DamageNegotation(damage);
    }

    public static DamageNegotation create(@NotNull Function<IDamager, Integer> negotation) {
        return new DamageNegotation(negotation);
    }
}