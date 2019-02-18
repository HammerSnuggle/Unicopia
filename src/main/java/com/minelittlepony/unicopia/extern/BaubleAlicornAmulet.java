package com.minelittlepony.unicopia.extern;

import com.minelittlepony.unicopia.item.ItemAlicornAmulet;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class BaubleAlicornAmulet extends ItemAlicornAmulet implements IBauble {

    public BaubleAlicornAmulet(String domain, String name) {
        super(domain, name);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.AMULET;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        IBaublesItemHandler iplayer = BaublesApi.getBaublesHandler(player);

        for (int slot : getBaubleType(itemstack).getValidSlots()) {
            if (iplayer.getStackInSlot(slot).isEmpty()) {
                iplayer.setStackInSlot(slot, itemstack.copy());
                itemstack.setCount(0);

                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.MAINHAND;
    }
}