package net.sabio.moreweapons.registries;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.sabio.moreweapons.items.DaggerItem;
import net.sabio.moreweapons.items.SlingshotItem;
import net.sabio.moreweapons.items.SpikedShieldItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ModItems {
    public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        Item item = itemFactory.apply(settings.setId(itemKey));

        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    /** Defensive **/

    public static final SpikedShieldItem SPIKED_SHIELD = (SpikedShieldItem) register(
            ModItemIds.SPIKED_SHIELD,
            SpikedShieldItem::new,
            new Item.Properties()
                    .equippableUnswappable(EquipmentSlot.OFFHAND)
                    .delayedComponent(DataComponents.BLOCKS_ATTACKS, (context) -> new BlocksAttacks(0.25F, 1.0F,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                            Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)))
                    .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
    );

    /** Ranged **/

    public static final SlingshotItem SLINGSHOT = (SlingshotItem) register(ModItemIds.SLINGSHOT, SlingshotItem::new, new Item.Properties().enchantable(1));

    /** Melee **/

    /* Daggers */

    public static final Item WOODEN_DAGGER = register(ModItemIds.WOODEN_DAGGER, properties -> new DaggerItem(properties, Items.WOODEN_SWORD), DaggerItem.properties(ToolMaterial.WOOD));
    public static final Item GOLDEN_DAGGER = register(ModItemIds.GOLDEN_DAGGER, properties -> new DaggerItem(properties, Items.GOLDEN_SWORD), DaggerItem.properties(ToolMaterial.GOLD));
    public static final Item STONE_DAGGER = register(ModItemIds.STONE_DAGGER, properties -> new DaggerItem(properties, Items.STONE_SWORD), DaggerItem.properties(ToolMaterial.STONE));
    public static final Item COPPER_DAGGER = register(ModItemIds.COPPER_DAGGER, properties -> new DaggerItem(properties, Items.COPPER_SWORD), DaggerItem.properties(ToolMaterial.COPPER));
    public static final Item IRON_DAGGER = register(ModItemIds.IRON_DAGGER, properties -> new DaggerItem(properties, Items.IRON_SWORD), DaggerItem.properties(ToolMaterial.IRON));
    public static final Item DIAMOND_DAGGER = register(ModItemIds.DIAMOND_DAGGER, properties -> new DaggerItem(properties, Items.DIAMOND_SWORD), DaggerItem.properties(ToolMaterial.DIAMOND));
    public static final Item NETHERITE_DAGGER = register(ModItemIds.NETHERITE_DAGGER, properties -> new DaggerItem(properties, Items.NETHERITE_SWORD), DaggerItem.properties(ToolMaterial.NETHERITE));

    private static final List<Item> DAGGERS = List.of(WOODEN_DAGGER, GOLDEN_DAGGER, STONE_DAGGER, COPPER_DAGGER, IRON_DAGGER, DIAMOND_DAGGER, NETHERITE_DAGGER);

    public static void initialize() {
        SlingshotItem.registerEvents();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT) // TODO: fix creative inventory
                .register((creativeTab) -> {
                    creativeTab.accept(ModItems.SPIKED_SHIELD);
                    creativeTab.accept(ModItems.SLINGSHOT);
                    for (Item dagger : DAGGERS) creativeTab.accept(dagger);
                });
    }
}
