package lotr.common.entity.npc;

import lotr.common.LOTRMod;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTREntityBreeHobbitFlorist extends LOTREntityBreeHobbitTrader {
	public LOTREntityBreeHobbitFlorist(World world) {
		super(world);
	}

	@Override
	public LOTRTradeEntries getBuyPool() {
		return LOTRTradeEntries.BREE_FLORIST_BUY;
	}

	@Override
	public LOTRTradeEntries getSellPool() {
		return LOTRTradeEntries.BREE_FLORIST_SELL;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		npcItemsInv.setIdleItem(new ItemStack(LOTRMod.lavender, 1, 0));
		return data;
	}
}
