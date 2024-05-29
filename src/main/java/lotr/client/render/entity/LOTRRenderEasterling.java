package lotr.client.render.entity;

import lotr.client.model.LOTRModelHuman;
import lotr.common.entity.npc.LOTREntityEasterling;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderEasterling extends LOTRRenderBiped {
	public static LOTRRandomSkins easterlingSkinsMale;
	public static LOTRRandomSkins easterlingSkinsFemale;
	public ModelBiped outfitModel = new LOTRModelHuman(0.6f, false);

	public LOTRRenderEasterling() {
		super(new LOTRModelHuman(), 0.5f);
		setRenderPassModel(outfitModel);
		easterlingSkinsMale = LOTRRandomSkins.loadSkinsList("lotr:mob/rhun/easterling_male");
		easterlingSkinsFemale = LOTRRandomSkins.loadSkinsList("lotr:mob/rhun/easterling_female");
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		LOTREntityEasterling easterling = (LOTREntityEasterling) entity;
		if (easterling.familyInfo.isMale()) {
			return easterlingSkinsMale.getRandomSkin(easterling);
		}
		return easterlingSkinsFemale.getRandomSkin(easterling);
	}
}
