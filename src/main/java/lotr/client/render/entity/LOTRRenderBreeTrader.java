package lotr.client.render.entity;

import lotr.common.entity.npc.LOTREntityBreeMan;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderBreeTrader extends LOTRRenderBreeMan {
	public ResourceLocation traderOutfit;

	public LOTRRenderBreeTrader(String s) {
		traderOutfit = new ResourceLocation("lotr:mob/bree/" + s + ".png");
	}

	@Override
	public int shouldRenderPass(EntityLiving entity, int pass, float f) {
		LOTREntityBreeMan man = (LOTREntityBreeMan) entity;
		if (pass == 1 && man.getEquipmentInSlot(3) == null) {
			setRenderPassModel(outfitModel);
			bindTexture(traderOutfit);
			return 1;
		}
		return super.shouldRenderPass(man, pass, f);
	}
}
