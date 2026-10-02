package net.sabio.moreweapons.registries;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.sabio.moreweapons.MoreWeapons;
import net.sabio.moreweapons.entities.ThrownDagger;

public class ModEntities {
    private static final ResourceKey<EntityType<?>> THROWN_DAGGER_ID = ResourceKey.create(Registries.ENTITY_TYPE, MoreWeapons.id("thrown_dagger"));

    public static final EntityType<ThrownDagger> THROWN_DAGGER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            THROWN_DAGGER_ID,
            EntityType.Builder.<ThrownDagger>of(ThrownDagger::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(THROWN_DAGGER_ID)
    );

    public static void initialize() {
        PolymerEntityUtils.registerType(THROWN_DAGGER);
    }
}
