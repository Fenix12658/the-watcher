package com.thewatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;

public class TheWatcher implements ModInitializer {
	public static final String MOD_ID = "thewatcher";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceKey<EntityType<?>> STALKER_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("stalker"));

	/** L'entità che ti perseguita: si muove solo quando non la guardi. */
	public static final EntityType<StalkerEntity> STALKER = FabricEntityType.Builder
			.createMob(StalkerEntity::new, MobCategory.MONSTER,
					builder -> builder.defaultAttributes(StalkerEntity::createAttributes))
			.sized(0.6f, 1.95f)
			.clientTrackingRange(10)
			.build(STALKER_KEY);

	@Override
	public void onInitialize() {
		ModSounds.initialize();

		Registry.register(BuiltInRegistries.ENTITY_TYPE, STALKER_KEY, STALKER);

		// Pacchetto server -> client che fa partire jumpscare e suoni
		PayloadTypeRegistry.clientboundPlay().register(HorrorPayload.TYPE, HorrorPayload.CODEC);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				HorrorCommands.register(dispatcher));

		ServerTickEvents.END_SERVER_TICK.register(HorrorDirector::tick);

		LOGGER.info("The Watcher e' sveglio. Non voltarti.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
