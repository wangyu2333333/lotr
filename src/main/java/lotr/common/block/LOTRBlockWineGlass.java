package lotr.common.block;

import cpw.mods.fml.relauncher.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class LOTRBlockWineGlass extends LOTRBlockMug {
	public LOTRBlockWineGlass() {
		super(2.5f, 10.0f);
		setStepSound(Block.soundTypeGlass);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		return Blocks.glass.getIcon(i, 0);
	}
}
