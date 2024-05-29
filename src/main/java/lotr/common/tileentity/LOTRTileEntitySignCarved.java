package lotr.common.tileentity;

import cpw.mods.fml.relauncher.*;
import lotr.common.block.LOTRBlockSignCarved;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class LOTRTileEntitySignCarved extends LOTRTileEntitySign {
	@Override
	@SideOnly(value = Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 1600.0;
	}

	@Override
	public int getNumLines() {
		return 8;
	}

	public IIcon getOnBlockIcon() {
		World world = getWorldObj();
		Block block = getBlockType();
		if (block instanceof LOTRBlockSignCarved) {
			LOTRBlockSignCarved signBlock = (LOTRBlockSignCarved) block;
			int meta = getBlockMetadata();
			int i = xCoord;
			int j = yCoord;
			int k = zCoord;
			int onSide = meta;
			return signBlock.getOnBlockIcon(world, i, j, k, onSide);
		}
		return Blocks.stone.getIcon(0, 0);
	}
}
