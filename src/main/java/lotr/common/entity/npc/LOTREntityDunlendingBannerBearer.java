package lotr.common.entity.npc;

import lotr.common.item.LOTRItemBanner;
import net.minecraft.world.World;

public class LOTREntityDunlendingBannerBearer extends LOTREntityDunlendingWarrior implements LOTRBannerBearer {
	public LOTREntityDunlendingBannerBearer(World world) {
		super(world);
	}

	@Override
	public LOTRItemBanner.BannerType getBannerType() {
		return LOTRItemBanner.BannerType.DUNLAND;
	}
}
