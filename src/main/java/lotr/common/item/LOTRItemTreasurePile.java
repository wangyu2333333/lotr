package lotr.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class LOTRItemTreasurePile extends ItemBlock {
	public LOTRItemTreasurePile(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}
}
