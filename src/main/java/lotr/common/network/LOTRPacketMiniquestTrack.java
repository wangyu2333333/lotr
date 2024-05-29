package lotr.common.network;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.quest.LOTRMiniQuest;
import net.minecraft.entity.player.EntityPlayerMP;

public class LOTRPacketMiniquestTrack implements IMessage {
	public UUID questID;

	public LOTRPacketMiniquestTrack() {
	}

	public LOTRPacketMiniquestTrack(LOTRMiniQuest quest) {
		questID = quest == null ? null : quest.questUUID;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		boolean hasQuest = data.readBoolean();
		questID = hasQuest ? new UUID(data.readLong(), data.readLong()) : null;
	}

	@Override
	public void toBytes(ByteBuf data) {
		boolean hasQuest = questID != null;
		data.writeBoolean(hasQuest);
		if (hasQuest) {
			data.writeLong(questID.getMostSignificantBits());
			data.writeLong(questID.getLeastSignificantBits());
		}
	}

	public static class Handler implements IMessageHandler<LOTRPacketMiniquestTrack, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketMiniquestTrack packet, MessageContext context) {
			EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
			LOTRPlayerData pd = LOTRLevelData.getData(entityplayer);
			if (packet.questID == null) {
				pd.setTrackingMiniQuestID(null);
			} else {
				pd.setTrackingMiniQuestID(packet.questID);
			}
			return null;
		}
	}

}
