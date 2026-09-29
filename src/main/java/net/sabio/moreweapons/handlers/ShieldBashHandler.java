package net.sabio.moreweapons.handlers;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.sabio.moreweapons.items.SpikedShieldItem;

import java.util.*;

public class ShieldBashHandler {
    private static final Map<UUID, ArrayDeque<Vec3>> HISTORY = new HashMap<>();
    private static final Map<UUID, Double> PLAYER_BPS = new HashMap<>();

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(ShieldBashHandler::onIncomingAttack);
        ServerTickEvents.END_SERVER_TICK.register(ShieldBashHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            online.add(uuid);

            ArrayDeque<Vec3> history = HISTORY.computeIfAbsent(uuid, k -> new ArrayDeque<>());
            history.addLast(player.position());
            while (history.size() > 6) {
                history.removeFirst();
            }
            if (history.size() > 1) {
                Vec3 oldest = history.peekFirst();
                Vec3 newest = history.peekLast();
                double dx = newest.x - oldest.x;
                double dz = newest.z - oldest.z;
                double seconds = (history.size() - 1) / 20.0;
                PLAYER_BPS.put(uuid, Math.sqrt(dx * dx + dz * dz) / seconds);
            }
        }
        HISTORY.keySet().retainAll(online);
        PLAYER_BPS.keySet().retainAll(online);
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

        if (!isInBlockingArc(victim, source)) {
            return true;
        }

        double speed = horizontalSpeedBps(flame);
        double bashDamage = bashDamage(speed);

        cancelAttackerSwing(flame);

        flame.hurtServer(level, level.damageSources().thorns(victim), (float) bashDamage);

        activeStack.hurtAndBreak(1, victim, victim.getUsedItemHand());

        return false;
    }

    private static boolean isInBlockingArc(LivingEntity victim, DamageSource source) {
        Vec3 sourcePos = source.getSourcePosition();
        if (sourcePos == null) {
            return false;
        }

        Vec3 view = victim.getViewVector(1.0F);
        Vec3 facing = new Vec3(view.x, 0.0, view.z).normalize();

        Vec3 toSource = sourcePos.subtract(victim.position());
        Vec3 direction = new Vec3(toSource.x, 0.0, toSource.z).normalize();

        return direction.dot(facing) >= Math.cos(Math.toRadians(90.0));
    }

    private static double bashDamage(double bps) {
        return 2.0 + 1.5 * bps;
    }

    private static double horizontalSpeedBps(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return PLAYER_BPS.getOrDefault(player.getUUID(), 0.0);
        }
        return deltaBps(entity);
    }

    private static double deltaBps(LivingEntity entity) {
        Vec3 v = entity.getDeltaMovement();
        return Math.sqrt(v.x * v.x + v.z * v.z) * 20.0;
    }

    private static void cancelAttackerSwing(LivingEntity attacker) {
        attacker.stopUsingItem();
    }
}
