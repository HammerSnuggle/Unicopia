package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.USounds;
import com.minelittlepony.unicopia.mixin.MixinEntity;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

class PlayerGravityDelegate implements IUpdatable<EntityPlayer>, IGravity, InbtSerialisable, IFlyingPredicate, IPlayerHeightPredicate {

    private final IPlayer player;

    private static final float MAXIMUM_FLIGHT_EXPERIENCE = 500;

    public int ticksInAir = 0;
    public float flightExperience = 0;

    public boolean isFlying = false;

    public PlayerGravityDelegate(IPlayer player) {
        this.player = player;
    }

    @Override
    public boolean checkCanFly(IPlayer player) {
        if (player.getOwner().capabilities.isCreativeMode) {
            return true;
        }

        if (player.hasEffect()) {
            IMagicEffect effect = player.getEffect();
            if (!effect.getDead() && effect instanceof IFlyingPredicate) {
                return ((IFlyingPredicate)effect).checkCanFly(player);
            }
        }

        return player.getPlayerSpecies().canFly();
    }

    protected boolean isRainboom(IPlayer player) {
        return Math.sqrt(getHorizontalMotion(player.getOwner())) > 0.4F;
    }

    @Override
    public float getTargetEyeHeight(IPlayer player) {
        if (player.hasEffect()) {
            IMagicEffect effect = player.getEffect();
            if (!effect.getDead() && effect instanceof IPlayerHeightPredicate) {
                float val = ((IPlayerHeightPredicate)effect).getTargetEyeHeight(player);
                if (val > 0) {
                    return val;
                }
            }
        }

        if (isFlying && isRainboom(player)) {
            return 0.5F;
        }

        return player.getOwner().getDefaultEyeHeight();
    }

    @Override
    public float getTargetBodyHeight(IPlayer player) {
        if (player.hasEffect()) {
            IMagicEffect effect = player.getEffect();
            if (!effect.getDead() && effect instanceof IPlayerHeightPredicate) {
                float val = ((IPlayerHeightPredicate)effect).getTargetBodyHeight(player);
                if (val > 0) {
                    return val;
                }
            }
        }

        // Player height is reset at this point, so we can use it as our baseline.

        if (isFlying && isRainboom(player)) {
            return player.getOwner().height / 2;
        }

        return player.getOwner().height;
    }


    @Override
    public void onUpdate(EntityPlayer entity) {

        entity.capabilities.allowFlying = checkCanFly(player);

        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.isFlying |= entity.capabilities.allowFlying && isFlying && !entity.onGround && !entity.isWet();
        }

        isFlying = entity.capabilities.isFlying && !entity.capabilities.isCreativeMode;

        MixinEntity.setSize(entity, entity.width, player.getInterpolator().interpolate("standingHeight", getTargetBodyHeight(player), 10));
        entity.eyeHeight = player.getInterpolator().interpolate("eyeHeight", getTargetEyeHeight(player), 10);

        if (!entity.capabilities.isCreativeMode && !entity.isElytraFlying()) {
            if (isFlying && !entity.isRiding()) {

                entity.fallDistance = 0;

                if (player.getPlayerSpecies() != Race.CHANGELING && entity.world.rand.nextInt(100) == 0) {
                    float exhaustion = (0.3F * ticksInAir) / 70;
                    if (entity.isSprinting()) {
                        exhaustion *= 3.11F;
                    }

                    exhaustion *= (1 - flightExperience/MAXIMUM_FLIGHT_EXPERIENCE);

                    entity.addExhaustion(exhaustion);
                }

                if (ticksInAir++ >= MAXIMUM_FLIGHT_EXPERIENCE) {
                    ticksInAir = 0;
                    addFlightExperience(entity);
                    entity.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, 1, 1);
                }

                float forward = 0.00015F * flightExperience;

                entity.motionX += - forward * MathHelper.sin(entity.rotationYaw * 0.017453292F);
                entity.motionY -= 0.05F - getHorizontalMotion(entity) / 100;
                entity.motionZ += forward * MathHelper.cos(entity.rotationYaw * 0.017453292F);

                if (ticksInAir > 0 && ticksInAir % 12 == 0) {
                    entity.playSound(USounds.WING_FLAP, 0.5F, 1);
                }
            } else {
                if (ticksInAir != 0) {
                    entity.playSound(USounds.WING_FLAP, 0.4F, 1);
                }

                ticksInAir = 0;
                flightExperience *= 0.9991342;
            }
        }
    }

    public void landHard(EntityPlayer player, float distance, float damageMultiplier) {
        if (distance <= 0) {
            return;
        }

        PotionEffect potioneffect = player.getActivePotionEffect(MobEffects.JUMP_BOOST);
        float potion = potioneffect != null ? potioneffect.getAmplifier() + 1 : 0;
        int i = MathHelper.ceil((distance - 8.0F - potion) * damageMultiplier);

        if (i > 0) {
            int j = MathHelper.floor(player.posX);
            int k = MathHelper.floor(player.posY - 0.20000000298023224D);
            int l = MathHelper.floor(player.posZ);

            BlockPos pos = new BlockPos(j, k, l);

            IBlockState state = player.world.getBlockState(pos);
            Block block = state.getBlock();

            if (state.getMaterial() != Material.AIR) {

                player.playSound(getFallSound(i), 1, 1);
                player.attackEntityFrom(DamageSource.FALL, i);

                SoundType soundtype = block.getSoundType(state, player.getEntityWorld(), pos, player);

                player.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.5f, soundtype.getPitch() * 0.75f);
            }
        }
    }

    protected double getHorizontalMotion(Entity e) {
        return (e.motionX * e.motionX)
               + (e.motionZ * e.motionZ);
    }

    protected SoundEvent getFallSound(int distance) {
        return distance > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }

    private void addFlightExperience(EntityPlayer entity) {
        entity.addExperience(1);

        flightExperience += (MAXIMUM_FLIGHT_EXPERIENCE - flightExperience) / 20;
    }

    public void updateFlightStat(EntityPlayer entity, boolean flying) {
        if (!entity.capabilities.isCreativeMode) {
            entity.capabilities.allowFlying = player.getPlayerSpecies().canFly();

            if (entity.capabilities.allowFlying) {
                entity.capabilities.isFlying |= flying;

                isFlying = entity.capabilities.isFlying;

                if (isFlying) {
                    ticksInAir = 0;
                }

            } else {
                entity.capabilities.isFlying = false;
                isFlying = false;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("flightDuration", ticksInAir);
        compound.setFloat("flightExperience", flightExperience);
        compound.setBoolean("isFlying", isFlying);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ticksInAir = compound.getInteger("flightDuration");
        flightExperience = compound.getFloat("flightExperience");
        isFlying = compound.getBoolean("isFlying");
    }

    @Override
    public boolean isFlying() {
        return isFlying;
    }

    @Override
    public float getFlightExperience() {
        return flightExperience / MAXIMUM_FLIGHT_EXPERIENCE;
    }

    @Override
    public float getFlightDuration() {
        return ticksInAir / MAXIMUM_FLIGHT_EXPERIENCE;
    }
}
