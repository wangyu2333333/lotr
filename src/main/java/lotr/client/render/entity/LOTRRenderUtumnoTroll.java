package lotr.client.render.entity;

import lotr.client.model.LOTRModelTroll;
import lotr.common.entity.npc.LOTREntityTroll;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderUtumnoTroll extends LOTRRenderTroll {
	public static LOTRRandomSkins utumnoTrollSkins;

	public LOTRRenderUtumnoTroll() {
		utumnoTrollSkins = LOTRRandomSkins.loadSkinsList("lotr:mob/troll/utumno");
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		return utumnoTrollSkins.getRandomSkin((LOTREntityTroll) entity);
	}

	@Override
	public void renderTrollWeapon(EntityLivingBase entity, float f) {
		((LOTRModelTroll) mainModel).renderWoodenClubWithSpikes(0.0625f);
	}
}
