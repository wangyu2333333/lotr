package lotr.client.render.entity;

import java.util.HashMap;

import lotr.client.LOTRTextures;
import lotr.client.model.LOTRModelEnt;
import lotr.common.entity.npc.*;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderEnt extends RenderLiving {
	public static HashMap entTextures = new HashMap();
	public LOTRModelEnt eyesModel = new LOTRModelEnt(0.05f);

	public LOTRRenderEnt() {
		super(new LOTRModelEnt(), 0.5f);
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		int treeType = ((LOTREntityEnt) entity).getTreeType();
		String s = "lotr:mob/ent/" + LOTREntityTree.TYPES[treeType] + ".png";
		ResourceLocation r = (ResourceLocation) entTextures.get(treeType);
		if (r == null) {
			r = new ResourceLocation(s);
			entTextures.put(treeType, r);
		}
		return r;
	}

	@Override
	public void renderModel(EntityLivingBase entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.renderModel(entity, f, f1, f2, f3, f4, f5);
		ResourceLocation eyes = LOTRTextures.getEyesTexture(getEntityTexture(entity), new int[][] { { 15, 23 }, { 22, 23 } }, 3, 2);
		LOTRGlowingEyes.renderGlowingEyes(entity, eyes, eyesModel, f, f1, f2, f3, f4, f5);
	}
}
