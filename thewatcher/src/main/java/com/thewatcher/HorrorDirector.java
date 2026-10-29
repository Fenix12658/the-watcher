package com.thewatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Il "regista" dell'horror: una volta al secondo decide, per ogni giocatore,
 * se far partire suoni d'atmosfera, far comparire l'Osservatore o lanciare un jumpscare.
 */
public final class HorrorDirector {
	/** Moltiplicatore dei tempi di attesa per intensita' 1..5 (piu' basso = piu' frequente). */
	private static final double[] WAIT_FACTOR = {0.0, 1.6, 1.0, 0.7, 0.5, 0.3};

	private static final Map<UUID, PlayerState> STATES = new HashMap<>();
	private static int tickCounter = 0;

	private HorrorDirector() {
	}

	private static final class PlayerState {
		boolean enabled = true;
		int intensity = 2;
		int graceSeconds = 45;
		int ambientSeconds = 20 + ThreadLocalRandom.current().nextInt(30);
		int eventSeconds = 70 + ThreadLocalRandom.current().nextInt(60);
	}

	private static PlayerState state(ServerPlayer player) {
		return STATES.computeIfAbsent(player.getUUID(), id -> new PlayerState());
	}

	// ------------------------------------------------------------------ API

	public static void setEnabled(ServerPlayer player, boolean enabled) {
		state(player).enabled = enabled;
	}

	public static void setIntensity(ServerPlayer player, int intensity) {
		state(player).intensity = Math.max(1, Math.min(5, intensity));
	}

	public static void send(ServerPlayer player, int kind) {
		ServerPlayNetworking.send(player, new HorrorPayload(kind));
	}

	/** Jumpscare completo: immagine a schermo intero, urlo e qualche secondo di cecita'. */
	public static void jumpscare(ServerPlayer player) {
		send(player, HorrorPayload.JUMPSCARE);
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80));
	}

	/** Prova a far comparire l'Osservatore a 20-30 blocchi dal giocatore, fuori dalla sua visuale. */
	public static boolean spawnStalker(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();

		if (!level.getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(96.0)).isEmpty()) {
			return false;
		}

		ThreadLocalRandom random = ThreadLocalRandom.current();
		StalkerEntity stalker = new StalkerEntity(TheWatcher.STALKER, level);
		int[] verticalOffsets = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6};

		for (int attempt = 0; attempt < 16; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double distance = 20.0 + random.nextDouble() * 10.0;
			double x = player.getX() + Math.cos(angle) * distance;
			double z = player.getZ() + Math.sin(angle) * distance;

			for (int offset : verticalOffsets) {
				double y = Math.floor(player.getY()) + offset;

				if (!level.hasChunkAt(BlockPos.containing(x, y, z))) {
					break;
				}

				stalker.setPos(x, y, z);

				boolean freeSpace = level.noCollision(stalker);
				boolean hasGround = !level.noCollision(stalker, stalker.getBoundingBox().move(0.0, -0.5, 0.0));

				if (freeSpace && hasGround && !stalker.isPlayerLookingAtMe(player)) {
					level.addFreshEntity(stalker);
					return true;
				}
			}
		}

		return false;
	}

	// ----------------------------------------------------------------- tick

	public static void tick(MinecraftServer server) {
		tickCounter++;

		if (tickCounter % 20 != 0) {
			return;
		}

		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		STATES.keySet().removeIf(id -> players.stream().noneMatch(p -> p.getUUID().equals(id)));

		for (ServerPlayer player : players) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		PlayerState state = state(player);

		if (!state.enabled || player.isSpectator() || !player.isAlive()) {
			return;
		}

		if (player.level().dimension() != Level.OVERWORLD) {
			return;
		}

		if (state.graceSeconds > 0) {
			state.graceSeconds--;
			return;
		}

		ThreadLocalRandom random = ThreadLocalRandom.current();

		state.ambientSeconds--;
		if (state.ambientSeconds <= 0) {
			send(player, random.nextInt(100) < 70 ? HorrorPayload.AMBIENT : HorrorPayload.HEARTBEAT);
			state.ambientSeconds = scaled(25, 70, state.intensity);
		}

		state.eventSeconds--;
		if (state.eventSeconds <= 0) {
			runEvent(player, state);
			state.eventSeconds = scaled(80, 220, state.intensity);
		}
	}

	private static void runEvent(ServerPlayer player, PlayerState state) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		ServerLevel level = (ServerLevel) player.level();
		boolean inTheDark = level.getMaxLocalRawBrightness(player.blockPosition()) <= 4;
		int roll = random.nextInt(100);

		if (roll < 55) {
			if (!spawnStalker(player)) {
				send(player, HorrorPayload.STINGER);
			}
		} else if (roll < 85) {
			send(player, HorrorPayload.STINGER);
		} else if (inTheDark) {
			// Solo se sei al buio: jumpscare a sorpresa
			jumpscare(player);
		} else {
			send(player, HorrorPayload.HEARTBEAT);
		}
	}

	private static int scaled(int minSeconds, int maxSeconds, int intensity) {
		int base = minSeconds + ThreadLocalRandom.current().nextInt(maxSeconds - minSeconds + 1);
		return Math.max(5, (int) Math.round(base * WAIT_FACTOR[intensity]));
	}
}
