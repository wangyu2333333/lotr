package lotr.common.network;

import java.util.UUID;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.quest.LOTRMiniQuest;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRPacketMiniquestRemove implements IMessage {
	public UUID questUUID;
	public boolean wasCompleted;
	public boolean addToCompleted;

	public LOTRPacketMiniquestRemove() {
	}

	public LOTRPacketMiniquestRemove(LOTRMiniQuest quest, boolean wc, boolean atc) {
		questUUID = quest.questUUID;
		wasCompleted = wc;
		addToCompleted = atc;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		questUUID = new UUID(data.readLong(), data.readLong());
		wasCompleted = data.readBoolean();
		addToCompleted = data.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeLong(questUUID.getMostSignificantBits());
		data.writeLong(questUUID.getLeastSignificantBits());
		data.writeBoolean(wasCompleted);
		data.writeBoolean(addToCompleted);
	}

	public static class Handler implements IMessageHandler<LOTRPacketMiniquestRemove, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketMiniquestRemove packet, MessageContext context) {
			if (!LOTRMod.proxy.isSingleplayer()) {
				EntityPlayer entityplayer = LOTRMod.proxy.getClientPlayer();
				LOTRPlayerData pd = LOTRLevelData.getData(entityplayer);
				LOTRMiniQuest removeQuest = pd.getMiniQuestForID(packet.questUUID, packet.wasCompleted);
				if (removeQuest != null) {
					if (packet.addToCompleted) {
						pd.completeMiniQuest(removeQuest);
					} else {
						pd.removeMiniQuest(removeQuest, packet.wasCompleted);
					}
				} else {
					FMLLog.warning("Tried to remove a LOTR miniquest that doesn't exist");
				}
			}
			return null;
		}
	}

}
