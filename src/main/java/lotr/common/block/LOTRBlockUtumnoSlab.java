package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import lotr.common.LOTRMod;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class LOTRBlockUtumnoSlab extends LOTRBlockUtumnoSlabBase {
	public LOTRBlockUtumnoSlab(boolean flag) {
		super(flag, 6);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		j &= 7;
		if (j == 0) {
			return LOTRMod.utumnoBrick.getIcon(i, 0);
		}
		switch (j) {
		case 1:
			return LOTRMod.utumnoBrick.getIcon(i, 2);
		case 2:
			return LOTRMod.utumnoBrick.getIcon(i, 4);
		case 3:
			return LOTRMod.utumnoPillar.getIcon(i, 0);
		case 4:
			return LOTRMod.utumnoPillar.getIcon(i, 1);
		case 5:
			return LOTRMod.utumnoPillar.getIcon(i, 2);
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
