package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import lotr.common.LOTRMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class LOTRBlockSlabGravel extends LOTRBlockSlabFalling {
	public LOTRBlockSlabGravel(boolean flag) {
		super(flag, Material.sand, 3);
		setHardness(0.6f);
		setStepSound(Block.soundTypeGravel);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		j &= 7;
		switch (j) {
		case 0:
			return Blocks.gravel.getIcon(i, 0);
		case 1:
			return LOTRMod.mordorGravel.getIcon(i, 0);
		case 2:
			return LOTRMod.obsidianGravel.getIcon(i, 0);
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
