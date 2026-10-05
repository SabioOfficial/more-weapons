package net.sabio.moreweapons.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.sabio.moreweapons.MoreWeapons;

public class ModItemIds {
    public static ResourceKey<Item> create(String name) {
        return ResourceKey.create(Registries.ITEM, MoreWeapons.id(name));
    }

    /** Defensive **/

    public static final ResourceKey<Item> SPIKED_SHIELD = create("spiked_shield");

    /** Ranged **/

    public static final ResourceKey<Item> SLINGSHOT = create("slingshot");

    /** Melee **/

    /* Daggers */

    public static final ResourceKey<Item> WOODEN_DAGGER = create("wooden_dagger");
    public static final ResourceKey<Item> GOLDEN_DAGGER = create("golden_dagger");
    public static final ResourceKey<Item> STONE_DAGGER = create("stone_dagger");
    public static final ResourceKey<Item> COPPER_DAGGER = create("copper_dagger");
    public static final ResourceKey<Item> IRON_DAGGER = create("iron_dagger");
    public static final ResourceKey<Item> DIAMOND_DAGGER = create("diamond_dagger");
    public static final ResourceKey<Item> NETHERITE_DAGGER = create("netherite_dagger");

    /* Scythe */

    public static final ResourceKey<Item> WOODEN_SCYTHE = create("wooden_scythe");
    public static final ResourceKey<Item> GOLDEN_SCYTHE = create("golden_scythe");
    public static final ResourceKey<Item> STONE_SCYTHE = create("stone_scythe");
    public static final ResourceKey<Item> COPPER_SCYTHE = create("copper_scythe");
    public static final ResourceKey<Item> IRON_SCYTHE = create("iron_scythe");
    public static final ResourceKey<Item> DIAMOND_SCYTHE = create("diamond_scythe");
    public static final ResourceKey<Item> NETHERITE_SCYTHE = create("netherite_scythe");
}
