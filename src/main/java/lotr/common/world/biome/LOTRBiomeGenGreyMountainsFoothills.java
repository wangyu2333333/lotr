package lotr.common.world.biome;

import java.util.Random;

import lotr.common.world.biome.variant.LOTRBiomeVariant;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class LOTRBiomeGenGreyMountainsFoothills extends LOTRBiomeGenGreyMountains {
	public LOTRBiomeGenGreyMountainsFoothills(int i, boolean major) {
		super(i, major);
		decorator.biomeGemFactor = 0.75f;
	}

	@Override
	public void generateMountainTerrain(World world, Random random, Block[] blocks, byte[] meta, int i, int k, int xzIndex, int ySize, int height, int rockDepth, LOTRBiomeVariant variant) {
	}
}
