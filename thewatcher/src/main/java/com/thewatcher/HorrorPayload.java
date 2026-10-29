package com.thewatcher;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Dice al client quale effetto horror riprodurre. */
public record HorrorPayload(int kind) implements CustomPacketPayload {
	public static final int JUMPSCARE = 0;
	public static final int HEARTBEAT = 1;
	public static final int AMBIENT = 2;
	public static final int STINGER = 3;

	public static final CustomPacketPayload.Type<HorrorPayload> TYPE =
			new CustomPacketPayload.Type<>(TheWatcher.id("horror"));

	public static final StreamCodec<RegistryFriendlyByteBuf, HorrorPayload> CODEC =
			ByteBufCodecs.VAR_INT.map(HorrorPayload::new, HorrorPayload::kind).cast();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
