package net.sabio.moreweapons.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.sabio.moreweapons.MoreWeapons;

public class ModItemIds {
    public static ResourceKey<Item> create(String name) {
        return ResourceKey.create(Registries.ITEM, MoreWeapons.id(name));
    }

    public static final ResourceKey<Item> SPIKED_SHIELD = create("spiked_shield");
    public static final ResourceKey<Item> SLINGSHOT = create("slingshot");
}
