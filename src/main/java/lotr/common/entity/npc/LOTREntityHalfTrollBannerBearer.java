package lotr.common.entity.npc;

import lotr.common.item.LOTRItemBanner;
import net.minecraft.world.World;

public class LOTREntityHalfTrollBannerBearer extends LOTREntityHalfTrollWarrior implements LOTRBannerBearer {
	public LOTREntityHalfTrollBannerBearer(World world) {
		super(world);
	}

	@Override
	public LOTRItemBanner.BannerType getBannerType() {
		return LOTRItemBanner.BannerType.HALF_TROLL;
	}
}
