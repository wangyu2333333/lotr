package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import lotr.common.LOTRMod;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class LOTRBlockSlab13 extends LOTRBlockSlabBase {
	public LOTRBlockSlab13(boolean flag) {
		super(flag, Material.rock, 8);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		j &= 7;
		if (j == 0) {
			return LOTRMod.brick6.getIcon(i, 3);
		}
		switch (j) {
		case 1:
			return LOTRMod.brick6.getIcon(i, 4);
		case 2:
			return LOTRMod.brick6.getIcon(i, 6);
		case 3:
			return LOTRMod.brick6.getIcon(i, 7);
		case 4:
			return LOTRMod.pillar2.getIcon(i, 10);
		case 5:
			return LOTRMod.pillar2.getIcon(i, 11);
		case 6:
			return LOTRMod.pillar2.getIcon(i, 12);
		case 7:
			return LOTRMod.pillar2.getIcon(i, 13);
		default:
			break;
		}
		return super.getIcon(i, j);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
	}
}
