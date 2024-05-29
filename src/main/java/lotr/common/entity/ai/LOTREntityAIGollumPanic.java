package lotr.common.entity.ai;

import lotr.common.entity.npc.LOTREntityGollum;
import net.minecraft.entity.ai.EntityAIPanic;

public class LOTREntityAIGollumPanic extends EntityAIPanic {
	public LOTREntityGollum theGollum;

	public LOTREntityAIGollumPanic(LOTREntityGollum gollum, double d) {
		super(gollum, d);
		theGollum = gollum;
	}

	@Override
	public void resetTask() {
		super.resetTask();
		theGollum.setGollumFleeing(false);
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		theGollum.setGollumFleeing(true);
	}
}
