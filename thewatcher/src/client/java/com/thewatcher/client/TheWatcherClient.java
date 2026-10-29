package com.thewatcher.client;

import java.util.concurrent.ThreadLocalRandom;

import com.thewatcher.HorrorPayload;
import com.thewatcher.ModSounds;
import com.thewatcher.TheWatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class TheWatcherClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(TheWatcher.STALKER, StalkerRenderer::new);

		// L'overlay del jumpscare viene disegnato sopra tutto il resto dell'HUD
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
				TheWatcher.id("jumpscare"), JumpscareOverlay::extract);

		ClientTickEvents.END_CLIENT_TICK.register(client -> JumpscareOverlay.tick());

		ClientPlayNetworking.registerGlobalReceiver(HorrorPayload.TYPE, (payload, context) -> handle(payload.kind()));
	}

	private static void handle(int kind) {
		switch (kind) {
			case HorrorPayload.JUMPSCARE -> {
				JumpscareOverlay.trigger();
				play(ModSounds.JUMPSCARE, 1.0f, 0.0f);
			}
			case HorrorPayload.HEARTBEAT -> play(ModSounds.HEARTBEAT, 0.9f, 0.0f);
			case HorrorPayload.AMBIENT -> play(ModSounds.AMBIENT, 0.8f, 0.1f);
			case HorrorPayload.STINGER -> play(ModSounds.STINGER, 0.9f, 0.1f);
			default -> {
			}
		}
	}

	/** Riproduce un suono "a tutto schermo" (non posizionale), con una leggera variazione di tono. */
	private static void play(SoundEvent sound, float volume, float pitchVariation) {
		float pitch = 1.0f + (float) ((ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * pitchVariation);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
	}
}
