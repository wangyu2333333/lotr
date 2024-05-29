package lotr.common.network;

import java.util.*;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.fac.LOTRFaction;
import net.minecraft.entity.player.EntityPlayerMP;

public class LOTRPacketAlignmentChoices implements IMessage {
	public Set<LOTRFaction> setZeroFacs = new HashSet<>();

	public LOTRPacketAlignmentChoices() {
	}

	public LOTRPacketAlignmentChoices(Set<LOTRFaction> facs) {
		setZeroFacs = facs;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		byte facID = 0;
		while ((facID = data.readByte()) >= 0) {
			LOTRFaction fac = LOTRFaction.forID(facID);
			if (fac == null) {
				continue;
			}
			setZeroFacs.add(fac);
		}
	}

	@Override
	public void toBytes(ByteBuf data) {
		for (LOTRFaction fac : setZeroFacs) {
			data.writeByte(fac.ordinal());
		}
		data.writeByte(-1);
	}

	public static class Handler implements IMessageHandler<LOTRPacketAlignmentChoices, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketAlignmentChoices packet, MessageContext context) {
			EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
			LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
			playerData.chooseUnwantedAlignments(entityplayer, packet.setZeroFacs);
			return null;
		}
	}

}
