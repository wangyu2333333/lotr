package lotr.common.world.structure2;

import java.util.Random;

import lotr.common.entity.npc.*;
import net.minecraft.world.World;

public class LOTRWorldGenDorwinionBath extends LOTRWorldGenGondorBath {
	public LOTRWorldGenDorwinionHouse houseGenForBlocks;

	public LOTRWorldGenDorwinionBath(boolean flag) {
		super(flag);
		houseGenForBlocks = new LOTRWorldGenDorwinionHouse(flag);
	}

	@Override
	public LOTREntityNPC createBather(World world) {
		return new LOTREntityDorwinionMan(world);
	}

	@Override
	public void setupRandomBlocks(Random random) {
		super.setupRandomBlocks(random);
		houseGenForBlocks.setupRandomBlocks(random);
		brickBlock = houseGenForBlocks.brickBlock;
		brickMeta = houseGenForBlocks.brickMeta;
		brickSlabBlock = houseGenForBlocks.brickSlabBlock;
		brickSlabMeta = houseGenForBlocks.brickSlabMeta;
		brickStairBlock = houseGenForBlocks.brickStairBlock;
		brickWallBlock = houseGenForBlocks.brickWallBlock;
		brickWallMeta = houseGenForBlocks.brickWallMeta;
		pillarBlock = houseGenForBlocks.pillarBlock;
		pillarMeta = houseGenForBlocks.pillarMeta;
		brick2Block = houseGenForBlocks.clayBlock;
		brick2Meta = houseGenForBlocks.clayMeta;
		brick2SlabBlock = houseGenForBlocks.claySlabBlock;
		brick2SlabMeta = houseGenForBlocks.claySlabMeta;
		brick2StairBlock = houseGenForBlocks.clayStairBlock;
	}
}
