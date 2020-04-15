package com.minelittlepony.unicopia.core.entity.player;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.InteractionManager;
import com.minelittlepony.unicopia.core.ability.IAbilityReceiver;
import com.minelittlepony.unicopia.core.enchanting.IPageOwner;
import com.minelittlepony.unicopia.core.entity.FlightControl;
import com.minelittlepony.unicopia.core.entity.RaceContainer;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.magic.IHeldEffect;
import com.minelittlepony.unicopia.core.network.ITransmittable;
import com.minelittlepony.util.IInterpolator;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

/**
 * The player.
 *
 * This is the core of unicopia.
 */
public interface IPlayer extends ICaster<PlayerEntity>, RaceContainer<PlayerEntity>, ITransmittable, IPageOwner {

    /**
     * Gets the player's magical abilities delegate responsible for all spell casting and persisting/updating.
     */
    IAbilityReceiver getAbilities();

    /**
     * Gets the gravity delegate responsible for updating flight states
     */
    GravityDelegate getGravity();

    /**
     * Gets the flight delegate.
     */
    FlightControl getFlight();

    /**
     * Gets the player's viewport.
     */
    PlayerCamera getCamera();

    /**
     * Gets the inventory delegate for this player.
     */
    PlayerInventory getInventory();

    /**
     * Gets an animation interpolator.
     */
    IInterpolator getInterpolator();

    /**
     * Gets the amount of exertion this player has put toward any given activity.
     * This is simillar to tiredness.
     */
    float getExertion();

    /**
     * Sets the player's exertion level.
     */
    void setExertion(float exertion);

    /**
     * Adds player tiredness.
     */
    default void addExertion(int exertion) {
        setExertion(getExertion() + exertion/100F);
    }

    /**
     * Gets the amount of excess energy the player has.
     * This is increased by eating sugar.
     */
    float getEnergy();

    /**
     * Sets the player's energy level.
     */
    void setEnergy(float energy);

    /**
     * Adds energy to the player's existing energy level.
     */
    default void addEnergy(int energy) {
        setEnergy(getEnergy() + energy / 100F);
    }

    void copyFrom(IPlayer oldPlayer);

    /**
     * Called when the player steps on clouds.
     */
    boolean stepOnCloud();

    /**
     * Gets the held effect for the given item.
     * Updates it if the current held effect doesn't match or is empty.
     *
     * Returns null if the passed item has no held effect.
     */
    @Nullable
    IHeldEffect getHeldEffect(ItemStack stack);

    /**
     * Called when this player falls.
     */
    float onImpact(float distance);

    /**
     * Attempts to sleep in a bed.
     *
     * @param pos The position of the bed
     *
     * @return The sleep result.
     */
    Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos);

    /**
     * Returns true if this player is the use.
     */
    default boolean isClientPlayer() {
        return InteractionManager.instance().isClientPlayer(getOwner());
    }

    static boolean equal(GameProfile one, GameProfile two) {
        return one == two || (one != null && two != null && one.getId().equals(two.getId()));
    }

    static boolean equal(PlayerEntity one, PlayerEntity two) {
        return one == two || (one != null && two != null && equal(one.getGameProfile(), two.getGameProfile()));
    }
}
