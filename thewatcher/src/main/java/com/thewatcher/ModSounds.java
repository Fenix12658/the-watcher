package com.thewatcher;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Suoni della mod (i file .ogg sono in assets/thewatcher/sounds, elencati in sounds.json). */
public final class ModSounds {
	public static final SoundEvent JUMPSCARE = register("jumpscare");
	public static final SoundEvent STINGER = register("stinger");
	public static final SoundEvent HEARTBEAT = register("heartbeat");
	public static final SoundEvent AMBIENT = register("ambient");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = TheWatcher.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	/** Chiamato dall'inizializzatore della mod per forzare la registrazione. */
	public static void initialize() {
	}
}
