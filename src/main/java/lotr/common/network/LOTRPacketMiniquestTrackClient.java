package lotr.common.network;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRPacketMiniquestTrackClient implements IMessage {
	public UUID questID;

	public LOTRPacketMiniquestTrackClient() {
	}

	public LOTRPacketMiniquestTrackClient(UUID uuid) {
		questID = uuid;
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

	public static class Handler implements IMessageHandler<LOTRPacketMiniquestTrackClient, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketMiniquestTrackClient packet, MessageContext context) {
			if (!LOTRMod.proxy.isSingleplayer()) {
				EntityPlayer entityplayer = LOTRMod.proxy.getClientPlayer();
				LOTRPlayerData pd = LOTRLevelData.getData(entityplayer);
				pd.setTrackingMiniQuestID(packet.questID);
			}
			return null;
		}
	}

}
