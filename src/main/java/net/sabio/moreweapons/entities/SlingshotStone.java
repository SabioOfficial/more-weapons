package net.sabio.moreweapons.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class SlingshotStone extends Snowball {
    private final float damage;
    private final double knockbackMultiplier;
    private final boolean fullCharge;
    private final boolean burning;

    public SlingshotStone(Level level, LivingEntity owner, ItemStack item, float damage, double knockbackMultiplier, boolean fullCharge, boolean burning) {
        super(level, owner, item);
        this.damage = damage;
        this.knockbackMultiplier = knockbackMultiplier;
        this.fullCharge = fullCharge;
        this.burning = burning;
        if (burning) {
            setRemainingFireTicks(20 * 100);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (fullCharge && !isRemoved() && level() instanceof ServerLevel serverLevel) {
            Vec3 v = getDeltaMovement();
            for (int i = 0; i < 4; i++) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        getX() + v.x * i / 4.0,
                        getY() + v.y * i / 4.0,
                        getZ() + v.z * i / 4.0,
                        0,
                        -v.x,
                        -v.y + 0.2,
                        -v.z,
                        1.0
                );
            }
        }
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult result) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity target = result.getEntity();
        Vec3 heading = getDeltaMovement();
        Vec3 before = target.getDeltaMovement();

        DamageSource source = damageSources().thrown(this, getOwner());
        boolean hurt = target.hurtServer(serverLevel, source, damage);

        if (hurt && burning) {
            target.igniteForSeconds(5.0F);
        }

        if (hurt && target instanceof LivingEntity living) {
            living.setDeltaMovement(before);
            living.knockback(0.4 * knockbackMultiplier, -heading.x, -heading.z, source, damage);
        }
    }
}
