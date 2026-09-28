package net.sabio.moreweapons.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.sabio.moreweapons.MoreWeapons;

public class SpikedShieldItem extends ShieldItem implements PolymerItem {
    public SpikedShieldItem(Properties properties) {
        super(properties.durability(672));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.SHIELD;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return MoreWeapons.id("spiked_shield");
    }

    public boolean isSpiked(ItemStack stack) {
        return stack.getItem() == this;
    }
}
