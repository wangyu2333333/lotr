package lotr.client.render.entity;

import org.lwjgl.opengl.GL11;

import lotr.client.model.LOTRModelRabbit;
import lotr.common.entity.animal.LOTREntityRabbit;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderRabbit extends RenderLiving {
	public static LOTRRandomSkins rabbitSkins;

	public LOTRRenderRabbit() {
		super(new LOTRModelRabbit(), 0.3f);
		rabbitSkins = LOTRRandomSkins.loadSkinsList("lotr:mob/rabbit");
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		LOTREntityRabbit rabbit = (LOTREntityRabbit) entity;
		return rabbitSkins.getRandomSkin(rabbit);
	}

	@Override
	public void preRenderCallback(EntityLivingBase entity, float f) {
		GL11.glScalef(0.75f, 0.75f, 0.75f);
	}
}
