package lotr.common.world.biome;

import lotr.common.world.biome.variant.LOTRBiomeVariant;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.biome.BiomeGenBase;

public class LOTRBiomeGenLoneLandsHills extends LOTRBiomeGenLoneLands {
	public LOTRBiomeGenLoneLandsHills(int i, boolean major) {
		super(i, major);
		spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 10, 4, 8));
		clearBiomeVariants();
		addBiomeVariantSet(LOTRBiomeVariant.SET_MOUNTAINS);
		decorator.treesPerChunk = 1;
	}
}
