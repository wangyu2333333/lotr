package lotr.common.entity.npc;

import lotr.common.*;
import lotr.common.world.spawning.LOTRInvasions;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTREntityGulfHaradWarlord extends LOTREntityGulfHaradWarrior implements LOTRUnitTradeable {
	public LOTREntityGulfHaradWarlord(World world) {
		super(world);
		this.addTargetTasks(false);
		npcCape = LOTRCapes.GULF_HARAD;
	}

	@Override
	public void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(25.0);
	}

	@Override
	public boolean canTradeWith(EntityPlayer entityplayer) {
		return LOTRLevelData.getData(entityplayer).getAlignment(getFaction()) >= 150.0f && isFriendly(entityplayer);
	}

	@Override
	public float getAlignmentBonus() {
		return 5.0f;
	}

	@Override
	public String getSpeechBank(EntityPlayer entityplayer) {
		if (isFriendly(entityplayer)) {
			if (canTradeWith(entityplayer)) {
				return "nearHarad/gulf/warlord/friendly";
			}
			return "nearHarad/gulf/warlord/neutral";
		}
		return "nearHarad/gulf/warrior/hostile";
	}

	@Override
	public LOTRUnitTradeEntries getUnits() {
		return LOTRUnitTradeEntries.GULF_WARLORD;
	}

	@Override
	public LOTRInvasions getWarhorn() {
		return LOTRInvasions.NEAR_HARAD_GULF;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		npcItemsInv.setMeleeWeapon(new ItemStack(LOTRMod.pikeHarad));
		npcItemsInv.setIdleItem(npcItemsInv.getMeleeWeapon());
		setCurrentItemOrArmor(4, null);
		return data;
	}

	@Override
	public void onUnitTrade(EntityPlayer entityplayer) {
		LOTRLevelData.getData(entityplayer).addAchievement(LOTRAchievement.tradeGulfWarlord);
	}

	@Override
	public boolean shouldTraderRespawn() {
		return true;
	}
}
