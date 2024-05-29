package lotr.common.entity.npc;

import com.google.common.base.Predicate;

import lotr.common.*;
import lotr.common.entity.ai.*;
import lotr.common.inventory.LOTRInventoryNPC;
import lotr.common.item.*;
import lotr.common.quest.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class LOTREntityRuffianSpy extends LOTREntityBreeRuffian implements IBandit {
	public LOTRInventoryNPC ruffianInventory = IBandit.Helper.createInv(this);
	public EntityPlayer playerToRob;

	public LOTREntityRuffianSpy(World world) {
		super(world);
		questInfo.setBountyHelpPredicate(new Predicate<EntityPlayer>() {

			@Override
			public boolean apply(EntityPlayer player) {
				ItemStack itemstack = player.getHeldItem();
				if (LOTRItemCoin.getStackValue(itemstack, true) > 0) {
					return true;
				}
				if (itemstack != null) {
					Item item = itemstack.getItem();
					return item == Items.gold_ingot || item == LOTRMod.silver || item instanceof LOTRItemGem || item instanceof LOTRItemRing;
				}
				return false;
			}
		});
		questInfo.setBountyHelpConsumer(new Predicate<EntityPlayer>() {

			@Override
			public boolean apply(EntityPlayer player) {
				ItemStack itemstack;
				if (!player.capabilities.isCreativeMode && (itemstack = player.getHeldItem()) != null) {
					--itemstack.stackSize;
					if (itemstack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
				}
				LOTREntityRuffianSpy.this.playTradeSound();
				return true;
			}
		});
		questInfo.setActiveBountySelector(new MiniQuestSelector.BountyActiveAnyFaction());
	}

	@Override
	public int addBreeAttackAI(int prio) {
		tasks.addTask(prio, new LOTREntityAIBanditSteal(this, 1.6));
		prio++;
		tasks.addTask(prio, new LOTREntityAIAttackOnCollide(this, 1.4, false));
		prio++;
		tasks.addTask(prio, new LOTREntityAIBanditFlee(this, 1.4));
		return prio;
	}

	@Override
	public boolean canTargetPlayerForTheft(EntityPlayer player) {
		return player == playerToRob || canRuffianTarget(player);
	}

	@Override
	public LOTRMiniQuest createMiniQuest() {
		return LOTRMiniQuestFactory.RUFFIAN_SPY.createQuest(this);
	}

	@Override
	public LOTREntityNPC getBanditAsNPC() {
		return this;
	}

	@Override
	public LOTRInventoryNPC getBanditInventory() {
		return ruffianInventory;
	}

	@Override
	public LOTRMiniQuestFactory getBountyHelpSpeechDir() {
		return LOTRMiniQuestFactory.RUFFIAN_SPY;
	}

	@Override
	public LOTRAchievement getKillAchievement() {
		return LOTRAchievement.killRuffianSpy;
	}

	@Override
	public int getMaxThefts() {
		return 1;
	}

	@Override
	public IChatComponent getTheftChatMsg(EntityPlayer player) {
		return new ChatComponentTranslation("chat.lotr.ruffianSteal");
	}

	@Override
	public String getTheftSpeechBank(EntityPlayer player) {
		return "bree/ruffian/hostile";
	}

	@Override
	public void onDeath(DamageSource damagesource) {
		super.onDeath(damagesource);
		if (!worldObj.isRemote) {
			ruffianInventory.dropAllItems();
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		ruffianInventory.readFromNBT(nbt);
	}

	@Override
	public void setAttackTarget(EntityLivingBase target, boolean speak) {
		if (target instanceof EntityPlayer && !((EntityPlayer) target).capabilities.isCreativeMode) {
			playerToRob = (EntityPlayer) target;
		}
		super.setAttackTarget(target, speak);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		ruffianInventory.writeToNBT(nbt);
	}

}
