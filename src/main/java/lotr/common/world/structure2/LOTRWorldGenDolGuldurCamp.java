package lotr.common.world.structure2;

import java.util.Random;

import lotr.common.LOTRMod;
import lotr.common.entity.LOTREntityNPCRespawner;
import lotr.common.entity.npc.*;
import net.minecraft.world.World;

public class LOTRWorldGenDolGuldurCamp extends LOTRWorldGenCampBase {
	public LOTRWorldGenDolGuldurCamp(boolean flag) {
		super(flag);
	}

	@Override
	public LOTRWorldGenStructureBase2 createTent(boolean flag, Random random) {
		if (random.nextInt(6) == 0) {
			return new LOTRWorldGenDolGuldurForgeTent(false);
		}
		return new LOTRWorldGenDolGuldurTent(false);
	}

	@Override
	public LOTREntityNPC getCampCaptain(World world, Random random) {
		if (random.nextBoolean()) {
			return new LOTREntityDolGuldurOrcTrader(world);
		}
		return null;
	}

	@Override
	public void placeNPCRespawner(World world, Random random, int i, int j, int k) {
		LOTREntityNPCRespawner respawner = new LOTREntityNPCRespawner(world);
		respawner.setSpawnClasses(LOTREntityDolGuldurOrc.class, LOTREntityDolGuldurOrcArcher.class);
		respawner.setCheckRanges(24, -12, 12, 12);
		respawner.setSpawnRanges(8, -4, 4, 16);
		this.placeNPCRespawner(respawner, world, i, j, k);
	}

	@Override
	public void setupRandomBlocks(Random random) {
		super.setupRandomBlocks(random);
		tableBlock = LOTRMod.commandTable;
		brickBlock = LOTRMod.brick2;
		brickMeta = 8;
		brickSlabBlock = LOTRMod.slabSingle4;
		brickSlabMeta = 5;
		fenceBlock = LOTRMod.fence;
		fenceMeta = 3;
		fenceGateBlock = LOTRMod.fenceGateCharred;
		hasOrcTorches = true;
		hasSkulls = true;
	}
}
