package lotr.client.fx;

import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.world.World;

public class LOTREntityMarshFlameFX extends EntityFlameFX {
	public LOTREntityMarshFlameFX(World world, double d, double d1, double d2, double d3, double d4, double d5) {
		super(world, d, d1, d2, d3, d4, d5);
		particleMaxAge = 40 + rand.nextInt(20);
	}
}
