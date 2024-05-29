package lotr.common.item;

import lotr.common.LOTRCreativeTabs;
import lotr.common.dispenser.LOTRDispenseMysteryWeb;
import lotr.common.entity.projectile.LOTREntityMysteryWeb;
import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;

public class LOTRItemMysteryWeb extends Item {
	public LOTRItemMysteryWeb() {
		setCreativeTab(LOTRCreativeTabs.tabMisc);
		BlockDispenser.dispenseBehaviorRegistry.putObject(this, new LOTRDispenseMysteryWeb());
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		if (!entityplayer.capabilities.isCreativeMode) {
			--itemstack.stackSize;
		}
		world.playSoundAtEntity(entityplayer, "random.bow", 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f));
		if (!world.isRemote) {
			world.spawnEntityInWorld(new LOTREntityMysteryWeb(world, entityplayer));
		}
		return itemstack;
	}
}
