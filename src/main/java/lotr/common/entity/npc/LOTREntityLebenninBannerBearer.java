package lotr.common.entity.npc;

import lotr.common.item.LOTRItemBanner;
import net.minecraft.world.World;

public class LOTREntityLebenninBannerBearer extends LOTREntityLebenninLevyman implements LOTRBannerBearer {
	public LOTREntityLebenninBannerBearer(World world) {
		super(world);
	}

	@Override
	public LOTRItemBanner.BannerType getBannerType() {
		return LOTRItemBanner.BannerType.LEBENNIN;
	}
}
