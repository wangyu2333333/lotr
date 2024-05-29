package lotr.common.command;

import lotr.common.entity.LOTREntities;
import net.minecraft.command.server.CommandSummon;

public class LOTRCommandSummon extends CommandSummon {
	@Override
	public String[] func_147182_d() {
		return LOTREntities.getAllEntityNames().toArray(new String[0]);
	}

	@Override
	public String getCommandName() {
		return "lotr_summon";
	}
}
