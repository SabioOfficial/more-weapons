package net.sabio.moreweapons.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.sabio.moreweapons.MoreWeapons;
import net.sabio.moreweapons.entities.SlingshotStone;

import java.util.OptionalDouble;

public class SlingshotItem extends Item implements PolymerItem {
    public static final ResourceKey<Enchantment> MULTISTONE = ResourceKey.create(Registries.ENCHANTMENT, MoreWeapons.id("multistone"));
    public static final ResourceKey<Enchantment> CONTROL = ResourceKey.create(Registries.ENCHANTMENT, MoreWeapons.id("control"));
    public static final ResourceKey<Enchantment> BURNING = ResourceKey.create(Registries.ENCHANTMENT, MoreWeapons.id("burning"));

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

    private record Ammo(ItemStack stack, Item visualItem, double multiplier) {}

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
        ItemStack stack = player.getItemInHand(hand);
        if (!player.hasInfiniteMaterials() && findAmmo(player, stack, level) == null) {
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
        Ammo ammo = findAmmo(player, stack, level);
        if (ammo == null && !infinite) {
            return false;
        }
        if (ammo != null && !infinite) {
            ammo.stack().shrink(1);
        }

        Item visualItem = ammo != null ? ammo.visualItem() : Items.COBBLESTONE;
        double multiplier = ammo != null ? ammo.multiplier() : 1.0;

        Stage stage = Stage.fromTicks(72000 - remainingUseTicks);

        int controlLevel = getLevel(stack, level, CONTROL);
        double controlMultiplier = controlLevel > 0 ? 1.0 + 0.25 * (controlLevel + 1) : 1.0;
        boolean burning = getLevel(stack, level, BURNING) > 0;

        SlingshotStone stone = new SlingshotStone(serverLevel, player, new ItemStack(visualItem), stage.damage * (float) (multiplier * controlMultiplier), stage.knockback * multiplier, stage == Stage.FULL, burning);
        stone.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, stage.velocity, 1.0F);
        serverLevel.addFreshEntity(stone);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.8F + 0.2F * stage.ordinal());

        stack.hurtAndBreak(1, player, player.getUsedItemHand());
        return true;
    }

    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack stack = player.getMainHandItem();
            if (hand == InteractionHand.OFF_HAND && stack.getItem() instanceof SlingshotItem && hasMultistone(stack, level) && blockHardness(player.getOffhandItem(), level).isPresent()) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    private static OptionalDouble blockHardness(ItemStack stack, Level level) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return OptionalDouble.empty();
        }
        Block block = blockItem.getBlock();
        if (!BuiltInRegistries.BLOCK.getKey(block).equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return OptionalDouble.empty();
        }
        float hardness = block.defaultBlockState().getDestroySpeed(level, BlockPos.ZERO);
        return hardness < 0 ? OptionalDouble.empty() : OptionalDouble.of(hardness);
    }

    private static int getLevel(ItemStack stack, Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    private static boolean hasMultistone(ItemStack stack, Level level) {
        var holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(MULTISTONE);
        return holder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(holder.get(), stack) > 0;
    }

    private static Ammo findAmmo(Player player, ItemStack slingshot, Level level) {
        if (hasMultistone(slingshot, level)) {
            ItemStack offhand = player.getOffhandItem();
            OptionalDouble hardness = blockHardness(offhand, level);
            if (hardness.isPresent()) {
                double multiplier = Mth.clamp(hardness.getAsDouble() / 2.0, 0.25, 2.5);
                return new Ammo(offhand, offhand.getItem(), multiplier);
            }
        }
        ItemStack cobblestone = findCobblestone(player);
        if (!cobblestone.isEmpty()) {
            return new Ammo(cobblestone, Items.COBBLESTONE, 1.0);
        }
        return null;
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
