package lotr.common.network;

import com.google.common.base.Charsets;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.util.LOTRLog;
import lotr.common.world.map.LOTRCustomWaypoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class LOTRPacketCreateCWP implements IMessage {
	public String name;

	public LOTRPacketCreateCWP() {
	}

	public LOTRPacketCreateCWP(String s) {
		name = s;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		short length = data.readShort();
		name = data.readBytes(length).toString(Charsets.UTF_8);
	}

	@Override
	public void toBytes(ByteBuf data) {
		byte[] nameBytes = name.getBytes(Charsets.UTF_8);
		data.writeShort(nameBytes.length);
		data.writeBytes(nameBytes);
	}

	public static class Handler implements IMessageHandler<LOTRPacketCreateCWP, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketCreateCWP packet, MessageContext context) {
			EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
			World world = entityplayer.worldObj;
			LOTRPlayerData pd = LOTRLevelData.getData(entityplayer);
			int minY = LOTRConfig.customWaypointMinY;
			if (minY >= 0 && entityplayer.boundingBox.minY < minY) {
				LOTRLog.logger.warn("Player " + entityplayer.getCommandSenderName() + " tried to create a custom waypoint below the config file minimum of " + minY + "!");
				return null;
			}
			int numWaypoints = pd.getCustomWaypoints().size();
			if (numWaypoints <= pd.getMaxCustomWaypoints()) {
				IChatComponent[] protectionMessage = new IChatComponent[1];
				boolean protection = LOTRBannerProtection.isProtected(world, entityplayer, LOTRBannerProtection.forPlayer_returnMessage(entityplayer, LOTRBannerProtection.Permission.FULL, protectionMessage), true);
				if (!protection) {
					String wpName = LOTRCustomWaypoint.validateCustomName(packet.name);
					if (wpName != null) {
						LOTRCustomWaypoint.createForPlayer(wpName, entityplayer);
					}
				} else {
					IChatComponent clientMessage = protectionMessage[0];
					LOTRPacketCWPProtectionMessage packetMessage = new LOTRPacketCWPProtectionMessage(clientMessage);
					LOTRPacketHandler.networkWrapper.sendTo(packetMessage, entityplayer);
				}
			}
			return null;
		}
	}

}
