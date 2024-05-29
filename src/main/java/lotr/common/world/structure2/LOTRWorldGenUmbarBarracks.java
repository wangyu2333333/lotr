package lotr.common.world.structure2;

import java.util.Random;

import lotr.common.entity.npc.*;
import net.minecraft.world.World;

public class LOTRWorldGenUmbarBarracks extends LOTRWorldGenSouthronBarracks {
	public LOTRWorldGenUmbarBarracks(boolean flag) {
		super(flag);
	}

	@Override
	public LOTREntityNearHaradrimBase createWarrior(World world, Random random) {
		return random.nextInt(3) == 0 ? new LOTREntityUmbarArcher(world) : new LOTREntityUmbarWarrior(world);
	}

	@Override
	public boolean isUmbar() {
		return true;
	}
}
