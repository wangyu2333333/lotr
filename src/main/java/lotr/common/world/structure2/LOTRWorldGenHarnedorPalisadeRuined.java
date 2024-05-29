package lotr.common.world.structure2;

import java.util.Random;

import lotr.common.LOTRMod;

public class LOTRWorldGenHarnedorPalisadeRuined extends LOTRWorldGenHarnedorPalisade {
	public LOTRWorldGenHarnedorPalisadeRuined(boolean flag) {
		super(flag);
	}

	@Override
	public boolean isRuined() {
		return true;
	}

	@Override
	public void setupRandomBlocks(Random random) {
		super.setupRandomBlocks(random);
		if (random.nextBoolean()) {
			woodBlock = LOTRMod.wood;
			woodMeta = 3;
		}
	}
}
