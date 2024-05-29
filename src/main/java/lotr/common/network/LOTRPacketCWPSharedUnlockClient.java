package lotr.common.network;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.world.map.LOTRCustomWaypoint;

public class LOTRPacketCWPSharedUnlockClient implements IMessage {
	public int cwpID;
	public UUID sharingPlayer;

	public LOTRPacketCWPSharedUnlockClient() {
	}

	public LOTRPacketCWPSharedUnlockClient(int id, UUID player) {
		cwpID = id;
		sharingPlayer = player;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		cwpID = data.readInt();
		sharingPlayer = new UUID(data.readLong(), data.readLong());
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeInt(cwpID);
		data.writeLong(sharingPlayer.getMostSignificantBits());
		data.writeLong(sharingPlayer.getLeastSignificantBits());
	}

	public static class Handler implements IMessageHandler<LOTRPacketCWPSharedUnlockClient, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketCWPSharedUnlockClient packet, MessageContext context) {
			LOTRCustomWaypoint cwp;
			LOTRPlayerData pd;
			if (!LOTRMod.proxy.isSingleplayer() && (cwp = (pd = LOTRLevelData.getData(LOTRMod.proxy.getClientPlayer())).getSharedCustomWaypointByID(packet.sharingPlayer, packet.cwpID)) != null) {
				pd.unlockSharedCustomWaypoint(cwp);
			}
			return null;
		}
	}

}
