package lotr.common.entity.npc;

import lotr.common.LOTRMod;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTREntityGulfButcher extends LOTREntityGulfTrader {
	public LOTREntityGulfButcher(World world) {
		super(world);
	}

	@Override
	public LOTRTradeEntries getBuyPool() {
		return LOTRTradeEntries.HARAD_BUTCHER_BUY;
	}

	@Override
	public LOTRTradeEntries getSellPool() {
		return LOTRTradeEntries.HARAD_BUTCHER_SELL;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		npcItemsInv.setIdleItem(new ItemStack(LOTRMod.rabbitRaw));
		return data;
	}

	@Override
	public void setupNPCGender() {
		familyInfo.setMale(true);
	}
}
