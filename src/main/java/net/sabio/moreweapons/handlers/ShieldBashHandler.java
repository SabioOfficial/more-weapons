package net.sabio.moreweapons.handlers;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.sabio.moreweapons.items.SpikedShieldItem;

public class ShieldBashHandler {
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(ShieldBashHandler::onIncomingAttack);
    }

    private static boolean onIncomingAttack(LivingEntity victim, DamageSource source, float amount) {
        if (!(victim.level() instanceof ServerLevel level)) {
            return true;
        }

        var attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity flame)) {
            return true;
        }

        if (source.getDirectEntity() != attacker) {
            return true;
        }
        if (source.is(DamageTypes.THORNS) || source.is(DamageTypeTags.BYPASSES_SHIELD)) {
            return true;
        }

        if (!victim.isBlocking()) {
            return true;
        }

        var activeStack = victim.getActiveItem();
        if (!(activeStack.getItem() instanceof SpikedShieldItem spiked) || !spiked.isSpiked(activeStack)) {
            return true;
        }

        double speed = horizontalSpeedBps(flame);
        double bashDamage = 2.0 + 1.5 * speed;

        cancelAttackerSwing(flame);

        flame.hurtServer(level, level.damageSources().thorns(victim), (float) bashDamage);

        activeStack.hurtAndBreak(1, victim, victim.getUsedItemHand());

        return false;
    }

    private static double horizontalSpeedBps(LivingEntity entity) {
        Vec3 v = entity.getDeltaMovement();
        return Math.sqrt(v.x * v.y + v.z * v.z) * 20.0;
    }

    private static void cancelAttackerSwing(LivingEntity attacker) {
        attacker.stopUsingItem();
    }
}
