package net.sabio.moreweapons.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.sabio.moreweapons.MoreWeapons;
import net.sabio.moreweapons.entities.ThrownDagger;

public class DaggerItem extends Item implements PolymerItem {
    private final Item blade;

    public DaggerItem(Properties properties, Item blade) {
        super(properties);
        this.blade = blade;
    }

    public static Properties properties(ToolMaterial material) {
        double damage = (4.0F + material.attackDamageBonus()) * 0.7F - 1.0;
        double speed = 2.0F - 4.0;

        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(MoreWeapons.id("dagger_reach"), -1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();

        return new Properties()
                .sword(material, 3.0F, -2.4F)
                .durability(Math.round(material.durability() * 0.8F))
                .attributes(attributes);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return blade;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return BuiltInRegistries.ITEM.getKey(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            ThrownDagger thrown = new ThrownDagger(serverLevel, player, stack.copyWithCount(1), player.hasInfiniteMaterials());
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            serverLevel.addFreshEntity(thrown);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 0.6F);

            player.getCooldowns().addCooldown(stack, 10);
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
