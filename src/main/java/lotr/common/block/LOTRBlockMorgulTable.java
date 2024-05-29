package lotr.common.block;

import java.util.Random;

import cpw.mods.fml.relauncher.*;
import lotr.common.LOTRMod;
import lotr.common.fac.LOTRFaction;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class LOTRBlockMorgulTable extends LOTRBlockCraftingTable {
	@SideOnly(value = Side.CLIENT)
	public IIcon[] tableIcons;

	public LOTRBlockMorgulTable() {
		super(Material.rock, LOTRFaction.MORDOR, 1);
		setStepSound(Block.soundTypeStone);
		setLightLevel(0.5f);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int i, int j) {
		if (i == 1) {
			return tableIcons[1];
		}
		if (i == 0) {
			return LOTRMod.rock.getIcon(2, 0);
		}
		return tableIcons[0];
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		for (int l = 0; l < 2; ++l) {
			double d = i + 0.25 + random.nextFloat() * 0.5f;
			double d1 = j + 1.0;
			double d2 = k + 0.25 + random.nextFloat() * 0.5f;
			world.spawnParticle("flame", d, d1, d2, 0.0, 0.0, 0.0);
		}
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
		tableIcons = new IIcon[2];
		tableIcons[0] = iconregister.registerIcon(getTextureName() + "_side");
		tableIcons[1] = iconregister.registerIcon(getTextureName() + "_top");
	}
}
