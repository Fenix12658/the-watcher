package com.thewatcher.client;

import java.util.concurrent.ThreadLocalRandom;

import com.thewatcher.TheWatcher;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Immagine a schermo intero con flash, tremolio e dissolvenza finale. */
public final class JumpscareOverlay {
	private static final Identifier[] FACES = {
			TheWatcher.id("textures/gui/jumpscare_1.png"),
			TheWatcher.id("textures/gui/jumpscare_2.png")
	};
	private static final int TEX_W = 640;
	private static final int TEX_H = 360;

	/** Durata totale in tick (20 tick = 1 secondo). */
	private static final int DURATION = 50;
	private static final int FLASH_TICKS = 2;
	private static final int FADE_TICKS = 12;

	private static int ticksLeft = 0;
	private static int face = 0;

	private JumpscareOverlay() {
	}

	public static void trigger() {
		ticksLeft = DURATION;
		face = ThreadLocalRandom.current().nextInt(FACES.length);
	}

	public static void tick() {
		if (ticksLeft > 0) {
			ticksLeft--;
		}
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (ticksLeft <= 0) {
			return;
		}

		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int elapsed = DURATION - ticksLeft;

		// Flash bianco iniziale
		if (elapsed < FLASH_TICKS) {
			graphics.fill(0, 0, width, height, 0xFFFFFFFF);
			return;
		}

		graphics.fill(0, 0, width, height, 0xFF000000);

		// Tremolio: forte all'inizio, poi piu' leggero. L'immagine e' leggermente piu' grande dello schermo.
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int shake = elapsed < 30 ? 14 : 4;
		int offsetX = random.nextInt(-shake, shake + 1);
		int offsetY = random.nextInt(-shake, shake + 1);

		graphics.blit(RenderPipelines.GUI_TEXTURED, FACES[face],
				-shake + offsetX, -shake + offsetY,
				0, 0,
				width + shake * 2, height + shake * 2,
				TEX_W, TEX_H,
				TEX_W, TEX_H);

		// Lampi rossi intermittenti
		if (elapsed % 6 < 2) {
			graphics.fill(0, 0, width, height, 0x40FF0000);
		}

		// Dissolvenza al nero
		if (ticksLeft <= FADE_TICKS) {
			int alpha = 255 - (int) (255.0 * ticksLeft / FADE_TICKS);
			graphics.fill(0, 0, width, height, alpha << 24);
		}
	}
}
