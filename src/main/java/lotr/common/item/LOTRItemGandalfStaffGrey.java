package lotr.common.item;

import lotr.common.LOTRCreativeTabs;
import net.minecraft.item.Item;

public class LOTRItemGandalfStaffGrey extends LOTRItemSword implements LOTRStoryItem {
	public LOTRItemGandalfStaffGrey() {
		super(Item.ToolMaterial.WOOD);
		setMaxDamage(1000);
		setCreativeTab(LOTRCreativeTabs.tabStory);
		lotrWeaponDamage = 4.0f;
	}
}
