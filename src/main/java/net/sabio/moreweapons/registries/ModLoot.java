package net.sabio.moreweapons.registries;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.sabio.moreweapons.items.SlingshotItem;

import java.util.Set;

public class ModLoot {
    private static final Set<ResourceKey<LootTable>> TARGETS = Set.of(
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.WOODLAND_MANSION,
            BuiltInLootTables.PILLAGER_OUTPOST,
            BuiltInLootTables.UNDERWATER_RUIN_BIG,
            BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.BASTION_OTHER,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_CROSSING
    );

    public static void initialize() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, holder) -> {
            if (!source.isBuiltin() || !TARGETS.contains(key)) {
                return;
            }

            Holder<Enchantment> burning = holder.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SlingshotItem.BURNING);
            Holder<Enchantment> control = holder.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SlingshotItem.CONTROL);
            Holder<Enchantment> multistone = holder.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SlingshotItem.MULTISTONE);
            Holder<Enchantment> pouch = holder.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SlingshotItem.POUCH);

            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.15F))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                            .apply(new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(burning, ConstantValue.exactly(1))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                            .apply(new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(control, UniformGenerator.between(1, 5))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                            .apply(new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(multistone, ConstantValue.exactly(1))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                            .apply(new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(pouch, UniformGenerator.between(1, 3)))));
        });
    }
}
