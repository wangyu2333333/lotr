package lotr.common.world.map;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import lotr.common.*;
import lotr.common.fellowship.*;
import lotr.common.network.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class LOTRCustomWaypoint implements LOTRAbstractWaypoint {
	public String customName;
	public double mapX;
	public double mapY;
	public int xCoord;
	public int yCoord;
	public int zCoord;
	public int ID;
	public List<UUID> sharedFellowshipIDs = new ArrayList<>();
	public UUID sharingPlayer;
	public String sharingPlayerName;
	public boolean sharedUnlocked;
	public boolean sharedHidden;

	public LOTRCustomWaypoint(String name, double i, double j, int posX, int posY, int posZ, int id) {
		customName = name;
		mapX = i;
		mapY = j;
		xCoord = posX;
		yCoord = posY;
		zCoord = posZ;
		ID = id;
	}

	public void addSharedFellowship(UUID fsID) {
		if (!sharedFellowshipIDs.contains(fsID)) {
			sharedFellowshipIDs.add(fsID);
		}
	}

	public boolean canUnlockShared(EntityPlayer entityplayer) {
		if (yCoord >= 0) {
			double distSq = entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
			return distSq <= 1000000.0;
		}
		return false;
	}

	public LOTRCustomWaypoint createCopyOfShared(UUID sharer) {
		LOTRCustomWaypoint copy = new LOTRCustomWaypoint(customName, mapX, mapY, xCoord, yCoord, zCoord, ID);
		copy.setSharingPlayerID(sharer);
		copy.setSharedFellowshipIDs(new ArrayList<>(sharedFellowshipIDs));
		return copy;
	}

	public LOTRPacketShareCWPClient getClientAddFellowshipPacket(UUID fsID) {
		return new LOTRPacketShareCWPClient(ID, fsID, true);
	}

	public LOTRPacketDeleteCWPClient getClientDeletePacket() {
		return new LOTRPacketDeleteCWPClient(ID);
	}

	public LOTRPacketDeleteCWPClient getClientDeletePacketShared() {
		return new LOTRPacketDeleteCWPClient(ID).setSharingPlayer(sharingPlayer);
	}

	public LOTRPacketCreateCWPClient getClientPacket() {
		return new LOTRPacketCreateCWPClient(mapX, mapY, xCoord, yCoord, zCoord, ID, customName, sharedFellowshipIDs);
	}

	public LOTRPacketCreateCWPClient getClientPacketShared() {
		return new LOTRPacketCreateCWPClient(mapX, mapY, xCoord, yCoord, zCoord, ID, customName, sharedFellowshipIDs).setSharingPlayer(sharingPlayer, sharingPlayerName, sharedUnlocked, sharedHidden);
	}

	public LOTRPacketShareCWPClient getClientRemoveFellowshipPacket(UUID fsID) {
		return new LOTRPacketShareCWPClient(ID, fsID, false);
	}

	public LOTRPacketRenameCWPClient getClientRenamePacket() {
		return new LOTRPacketRenameCWPClient(ID, customName);
	}

	public LOTRPacketRenameCWPClient getClientRenamePacketShared() {
		return new LOTRPacketRenameCWPClient(ID, customName).setSharingPlayer(sharingPlayer);
	}

	public LOTRPacketCWPSharedHideClient getClientSharedHidePacket(boolean hide) {
		return new LOTRPacketCWPSharedHideClient(ID, sharingPlayer, hide);
	}

	public LOTRPacketCWPSharedUnlockClient getClientSharedUnlockPacket() {
		return new LOTRPacketCWPSharedUnlockClient(ID, sharingPlayer);
	}

	@Override
	public String getCodeName() {
		return customName;
	}

	@Override
	public String getDisplayName() {
		if (isShared()) {
			return StatCollector.translateToLocalFormatted("lotr.waypoint.shared", customName);
		}
		return StatCollector.translateToLocalFormatted("lotr.waypoint.custom", customName);
	}

	@Override
	public int getID() {
		return ID;
	}

	@Override
	public LOTRAbstractWaypoint.WaypointLockState getLockState(EntityPlayer entityplayer) {
		boolean unlocked = hasPlayerUnlocked(entityplayer);
		if (isShared()) {
			return unlocked ? LOTRAbstractWaypoint.WaypointLockState.SHARED_CUSTOM_UNLOCKED : LOTRAbstractWaypoint.WaypointLockState.SHARED_CUSTOM_LOCKED;
		}
		return unlocked ? LOTRAbstractWaypoint.WaypointLockState.CUSTOM_UNLOCKED : LOTRAbstractWaypoint.WaypointLockState.CUSTOM_LOCKED;
	}

	@Override
	public String getLoreText(EntityPlayer entityplayer) {
		boolean shared;
		boolean ownShared = !isShared() && !sharedFellowshipIDs.isEmpty();
		shared = isShared() && sharingPlayerName != null;
		if (ownShared || shared) {
			int numShared = sharedFellowshipIDs.size();
			int numShown = 0;
			ArrayList<String> fsNames = new ArrayList<>();
			for (int i = 0; i < 3 && i < sharedFellowshipIDs.size(); ++i) {
				UUID fsID = sharedFellowshipIDs.get(i);
				LOTRFellowshipClient fs = LOTRLevelData.getData(entityplayer).getClientFellowshipByID(fsID);
				if (fs == null) {
					continue;
				}
				fsNames.add(fs.getName());
				++numShown;
			}
			String sharedFsNames = "";
			for (String s : fsNames) {
				sharedFsNames = sharedFsNames + "\n" + s;
			}
			if (numShared > numShown) {
				int numMore = numShared - numShown;
				sharedFsNames = sharedFsNames + "\n" + StatCollector.translateToLocalFormatted("lotr.waypoint.custom.andMore", numMore);
			}
			if (ownShared) {
				return StatCollector.translateToLocalFormatted("lotr.waypoint.custom.info", sharedFsNames);
			}
			if (shared) {
				return StatCollector.translateToLocalFormatted("lotr.waypoint.shared.info", sharingPlayerName, sharedFsNames);
			}
		}
		return null;
	}

	public List<UUID> getPlayersInAllSharedFellowships() {
		ArrayList<UUID> allPlayers = new ArrayList<>();
		for (UUID fsID : sharedFellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null) {
				continue;
			}
			List<UUID> fsPlayers = fs.getAllPlayerUUIDs();
			for (UUID player : fsPlayers) {
				if (player.equals(sharingPlayer) || allPlayers.contains(player)) {
					continue;
				}
				allPlayers.add(player);
			}
		}
		return allPlayers;
	}

	public List<UUID> getSharedFellowshipIDs() {
		return sharedFellowshipIDs;
	}

	public UUID getSharingPlayerID() {
		return sharingPlayer;
	}

	public String getSharingPlayerName() {
		return sharingPlayerName;
	}

	@Override
	public double getX() {
		return mapX;
	}

	@Override
	public int getXCoord() {
		return xCoord;
	}

	@Override
	public double getY() {
		return mapY;
	}

	@Override
	public int getYCoord(World world, int i, int k) {
		int j = yCoord;
		if (j < 0) {
			yCoord = LOTRMod.getTrueTopBlock(world, i, k);
		} else if (!isSafeBlock(world, i, j, k)) {
			int j1;
			Block below = world.getBlock(i, j - 1, k);
			Block block = world.getBlock(i, j, k);
			Block above = world.getBlock(i, j + 1, k);
			boolean belowSafe = below.getMaterial().blocksMovement();
			boolean blockSafe = !block.isNormalCube(world, i, j, k);
			boolean aboveSafe = !above.isNormalCube(world, i, j + 1, k);
			boolean foundSafe = false;
			if (!belowSafe) {
				for (j1 = j - 1; j1 >= 1; --j1) {
					if (!isSafeBlock(world, i, j1, k)) {
						continue;
					}
					yCoord = j1;
					foundSafe = true;
					break;
				}
			}
			if (!foundSafe && (!blockSafe || !aboveSafe)) {
				for (j1 = aboveSafe ? j + 1 : j + 2; j1 <= world.getHeight() - 1; ++j1) {
					if (!isSafeBlock(world, i, j1, k)) {
						continue;
					}
					yCoord = j1;
					foundSafe = true;
					break;
				}
			}
			if (!foundSafe) {
				yCoord = LOTRMod.getTrueTopBlock(world, i, k);
			}
		}
		return yCoord;
	}

	@Override
	public int getYCoordSaved() {
		return yCoord;
	}

	@Override
	public int getZCoord() {
		return zCoord;
	}

	@Override
	public boolean hasPlayerUnlocked(EntityPlayer entityplayer) {
		if (isShared()) {
			return isSharedUnlocked();
		}
		return true;
	}

	public boolean hasSharedFellowship(LOTRFellowship fs) {
		return this.hasSharedFellowship(fs.getFellowshipID());
	}

	public boolean hasSharedFellowship(UUID fsID) {
		return sharedFellowshipIDs.contains(fsID);
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	public boolean isSafeBlock(World world, int i, int j, int k) {
		Block below = world.getBlock(i, j - 1, k);
		Block block = world.getBlock(i, j, k);
		Block above = world.getBlock(i, j + 1, k);
		if (below.getMaterial().blocksMovement() && !block.isNormalCube(world, i, j, k) && !above.isNormalCube(world, i, j + 1, k)) {
			if (block.getMaterial().isLiquid() || block.getMaterial() == Material.fire) {
				return false;
			}
			return !above.getMaterial().isLiquid() && above.getMaterial() != Material.fire;
		}
		return false;
	}

	public boolean isShared() {
		return sharingPlayer != null;
	}

	public boolean isSharedHidden() {
		return sharedHidden;
	}

	public boolean isSharedUnlocked() {
		return sharedUnlocked;
	}

	public void removeSharedFellowship(UUID fsID) {
		if (sharedFellowshipIDs.contains(fsID)) {
			sharedFellowshipIDs.remove(fsID);
		}
	}

	public void rename(String newName) {
		customName = newName;
	}

	public void setSharedFellowshipIDs(List<UUID> fsIDs) {
		sharedFellowshipIDs = fsIDs;
	}

	public void setSharedHidden(boolean flag) {
		sharedHidden = flag;
	}

	public void setSharedUnlocked() {
		sharedUnlocked = true;
	}

	public void setSharingPlayerID(UUID id) {
		UUID prev = sharingPlayer;
		sharingPlayer = id;
		if (MinecraftServer.getServer() != null && (prev == null || !prev.equals(sharingPlayer))) {
			sharingPlayerName = LOTRPacketFellowship.getPlayerProfileWithUsername(sharingPlayer).getName();
		}
	}

	public void setSharingPlayerName(String s) {
		sharingPlayerName = s;
	}

	public void validateFellowshipIDs(LOTRPlayerData ownerData) {
		UUID ownerUUID = ownerData.getPlayerUUID();
		HashSet<UUID> removeIDs = new HashSet<>();
		for (UUID fsID : sharedFellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs != null && fs.containsPlayer(ownerUUID)) {
				continue;
			}
			removeIDs.add(fsID);
		}
		sharedFellowshipIDs.removeAll(removeIDs);
	}

	public void writeToNBT(NBTTagCompound nbt, LOTRPlayerData pd) {
		nbt.setString("Name", customName);
		nbt.setDouble("XMap", mapX);
		nbt.setDouble("YMap", mapY);
		nbt.setInteger("XCoord", xCoord);
		nbt.setInteger("YCoord", yCoord);
		nbt.setInteger("ZCoord", zCoord);
		nbt.setInteger("ID", ID);
		validateFellowshipIDs(pd);
		if (!sharedFellowshipIDs.isEmpty()) {
			NBTTagList sharedFellowshipTags = new NBTTagList();
			for (UUID fsID : sharedFellowshipIDs) {
				NBTTagString tag = new NBTTagString(fsID.toString());
				sharedFellowshipTags.appendTag(tag);
			}
			nbt.setTag("SharedFellowships", sharedFellowshipTags);
		}
	}

	public static LOTRCustomWaypoint createForPlayer(String name, EntityPlayer entityplayer) {
		LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
		int cwpID = playerData.getNextCwpID();
		int i = MathHelper.floor_double(entityplayer.posX);
		int j = MathHelper.floor_double(entityplayer.boundingBox.minY);
		int k = MathHelper.floor_double(entityplayer.posZ);
		double mapX = LOTRWaypoint.worldToMapX(i);
		double mapY = LOTRWaypoint.worldToMapZ(k);
		LOTRCustomWaypoint cwp = new LOTRCustomWaypoint(name, mapX, mapY, i, j, k, cwpID);
		playerData.addCustomWaypoint(cwp);
		playerData.incrementNextCwpID();
		return cwp;
	}

	public static LOTRCustomWaypoint readFromNBT(NBTTagCompound nbt, LOTRPlayerData pd) {
		String name = nbt.getString("Name");
		double x = 0.0;
		double y = 0.0;
		if (nbt.hasKey("XMap")) {
			x = nbt.getDouble("XMap");
		} else if (nbt.hasKey("X")) {
			x = nbt.getInteger("X");
		}
		if (nbt.hasKey("YMap")) {
			y = nbt.getDouble("YMap");
		} else if (nbt.hasKey("Y")) {
			y = nbt.getInteger("Y");
		}
		int xCoord = nbt.getInteger("XCoord");
		int zCoord = nbt.getInteger("ZCoord");
		int yCoord = nbt.hasKey("YCoord") ? nbt.getInteger("YCoord") : -1;
		int ID = nbt.getInteger("ID");
		LOTRCustomWaypoint cwp = new LOTRCustomWaypoint(name, x, y, xCoord, yCoord, zCoord, ID);
		cwp.sharedFellowshipIDs.clear();
		if (nbt.hasKey("SharedFellowships")) {
			NBTTagList sharedFellowshipTags = nbt.getTagList("SharedFellowships", 8);
			for (int i = 0; i < sharedFellowshipTags.tagCount(); ++i) {
				UUID fsID = UUID.fromString(sharedFellowshipTags.getStringTagAt(i));
				if (fsID == null) {
					continue;
				}
				cwp.sharedFellowshipIDs.add(fsID);
			}
		}
		cwp.validateFellowshipIDs(pd);
		return cwp;
	}

	public static String validateCustomName(String name) {
		if (!StringUtils.isBlank(name = StringUtils.trim(name))) {
			return name;
		}
		return null;
	}
}
