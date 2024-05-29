package lotr.client.render.entity;

import lotr.client.model.LOTRModelBoar;
import lotr.common.entity.animal.LOTREntityWildBoar;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderWildBoar extends RenderLiving {
	public static ResourceLocation boarSkin = new ResourceLocation("lotr:mob/boar/boar.png");
	public static ResourceLocation saddleTexture = new ResourceLocation("lotr:mob/boar/saddle.png");

	public LOTRRenderWildBoar() {
		super(new LOTRModelBoar(), 0.7f);
		setRenderPassModel(new LOTRModelBoar(0.5f));
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		LOTREntityWildBoar boar = (LOTREntityWildBoar) entity;
		return LOTRRenderHorse.getLayeredMountTexture(boar, boarSkin);
	}

	@Override
	public int shouldRenderPass(EntityLivingBase entity, int pass, float f) {
		if (pass == 0 && ((LOTREntityWildBoar) entity).isMountSaddled()) {
			bindTexture(saddleTexture);
			return 1;
		}
		return super.shouldRenderPass(entity, pass, f);
	}
}
