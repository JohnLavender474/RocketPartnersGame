package com.rocketpartners.game.entities.contracts;

import com.engine.entities.contracts.IPointsEntity;
import com.engine.points.Points;
import com.rocketpartners.game.Constants;

/**
 * An entity that has health points.
 */
public interface IHealthEntity extends IPointsEntity {

    /**
     * Returns the health ratio of the entity.
     *
     * @return the health ratio
     */
    default float getHealthRatio() {
        return (float) getCurrentHealth() / (float) getMaxHealth();
    }

    /**
     * Returns the health points of the entity.
     *
     * @return the health points
     */
    default Points getHealthPoints() {
        return getPoints(Constants.ConstKeys.HEALTH);
    }

    /**
     * Returns the current health of the entity.
     *
     * @return the current health
     */
    default int getCurrentHealth() {
        return getHealthPoints().getCurrent();
    }

    /**
     * Sets the health of the entity.
     *
     * @param health the health to set
     */
    default void setHealth(int health) {
        getHealthPoints().set(health);
    }

    /**
     * Adds health to the entity.
     *
     * @param health the health to add
     */
    default void addHealth(int health) {
        getHealthPoints().set(getCurrentHealth() + health);
    }

    /**
     * Returns the minimum health of the entity.
     *
     * @return the minimum health
     */
    default int getMinHealth() {
        return getHealthPoints().getMin();
    }

    /**
     * Sets the minimum health of the entity.
     *
     * @param minHealth the minimum health to set
     */
    default void setMinHealth(int minHealth) {
        getHealthPoints().setMin(minHealth);
    }

    /**
     * Returns the maximum health of the entity.
     *
     * @return the maximum health
     */
    default int getMaxHealth() {
        return getHealthPoints().getMax();
    }

    /**
     * Sets the maximum health of the entity.
     *
     * @param maxHealth the maximum health to set
     */
    default void setMaxHealth(int maxHealth) {
        getHealthPoints().setMax(maxHealth);
    }

    /**
     * Returns whether the entity has maximum health.
     *
     * @return true if the entity has maximum health, false otherwise
     */
    default boolean hasMaxHealth() {
        return getCurrentHealth() >= getMaxHealth();
    }

    /**
     * Returns whether the entity has depleted health.
     *
     * @return true if the entity has depleted health, false otherwise
     */
    default boolean hasDepletedHealth() {
        return getCurrentHealth() <= getMinHealth();
    }
}