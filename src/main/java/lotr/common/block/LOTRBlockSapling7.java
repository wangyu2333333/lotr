package lotr.common.block;

import java.util.Random;

import lotr.common.world.feature.LOTRTreeType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class LOTRBlockSapling7 extends LOTRBlockSaplingBase {
	public LOTRBlockSapling7() {
		setSaplingNames("aspen", "greenOak", "lairelosse", "almond");
	}

	@Override
	public void growTree(World world, int i, int j, int k, Random random) {
		int i1;
		int k1;
		int meta = world.getBlockMetadata(i, j, k) & 7;
		WorldGenAbstractTree treeGen = null;
		int trunkNeg = 0;
		int trunkPos = 0;
		int xOffset = 0;
		int zOffset = 0;
		if (meta == 0) {
			block0: for (i1 = 0; i1 >= -1; --i1) {
				for (k1 = 0; k1 >= -1; --k1) {
					if (!this.isSameSapling(world, i + i1, j, k + k1, meta) || !this.isSameSapling(world, i + i1 + 1, j, k + k1, meta) || !this.isSameSapling(world, i + i1, j, k + k1 + 1, meta) || !this.isSameSapling(world, i + i1 + 1, j, k + k1 + 1, meta)) {
						continue;
					}
					treeGen = LOTRTreeType.ASPEN_LARGE.create(true, random);
					trunkNeg = 0;
					trunkPos = 1;
					xOffset = i1;
					zOffset = k1;
					break block0;
				}
			}
			if (treeGen == null) {
				trunkPos = 0;
				trunkNeg = 0;
				xOffset = 0;
				zOffset = 0;
				treeGen = LOTRTreeType.ASPEN.create(true, random);
			}
		}
		if (meta == 1) {
			int[] partyTree = LOTRBlockSaplingBase.findPartyTree(world, i, j, k, this, meta);
			if (partyTree != null) {
				treeGen = LOTRTreeType.GREEN_OAK_LARGE.create(true, random);
				trunkPos = 1;
				trunkNeg = 1;
				xOffset = partyTree[0];
				zOffset = partyTree[1];
			}
			if (treeGen == null) {
				treeGen = LOTRTreeType.GREEN_OAK.create(true, random);
				trunkPos = 0;
				trunkNeg = 0;
				xOffset = 0;
				zOffset = 0;
			}
		}
		if (meta == 2) {
			block2: for (i1 = 0; i1 >= -1; --i1) {
				for (k1 = 0; k1 >= -1; --k1) {
					if (!this.isSameSapling(world, i + i1, j, k + k1, meta) || !this.isSameSapling(world, i + i1 + 1, j, k + k1, meta) || !this.isSameSapling(world, i + i1, j, k + k1 + 1, meta) || !this.isSameSapling(world, i + i1 + 1, j, k + k1 + 1, meta)) {
						continue;
					}
					treeGen = LOTRTreeType.LAIRELOSSE_LARGE.create(true, random);
					trunkNeg = 0;
					trunkPos = 1;
					xOffset = i1;
					zOffset = k1;
					break block2;
				}
			}
			if (treeGen == null) {
				xOffset = 0;
				zOffset = 0;
				treeGen = LOTRTreeType.LAIRELOSSE.create(true, random);
			}
		}
		if (meta == 3) {
			treeGen = LOTRTreeType.ALMOND.create(true, random);
		}
		for (i1 = -trunkNeg; i1 <= trunkPos; ++i1) {
			for (k1 = -trunkNeg; k1 <= trunkPos; ++k1) {
				world.setBlock(i + xOffset + i1, j, k + zOffset + k1, Blocks.air, 0, 4);
			}
		}
		if (treeGen != null && !treeGen.generate(world, random, i + xOffset, j, k + zOffset)) {
			for (i1 = -trunkNeg; i1 <= trunkPos; ++i1) {
				for (k1 = -trunkNeg; k1 <= trunkPos; ++k1) {
					world.setBlock(i + xOffset + i1, j, k + zOffset + k1, this, meta, 4);
				}
			}
		}
	}
}
