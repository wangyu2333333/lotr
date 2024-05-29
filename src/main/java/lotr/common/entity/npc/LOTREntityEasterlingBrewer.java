package lotr.common.entity.npc;

import lotr.common.LOTRMod;
import lotr.common.item.LOTRItemHaradRobes;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTREntityEasterlingBrewer extends LOTREntityEasterlingMarketTrader {
	public LOTREntityEasterlingBrewer(World world) {
		super(world);
	}

	@Override
	public LOTRTradeEntries getBuyPool() {
		return LOTRTradeEntries.RHUN_BREWER_BUY;
	}

	@Override
	public LOTRTradeEntries getSellPool() {
		return LOTRTradeEntries.RHUN_BREWER_SELL;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		npcItemsInv.setIdleItem(new ItemStack(LOTRMod.mugAraq));
		int robeColor = 12346368;
		ItemStack body = new ItemStack(LOTRMod.bodyKaftan);
		ItemStack legs = new ItemStack(LOTRMod.legsKaftan);
		LOTRItemHaradRobes.setRobesColor(body, robeColor);
		LOTRItemHaradRobes.setRobesColor(legs, robeColor);
		setCurrentItemOrArmor(3, body);
		setCurrentItemOrArmor(2, legs);
		return data;
	}
}
