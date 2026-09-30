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
import net.minecraft.world.item.component.BlocksAttacks;
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

    public static final SlingshotItem SLINGSHOT = (SlingshotItem) register(ModItemIds.SLINGSHOT, SlingshotItem::new, new Item.Properties());

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT) // TODO: fix creative inventory
                .register((creativeTab) -> {
                    creativeTab.accept(ModItems.SPIKED_SHIELD);
                    creativeTab.accept(ModItems.SLINGSHOT);
                });
    }
}
