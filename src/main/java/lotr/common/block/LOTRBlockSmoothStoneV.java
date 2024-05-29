package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class LOTRBlockSmoothStoneV extends LOTRBlockSmoothStoneBase {
	public LOTRBlockSmoothStoneV() {
		setBrickNames("stone");
		setCreativeTab(CreativeTabs.tabBlock);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		if (j == 0) {
			return Blocks.stone_slab.getIcon(i, 0);
		}
		return super.getIcon(i, j);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
	}
}
