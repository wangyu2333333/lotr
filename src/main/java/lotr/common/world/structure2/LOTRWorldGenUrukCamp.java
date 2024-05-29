package lotr.common.world.structure2;

import java.util.Random;

import lotr.common.LOTRMod;
import lotr.common.entity.LOTREntityNPCRespawner;
import lotr.common.entity.npc.*;
import net.minecraft.world.World;

public class LOTRWorldGenUrukCamp extends LOTRWorldGenCampBase {
	public LOTRWorldGenUrukCamp(boolean flag) {
		super(flag);
	}

	@Override
	public LOTRWorldGenStructureBase2 createTent(boolean flag, Random random) {
		if (random.nextInt(6) == 0) {
			return new LOTRWorldGenUrukForgeTent(false);
		}
		return new LOTRWorldGenUrukTent(false);
	}

	@Override
	public LOTREntityNPC getCampCaptain(World world, Random random) {
		return random.nextBoolean() ? new LOTREntityUrukHaiTrader(world) : new LOTREntityUrukHaiMercenaryCaptain(world);
	}

	@Override
	public void placeNPCRespawner(World world, Random random, int i, int j, int k) {
		LOTREntityNPCRespawner respawner = new LOTREntityNPCRespawner(world);
		respawner.setSpawnClasses(LOTREntityUrukHai.class, LOTREntityUrukHaiCrossbower.class);
		respawner.setCheckRanges(24, -12, 12, 12);
		respawner.setSpawnRanges(8, -4, 4, 16);
		this.placeNPCRespawner(respawner, world, i, j, k);
	}

	@Override
	public void setupRandomBlocks(Random random) {
		super.setupRandomBlocks(random);
		tableBlock = LOTRMod.commandTable;
		brickBlock = LOTRMod.brick2;
		brickMeta = 7;
		brickSlabBlock = LOTRMod.slabSingle4;
		brickSlabMeta = 4;
		fenceBlock = LOTRMod.fence;
		fenceMeta = 3;
		fenceGateBlock = LOTRMod.fenceGateCharred;
		hasOrcTorches = true;
		hasSkulls = true;
	}
}
