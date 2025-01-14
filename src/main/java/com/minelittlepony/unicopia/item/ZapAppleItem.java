package com.minelittlepony.unicopia.item;

import static com.minelittlepony.unicopia.item.toxin.Toxin.INNERT;

import java.util.Optional;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.toxin.Ailment;
import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import com.minelittlepony.unicopia.item.toxin.Toxicity;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ZapAppleItem extends Item implements ChameleonItem, ToxicHolder {
    private static final Optional<Toxic> TOXIC = Optional.of(new Toxic.Builder(Ailment.of(Toxicity.SEVERE, INNERT)).build("zap"));
    private static final Optional<Toxic> HIDDEN_TOXIC = Optional.of(new Toxic.Builder(Ailment.of(Toxicity.SAFE, INNERT)).build("zap_hidden"));

    public ZapAppleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        Optional<Entity> entity = RayTraceHelper.doTrace(player, 5, 1, EntityPredicates.CAN_COLLIDE.and(e -> canFeedTo(stack, e))).getEntity();

        if (entity.isPresent()) {
            return onFedTo(stack, player, entity.get());
        }

        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World w, LivingEntity player) {
        stack = super.finishUsing(stack, w, player);

        player.damage(MagicalDamageSource.ZAP_APPLE, 120);

        if (w instanceof ServerWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(w);
            lightning.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());

            player.onStruckByLightning((ServerWorld)w, lightning);

            if (player instanceof PlayerEntity) {
                UCriteria.EAT_TRICK_APPLE.trigger((PlayerEntity)player);
            }
        }

        player.emitGameEvent(GameEvent.LIGHTNING_STRIKE);
        ParticleUtils.spawnParticle(w, UParticles.LIGHTNING_BOLT, player.getPos(), Vec3d.ZERO);

        return stack;
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof VillagerEntity
                || e instanceof CreeperEntity
                || e instanceof PigEntity;
    }

    public TypedActionResult<ItemStack> onFedTo(ItemStack stack, PlayerEntity player, Entity e) {

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(e.world);
        lightning.refreshPositionAfterTeleport(e.getX(), e.getY(), e.getZ());
        lightning.setCosmetic(true);
        if (player instanceof ServerPlayerEntity) {
            lightning.setChanneler((ServerPlayerEntity)player);
        }

        if (e.world instanceof ServerWorld) {
            e.onStruckByLightning((ServerWorld)e.world, lightning);
            UCriteria.FEED_TRICK_APPLE.trigger(player);
        }
        player.world.spawnEntity(lightning);

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Override
    public void appendStacks(ItemGroup tab, DefaultedList<ItemStack> items) {
        super.appendStacks(tab, items);
        if (isIn(tab)) {
            Unicopia.SIDE.getPony().map(Pony::getWorld)
                    .stream()
                    .flatMap(world -> Registries.valuesForTag(world, UTags.APPLES))
                    .filter(a -> a != this).forEach(item -> {
                ItemStack stack = new ItemStack(this);
                stack.getOrCreateNbt().putString("appearance", Registry.ITEM.getId(item).toString());
                items.add(stack);
            });
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        return hasAppearance(stack) ? getAppearanceStack(stack).getName() : super.getName(stack);
    }

    @Override
    public Optional<Toxic> getToxic(ItemStack stack) {
        return hasAppearance(stack) ? TOXIC : HIDDEN_TOXIC;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        if (hasAppearance(stack)) {
            return Rarity.EPIC;
        }

        return Rarity.RARE;
    }
}
