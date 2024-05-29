package lotr.common.world.biome;

import lotr.common.entity.animal.LOTREntityDeer;
import lotr.common.world.biome.variant.LOTRBiomeVariant;
import lotr.common.world.feature.LOTRTreeType;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.biome.BiomeGenBase;

public class LOTRBiomeGenRhunForest extends LOTRBiomeGenRhun {
	public LOTRBiomeGenRhunForest(int i, boolean major) {
		super(i, major);
		spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 16, 4, 8));
		spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(LOTREntityDeer.class, 20, 4, 6));
		clearBiomeVariants();
		addBiomeVariantSet(LOTRBiomeVariant.SET_FOREST);
		decorator.treesPerChunk = 8;
		decorator.logsPerChunk = 1;
		decorator.flowersPerChunk = 4;
		decorator.doubleFlowersPerChunk = 1;
		decorator.grassPerChunk = 10;
		decorator.doubleGrassPerChunk = 2;
		decorator.addTree(LOTRTreeType.OAK_LARGE, 2000);
		decorator.addTree(LOTRTreeType.OAK_PARTY, 100);
		registerRhunForestFlowers();
		biomeColors.resetGrass();
	}

	@Override
	public float getChanceToSpawnAnimals() {
		return 0.5f;
	}
}
