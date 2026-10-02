package net.sabio.moreweapons;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.sabio.moreweapons.handlers.ShieldBashHandler;
import net.sabio.moreweapons.registries.ModEntities;
import net.sabio.moreweapons.registries.ModItems;
import net.sabio.moreweapons.registries.ModLoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreWeapons implements ModInitializer {
    public static final String MOD_ID = "moreweapons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModLoot.initialize();
        ModEntities.initialize();
        ShieldBashHandler.register();
        PolymerResourcePackUtils.addModAssets(MOD_ID);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
