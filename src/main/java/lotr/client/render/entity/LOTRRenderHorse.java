package lotr.client.render.entity;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import lotr.common.LOTRMod;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.npc.LOTRNPCMount;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderHorse extends RenderHorse {
	public static Map<String, ResourceLocation> layeredMountTextures = new HashMap<>();

	public LOTRRenderHorse() {
		super(new ModelHorse(), 0.75f);
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		LOTREntityHorse horse = (LOTREntityHorse) entity;
		boolean fools = LOTRMod.isAprilFools();
		int horseType = horse.getHorseType();
		if (fools) {
			horse.setHorseType(1);
		}
		super.doRender(entity, d, d1, d2, f, f1);
		if (fools) {
			horse.setHorseType(horseType);
		}
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		LOTREntityHorse horse = (LOTREntityHorse) entity;
		ResourceLocation horseSkin = super.getEntityTexture(entity);
		return LOTRRenderHorse.getLayeredMountTexture(horse, horseSkin);
	}

	public static ResourceLocation getLayeredMountTexture(LOTRNPCMount mount, ResourceLocation mountSkin) {
		String skinPath = mountSkin.toString();
		String armorPath = mount.getMountArmorTexture();
		if (armorPath == null || StringUtils.isBlank(armorPath)) {
			return mountSkin;
		}
		Minecraft mc = Minecraft.getMinecraft();
		String path = "lotr_" + skinPath + "_" + armorPath;
		ResourceLocation texture = layeredMountTextures.get(path);
		if (texture == null) {
			texture = new ResourceLocation(path);
			ArrayList<String> layers = new ArrayList<>();
			ITextureObject skinTexture = mc.getTextureManager().getTexture(mountSkin);
			if (skinTexture instanceof LayeredTexture) {
				LayeredTexture skinTextureLayered = (LayeredTexture) skinTexture;
				layers.addAll(skinTextureLayered.layeredTextureNames);
			} else {
				layers.add(skinPath);
			}
			layers.add(armorPath);
			mc.getTextureManager().loadTexture(texture, new LayeredTexture(layers.toArray(new String[0])));
			layeredMountTextures.put(path, texture);
		}
		return texture;
	}
}
