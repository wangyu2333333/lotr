package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import lotr.common.fac.LOTRFaction;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class LOTRBlockGulfTable extends LOTRBlockCraftingTable {
	@SideOnly(value = Side.CLIENT)
	public IIcon[] tableIcons;

	public LOTRBlockGulfTable() {
		super(Material.rock, LOTRFaction.NEAR_HARAD, 57);
		setStepSound(Block.soundTypeStone);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		if (i == 1) {
			return tableIcons[1];
		}
		if (i == 0) {
			return Blocks.sandstone.getIcon(0, 0);
		}
		return tableIcons[0];
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
		tableIcons = new IIcon[2];
		tableIcons[0] = iconregister.registerIcon(getTextureName() + "_side");
		tableIcons[1] = iconregister.registerIcon(getTextureName() + "_top");
	}
}
