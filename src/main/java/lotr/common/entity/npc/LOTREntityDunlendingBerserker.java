package lotr.common.entity.npc;

import lotr.common.*;
import lotr.common.entity.ai.LOTREntityAIAttackOnCollide;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTREntityDunlendingBerserker extends LOTREntityDunlendingWarrior {
	public LOTREntityDunlendingBerserker(World world) {
		super(world);
		npcShield = null;
		npcCape = LOTRCapes.DUNLENDING_BERSERKER;
	}

	@Override
	public void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(24.0);
		getEntityAttribute(npcAttackDamageExtra).setBaseValue(2.0);
	}

	@Override
	public EntityAIBase getDunlendingAttackAI() {
		return new LOTREntityAIAttackOnCollide(this, 1.7, false);
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		int i = rand.nextInt(2);
		if (i == 0) {
			npcItemsInv.setMeleeWeapon(new ItemStack(LOTRMod.battleaxeIron));
		} else if (i == 1) {
			npcItemsInv.setMeleeWeapon(new ItemStack(LOTRMod.battleaxeBronze));
		}
		npcItemsInv.setIdleItem(npcItemsInv.getMeleeWeapon());
		setCurrentItemOrArmor(1, new ItemStack(LOTRMod.bootsFur));
		setCurrentItemOrArmor(2, new ItemStack(LOTRMod.legsFur));
		setCurrentItemOrArmor(3, new ItemStack(LOTRMod.bodyBone));
		setCurrentItemOrArmor(4, new ItemStack(LOTRMod.helmetDunlending));
		return data;
	}
}
