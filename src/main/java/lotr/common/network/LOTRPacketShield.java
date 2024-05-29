package lotr.common.network;

import java.util.UUID;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;

public class LOTRPacketShield implements IMessage {
	public UUID player;
	public LOTRShields shield;

	public LOTRPacketShield() {
	}

	public LOTRPacketShield(UUID uuid) {
		player = uuid;
		LOTRPlayerData pd = LOTRLevelData.getData(player);
		shield = pd.getShield();
	}

	@Override
	public void fromBytes(ByteBuf data) {
		player = new UUID(data.readLong(), data.readLong());
		boolean hasShield = data.readBoolean();
		if (hasShield) {
			byte shieldID = data.readByte();
			byte shieldTypeID = data.readByte();
			if (shieldTypeID < 0 || shieldTypeID >= LOTRShields.ShieldType.values().length) {
				FMLLog.severe("Failed to update LOTR shield on client side: There is no shieldtype with ID " + shieldTypeID);
			} else {
				LOTRShields.ShieldType shieldType = LOTRShields.ShieldType.values()[shieldTypeID];
				if (shieldID < 0 || shieldID >= shieldType.list.size()) {
					FMLLog.severe("Failed to update LOTR shield on client side: There is no shield with ID " + shieldID + " for shieldtype " + shieldTypeID);
				} else {
					shield = shieldType.list.get(shieldID);
				}
			}
		} else {
			shield = null;
		}
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeLong(player.getMostSignificantBits());
		data.writeLong(player.getLeastSignificantBits());
		boolean hasShield = shield != null;
		data.writeBoolean(hasShield);
		if (hasShield) {
			data.writeByte(shield.getShieldId());
			data.writeByte(shield.getShieldType().ordinal());
		}
	}

	public static class Handler implements IMessageHandler<LOTRPacketShield, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketShield packet, MessageContext context) {
			LOTRPlayerData pd = LOTRLevelData.getData(packet.player);
			pd.setShield(packet.shield);
			return null;
		}
	}

}
