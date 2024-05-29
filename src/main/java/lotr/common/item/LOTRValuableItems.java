package lotr.common.item;

import java.util.*;

import lotr.common.recipe.LOTRRecipes;
import net.minecraft.item.*;

public class LOTRValuableItems {
	public static List<ItemStack> toolMaterials = new ArrayList<>();
	public static boolean initTools = false;

	public static boolean canMagpieSteal(ItemStack itemstack) {
		LOTRValuableItems.registerToolMaterials();
		Item item = itemstack.getItem();
		if (item instanceof LOTRItemCoin || item instanceof LOTRItemRing || item instanceof LOTRItemGem) {
			return true;
		}
		for (ItemStack listItem : toolMaterials) {
			if (!LOTRRecipes.checkItemEquals(listItem, itemstack)) {
				continue;
			}
			return true;
		}
		return false;
	}

	public static List<ItemStack> getToolMaterials() {
		LOTRValuableItems.registerToolMaterials();
		return toolMaterials;
	}

	public static void registerToolMaterials() {
		if (!initTools) {
			toolMaterials.clear();
			for (Item.ToolMaterial material : Item.ToolMaterial.values()) {
				ItemStack repair;
				if (material.getHarvestLevel() < 2 || (repair = material.getRepairItemStack()) == null || repair.getItem() == null) {
					continue;
				}
				toolMaterials.add(repair.copy());
			}
			initTools = true;
		}
	}
}
