package com.thewatcher.client;

import com.thewatcher.StalkerEntity;
import com.thewatcher.TheWatcher;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Usa il modello umanoide vanilla con la skin scura in assets/thewatcher/textures/entity/stalker.png. */
public class StalkerRenderer extends MobRenderer<StalkerEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private static final Identifier TEXTURE = TheWatcher.id("textures/entity/stalker.png");

	public StalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<HumanoidRenderState>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState renderState) {
		return TEXTURE;
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}
}
