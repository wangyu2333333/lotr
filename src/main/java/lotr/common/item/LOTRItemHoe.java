package lotr.common.item;

import lotr.common.LOTRCreativeTabs;
import net.minecraft.item.*;

public class LOTRItemHoe extends ItemHoe {
	public LOTRItemHoe(Item.ToolMaterial material) {
		super(material);
		setCreativeTab(LOTRCreativeTabs.tabTools);
	}

	public LOTRItemHoe(LOTRMaterial material) {
		this(material.toToolMaterial());
	}
}
