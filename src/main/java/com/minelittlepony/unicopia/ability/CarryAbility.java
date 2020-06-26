package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Pegasi ability to pick up and carry other players
 */
public class CarryAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 0;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 10;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canFly();
    }

    @Override
    public Hit tryActivate(Pony player) {
        return Hit.INSTANCE;
    }

    protected LivingEntity findRider(PlayerEntity player, World w) {
        Entity hit = VecHelper.getLookedAtEntity(player, 10);

        if (hit instanceof LivingEntity && !player.isConnectedThroughVehicle(hit)) {
            if (!(hit instanceof IPickupImmuned)) {
                return (LivingEntity)hit;
            }
        }

        return null;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.getOwner();
        LivingEntity rider = findRider(player, iplayer.getWorld());

        if (rider != null) {
            rider.startRiding(player, true);
        } else {
            player.removeAllPassengers();
        }

        if (player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)player).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }

    public interface IPickupImmuned {

    }
}
