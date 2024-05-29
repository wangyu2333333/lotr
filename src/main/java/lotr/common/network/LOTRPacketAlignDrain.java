package lotr.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;

public class LOTRPacketAlignDrain implements IMessage {
	public int numFactions;

	public LOTRPacketAlignDrain() {
	}

	public LOTRPacketAlignDrain(int num) {
		numFactions = num;
	}

	@Override
	public void fromBytes(ByteBuf data) {
		numFactions = data.readByte();
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeByte(numFactions);
	}

	public static class Handler implements IMessageHandler<LOTRPacketAlignDrain, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketAlignDrain packet, MessageContext context) {
			LOTRMod.proxy.displayAlignDrain(packet.numFactions);
			return null;
		}
	}

}
