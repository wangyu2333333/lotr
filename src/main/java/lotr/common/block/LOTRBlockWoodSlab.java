package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import lotr.common.LOTRMod;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class LOTRBlockWoodSlab extends LOTRBlockSlabBase {
	public LOTRBlockWoodSlab(boolean flag) {
		super(flag, Material.wood, 8);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		return LOTRMod.planks.getIcon(i, j &= 7);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
	}
}
