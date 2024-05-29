package lotr.common.block;

import java.util.Random;

import cpw.mods.fml.relauncher.*;
import lotr.common.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.*;

public class LOTRBlockBanana extends LOTRBlockHangingFruit {
	@SideOnly(value = Side.CLIENT)
	@Override
	public Item getItem(World world, int i, int j, int k) {
		return LOTRMod.banana;
	}

	@Override
	public Item getItemDropped(int i, Random random, int j) {
		return LOTRMod.banana;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer entityplayer, int i, int j, int k, boolean willHarvest) {
		boolean flag = super.removedByPlayer(world, entityplayer, i, j, k, willHarvest);
		if (flag && !world.isRemote) {
			LOTRLevelData.getData(entityplayer).addAchievement(LOTRAchievement.pickBanana);
		}
		return flag;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int i, int j, int k) {
		int dir = world.getBlockMetadata(i, j, k);
		switch (dir) {
		case 0: {
			setBlockBounds(0.375f, 0.1875f, 0.0f, 0.625f, 0.9375f, 0.25f);
			break;
		}
		case 1: {
			setBlockBounds(0.375f, 0.1875f, 0.75f, 0.625f, 0.9375f, 1.0f);
			break;
		}
		case 2: {
			setBlockBounds(0.0f, 0.1875f, 0.375f, 0.25f, 0.9375f, 0.625f);
			break;
		}
		case 3: {
			setBlockBounds(0.75f, 0.1875f, 0.375f, 1.0f, 0.9375f, 0.625f);
		}
		}
	}
}
