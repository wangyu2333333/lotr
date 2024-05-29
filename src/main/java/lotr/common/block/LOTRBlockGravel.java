package lotr.common.block;

import lotr.common.LOTRCreativeTabs;
import net.minecraft.block.*;

public class LOTRBlockGravel extends BlockGravel {
	public LOTRBlockGravel() {
		setCreativeTab(LOTRCreativeTabs.tabBlock);
		setHardness(0.6f);
		setStepSound(Block.soundTypeGravel);
	}
}
