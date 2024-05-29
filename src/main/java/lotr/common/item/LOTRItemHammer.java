package lotr.common.item;

import net.minecraft.item.*;

public class LOTRItemHammer extends LOTRItemSword {
	public LOTRItemHammer(Item.ToolMaterial material) {
		super(material);
	}

	public LOTRItemHammer(LOTRMaterial material) {
		this(material.toToolMaterial());
		lotrWeaponDamage += 2.0f;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack) {
		return EnumAction.none;
	}
}
