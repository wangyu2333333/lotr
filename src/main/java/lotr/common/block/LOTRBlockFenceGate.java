package lotr.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.util.IIcon;

public class LOTRBlockFenceGate extends BlockFenceGate {
	public Block plankBlock;
	public int plankMeta;

	public LOTRBlockFenceGate(Block block, int meta) {
		plankBlock = block;
		plankMeta = meta;
		setCreativeTab(LOTRCreativeTabs.tabUtil);
		setHardness(2.0f);
		setResistance(5.0f);
		setStepSound(soundTypeWood);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		return plankBlock.getIcon(i, plankMeta);
	}
}
