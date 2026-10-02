package net.sabio.moreweapons.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.sabio.moreweapons.registries.ModEntities;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class ThrownDagger extends Snowball implements PolymerEntity {
    private float damage = 1.0F;
    private boolean creativeOnly;
    private boolean lodged;
    private boolean spent;
    private int pickupDelay;
    private int lodgedTicks;
    private final ElementHolder holder = new ElementHolder();
    private final ItemDisplayElement blade = new ItemDisplayElement();
    private Vec3 heading = new Vec3(0, 0, 1);
    private boolean settled;

    public ThrownDagger(EntityType<? extends ThrownDagger> type, Level level) {
        super(type, level);
        blade.setItemDisplayContext(ItemDisplayContext.NONE);
        blade.setScale(new Vector3f(0.8F));
        blade.setInterpolationDuration(2);
        blade.setTeleportDuration(2);
        holder.addElement(blade);
        EntityAttachment.ofTicking(holder, this);
    }

    public ThrownDagger(Level level, LivingEntity owner, ItemStack dagger, boolean creativeOnly) {
        this(ModEntities.THROWN_DAGGER, level);
        this.creativeOnly = creativeOnly;
        this.damage = damageOf(dagger);
        setOwner(owner);
        setItem(dagger);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    public void setItem(ItemStack stack) {
        super.setItem(stack);
        blade.setItem(stack.copy());
    }

    private static float damageOf(ItemStack dagger) {
        float total = 1.0F;
        for (var entry : dagger.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                total += (float) entry.modifier().amount();
            }
        }
        return total;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel)) return;

        updateModel();
        if (!lodged) return;

        if (pickupDelay > 0) pickupDelay--;
        if (level().noCollision(getBoundingBox().inflate(0.06))) {
            lodged = false;
            settled = false;
            setNoGravity(false);
            return;
        }
        if (creativeOnly && ++lodgedTicks > 1200) discard();
    }

    private void updateModel() {
        if (lodged && settled) return;

        Vec3 v = getDeltaMovement();
        if (!lodged && v.lengthSqr() > (spent ? 0.05 : 1.0E-4)) {
            heading = v.normalize();
        }

        blade.setLeftRotation(tipRotation(heading));

        double back = lodged ? 0.1 : 0.0;
        blade.setTranslation(new Vector3f((float) (-heading.x * back), (float) (-heading.y * back), (float) (-heading.z * back)));
        blade.startInterpolation();
        settled = lodged;
    }

    private static Quaternionf tipRotation(Vec3 heading) {
        Vec3 h = heading.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : heading.normalize();
        double horizontal = Math.sqrt(h.x * h.x + h.z * h.z);

        float elevation = (float) Math.atan2(h.y, horizontal);
        float yaw = horizontal > 1.0E-6 ? (float) Math.atan2(h.z, -h.x) : 0.0F;

        return new Quaternionf()
                .rotationY(yaw)
                .rotateZ(Mth.HALF_PI - elevation)
                .rotateZ(-Mth.PI / 4);
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        if (level().isClientSide()) return;
        if (hitResult instanceof EntityHitResult entityHitResult) {
            onHitEntity(entityHitResult);
        } else if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            onHitBlock(blockHitResult);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        Entity target = result.getEntity();
        Entity owner = getOwner();
        DamageSource source = damageSources().thrown(this, owner == null ? this : owner);
        ItemStack weapon = getItem().copy();

        spent = true;
        float dealt = EnchantmentHelper.modifyDamage(serverLevel, weapon, target, source, damage);
        if (target.hurtServer(serverLevel, source, dealt)) {
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, source, weapon);
        }

        setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        playSound(SoundEvents.ARROW_HIT, 1.0F, 1.0F);

        boolean[] broke = {false};
        weapon.hurtAndBreak(1, serverLevel, owner instanceof ServerPlayer sp ? sp : null, _ -> broke[0] = true);
        if (broke[0]) {
            discard();
        } else {
            setItem(weapon);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-4) heading = motion.normalize();
        Vec3 nudge = motion.lengthSqr() > 0 ? motion.normalize().scale(0.05) : Vec3.ZERO;
        setPos(hitResult.getLocation().subtract(nudge));
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
        lodged = true;
        spent = true;
        pickupDelay = 6;
        playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !spent && super.canHitEntity(entity);
    }

    @Override
    public void playerTouch(Player player) {
        if (level().isClientSide() || !lodged || pickupDelay > 0) return;
        boolean taken = creativeOnly ? player.hasInfiniteMaterials() : player.getInventory().add(getItem().copy());
        if (taken) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.3F, 1.2F);
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("Damage", damage);
        output.putBoolean("Lodged", lodged);
        output.putBoolean("Spent", spent);
        output.putBoolean("CreativeOnly", creativeOnly);
        output.putFloat("HeadingX", (float) heading.x);
        output.putFloat("HeadingY", (float) heading.y);
        output.putFloat("HeadingZ", (float) heading.z);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        damage = input.getFloatOr("Damage", 1.0F);
        lodged = input.getBooleanOr("Lodged", false);
        spent = input.getBooleanOr("Spent", false);
        creativeOnly = input.getBooleanOr("CreativeOnly", false);
        heading = new Vec3(input.getFloatOr("HeadingX", 0.0F), input.getFloatOr("HeadingY", 0.0F), input.getFloatOr("HeadingZ", 1.0F));
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityTypes.MARKER;
    }
}
