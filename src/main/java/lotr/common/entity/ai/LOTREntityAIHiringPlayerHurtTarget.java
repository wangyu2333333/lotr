package lotr.common.entity.ai;

import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;

public class LOTREntityAIHiringPlayerHurtTarget extends EntityAITarget {
	public LOTREntityNPC theNPC;
	public EntityLivingBase theTarget;
	public int playerLastAttackerTime;

	public LOTREntityAIHiringPlayerHurtTarget(LOTREntityNPC entity) {
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
		theTarget = entityplayer.getLastAttacker();
		int i = entityplayer.getLastAttackerTime();
		if (i == playerLastAttackerTime) {
			return false;
		}
		return LOTRMod.canNPCAttackEntity(theNPC, theTarget, true) && isSuitableTarget(theTarget, false);
	}

	@Override
	public void startExecuting() {
		theNPC.setAttackTarget(theTarget);
		theNPC.hiredNPCInfo.wasAttackCommanded = true;
		EntityPlayer entityplayer = theNPC.hiredNPCInfo.getHiringPlayer();
		if (entityplayer != null) {
			playerLastAttackerTime = entityplayer.getLastAttackerTime();
		}
		super.startExecuting();
	}
}
