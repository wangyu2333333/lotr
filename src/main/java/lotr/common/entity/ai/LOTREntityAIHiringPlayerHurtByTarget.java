package lotr.common.entity.ai;

import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;

public class LOTREntityAIHiringPlayerHurtByTarget extends EntityAITarget {
	public LOTREntityNPC theNPC;
	public EntityLivingBase theTarget;
	public int playerRevengeTimer;

	public LOTREntityAIHiringPlayerHurtByTarget(LOTREntityNPC entity) {
		super(entity, false);
		theNPC = entity;
		setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (!theNPC.hiredNPCInfo.isActive || theNPC.hiredNPCInfo.isHalted()) {
			return false;
		}
		EntityPlayer entityplayer = theNPC.hiredNPCInfo.getHiringPlayer();
		if (entityplayer == null) {
			return false;
		}
		theTarget = entityplayer.getAITarget();
		int i = entityplayer.func_142015_aE();
		if (i == playerRevengeTimer) {
			return false;
		}
		return LOTRMod.canNPCAttackEntity(theNPC, theTarget, true) && isSuitableTarget(theTarget, false);
	}

	@Override
	public void startExecuting() {
		theNPC.setAttackTarget(theTarget);
		EntityPlayer entityplayer = theNPC.hiredNPCInfo.getHiringPlayer();
		if (entityplayer != null) {
			playerRevengeTimer = entityplayer.func_142015_aE();
		}
		super.startExecuting();
	}
}
