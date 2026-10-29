package com.thewatcher;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * L'Osservatore. Comportamento stile "non battere le palpebre":
 * <ul>
 *   <li>se lo guardi si blocca e ti fissa;</li>
 *   <li>appena distogli lo sguardo ti corre incontro;</li>
 *   <li>se ti raggiunge (o se lo fissi troppo da vicino) scatta il jumpscare;</li>
 *   <li>se lo fissi da lontano abbastanza a lungo, svanisce.</li>
 * </ul>
 */
public class StalkerEntity extends Monster {
	/** Vita massima: dopo 90 secondi sparisce da solo. */
	private static final int MAX_LIFE_TICKS = 20 * 90;
	/** Secondi di sguardo da lontano dopo i quali svanisce. */
	private static final int VANISH_AFTER_TICKS = 50;
	/** Sguardo da vicino che fa scattare il jumpscare. */
	private static final int STARE_JUMPSCARE_TICKS = 100;

	private int lifeTicks = 0;
	private int lookedAtTicks = 0;

	public StalkerEntity(EntityType<? extends StalkerEntity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
		this.setInvulnerable(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0)
				.add(Attributes.MOVEMENT_SPEED, 0.30)
				.add(Attributes.FOLLOW_RANGE, 64.0);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) {
			return;
		}

		this.lifeTicks++;
		if (this.lifeTicks > MAX_LIFE_TICKS) {
			this.discard();
			return;
		}

		Player nearest = this.level().getNearestPlayer(this, 64.0);

		if (!(nearest instanceof ServerPlayer player) || player.isSpectator()) {
			if (this.lifeTicks > 200) {
				this.discard();
			}

			return;
		}

		this.getLookControl().setLookAt(player);

		double distance = this.distanceTo(player);
		boolean watched = isPlayerLookingAtMe(player);

		// Battito cardiaco quando e' vicino
		if (distance < 30.0 && this.tickCount % 60 == 0) {
			HorrorDirector.send(player, HorrorPayload.HEARTBEAT);
		}

		if (watched) {
			// Ti fissa, immobile
			this.getNavigation().stop();
			this.lookedAtTicks++;

			if (distance > 8.0 && this.lookedAtTicks > VANISH_AFTER_TICKS) {
				this.discard();
			} else if (distance <= 8.0 && this.lookedAtTicks > STARE_JUMPSCARE_TICKS) {
				HorrorDirector.jumpscare(player);
				this.discard();
			}
		} else {
			this.lookedAtTicks = Math.max(0, this.lookedAtTicks - 2);

			if (this.tickCount % 10 == 0 || this.getNavigation().isDone()) {
				this.getNavigation().moveTo(player, 1.25);
			}

			if (distance < 2.2) {
				HorrorDirector.jumpscare(player);
				this.discard();
			}
		}
	}

	/** True se il giocatore ha l'entita' nel campo visivo (circa 50 gradi) e in linea d'aria. */
	public boolean isPlayerLookingAtMe(ServerPlayer player) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toMe = new Vec3(
				this.getX() - player.getX(),
				this.getEyeY() - player.getEyeY(),
				this.getZ() - player.getZ());
		double length = toMe.length();

		if (length < 0.001) {
			return true;
		}

		double dot = look.dot(toMe.scale(1.0 / length));
		return dot > 0.65 && player.hasLineOfSight(this);
	}
}
