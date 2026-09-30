package net.sabio.moreweapons.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.sabio.moreweapons.MoreWeapons;
import net.sabio.moreweapons.entities.SlingshotStone;

public class SlingshotItem extends Item implements PolymerItem {
    public enum Stage {
        NONE(1.0F, 0.30, 1.0F),
        MEDIUM(2.0F, 1.00, 1.6F),
        FULL(4.0F, 1.70, 2.2F);

        public final float damage;
        public final double knockback;
        public final float velocity;

        Stage(float damage, double knockback, float velocity) {
            this.damage = damage;
            this.knockback = knockback;
            this.velocity = velocity;
        }

        static Stage fromTicks(int ticks) {
            if (ticks < 2) return NONE;
            if (ticks < 5) return MEDIUM;
            return FULL;
        }
    }

    public SlingshotItem(Properties properties) {
        super(properties.durability(384));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.BOW;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return MoreWeapons.id("slingshot");
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.hasInfiniteMaterials() && findCobblestone(player).isEmpty()) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingUseTicks) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        boolean infinite = player.hasInfiniteMaterials();
        ItemStack ammo = findCobblestone(player);
        if (ammo.isEmpty() && !infinite) {
            return false;
        }
        if (!infinite) {
            ammo.shrink(1);
        }

        Stage stage = Stage.fromTicks(72000 - remainingUseTicks);

        SlingshotStone stone = new SlingshotStone(serverLevel, player, new ItemStack(Items.COBBLESTONE), stage.damage, stage.knockback);
        stone.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, stage.velocity, 1.0F);
        serverLevel.addFreshEntity(stone);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.8F + 0.2F * stage.ordinal());

        stack.hurtAndBreak(1, player, player.getUsedItemHand());
        return true;
    }

    private static ItemStack findCobblestone(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack candidate = inventory.getItem(i);
            if (candidate.is(Items.COBBLESTONE)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }
}
