package lotr.common.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.entity.item.LOTREntityBanner;
import lotr.common.fellowship.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class LOTRPacketBannerRequestInvalidName implements IMessage {
	public int bannerID;
	public int slot;
	public String username;

	public LOTRPacketBannerRequestInvalidName() {
	}

	public LOTRPacketBannerRequestInvalidName(LOTREntityBanner banner, int i, String s) {
		bannerID = banner.getEntityId();
		slot = i;
		username = s;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		bannerID = data.readInt();
		slot = data.readShort();
		byte length = data.readByte();
		username = data.readBytes(length).toString(Charsets.UTF_8);
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeInt(bannerID);
		data.writeShort(slot);
		byte[] nameBytes = username.getBytes(Charsets.UTF_8);
		data.writeByte(nameBytes.length);
		data.writeBytes(nameBytes);
	}

	public static class Handler implements IMessageHandler<LOTRPacketBannerRequestInvalidName, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketBannerRequestInvalidName packet, MessageContext context) {
			EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
			World world = entityplayer.worldObj;
			Entity bEntity = world.getEntityByID(packet.bannerID);
			if (bEntity instanceof LOTREntityBanner) {
				LOTREntityBanner banner = (LOTREntityBanner) bEntity;
				String username = packet.username;
				boolean valid = false;
				if (LOTRFellowshipProfile.hasFellowshipCode(username)) {
					String fsName = LOTRFellowshipProfile.stripFellowshipCode(username);
					LOTRFellowship fellowship = banner.getPlacersFellowshipByName(fsName);
					if (fellowship != null) {
						valid = true;
					}
				} else {
					GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(packet.username);
					if (profile != null) {
						valid = true;
					}
				}
				LOTRPacketBannerValidate packetResponse = new LOTRPacketBannerValidate(banner.getEntityId(), packet.slot, packet.username, valid);
				LOTRPacketHandler.networkWrapper.sendTo(packetResponse, entityplayer);
			}
			return null;
		}
	}

}
