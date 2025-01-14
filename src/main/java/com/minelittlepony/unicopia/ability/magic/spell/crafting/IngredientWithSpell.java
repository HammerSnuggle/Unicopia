package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.GemstoneItem;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class IngredientWithSpell implements Predicate<ItemStack> {
    private Optional<Ingredient> stack = Optional.empty();
    private Optional<SpellType<?>> spell = Optional.empty();

    @Nullable
    private ItemStack[] stacks;

    private IngredientWithSpell() {}

    @Override
    public boolean test(ItemStack t) {
        boolean stackMatch = stack.map(m -> m.test(t)).orElse(true);
        boolean spellMatch = spell.map(m -> GemstoneItem.getSpellKey(t).equals(m)).orElse(true);
        return stackMatch && spellMatch;
    }

    public ItemStack[] getMatchingStacks() {
        if (stacks == null) {
            stacks = stack.stream()
                    .map(Ingredient::getMatchingStacks)
                    .flatMap(Arrays::stream)
                    .map(stack -> spell.map(spell -> GemstoneItem.enchant(stack, spell)).orElse(stack))
                    .toArray(ItemStack[]::new);
        }
        return stacks;
    }

    public void write(PacketByteBuf buf) {
        stack.ifPresentOrElse(i -> {
            buf.writeBoolean(true);
            i.write(buf);
        }, () -> buf.writeBoolean(false));
        spell.ifPresentOrElse(i -> {
            buf.writeBoolean(true);
            buf.writeIdentifier(i.getId());
        }, () -> buf.writeBoolean(false));
    }

    public static IngredientWithSpell fromPacket(PacketByteBuf buf) {
        IngredientWithSpell ingredient = new IngredientWithSpell();

        if (buf.readBoolean()) {
            ingredient.stack = Optional.ofNullable(Ingredient.fromPacket(buf));
        }
        if (buf.readBoolean()) {
            ingredient.spell = SpellType.REGISTRY.getOrEmpty(buf.readIdentifier());
        }

        return ingredient;
    }

    public static IngredientWithSpell fromJson(JsonObject json) {

        IngredientWithSpell ingredient = new IngredientWithSpell();

        if (json.has("item") || json.has("spell")) {
            if (json.has("item")) {
                ingredient.stack = Optional.ofNullable(Ingredient.fromJson(JsonHelper.getObject(json, "item")));
            }
            if (json.has("spell")) {
                ingredient.spell = SpellType.REGISTRY.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "spell")));
            }
        } else {
            ingredient.stack = Optional.ofNullable(Ingredient.fromJson(json));
        }

        return ingredient;
    }
}
