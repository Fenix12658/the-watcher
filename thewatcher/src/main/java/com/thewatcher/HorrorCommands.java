package com.thewatcher;

import java.util.function.Consumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comandi di prova. Agiscono sempre e solo su chi li esegue,
 * cosi' nessuno puo' spaventare gli altri giocatori di un server.
 */
public final class HorrorCommands {
	private HorrorCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("watcher")
				.then(Commands.literal("jumpscare")
						.executes(ctx -> run(ctx, HorrorDirector::jumpscare, "Jumpscare!")))
				.then(Commands.literal("stinger")
						.executes(ctx -> run(ctx, p -> HorrorDirector.send(p, HorrorPayload.STINGER), "Accordo dissonante.")))
				.then(Commands.literal("heartbeat")
						.executes(ctx -> run(ctx, p -> HorrorDirector.send(p, HorrorPayload.HEARTBEAT), "Battito cardiaco.")))
				.then(Commands.literal("ambient")
						.executes(ctx -> run(ctx, p -> HorrorDirector.send(p, HorrorPayload.AMBIENT), "Sussurri nel buio.")))
				.then(Commands.literal("stalker")
						.executes(ctx -> run(ctx, p -> {
							boolean ok = HorrorDirector.spawnStalker(p);
							p.sendSystemMessage(Component.literal(ok
									? "Qualcosa ti sta osservando..."
									: "Nessun posto adatto qui (c'e' gia' un Osservatore o manca spazio). Riprova."));
						}, null)))
				.then(Commands.literal("on")
						.executes(ctx -> run(ctx, p -> HorrorDirector.setEnabled(p, true), "Eventi horror attivati.")))
				.then(Commands.literal("off")
						.executes(ctx -> run(ctx, p -> HorrorDirector.setEnabled(p, false), "Eventi horror disattivati.")))
				.then(Commands.literal("intensity")
						.then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
								.executes(ctx -> {
									int level = IntegerArgumentType.getInteger(ctx, "level");
									return run(ctx, p -> HorrorDirector.setIntensity(p, level),
											"Intensita' impostata a " + level + " (1 = rara, 5 = incubo).");
								}))));
	}

	private static int run(CommandContext<CommandSourceStack> ctx, Consumer<ServerPlayer> action, String message) {
		ServerPlayer player = ctx.getSource().getPlayer();

		if (player == null) {
			ctx.getSource().sendFailure(Component.literal("Questo comando va eseguito da un giocatore."));
			return 0;
		}

		action.accept(player);

		if (message != null) {
			ctx.getSource().sendSuccess(() -> Component.literal(message), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
