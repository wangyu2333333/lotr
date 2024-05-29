package lotr.common;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import lotr.common.block.LOTRBlockCraftingTable;
import lotr.common.command.LOTRCommandAdminHideMap;
import lotr.common.entity.npc.*;
import lotr.common.fac.*;
import lotr.common.fellowship.*;
import lotr.common.item.*;
import lotr.common.network.*;
import lotr.common.quest.*;
import lotr.common.util.LOTRLog;
import lotr.common.world.*;
import lotr.common.world.biome.*;
import lotr.common.world.map.*;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;

public class LOTRPlayerData {
	public static int PLEDGE_KILL_COOLDOWN_MAX = 24000;
	public static int ticksUntilFT_max = 200;
	public UUID playerUUID;
	public boolean needsSave = false;
	public int pdTick = 0;
	public Map<LOTRFaction, Float> alignments = new HashMap<>();
	public Map<LOTRFaction, LOTRFactionData> factionDataMap = new HashMap<>();
	public LOTRFaction viewingFaction;
	public Map<LOTRDimension.DimensionRegion, LOTRFaction> prevRegionFactions = new HashMap<>();
	public boolean hideAlignment = false;
	public Set<LOTRFaction> takenAlignmentRewards = new HashSet<>();
	public LOTRFaction pledgeFaction;
	public int pledgeKillCooldown = 0;
	public int pledgeBreakCooldown;
	public int pledgeBreakCooldownStart;
	public LOTRFaction brokenPledgeFaction = null;
	public boolean hasPre35Alignments = false;
	public boolean chosenUnwantedAlignments = false;
	public boolean hideOnMap = false;
	public boolean adminHideMap = false;
	public boolean showWaypoints = true;
	public boolean showCustomWaypoints = true;
	public boolean showHiddenSharedWaypoints = true;
	public boolean conquestKills = true;
	public List<LOTRAchievement> achievements = new ArrayList<>();
	public LOTRShields shield;
	public boolean friendlyFire = false;
	public boolean hiredDeathMessages = true;
	public ChunkCoordinates deathPoint;
	public int deathDim;
	public int alcoholTolerance;
	public List<LOTRMiniQuest> miniQuests = new ArrayList<>();
	public List<LOTRMiniQuest> miniQuestsCompleted = new ArrayList<>();
	public int completedMiniquestCount;
	public int completedBountyQuests;
	public UUID trackingMiniQuestID;
	public List<LOTRFaction> bountiesPlaced = new ArrayList<>();
	public LOTRWaypoint lastWaypoint;
	public LOTRBiome lastBiome;
	public Map<LOTRGuiMessageTypes, Boolean> sentMessageTypes = new HashMap<>();
	public LOTRTitle.PlayerTitle playerTitle;
	public boolean femRankOverride = false;
	public int ftSinceTick;
	public LOTRAbstractWaypoint targetFTWaypoint;
	public int ticksUntilFT;
	public UUID uuidToMount;
	public int uuidToMountTime;
	public long lastOnlineTime = -1L;
	public Set<LOTRWaypoint.Region> unlockedFTRegions = new HashSet<>();
	public List<LOTRCustomWaypoint> customWaypoints = new ArrayList<>();
	public List<LOTRCustomWaypoint> customWaypointsShared = new ArrayList<>();
	public Set<CWPSharedKey> cwpSharedUnlocked = new HashSet<>();
	public Set<CWPSharedKey> cwpSharedHidden = new HashSet<>();
	public Map<LOTRWaypoint, Integer> wpUseCounts = new HashMap<>();
	public Map<Integer, Integer> cwpUseCounts = new HashMap<>();
	public Map<CWPSharedKey, Integer> cwpSharedUseCounts = new HashMap<>();
	public int nextCwpID = 20000;
	public List<UUID> fellowshipIDs = new ArrayList<>();
	public List<LOTRFellowshipClient> fellowshipsClient = new ArrayList<>();
	public List<LOTRFellowshipInvite> fellowshipInvites = new ArrayList<>();
	public List<LOTRFellowshipClient> fellowshipInvitesClient = new ArrayList<>();
	public UUID chatBoundFellowshipID;
	public boolean structuresBanned = false;
	public boolean teleportedME = false;
	public LOTRPlayerQuestData questData = new LOTRPlayerQuestData(this);
	public int siegeActiveTime;

	public LOTRPlayerData(UUID uuid) {
		playerUUID = uuid;
		viewingFaction = LOTRFaction.HOBBIT;
		ftSinceTick = LOTRLevelData.getWaypointCooldownMax() * 20;
	}

	public void acceptFellowshipInvite(LOTRFellowship fs, boolean respectSizeLimit) {
		UUID fsID = fs.getFellowshipID();
		LOTRFellowshipInvite existingInvite = null;
		for (LOTRFellowshipInvite invite : fellowshipInvites) {
			if (!invite.fellowshipID.equals(fsID)) {
				continue;
			}
			existingInvite = invite;
			break;
		}
		if (existingInvite != null) {
			EntityPlayer entityplayer = getPlayer();
			if (fs.isDisbanded()) {
				rejectFellowshipInvite(fs);
				if (entityplayer != null && !entityplayer.worldObj.isRemote) {
					LOTRPacketFellowshipAcceptInviteResult resultPacket = new LOTRPacketFellowshipAcceptInviteResult(fs, LOTRPacketFellowshipAcceptInviteResult.AcceptInviteResult.DISBANDED);
					LOTRPacketHandler.networkWrapper.sendTo((IMessage) resultPacket, (EntityPlayerMP) entityplayer);
				}
			} else {
				int limit = LOTRConfig.fellowshipMaxSize;
				if (respectSizeLimit && limit >= 0 && fs.getPlayerCount() >= limit) {
					rejectFellowshipInvite(fs);
					if (entityplayer != null && !entityplayer.worldObj.isRemote) {
						LOTRPacketFellowshipAcceptInviteResult resultPacket = new LOTRPacketFellowshipAcceptInviteResult(fs, LOTRPacketFellowshipAcceptInviteResult.AcceptInviteResult.TOO_LARGE);
						LOTRPacketHandler.networkWrapper.sendTo((IMessage) resultPacket, (EntityPlayerMP) entityplayer);
					}
				} else {
					fs.addMember(playerUUID);
					fellowshipInvites.remove(existingInvite);
					markDirty();
					sendFellowshipInviteRemovePacket(fs);
					if (entityplayer != null && !entityplayer.worldObj.isRemote) {
						EntityPlayer inviter;
						LOTRPacketFellowshipAcceptInviteResult resultPacket = new LOTRPacketFellowshipAcceptInviteResult(fs, LOTRPacketFellowshipAcceptInviteResult.AcceptInviteResult.JOINED);
						LOTRPacketHandler.networkWrapper.sendTo((IMessage) resultPacket, (EntityPlayerMP) entityplayer);
						UUID inviterID = existingInvite.inviterID;
						if (inviterID == null) {
							inviterID = fs.getOwner();
						}
						inviter = getOtherPlayer(inviterID);
						if (inviter != null) {
							fs.sendNotification(inviter, "lotr.gui.fellowships.notifyAccept", entityplayer.getCommandSenderName());
						}
					}
				}
			}
		}
	}

	public void addAchievement(LOTRAchievement achievement) {
		if (hasAchievement(achievement) || isSiegeActive()) {
			return;
		}
		achievements.add(achievement);
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			boolean canEarn = achievement.canPlayerEarn(entityplayer);
			sendAchievementPacket((EntityPlayerMP) entityplayer, achievement, canEarn);
			if (canEarn) {
				achievement.broadcastEarning(entityplayer);
				List<LOTRAchievement> earnedAchievements = getEarnedAchievements(LOTRDimension.MIDDLE_EARTH);
				int biomes = 0;
				for (LOTRAchievement earnedAchievement : earnedAchievements) {
					if (!earnedAchievement.isBiomeAchievement) {
						continue;
					}
					++biomes;
				}
				if (biomes >= 10) {
					addAchievement(LOTRAchievement.travel10);
				}
				if (biomes >= 20) {
					addAchievement(LOTRAchievement.travel20);
				}
				if (biomes >= 30) {
					addAchievement(LOTRAchievement.travel30);
				}
				if (biomes >= 40) {
					addAchievement(LOTRAchievement.travel40);
				}
				if (biomes >= 50) {
					addAchievement(LOTRAchievement.travel50);
				}
			}
		}
	}

	public LOTRAlignmentBonusMap addAlignment(EntityPlayer entityplayer, LOTRAlignmentValues.AlignmentBonus source, LOTRFaction faction, double posX, double posY, double posZ) {
		return this.addAlignment(entityplayer, source, faction, null, posX, posY, posZ);
	}

	public LOTRAlignmentBonusMap addAlignment(EntityPlayer entityplayer, LOTRAlignmentValues.AlignmentBonus source, LOTRFaction faction, Entity entity) {
		return this.addAlignment(entityplayer, source, faction, null, entity);
	}

	public LOTRAlignmentBonusMap addAlignment(EntityPlayer entityplayer, LOTRAlignmentValues.AlignmentBonus source, LOTRFaction faction, List<LOTRFaction> forcedBonusFactions, double posX, double posY, double posZ) {
		float bonus = source.bonus;
		LOTRAlignmentBonusMap factionBonusMap = new LOTRAlignmentBonusMap();
		float prevMainAlignment = getAlignment(faction);
		float conquestBonus = 0.0f;
		if (source.isKill) {
			List<LOTRFaction> killBonuses = faction.getBonusesForKilling();
			for (LOTRFaction bonusFaction : killBonuses) {
				if (!bonusFaction.isPlayableAlignmentFaction() || !bonusFaction.approvesWarCrimes && source.isCivilianKill) {
					continue;
				}
				if (!source.killByHiredUnit) {
					float mplier = forcedBonusFactions != null && forcedBonusFactions.contains(bonusFaction) ? 1.0f : bonusFaction.getControlZoneAlignmentMultiplier(entityplayer);
					if (mplier > 0.0f) {
						float alignment = getAlignment(bonusFaction);
						float factionBonus = Math.abs(bonus);
						factionBonus *= mplier;
						if (alignment >= bonusFaction.getPledgeAlignment() && !isPledgedTo(bonusFaction)) {
							factionBonus *= 0.5f;
						}
						factionBonus = checkBonusForPledgeEnemyLimit(bonusFaction, factionBonus);
						setAlignment(bonusFaction, alignment += factionBonus);
						factionBonusMap.put(bonusFaction, Float.valueOf(factionBonus));
					}
				}
				if (bonusFaction != getPledgeFaction()) {
					continue;
				}
				float conq = bonus;
				if (source.killByHiredUnit) {
					conq *= 0.25f;
				}
				conquestBonus = LOTRConquestGrid.onConquestKill(entityplayer, bonusFaction, faction, conq);
				getFactionData(bonusFaction).addConquest(Math.abs(conquestBonus));
			}
			List<LOTRFaction> killPenalties = faction.getPenaltiesForKilling();
			for (LOTRFaction penaltyFaction : killPenalties) {
				if (!penaltyFaction.isPlayableAlignmentFaction() || source.killByHiredUnit) {
					continue;
				}
				float mplier = penaltyFaction == faction ? 1.0f : penaltyFaction.getControlZoneAlignmentMultiplier(entityplayer);
				if (mplier <= 0.0f) {
					continue;
				}
				float alignment = getAlignment(penaltyFaction);
				float factionPenalty = -Math.abs(bonus);
				factionPenalty *= mplier;
				factionPenalty = LOTRAlignmentValues.AlignmentBonus.scalePenalty(factionPenalty, alignment);
				setAlignment(penaltyFaction, alignment += factionPenalty);
				factionBonusMap.put(penaltyFaction, Float.valueOf(factionPenalty));
			}
		} else if (faction.isPlayableAlignmentFaction()) {
			float alignment = getAlignment(faction);
			float factionBonus = bonus;
			if (factionBonus > 0.0f && alignment >= faction.getPledgeAlignment() && !isPledgedTo(faction)) {
				factionBonus *= 0.5f;
			}
			factionBonus = checkBonusForPledgeEnemyLimit(faction, factionBonus);
			setAlignment(faction, alignment += factionBonus);
			factionBonusMap.put(faction, Float.valueOf(factionBonus));
		}
		if (!factionBonusMap.isEmpty() || conquestBonus != 0.0f) {
			sendAlignmentBonusPacket(source, faction, prevMainAlignment, factionBonusMap, conquestBonus, posX, posY, posZ);
		}
		return factionBonusMap;
	}

	public LOTRAlignmentBonusMap addAlignment(EntityPlayer entityplayer, LOTRAlignmentValues.AlignmentBonus source, LOTRFaction faction, List<LOTRFaction> forcedBonusFactions, Entity entity) {
		return this.addAlignment(entityplayer, source, faction, forcedBonusFactions, entity.posX, entity.boundingBox.minY + entity.height * 0.7, entity.posZ);
	}

	public void addAlignmentFromCommand(LOTRFaction faction, float add) {
		float alignment = getAlignment(faction);
		setAlignment(faction, alignment += add);
	}

	public void addCompletedBountyQuest() {
		++completedBountyQuests;
		markDirty();
	}

	public void addCustomWaypoint(LOTRCustomWaypoint waypoint) {
		customWaypoints.add(waypoint);
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketCreateCWPClient packet = waypoint.getClientPacket();
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			LOTRCustomWaypointLogger.logCreate(entityplayer, waypoint);
		}
		LOTRCustomWaypoint shareCopy = waypoint.createCopyOfShared(playerUUID);
		List<UUID> sharedPlayers = shareCopy.getPlayersInAllSharedFellowships();
		for (UUID sharedPlayerUUID : sharedPlayers) {
			EntityPlayer sharedPlayer = getOtherPlayer(sharedPlayerUUID);
			if (sharedPlayer == null || sharedPlayer.worldObj.isRemote) {
				continue;
			}
			LOTRLevelData.getData(sharedPlayerUUID).addOrUpdateSharedCustomWaypoint(shareCopy);
		}
	}

	public void addCustomWaypointClient(LOTRCustomWaypoint waypoint) {
		customWaypoints.add(waypoint);
	}

	public void addFellowship(LOTRFellowship fs) {
		UUID fsID;
		if (fs.containsPlayer(playerUUID) && !fellowshipIDs.contains(fsID = fs.getFellowshipID())) {
			fellowshipIDs.add(fsID);
			markDirty();
			sendFellowshipPacket(fs);
			addSharedCustomWaypointsFromAllIn(fs.getFellowshipID());
		}
	}

	public void addFellowshipInvite(LOTRFellowship fs, UUID inviterUUID, String inviterUsername) {
		UUID fsID = fs.getFellowshipID();
		boolean hasInviteAlready = false;
		for (LOTRFellowshipInvite invite : fellowshipInvites) {
			if (!invite.fellowshipID.equals(fsID)) {
				continue;
			}
			hasInviteAlready = true;
			break;
		}
		if (!hasInviteAlready) {
			fellowshipInvites.add(new LOTRFellowshipInvite(fsID, inviterUUID));
			markDirty();
			sendFellowshipInvitePacket(fs);
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				fs.sendNotification(entityplayer, "lotr.gui.fellowships.notifyInvite", inviterUsername);
			}
		}
	}

	public void addMiniQuest(LOTRMiniQuest quest) {
		miniQuests.add(quest);
		updateMiniQuest(quest);
	}

	public void addMiniQuestCompleted(LOTRMiniQuest quest) {
		miniQuestsCompleted.add(quest);
		markDirty();
	}

	public void addOrUpdateClientFellowship(LOTRFellowshipClient fs) {
		UUID fsID = fs.getFellowshipID();
		LOTRFellowshipClient inList = null;
		for (LOTRFellowshipClient fsInList : fellowshipsClient) {
			if (!fsInList.getFellowshipID().equals(fsID)) {
				continue;
			}
			inList = fsInList;
			break;
		}
		if (inList != null) {
			inList.updateDataFrom(fs);
		} else {
			fellowshipsClient.add(fs);
		}
	}

	public void addOrUpdateClientFellowshipInvite(LOTRFellowshipClient fs) {
		UUID fsID = fs.getFellowshipID();
		LOTRFellowshipClient inList = null;
		for (LOTRFellowshipClient fsInList : fellowshipInvitesClient) {
			if (!fsInList.getFellowshipID().equals(fsID)) {
				continue;
			}
			inList = fsInList;
			break;
		}
		if (inList != null) {
			inList.updateDataFrom(fs);
		} else {
			fellowshipInvitesClient.add(fs);
		}
	}

	public void addOrUpdateSharedCustomWaypoint(LOTRCustomWaypoint waypoint) {
		if (!waypoint.isShared()) {
			FMLLog.warning("LOTR: Warning! Tried to cache a shared custom waypoint with no owner!");
			return;
		}
		if (waypoint.getSharingPlayerID().equals(playerUUID)) {
			FMLLog.warning("LOTR: Warning! Tried to share a custom waypoint to its own player (%s)!", playerUUID.toString());
			return;
		}
		CWPSharedKey key = CWPSharedKey.keyFor(waypoint.getSharingPlayerID(), waypoint.getID());
		if (cwpSharedUnlocked.contains(key)) {
			waypoint.setSharedUnlocked();
		}
		waypoint.setSharedHidden(cwpSharedHidden.contains(key));
		LOTRCustomWaypoint existing = getSharedCustomWaypointByID(waypoint.getSharingPlayerID(), waypoint.getID());
		if (existing == null) {
			customWaypointsShared.add(waypoint);
		} else {
			if (existing.isSharedUnlocked()) {
				waypoint.setSharedUnlocked();
			}
			waypoint.setSharedHidden(existing.isSharedHidden());
			int i = customWaypointsShared.indexOf(existing);
			customWaypointsShared.set(i, waypoint);
		}
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketCreateCWPClient packet = waypoint.getClientPacketShared();
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public int addSharedCustomWaypointsFrom(UUID onlyOneFellowshipID, List<UUID> checkSpecificPlayers) {
		List<UUID> checkFellowshipIDs;
		if (onlyOneFellowshipID != null) {
			checkFellowshipIDs = new ArrayList<>();
			checkFellowshipIDs.add(onlyOneFellowshipID);
		} else {
			checkFellowshipIDs = fellowshipIDs;
		}
		ArrayList<UUID> checkFellowPlayerIDs = new ArrayList<>();
		if (checkSpecificPlayers != null) {
			for (UUID player : checkSpecificPlayers) {
				if (player.equals(playerUUID)) {
					continue;
				}
				checkFellowPlayerIDs.add(player);
			}
		} else {
			for (UUID fsID : checkFellowshipIDs) {
				LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
				if (fs == null) {
					continue;
				}
				Set<UUID> playerIDs = fs.getWaypointSharerUUIDs();
				for (UUID player : playerIDs) {
					if (player.equals(playerUUID) || checkFellowPlayerIDs.contains(player)) {
						continue;
					}
					checkFellowPlayerIDs.add(player);
				}
			}
		}
		for (UUID player : checkFellowPlayerIDs) {
			LOTRPlayerData pd = LOTRLevelData.getData(player);
			List<LOTRCustomWaypoint> cwps = pd.getCustomWaypoints();
			for (LOTRCustomWaypoint waypoint : cwps) {
				boolean inSharedFellowship = false;
				for (UUID fsID : checkFellowshipIDs) {
					if (!waypoint.hasSharedFellowship(fsID)) {
						continue;
					}
					inSharedFellowship = true;
					break;
				}
				if (!inSharedFellowship) {
					continue;
				}
				addOrUpdateSharedCustomWaypoint(waypoint.createCopyOfShared(player));
			}
		}
		return checkFellowPlayerIDs.size();
	}

	public void addSharedCustomWaypointsFromAllFellowships() {
		addSharedCustomWaypointsFrom(null, null);
	}

	public void addSharedCustomWaypointsFromAllIn(UUID onlyOneFellowshipID) {
		addSharedCustomWaypointsFrom(onlyOneFellowshipID, null);
	}

	public boolean anyMatchingFellowshipNames(String name, boolean client) {
		name = StringUtils.strip(name).toLowerCase();
		if (client) {
			for (LOTRFellowshipClient fs : fellowshipsClient) {
				String otherName = fs.getName();
				if (!name.equals(otherName = StringUtils.strip(otherName).toLowerCase())) {
					continue;
				}
				return true;
			}
		} else {
			for (UUID fsID : fellowshipIDs) {
				LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
				if (fs == null) {
					continue;
				}
				String otherName = fs.getName();
				if (!name.equals(otherName = StringUtils.strip(otherName).toLowerCase())) {
					continue;
				}
				return true;
			}
		}
		return false;
	}

	public void cancelFastTravel() {
		if (targetFTWaypoint != null) {
			setTargetFTWaypoint(null);
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				entityplayer.addChatMessage(new ChatComponentTranslation("lotr.fastTravel.motion"));
			}
		}
	}

	public boolean canCreateFellowships(boolean client) {
		int max = getMaxLeadingFellowships();
		int leading = 0;
		if (client) {
			for (LOTRFellowshipClient fs : fellowshipsClient) {
				if (!fs.isOwned() || ++leading < max) {
					continue;
				}
				return false;
			}
		} else {
			for (UUID fsID : fellowshipIDs) {
				LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
				if (fs == null || !fs.isOwner(playerUUID) || ++leading < max) {
					continue;
				}
				return false;
			}
		}
		return leading < max;
	}

	public boolean canFastTravel() {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null) {
			World world = entityplayer.worldObj;
			if (!entityplayer.capabilities.isCreativeMode) {
				double range = 16.0;
				List entities = world.getEntitiesWithinAABB(EntityLiving.class, entityplayer.boundingBox.expand(range, range, range));
				for (Object element : entities) {
					EntityLiving entityliving = (EntityLiving) element;
					if (entityliving.getAttackTarget() != entityplayer) {
						continue;
					}
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean canMakeNewPledge() {
		return pledgeBreakCooldown <= 0;
	}

	public boolean canPledgeTo(LOTRFaction fac) {
		if (fac.isPlayableAlignmentFaction()) {
			return hasPledgeAlignment(fac) && getFactionsPreventingPledgeTo(fac).isEmpty();
		}
		return false;
	}

	public void checkAlignmentAchievements(LOTRFaction faction, float prevAlignment) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			float alignment = getAlignment(faction);
			faction.checkAlignmentAchievements(entityplayer, alignment);
		}
	}

	public float checkBonusForPledgeEnemyLimit(LOTRFaction fac, float bonus) {
		if (isPledgeEnemyAlignmentLimited(fac)) {
			float limit;
			float alignment = getAlignment(fac);
			limit = getPledgeEnemyAlignmentLimit(fac);
			if (alignment > limit) {
				bonus = 0.0f;
			} else if (alignment + bonus > limit) {
				bonus = limit - alignment;
			}
		}
		return bonus;
	}

	public void checkCustomWaypointsSharedBy(List<UUID> checkSpecificPlayers) {
		ArrayList<LOTRCustomWaypoint> removes = new ArrayList<>();
		for (LOTRCustomWaypoint waypoint : customWaypointsShared) {
			LOTRCustomWaypoint wpOriginal;
			UUID waypointSharingPlayer = waypoint.getSharingPlayerID();
			if (checkSpecificPlayers != null && !checkSpecificPlayers.contains(waypointSharingPlayer) || (wpOriginal = LOTRLevelData.getData(waypointSharingPlayer).getCustomWaypointByID(waypoint.getID())) == null || wpOriginal.getPlayersInAllSharedFellowships().contains(playerUUID)) {
				continue;
			}
			removes.add(waypoint);
		}
		for (LOTRCustomWaypoint waypoint : removes) {
			removeSharedCustomWaypoint(waypoint);
		}
	}

	public void checkCustomWaypointsSharedFromFellowships() {
		checkCustomWaypointsSharedBy(null);
	}

	public void checkIfStillWaypointSharerForFellowship(LOTRFellowship fs) {
		if (!hasAnyWaypointsSharedToFellowship(fs)) {
			fs.markIsWaypointSharer(playerUUID, false);
		}
	}

	public void chooseUnwantedAlignments(EntityPlayerMP entityplayer, Set<LOTRFaction> setZeroFacs) {
		if (LOTRConfig.alignmentDrain && hasPre35Alignments && !chosenUnwantedAlignments) {
			HashSet<LOTRFaction> validSelections = new HashSet<>();
			for (LOTRFaction fac : setZeroFacs) {
				boolean valid = false;
				if (getAlignment(fac) > 0.0f) {
					for (LOTRFaction otherFac : LOTRFaction.getPlayableAlignmentFactions()) {
						if (fac == otherFac || !doFactionsDrain(fac, otherFac) || getAlignment(otherFac) <= 0.0f) {
							continue;
						}
						valid = true;
						break;
					}
				}
				if (!valid) {
					continue;
				}
				validSelections.add(fac);
			}
			for (LOTRFaction fac : validSelections) {
				setAlignment(fac, 0.0f);
				entityplayer.addChatMessage(new ChatComponentTranslation("Set %s alignment to zero", fac.factionName()));
			}
			hasPre35Alignments = false;
			chosenUnwantedAlignments = true;
			markDirty();
		}
	}

	public void completeMiniQuest(LOTRMiniQuest quest) {
		if (miniQuests.remove(quest)) {
			addMiniQuestCompleted(quest);
			++completedMiniquestCount;
			getFactionData(quest.entityFaction).completeMiniQuest();
			markDirty();
			LOTRMod.proxy.setTrackedQuest(quest);
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketMiniquestRemove packet = new LOTRPacketMiniquestRemove(quest, false, true);
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		} else {
			FMLLog.warning("Warning: Attempted to remove a miniquest which does not belong to the player data");
		}
	}

	public void createFellowship(String name, boolean normalCreation) {
		if (normalCreation && (!LOTRConfig.enableFellowshipCreation || !canCreateFellowships(false))) {
			return;
		}
		if (!anyMatchingFellowshipNames(name, false)) {
			LOTRFellowship fellowship = new LOTRFellowship(playerUUID, name);
			fellowship.createAndRegister();
		}
	}

	public void customWaypointAddSharedFellowship(LOTRCustomWaypoint waypoint, LOTRFellowship fs) {
		UUID fsID = fs.getFellowshipID();
		waypoint.addSharedFellowship(fsID);
		markDirty();
		fs.markIsWaypointSharer(playerUUID, true);
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketShareCWPClient packet = waypoint.getClientAddFellowshipPacket(fsID);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
		LOTRCustomWaypoint shareCopy = waypoint.createCopyOfShared(playerUUID);
		for (UUID player : fs.getAllPlayerUUIDs()) {
			if (player.equals(playerUUID)) {
				continue;
			}
			LOTRLevelData.getData(player).addOrUpdateSharedCustomWaypoint(shareCopy);
		}
	}

	public void customWaypointAddSharedFellowshipClient(LOTRCustomWaypoint waypoint, LOTRFellowshipClient fs) {
		waypoint.addSharedFellowship(fs.getFellowshipID());
	}

	public void customWaypointRemoveSharedFellowship(LOTRCustomWaypoint waypoint, LOTRFellowship fs) {
		UUID fsID = fs.getFellowshipID();
		waypoint.removeSharedFellowship(fsID);
		markDirty();
		checkIfStillWaypointSharerForFellowship(fs);
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketShareCWPClient packet = waypoint.getClientRemoveFellowshipPacket(fsID);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
		LOTRCustomWaypoint shareCopy = waypoint.createCopyOfShared(playerUUID);
		for (UUID player : fs.getAllPlayerUUIDs()) {
			if (player.equals(playerUUID)) {
				continue;
			}
			LOTRPlayerData pd = LOTRLevelData.getData(player);
			pd.addOrUpdateSharedCustomWaypoint(shareCopy);
			pd.checkCustomWaypointsSharedBy(ImmutableList.of(playerUUID));
		}
	}

	public void customWaypointRemoveSharedFellowshipClient(LOTRCustomWaypoint waypoint, UUID fsID) {
		waypoint.removeSharedFellowship(fsID);
	}

	public void disbandFellowship(LOTRFellowship fs, String disbanderUsername) {
		if (fs.isOwner(playerUUID)) {
			ArrayList<UUID> memberUUIDs = new ArrayList<>(fs.getMemberUUIDs());
			fs.setDisbandedAndRemoveAllMembers();
			removeFellowship(fs);
			for (UUID memberID : memberUUIDs) {
				EntityPlayer member = getOtherPlayer(memberID);
				if (member == null || member.worldObj.isRemote) {
					continue;
				}
				fs.sendNotification(member, "lotr.gui.fellowships.notifyDisband", disbanderUsername);
			}
		}
	}

	public void distributeMQEvent(LOTRMiniQuestEvent event) {
		for (LOTRMiniQuest quest : miniQuests) {
			if (!quest.isActive()) {
				continue;
			}
			quest.handleEvent(event);
		}
	}

	public boolean doesFactionPreventPledge(LOTRFaction pledgeFac, LOTRFaction otherFac) {
		return pledgeFac.isMortalEnemy(otherFac);
	}

	public boolean doFactionsDrain(LOTRFaction fac1, LOTRFaction fac2) {
		return fac1.isMortalEnemy(fac2);
	}

	public <T extends EntityLiving> T fastTravelEntity(WorldServer world, T entity, int i, int j, int k) {
		String entityID = EntityList.getEntityString(entity);
		NBTTagCompound nbt = new NBTTagCompound();
		entity.writeToNBT(nbt);
		entity.setDead();
		EntityLiving newEntity = (EntityLiving) EntityList.createEntityByName(entityID, world);
		newEntity.readFromNBT(nbt);
		newEntity.setLocationAndAngles(i + 0.5, j, k + 0.5, newEntity.rotationYaw, newEntity.rotationPitch);
		newEntity.fallDistance = 0.0f;
		newEntity.getNavigator().clearPathEntity();
		newEntity.setAttackTarget(null);
		world.spawnEntityInWorld(newEntity);
		world.updateEntityWithOptionalForce(newEntity, false);
		return (T) newEntity;
	}

	public void fastTravelTo(LOTRAbstractWaypoint waypoint) {
		EntityPlayer player = getPlayer();
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP entityplayer = (EntityPlayerMP) player;
			WorldServer world = (WorldServer) entityplayer.worldObj;
			int startX = MathHelper.floor_double(entityplayer.posX);
			int startZ = MathHelper.floor_double(entityplayer.posZ);
			double range = 256.0;
			List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class, entityplayer.boundingBox.expand(range, range, range));
			HashSet<EntityLiving> entitiesToTransport = new HashSet<>();
			for (EntityLiving entity : entities) {
				EntityTameable pet;
				if (entity instanceof LOTREntityNPC) {
					LOTREntityGollum gollum;
					LOTREntityNPC npc = (LOTREntityNPC) entity;
					if (npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == entityplayer && npc.hiredNPCInfo.shouldFollowPlayer()) {
						entitiesToTransport.add(npc);
						continue;
					}
					if (npc instanceof LOTREntityGollum && (gollum = (LOTREntityGollum) npc).getGollumOwner() == entityplayer && !gollum.isGollumSitting()) {
						entitiesToTransport.add(gollum);
						continue;
					}
				}
				if (entity instanceof EntityTameable && (pet = (EntityTameable) entity).getOwner() == entityplayer && !pet.isSitting()) {
					entitiesToTransport.add(pet);
					continue;
				}
				if (!entity.getLeashed() || entity.getLeashedToEntity() != entityplayer) {
					continue;
				}
				entitiesToTransport.add(entity);
			}
			HashSet<EntityLiving> removes = new HashSet<>();
			for (EntityLiving entity : entitiesToTransport) {
				Entity rider = entity.riddenByEntity;
				if (rider == null || !entitiesToTransport.contains(rider)) {
					continue;
				}
				removes.add(entity);
			}
			entitiesToTransport.removeAll(removes);
			int i = waypoint.getXCoord();
			int k = waypoint.getZCoord();
			world.theChunkProviderServer.provideChunk(i >> 4, k >> 4);
			int j = waypoint.getYCoord(world, i, k);
			Entity playerMount = entityplayer.ridingEntity;
			entityplayer.mountEntity(null);
			entityplayer.setPositionAndUpdate(i + 0.5, j, k + 0.5);
			entityplayer.fallDistance = 0.0f;
			if (playerMount instanceof EntityLiving) {
				playerMount = this.fastTravelEntity(world, (EntityLiving) playerMount, i, j, k);
			}
			if (playerMount != null) {
				setUUIDToMount(playerMount.getUniqueID());
			}
			for (EntityLiving entity : entitiesToTransport) {
				Entity mount = entity.ridingEntity;
				entity.mountEntity(null);
				entity = this.fastTravelEntity(world, entity, i, j, k);
				if (!(mount instanceof EntityLiving)) {
					continue;
				}
				mount = this.fastTravelEntity(world, (EntityLiving) mount, i, j, k);
				entity.mountEntity(mount);
			}
			sendFTPacket(entityplayer, waypoint, startX, startZ);
			setTimeSinceFTWithUpdate(0);
			incrementWPUseCount(waypoint);
			if (waypoint instanceof LOTRWaypoint) {
				lastWaypoint = (LOTRWaypoint) waypoint;
				markDirty();
			}
			if (waypoint instanceof LOTRCustomWaypoint) {
				LOTRCustomWaypointLogger.logTravel((EntityPlayer) entityplayer, (LOTRCustomWaypoint) waypoint);
			}
		}
	}

	public List<LOTRAchievement> getAchievements() {
		return achievements;
	}

	public List<LOTRMiniQuest> getActiveMiniQuests() {
		return selectMiniQuests(new MiniQuestSelector.OptionalActive().setActiveOnly());
	}

	public boolean getAdminHideMap() {
		return adminHideMap;
	}

	public int getAlcoholTolerance() {
		return alcoholTolerance;
	}

	public float getAlignment(LOTRFaction faction) {
		if (faction.hasFixedAlignment) {
			return faction.fixedAlignment;
		}
		Float alignment = alignments.get(faction);
		return alignment != null ? alignment : 0.0f;
	}

	public List<LOTRAbstractWaypoint> getAllAvailableWaypoints() {
		ArrayList<LOTRAbstractWaypoint> waypoints = new ArrayList<>(LOTRWaypoint.listAllWaypoints());
		waypoints.addAll(getCustomWaypoints());
		waypoints.addAll(customWaypointsShared);
		return waypoints;
	}

	public LOTRFaction getBrokenPledgeFaction() {
		return brokenPledgeFaction;
	}

	public LOTRFellowship getChatBoundFellowship() {
		LOTRFellowship fs;
		if (chatBoundFellowshipID != null && (fs = LOTRFellowshipData.getActiveFellowship(chatBoundFellowshipID)) != null) {
			return fs;
		}
		return null;
	}

	public UUID getChatBoundFellowshipID() {
		return chatBoundFellowshipID;
	}

	public LOTRFellowshipClient getClientFellowshipByID(UUID fsID) {
		for (LOTRFellowshipClient fs : fellowshipsClient) {
			if (!fs.getFellowshipID().equals(fsID)) {
				continue;
			}
			return fs;
		}
		return null;
	}

	public LOTRFellowshipClient getClientFellowshipByName(String fsName) {
		for (LOTRFellowshipClient fs : fellowshipsClient) {
			if (!fs.getName().equalsIgnoreCase(fsName)) {
				continue;
			}
			return fs;
		}
		return null;
	}

	public List<LOTRFellowshipClient> getClientFellowshipInvites() {
		return fellowshipInvitesClient;
	}

	public List<LOTRFellowshipClient> getClientFellowships() {
		return fellowshipsClient;
	}

	public int getCompletedBountyQuests() {
		return completedBountyQuests;
	}

	public int getCompletedMiniQuestsTotal() {
		return completedMiniquestCount;
	}

	public long getCurrentOnlineTime() {
		return MinecraftServer.getServer().worldServerForDimension(0).getTotalWorldTime();
	}

	public LOTRCustomWaypoint getCustomWaypointByID(int ID) {
		for (LOTRCustomWaypoint waypoint : customWaypoints) {
			if (waypoint.getID() != ID) {
				continue;
			}
			return waypoint;
		}
		return null;
	}

	public List<LOTRCustomWaypoint> getCustomWaypoints() {
		return customWaypoints;
	}

	public int getDeathDimension() {
		return deathDim;
	}

	public ChunkCoordinates getDeathPoint() {
		return deathPoint;
	}

	public List<LOTRAchievement> getEarnedAchievements(LOTRDimension dimension) {
		ArrayList<LOTRAchievement> earnedAchievements = new ArrayList<>();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null) {
			for (LOTRAchievement achievement : achievements) {
				if (achievement.getDimension() != dimension || !achievement.canPlayerEarn(entityplayer)) {
					continue;
				}
				earnedAchievements.add(achievement);
			}
		}
		return earnedAchievements;
	}

	public boolean getEnableConquestKills() {
		return conquestKills;
	}

	public boolean getEnableHiredDeathMessages() {
		return hiredDeathMessages;
	}

	public LOTRFactionData getFactionData(LOTRFaction faction) {
		LOTRFactionData data = factionDataMap.get(faction);
		if (data == null) {
			data = new LOTRFactionData(this, faction);
			factionDataMap.put(faction, data);
		}
		return data;
	}

	public List<LOTRFaction> getFactionsPreventingPledgeTo(LOTRFaction fac) {
		ArrayList<LOTRFaction> enemies = new ArrayList<>();
		for (LOTRFaction otherFac : LOTRFaction.values()) {
			if (!otherFac.isPlayableAlignmentFaction() || !doesFactionPreventPledge(fac, otherFac) || getAlignment(otherFac) <= 0.0f) {
				continue;
			}
			enemies.add(otherFac);
		}
		return enemies;
	}

	public LOTRFellowship getFellowshipByName(String fsName) {
		for (UUID fsID : fellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null || !fs.getName().equalsIgnoreCase(fsName)) {
				continue;
			}
			return fs;
		}
		return null;
	}

	public List<UUID> getFellowshipIDs() {
		return fellowshipIDs;
	}

	public List<LOTRFellowship> getFellowships() {
		ArrayList<LOTRFellowship> fellowships = new ArrayList<>();
		for (UUID fsID : fellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null) {
				continue;
			}
			fellowships.add(fs);
		}
		return fellowships;
	}

	public boolean getFemRankOverride() {
		return femRankOverride;
	}

	public boolean getFriendlyFire() {
		return friendlyFire;
	}

	public boolean getHideAlignment() {
		return hideAlignment;
	}

	public boolean getHideMapLocation() {
		return hideOnMap;
	}

	public LOTRBiome getLastKnownBiome() {
		return lastBiome;
	}

	public LOTRWaypoint getLastKnownWaypoint() {
		return lastWaypoint;
	}

	public int getMaxCustomWaypoints() {
		int achievements = getEarnedAchievements(LOTRDimension.MIDDLE_EARTH).size();
		return 5 + achievements / 5;
	}

	public int getMaxLeadingFellowships() {
		int achievements = getEarnedAchievements(LOTRDimension.MIDDLE_EARTH).size();
		return 1 + achievements / 20;
	}

	public LOTRMiniQuest getMiniQuestForID(UUID id, boolean completed) {
		ArrayList<LOTRMiniQuest> threadSafe = completed ? new ArrayList<>(miniQuestsCompleted) : new ArrayList<>(miniQuests);
		for (LOTRMiniQuest quest : threadSafe) {
			if (!quest.questUUID.equals(id)) {
				continue;
			}
			return quest;
		}
		return null;
	}

	public List<LOTRMiniQuest> getMiniQuests() {
		return miniQuests;
	}

	public List<LOTRMiniQuest> getMiniQuestsCompleted() {
		return miniQuestsCompleted;
	}

	public List<LOTRMiniQuest> getMiniQuestsForEntity(LOTREntityNPC npc, boolean activeOnly) {
		return getMiniQuestsForEntityID(npc.getUniqueID(), activeOnly);
	}

	public List<LOTRMiniQuest> getMiniQuestsForEntityID(UUID npcID, boolean activeOnly) {
		MiniQuestSelector.EntityId sel = new MiniQuestSelector.EntityId(npcID);
		if (activeOnly) {
			sel.setActiveOnly();
		}
		return selectMiniQuests(sel);
	}

	public List<LOTRMiniQuest> getMiniQuestsForFaction(LOTRFaction f, boolean activeOnly) {
		MiniQuestSelector.Faction sel = new MiniQuestSelector.Faction(new Supplier<LOTRFaction>() {

			@Override
			public LOTRFaction get() {
				return f;
			}
		});
		if (activeOnly) {
			sel.setActiveOnly();
		}
		return selectMiniQuests(sel);
	}

	public int getNextCwpID() {
		return nextCwpID;
	}

	public EntityPlayer getOtherPlayer(UUID uuid) {
		for (WorldServer world : MinecraftServer.getServer().worldServers) {
			EntityPlayer entityplayer = world.func_152378_a(uuid);
			if (entityplayer == null) {
				continue;
			}
			return entityplayer;
		}
		return null;
	}

	public EntityPlayer getPlayer() {
		World[] searchWorlds = LOTRMod.proxy.isClient() ? new World[] { LOTRMod.proxy.getClientWorld() } : MinecraftServer.getServer().worldServers;
		for (World world : searchWorlds) {
			EntityPlayer entityplayer = world.func_152378_a(playerUUID);
			if (entityplayer == null) {
				continue;
			}
			return entityplayer;
		}
		return null;
	}

	public LOTRTitle.PlayerTitle getPlayerTitle() {
		return playerTitle;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public int getPledgeBreakCooldown() {
		return pledgeBreakCooldown;
	}

	public int getPledgeBreakCooldownStart() {
		return pledgeBreakCooldownStart;
	}

	public float getPledgeEnemyAlignmentLimit(LOTRFaction fac) {
		return 0.0f;
	}

	public LOTRFaction getPledgeFaction() {
		return pledgeFaction;
	}

	public LOTRPlayerQuestData getQuestData() {
		return questData;
	}

	public LOTRFaction getRegionLastViewedFaction(LOTRDimension.DimensionRegion region) {
		LOTRFaction fac = prevRegionFactions.get(region);
		if (fac == null) {
			fac = region.factionList.get(0);
			prevRegionFactions.put(region, fac);
		}
		return fac;
	}

	public LOTRCustomWaypoint getSharedCustomWaypointByID(UUID owner, int id) {
		for (LOTRCustomWaypoint waypoint : customWaypointsShared) {
			if (!waypoint.getSharingPlayerID().equals(owner) || waypoint.getID() != id) {
				continue;
			}
			return waypoint;
		}
		return null;
	}

	public LOTRShields getShield() {
		return shield;
	}

	public boolean getStructuresBanned() {
		return structuresBanned;
	}

	public LOTRAbstractWaypoint getTargetFTWaypoint() {
		return targetFTWaypoint;
	}

	public boolean getTeleportedME() {
		return teleportedME;
	}

	public int getTicksUntilFT() {
		return ticksUntilFT;
	}

	public int getTimeSinceFT() {
		return ftSinceTick;
	}

	public LOTRMiniQuest getTrackingMiniQuest() {
		if (trackingMiniQuestID == null) {
			return null;
		}
		return getMiniQuestForID(trackingMiniQuestID, false);
	}

	public LOTRFaction getViewingFaction() {
		return viewingFaction;
	}

	public int getWaypointFTTime(LOTRAbstractWaypoint wp, EntityPlayer entityplayer) {
		int baseMin = LOTRLevelData.getWaypointCooldownMin();
		int baseMax = LOTRLevelData.getWaypointCooldownMax();
		int useCount = getWPUseCount(wp);
		double dist = entityplayer.getDistance(wp.getXCoord() + 0.5, wp.getYCoordSaved(), wp.getZCoord() + 0.5);
		double time = baseMin;
		double added = (baseMax - baseMin) * Math.pow(0.9, useCount);
		time += added;
		int seconds = (int) Math.round(time *= Math.max(1.0, dist * 1.2E-5));
		seconds = Math.max(seconds, 0);
		return seconds * 20;
	}

	public int getWPUseCount(LOTRAbstractWaypoint wp) {
		if (wp instanceof LOTRCustomWaypoint) {
			LOTRCustomWaypoint cwp = (LOTRCustomWaypoint) wp;
			int ID = cwp.getID();
			if (cwp.isShared()) {
				UUID sharingPlayer = cwp.getSharingPlayerID();
				CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
				if (cwpSharedUseCounts.containsKey(key)) {
					return cwpSharedUseCounts.get(key);
				}
			} else if (cwpUseCounts.containsKey(ID)) {
				return cwpUseCounts.get(ID);
			}
		} else if (wpUseCounts.containsKey(wp)) {
			return wpUseCounts.get(wp);
		}
		return 0;
	}

	public void givePureConquestBonus(EntityPlayer entityplayer, LOTRFaction bonusFac, LOTRFaction enemyFac, float conq, String title, double posX, double posY, double posZ) {
		conq = LOTRConquestGrid.onConquestKill(entityplayer, bonusFac, enemyFac, conq);
		getFactionData(bonusFac).addConquest(Math.abs(conq));
		if (conq != 0.0f) {
			LOTRAlignmentValues.AlignmentBonus source = new LOTRAlignmentValues.AlignmentBonus(0.0f, title);
			LOTRPacketAlignmentBonus packet = new LOTRPacketAlignmentBonus(bonusFac, getAlignment(bonusFac), new LOTRAlignmentBonusMap(), conq, posX, posY, posZ, source);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public boolean hasAchievement(LOTRAchievement achievement) {
		for (LOTRAchievement a : achievements) {
			if (a.category != achievement.category || a.ID != achievement.ID) {
				continue;
			}
			return true;
		}
		return false;
	}

	public boolean hasActiveOrCompleteMQType(Class<? extends LOTRMiniQuest> type) {
		List<LOTRMiniQuest> quests = getMiniQuests();
		List<LOTRMiniQuest> questsComplete = getMiniQuestsCompleted();
		ArrayList<LOTRMiniQuest> allQuests = new ArrayList<>();
		for (LOTRMiniQuest q : quests) {
			if (!q.isActive()) {
				continue;
			}
			allQuests.add(q);
		}
		allQuests.addAll(questsComplete);
		for (LOTRMiniQuest q : allQuests) {
			if (!type.isAssignableFrom(q.getClass())) {
				continue;
			}
			return true;
		}
		return false;
	}

	public boolean hasAnyGWQuest() {
		return hasActiveOrCompleteMQType(LOTRMiniQuestWelcome.class);
	}

	public boolean hasAnyWaypointsSharedToFellowship(LOTRFellowship fs) {
		for (LOTRCustomWaypoint waypoint : customWaypoints) {
			if (!waypoint.hasSharedFellowship(fs)) {
				continue;
			}
			return true;
		}
		return false;
	}

	public boolean hasPledgeAlignment(LOTRFaction fac) {
		float alignment = getAlignment(fac);
		return alignment >= fac.getPledgeAlignment();
	}

	public void hideOrUnhideSharedCustomWaypoint(LOTRCustomWaypoint waypoint, boolean hide) {
		if (!waypoint.isShared()) {
			FMLLog.warning("LOTR: Warning! Tried to unlock a shared custom waypoint with no owner!");
			return;
		}
		waypoint.setSharedHidden(hide);
		CWPSharedKey key = CWPSharedKey.keyFor(waypoint.getSharingPlayerID(), waypoint.getID());
		if (hide) {
			cwpSharedHidden.add(key);
		} else {
			cwpSharedHidden.remove(key);
		}
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketCWPSharedHideClient packet = waypoint.getClientSharedHidePacket(hide);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void incrementNextCwpID() {
		++nextCwpID;
		markDirty();
	}

	public void incrementWPUseCount(LOTRAbstractWaypoint wp) {
		setWPUseCount(wp, getWPUseCount(wp) + 1);
	}

	public void invitePlayerToFellowship(LOTRFellowship fs, UUID invitedPlayerUUID, String inviterUsername) {
		if (fs.isOwner(playerUUID) || fs.isAdmin(playerUUID)) {
			LOTRLevelData.getData(invitedPlayerUUID).addFellowshipInvite(fs, playerUUID, inviterUsername);
		}
	}

	public boolean isFTRegionUnlocked(LOTRWaypoint.Region region) {
		return unlockedFTRegions.contains(region);
	}

	public LOTRMaterial isPlayerWearingFull(EntityPlayer entityplayer) {
		LOTRMaterial fullMaterial = null;
		for (ItemStack itemstack : entityplayer.inventory.armorInventory) {
			if (itemstack != null && itemstack.getItem() instanceof LOTRItemArmor) {
				LOTRItemArmor armor = (LOTRItemArmor) itemstack.getItem();
				LOTRMaterial thisMaterial = armor.getLOTRArmorMaterial();
				if (fullMaterial == null) {
					fullMaterial = thisMaterial;
					continue;
				}
				if (fullMaterial == thisMaterial) {
					continue;
				}
			}
			return null;
		}
		return fullMaterial;
	}

	public boolean isPledgedTo(LOTRFaction fac) {
		return pledgeFaction == fac;
	}

	public boolean isPledgeEnemyAlignmentLimited(LOTRFaction fac) {
		return pledgeFaction != null && doesFactionPreventPledge(pledgeFaction, fac);
	}

	public boolean isSiegeActive() {
		return siegeActiveTime > 0;
	}

	public void leaveFellowship(LOTRFellowship fs) {
		if (!fs.isOwner(playerUUID)) {
			EntityPlayer entityplayer;
			EntityPlayer owner;
			fs.removeMember(playerUUID);
			if (fellowshipIDs.contains(fs.getFellowshipID())) {
				removeFellowship(fs);
			}
			entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote && (owner = getOtherPlayer(fs.getOwner())) != null) {
				fs.sendNotification(owner, "lotr.gui.fellowships.notifyLeave", entityplayer.getCommandSenderName());
			}
		}
	}

	public List<String> listAllFellowshipNames() {
		ArrayList<String> list = new ArrayList<>();
		for (UUID fsID : fellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null || !fs.containsPlayer(playerUUID)) {
				continue;
			}
			list.add(fs.getName());
		}
		return list;
	}

	public List<String> listAllLeadingFellowshipNames() {
		ArrayList<String> list = new ArrayList<>();
		for (UUID fsID : fellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null || !fs.isOwner(playerUUID)) {
				continue;
			}
			list.add(fs.getName());
		}
		return list;
	}

	public void load(NBTTagCompound playerData) {
		LOTRShields savedShield;
		UUID fsID;
		LOTRTitle title;
		LOTRFaction cur;
		LOTRFaction faction;
		alignments.clear();
		NBTTagList alignmentTags = playerData.getTagList("AlignmentMap", 10);
		for (int i = 0; i < alignmentTags.tagCount(); ++i) {
			float alignment;
			NBTTagCompound nbt = alignmentTags.getCompoundTagAt(i);
			LOTRFaction faction2 = LOTRFaction.forName(nbt.getString("Faction"));
			if (faction2 == null) {
				continue;
			}
			if (nbt.hasKey("Alignment")) {
				alignment = nbt.getInteger("Alignment");
				hasPre35Alignments = true;
			} else {
				alignment = nbt.getFloat("AlignF");
			}
			alignments.put(faction2, alignment);
		}
		factionDataMap.clear();
		NBTTagList factionDataTags = playerData.getTagList("FactionData", 10);
		for (int i = 0; i < factionDataTags.tagCount(); ++i) {
			NBTTagCompound nbt = factionDataTags.getCompoundTagAt(i);
			LOTRFaction faction3 = LOTRFaction.forName(nbt.getString("Faction"));
			if (faction3 == null) {
				continue;
			}
			LOTRFactionData data = new LOTRFactionData(this, faction3);
			data.load(nbt);
			factionDataMap.put(faction3, data);
		}
		if (playerData.hasKey("CurrentFaction") && (cur = LOTRFaction.forName(playerData.getString("CurrentFaction"))) != null) {
			viewingFaction = cur;
		}
		prevRegionFactions.clear();
		NBTTagList prevRegionFactionTags = playerData.getTagList("PrevRegionFactions", 10);
		for (int i = 0; i < prevRegionFactionTags.tagCount(); ++i) {
			NBTTagCompound nbt = prevRegionFactionTags.getCompoundTagAt(i);
			LOTRDimension.DimensionRegion region = LOTRDimension.DimensionRegion.forName(nbt.getString("Region"));
			faction = LOTRFaction.forName(nbt.getString("Faction"));
			if (region == null || faction == null) {
				continue;
			}
			prevRegionFactions.put(region, faction);
		}
		hideAlignment = playerData.getBoolean("HideAlignment");
		takenAlignmentRewards.clear();
		NBTTagList takenRewardsTags = playerData.getTagList("TakenAlignmentRewards", 10);
		for (int i = 0; i < takenRewardsTags.tagCount(); ++i) {
			NBTTagCompound nbt = takenRewardsTags.getCompoundTagAt(i);
			faction = LOTRFaction.forName(nbt.getString("Faction"));
			if (faction == null) {
				continue;
			}
			takenAlignmentRewards.add(faction);
		}
		pledgeFaction = null;
		if (playerData.hasKey("PledgeFac")) {
			pledgeFaction = LOTRFaction.forName(playerData.getString("PledgeFac"));
		}
		pledgeKillCooldown = playerData.getInteger("PledgeKillCD");
		pledgeBreakCooldown = playerData.getInteger("PledgeBreakCD");
		pledgeBreakCooldownStart = playerData.getInteger("PledgeBreakCDStart");
		brokenPledgeFaction = null;
		if (playerData.hasKey("BrokenPledgeFac")) {
			brokenPledgeFaction = LOTRFaction.forName(playerData.getString("BrokenPledgeFac"));
		}
		if (!hasPre35Alignments && playerData.hasKey("Pre35Align")) {
			hasPre35Alignments = playerData.getBoolean("Pre35Align");
		}
		if (playerData.hasKey("Chosen35Align")) {
			chosenUnwantedAlignments = playerData.getBoolean("Chosen35Align");
		}
		hideOnMap = playerData.getBoolean("HideOnMap");
		adminHideMap = playerData.getBoolean("AdminHideMap");
		if (playerData.hasKey("ShowWP")) {
			showWaypoints = playerData.getBoolean("ShowWP");
		}
		if (playerData.hasKey("ShowCWP")) {
			showCustomWaypoints = playerData.getBoolean("ShowCWP");
		}
		if (playerData.hasKey("ShowHiddenSWP")) {
			showHiddenSharedWaypoints = playerData.getBoolean("ShowHiddenSWP");
		}
		if (playerData.hasKey("ConquestKills")) {
			conquestKills = playerData.getBoolean("ConquestKills");
		}
		achievements.clear();
		NBTTagList achievementTags = playerData.getTagList("Achievements", 10);
		for (int i = 0; i < achievementTags.tagCount(); ++i) {
			NBTTagCompound nbt = achievementTags.getCompoundTagAt(i);
			String category = nbt.getString("Category");
			int ID = nbt.getInteger("ID");
			LOTRAchievement achievement = LOTRAchievement.achievementForCategoryAndID(LOTRAchievement.categoryForName(category), ID);
			if (achievement == null || achievements.contains(achievement)) {
				continue;
			}
			achievements.add(achievement);
		}
		shield = null;
		if (playerData.hasKey("Shield") && (savedShield = LOTRShields.shieldForName(playerData.getString("Shield"))) != null) {
			shield = savedShield;
		}
		if (playerData.hasKey("FriendlyFire")) {
			friendlyFire = playerData.getBoolean("FriendlyFire");
		}
		if (playerData.hasKey("HiredDeathMessages")) {
			hiredDeathMessages = playerData.getBoolean("HiredDeathMessages");
		}
		deathPoint = null;
		if (playerData.hasKey("DeathX") && playerData.hasKey("DeathY") && playerData.hasKey("DeathZ")) {
			deathPoint = new ChunkCoordinates(playerData.getInteger("DeathX"), playerData.getInteger("DeathY"), playerData.getInteger("DeathZ"));
			deathDim = playerData.hasKey("DeathDim") ? playerData.getInteger("DeathDim") : LOTRDimension.MIDDLE_EARTH.dimensionID;
		}
		alcoholTolerance = playerData.getInteger("Alcohol");
		miniQuests.clear();
		NBTTagList miniquestTags = playerData.getTagList("MiniQuests", 10);
		for (int i = 0; i < miniquestTags.tagCount(); ++i) {
			NBTTagCompound nbt = miniquestTags.getCompoundTagAt(i);
			LOTRMiniQuest quest = LOTRMiniQuest.loadQuestFromNBT(nbt, this);
			if (quest == null) {
				continue;
			}
			miniQuests.add(quest);
		}
		miniQuestsCompleted.clear();
		NBTTagList miniquestCompletedTags = playerData.getTagList("MiniQuestsCompleted", 10);
		for (int i = 0; i < miniquestCompletedTags.tagCount(); ++i) {
			NBTTagCompound nbt = miniquestCompletedTags.getCompoundTagAt(i);
			LOTRMiniQuest quest = LOTRMiniQuest.loadQuestFromNBT(nbt, this);
			if (quest == null) {
				continue;
			}
			miniQuestsCompleted.add(quest);
		}
		completedMiniquestCount = playerData.getInteger("MQCompleteCount");
		completedBountyQuests = playerData.getInteger("MQCompletedBounties");
		trackingMiniQuestID = null;
		if (playerData.hasKey("MiniQuestTrack")) {
			String s = playerData.getString("MiniQuestTrack");
			trackingMiniQuestID = UUID.fromString(s);
		}
		bountiesPlaced.clear();
		NBTTagList bountyTags = playerData.getTagList("BountiesPlaced", 8);
		for (int i = 0; i < bountyTags.tagCount(); ++i) {
			String fName = bountyTags.getStringTagAt(i);
			LOTRFaction fac = LOTRFaction.forName(fName);
			if (fac == null) {
				continue;
			}
			bountiesPlaced.add(fac);
		}
		lastWaypoint = null;
		if (playerData.hasKey("LastWP")) {
			String lastWPName = playerData.getString("LastWP");
			lastWaypoint = LOTRWaypoint.waypointForName(lastWPName);
		}
		lastBiome = null;
		if (playerData.hasKey("LastBiome")) {
			short lastBiomeID = playerData.getShort("LastBiome");
			LOTRBiome[] biomeList = LOTRDimension.MIDDLE_EARTH.biomeList;
			if (lastBiomeID >= 0 && lastBiomeID < biomeList.length) {
				lastBiome = biomeList[lastBiomeID];
			}
		}
		sentMessageTypes.clear();
		NBTTagList sentMessageTags = playerData.getTagList("SentMessageTypes", 10);
		for (int i = 0; i < sentMessageTags.tagCount(); ++i) {
			NBTTagCompound nbt = sentMessageTags.getCompoundTagAt(i);
			LOTRGuiMessageTypes message = LOTRGuiMessageTypes.forSaveName(nbt.getString("Message"));
			if (message == null) {
				continue;
			}
			boolean sent = nbt.getBoolean("Sent");
			sentMessageTypes.put(message, sent);
		}
		playerTitle = null;
		if (playerData.hasKey("PlayerTitle") && (title = LOTRTitle.forName(playerData.getString("PlayerTitle"))) != null) {
			int colorCode = playerData.getInteger("PlayerTitleColor");
			EnumChatFormatting color = LOTRTitle.PlayerTitle.colorForID(colorCode);
			playerTitle = new LOTRTitle.PlayerTitle(title, color);
		}
		if (playerData.hasKey("FemRankOverride")) {
			femRankOverride = playerData.getBoolean("FemRankOverride");
		}
		if (playerData.hasKey("FTSince")) {
			ftSinceTick = playerData.getInteger("FTSince");
		}
		targetFTWaypoint = null;
		uuidToMount = null;
		if (playerData.hasKey("MountUUID")) {
			uuidToMount = UUID.fromString(playerData.getString("MountUUID"));
		}
		uuidToMountTime = playerData.getInteger("MountUUIDTime");
		if (playerData.hasKey("LastOnlineTime")) {
			lastOnlineTime = playerData.getLong("LastOnlineTime");
		}
		unlockedFTRegions.clear();
		NBTTagList unlockedFTRegionTags = playerData.getTagList("UnlockedFTRegions", 10);
		for (int i = 0; i < unlockedFTRegionTags.tagCount(); ++i) {
			NBTTagCompound nbt = unlockedFTRegionTags.getCompoundTagAt(i);
			String regionName = nbt.getString("Name");
			LOTRWaypoint.Region region = LOTRWaypoint.regionForName(regionName);
			if (region == null) {
				continue;
			}
			unlockedFTRegions.add(region);
		}
		customWaypoints.clear();
		NBTTagList customWaypointTags = playerData.getTagList("CustomWaypoints", 10);
		for (int i = 0; i < customWaypointTags.tagCount(); ++i) {
			NBTTagCompound nbt = customWaypointTags.getCompoundTagAt(i);
			LOTRCustomWaypoint waypoint = LOTRCustomWaypoint.readFromNBT(nbt, this);
			customWaypoints.add(waypoint);
		}
		cwpSharedUnlocked.clear();
		NBTTagList cwpSharedUnlockedTags = playerData.getTagList("CWPSharedUnlocked", 10);
		for (int i = 0; i < cwpSharedUnlockedTags.tagCount(); ++i) {
			NBTTagCompound nbt = cwpSharedUnlockedTags.getCompoundTagAt(i);
			UUID sharingPlayer = UUID.fromString(nbt.getString("SharingPlayer"));
			if (sharingPlayer == null) {
				continue;
			}
			int ID = nbt.getInteger("CustomID");
			CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
			cwpSharedUnlocked.add(key);
		}
		cwpSharedHidden.clear();
		NBTTagList cwpSharedHiddenTags = playerData.getTagList("CWPSharedHidden", 10);
		for (int i = 0; i < cwpSharedHiddenTags.tagCount(); ++i) {
			NBTTagCompound nbt = cwpSharedHiddenTags.getCompoundTagAt(i);
			UUID sharingPlayer = UUID.fromString(nbt.getString("SharingPlayer"));
			if (sharingPlayer == null) {
				continue;
			}
			int ID = nbt.getInteger("CustomID");
			CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
			cwpSharedHidden.add(key);
		}
		wpUseCounts.clear();
		NBTTagList wpCooldownTags = playerData.getTagList("WPUses", 10);
		for (int i = 0; i < wpCooldownTags.tagCount(); ++i) {
			NBTTagCompound nbt = wpCooldownTags.getCompoundTagAt(i);
			String name = nbt.getString("WPName");
			int count = nbt.getInteger("Count");
			LOTRWaypoint wp = LOTRWaypoint.waypointForName(name);
			if (wp == null) {
				continue;
			}
			wpUseCounts.put(wp, count);
		}
		cwpUseCounts.clear();
		NBTTagList cwpCooldownTags = playerData.getTagList("CWPUses", 10);
		for (int i = 0; i < cwpCooldownTags.tagCount(); ++i) {
			NBTTagCompound nbt = cwpCooldownTags.getCompoundTagAt(i);
			int ID = nbt.getInteger("CustomID");
			int count = nbt.getInteger("Count");
			cwpUseCounts.put(ID, count);
		}
		cwpSharedUseCounts.clear();
		NBTTagList cwpSharedCooldownTags = playerData.getTagList("CWPSharedUses", 10);
		for (int i = 0; i < cwpSharedCooldownTags.tagCount(); ++i) {
			NBTTagCompound nbt = cwpSharedCooldownTags.getCompoundTagAt(i);
			UUID sharingPlayer = UUID.fromString(nbt.getString("SharingPlayer"));
			if (sharingPlayer == null) {
				continue;
			}
			int ID = nbt.getInteger("CustomID");
			CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
			int count = nbt.getInteger("Count");
			cwpSharedUseCounts.put(key, count);
		}
		nextCwpID = 20000;
		if (playerData.hasKey("NextCWPID")) {
			nextCwpID = playerData.getInteger("NextCWPID");
		}
		fellowshipIDs.clear();
		NBTTagList fellowshipTags = playerData.getTagList("Fellowships", 10);
		for (int i = 0; i < fellowshipTags.tagCount(); ++i) {
			NBTTagCompound nbt = fellowshipTags.getCompoundTagAt(i);
			UUID fsID2 = UUID.fromString(nbt.getString("ID"));
			if (fsID2 == null) {
				continue;
			}
			fellowshipIDs.add(fsID2);
		}
		fellowshipInvites.clear();
		NBTTagList fellowshipInviteTags = playerData.getTagList("FellowshipInvites", 10);
		for (int i = 0; i < fellowshipInviteTags.tagCount(); ++i) {
			NBTTagCompound nbt = fellowshipInviteTags.getCompoundTagAt(i);
			UUID fsID3 = UUID.fromString(nbt.getString("ID"));
			if (fsID3 == null) {
				continue;
			}
			UUID inviterID = null;
			if (nbt.hasKey("InviterID")) {
				inviterID = UUID.fromString(nbt.getString("InviterID"));
			}
			fellowshipInvites.add(new LOTRFellowshipInvite(fsID3, inviterID));
		}
		chatBoundFellowshipID = null;
		if (playerData.hasKey("ChatBoundFellowship") && (fsID = UUID.fromString(playerData.getString("ChatBoundFellowship"))) != null) {
			chatBoundFellowshipID = fsID;
		}
		structuresBanned = playerData.getBoolean("StructuresBanned");
		teleportedME = playerData.getBoolean("TeleportedME");
		if (playerData.hasKey("QuestData")) {
			NBTTagCompound questNBT = playerData.getCompoundTag("QuestData");
			questData.load(questNBT);
		}
	}

	public void lockFTRegion(LOTRWaypoint.Region region) {
		if (unlockedFTRegions.remove(region)) {
			markDirty();
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketWaypointRegion packet = new LOTRPacketWaypointRegion(region, false);
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		}
	}

	public void markDirty() {
		needsSave = true;
	}

	public boolean needsSave() {
		return needsSave;
	}

	public void onPledgeKill(EntityPlayer entityplayer) {
		pledgeKillCooldown += 24000;
		markDirty();
		if (pledgeKillCooldown > 24000) {
			revokePledgeFaction(entityplayer, false);
		} else if (pledgeFaction != null) {
			ChatComponentTranslation msg = new ChatComponentTranslation("chat.lotr.pledgeKillWarn", pledgeFaction.factionName());
			entityplayer.addChatMessage(msg);
		}
	}

	public void onUpdate(EntityPlayerMP entityplayer, WorldServer world) {
		++pdTick;
		LOTRDimension.DimensionRegion currentRegion = viewingFaction.factionRegion;
		LOTRDimension currentDim = LOTRDimension.getCurrentDimensionWithFallback(world);
		if (currentRegion.getDimension() != currentDim) {
			currentRegion = currentDim.dimensionRegions.get(0);
			setViewingFaction(getRegionLastViewedFaction(currentRegion));
		}
		runAlignmentDraining(entityplayer);
		questData.onUpdate(entityplayer, world);
		if (!isSiegeActive()) {
			runAchievementChecks(entityplayer, world);
		}
		if (LOTRMod.playerDetailsCache.getPlayerDetails(playerUUID).isReceivedFromApi() && playerTitle != null && !playerTitle.getTitle().canPlayerUse(entityplayer)) {
			ChatComponentTranslation msg = new ChatComponentTranslation("chat.lotr.loseTitle", playerTitle.getFullTitleComponent(entityplayer));
			entityplayer.addChatMessage(msg);
			setPlayerTitle(null);
		}
		if (pledgeKillCooldown > 0) {
			--pledgeKillCooldown;
			if (pledgeKillCooldown == 0 || LOTRPlayerData.isTimerAutosaveTick()) {
				markDirty();
			}
		}
		if (pledgeBreakCooldown > 0) {
			setPledgeBreakCooldown(pledgeBreakCooldown - 1);
		}
		if (trackingMiniQuestID != null && getTrackingMiniQuest() == null) {
			setTrackingMiniQuest(null);
		}
		List<LOTRMiniQuest> activeMiniquests = getActiveMiniQuests();
		for (LOTRMiniQuest quest : activeMiniquests) {
			quest.onPlayerTick(entityplayer);
		}
		if (!bountiesPlaced.isEmpty()) {
			for (LOTRFaction fac : bountiesPlaced) {
				ChatComponentTranslation msg = new ChatComponentTranslation("chat.lotr.bountyPlaced", fac.factionName());
				msg.getChatStyle().setColor(EnumChatFormatting.YELLOW);
				entityplayer.addChatMessage(msg);
			}
			bountiesPlaced.clear();
			markDirty();
		}
		this.setTimeSinceFT(ftSinceTick + 1);
		if (targetFTWaypoint != null) {
			if (entityplayer.isPlayerSleeping()) {
				entityplayer.addChatMessage(new ChatComponentTranslation("lotr.fastTravel.inBed"));
				setTargetFTWaypoint(null);
			} else if (ticksUntilFT > 0) {
				int seconds = ticksUntilFT / 20;
				if (ticksUntilFT == ticksUntilFT_max) {
					entityplayer.addChatMessage(new ChatComponentTranslation("lotr.fastTravel.travelTicksStart", seconds));
				} else if (ticksUntilFT % 20 == 0 && seconds <= 5) {
					entityplayer.addChatMessage(new ChatComponentTranslation("lotr.fastTravel.travelTicks", seconds));
				}
				--ticksUntilFT;
				setTicksUntilFT(ticksUntilFT);
			} else {
				sendFTBouncePacket(entityplayer);
			}
		} else {
			setTicksUntilFT(0);
		}
		lastOnlineTime = getCurrentOnlineTime();
		if (uuidToMount != null) {
			if (uuidToMountTime > 0) {
				--uuidToMountTime;
			} else {
				double range = 32.0;
				List entities = world.getEntitiesWithinAABB(EntityLivingBase.class, entityplayer.boundingBox.expand(range, range, range));
				for (Object obj : entities) {
					Entity entity = (Entity) obj;
					if (!entity.getUniqueID().equals(uuidToMount)) {
						continue;
					}
					entityplayer.mountEntity(entity);
					break;
				}
				setUUIDToMount(null);
			}
		}
		if (pdTick % 24000 == 0 && alcoholTolerance > 0) {
			--alcoholTolerance;
			setAlcoholTolerance(alcoholTolerance);
		}
		unlockSharedCustomWaypoints(entityplayer);
		if (pdTick % 100 == 0 && world.provider instanceof LOTRWorldProvider) {
			int i = MathHelper.floor_double(entityplayer.posX);
			int k = MathHelper.floor_double(entityplayer.posZ);
			LOTRBiome biome = (LOTRBiome) world.provider.getBiomeGenForCoords(i, k);
			if (biome.biomeDimension == LOTRDimension.MIDDLE_EARTH) {
				LOTRBiome prevLastBiome = lastBiome;
				lastBiome = biome;
				if (prevLastBiome != biome) {
					markDirty();
				}
			}
		}
		if (adminHideMap) {
			boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(entityplayer.getGameProfile());
			if (!entityplayer.capabilities.isCreativeMode || !isOp) {
				setAdminHideMap(false);
				LOTRCommandAdminHideMap.notifyUnhidden(entityplayer);
			}
		}
		if (siegeActiveTime > 0) {
			--siegeActiveTime;
		}
	}

	public void placeBountyFor(LOTRFaction f) {
		bountiesPlaced.add(f);
		markDirty();
	}

	public void receiveFTBouncePacket() {
		if (targetFTWaypoint != null && ticksUntilFT <= 0) {
			fastTravelTo(targetFTWaypoint);
			setTargetFTWaypoint(null);
		}
	}

	public void rejectFellowshipInvite(LOTRFellowship fs) {
		UUID fsID = fs.getFellowshipID();
		LOTRFellowshipInvite existingInvite = null;
		for (LOTRFellowshipInvite invite : fellowshipInvites) {
			if (!invite.fellowshipID.equals(fsID)) {
				continue;
			}
			existingInvite = invite;
			break;
		}
		if (existingInvite != null) {
			fellowshipInvites.remove(existingInvite);
			markDirty();
			sendFellowshipInviteRemovePacket(fs);
		}
	}

	public void removeAchievement(LOTRAchievement achievement) {
		if (!hasAchievement(achievement)) {
			return;
		}
		if (achievements.remove(achievement)) {
			markDirty();
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				sendAchievementRemovePacket((EntityPlayerMP) entityplayer, achievement);
			}
		}
	}

	public void removeClientFellowship(UUID fsID) {
		LOTRFellowshipClient inList = null;
		for (LOTRFellowshipClient fsInList : fellowshipsClient) {
			if (!fsInList.getFellowshipID().equals(fsID)) {
				continue;
			}
			inList = fsInList;
			break;
		}
		if (inList != null) {
			fellowshipsClient.remove(inList);
		}
	}

	public void removeClientFellowshipInvite(UUID fsID) {
		LOTRFellowshipClient inList = null;
		for (LOTRFellowshipClient fsInList : fellowshipInvitesClient) {
			if (!fsInList.getFellowshipID().equals(fsID)) {
				continue;
			}
			inList = fsInList;
			break;
		}
		if (inList != null) {
			fellowshipInvitesClient.remove(inList);
		}
	}

	public void removeCustomWaypoint(LOTRCustomWaypoint waypoint) {
		if (customWaypoints.remove(waypoint)) {
			markDirty();
			for (UUID fsID : waypoint.getSharedFellowshipIDs()) {
				LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
				if (fs == null) {
					continue;
				}
				checkIfStillWaypointSharerForFellowship(fs);
			}
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketDeleteCWPClient packet = waypoint.getClientDeletePacket();
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
				LOTRCustomWaypointLogger.logDelete(entityplayer, waypoint);
			}
			LOTRCustomWaypoint shareCopy = waypoint.createCopyOfShared(playerUUID);
			List<UUID> sharedPlayers = shareCopy.getPlayersInAllSharedFellowships();
			for (UUID sharedPlayerUUID : sharedPlayers) {
				EntityPlayer sharedPlayer = getOtherPlayer(sharedPlayerUUID);
				if (sharedPlayer == null || sharedPlayer.worldObj.isRemote) {
					continue;
				}
				LOTRLevelData.getData(sharedPlayerUUID).removeSharedCustomWaypoint(shareCopy);
			}
		}
	}

	public void removeCustomWaypointClient(LOTRCustomWaypoint waypoint) {
		customWaypoints.remove(waypoint);
	}

	public void removeFellowship(LOTRFellowship fs) {
		UUID fsID;
		if ((fs.isDisbanded() || !fs.containsPlayer(playerUUID)) && fellowshipIDs.contains(fsID = fs.getFellowshipID())) {
			fellowshipIDs.remove(fsID);
			markDirty();
			sendFellowshipRemovePacket(fs);
			unshareFellowshipFromOwnCustomWaypoints(fs);
			checkCustomWaypointsSharedFromFellowships();
		}
	}

	public void removeMiniQuest(LOTRMiniQuest quest, boolean completed) {
		List<LOTRMiniQuest> removeList = completed ? miniQuestsCompleted : miniQuests;
		if (removeList.remove(quest)) {
			markDirty();
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketMiniquestRemove packet = new LOTRPacketMiniquestRemove(quest, quest.isCompleted(), false);
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		} else {
			FMLLog.warning("Warning: Attempted to remove a miniquest which does not belong to the player data");
		}
	}

	public void removePlayerFromFellowship(LOTRFellowship fs, UUID player, String removerUsername) {
		if (fs.isOwner(playerUUID) || fs.isAdmin(playerUUID)) {
			fs.removeMember(player);
			EntityPlayer removed = getOtherPlayer(player);
			if (removed != null && !removed.worldObj.isRemote) {
				fs.sendNotification(removed, "lotr.gui.fellowships.notifyRemove", removerUsername);
			}
		}
	}

	public void removeSharedCustomWaypoint(LOTRCustomWaypoint waypoint) {
		if (!waypoint.isShared()) {
			FMLLog.warning("LOTR: Warning! Tried to remove a shared custom waypoint with no owner!");
			return;
		}
		LOTRCustomWaypoint existing = customWaypointsShared.contains(waypoint) ? waypoint : getSharedCustomWaypointByID(waypoint.getSharingPlayerID(), waypoint.getID());
		if (existing != null) {
			customWaypointsShared.remove(existing);
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketDeleteCWPClient packet = waypoint.getClientDeletePacketShared();
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		} else {
			FMLLog.warning("LOTR: Warning! Tried to remove a shared custom waypoint that does not exist!");
		}
	}

	public void renameCustomWaypoint(LOTRCustomWaypoint waypoint, String newName) {
		waypoint.rename(newName);
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketRenameCWPClient packet = waypoint.getClientRenamePacket();
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			LOTRCustomWaypointLogger.logRename(entityplayer, waypoint);
		}
		LOTRCustomWaypoint shareCopy = waypoint.createCopyOfShared(playerUUID);
		List<UUID> sharedPlayers = shareCopy.getPlayersInAllSharedFellowships();
		for (UUID sharedPlayerUUID : sharedPlayers) {
			EntityPlayer sharedPlayer = getOtherPlayer(sharedPlayerUUID);
			if (sharedPlayer == null || sharedPlayer.worldObj.isRemote) {
				continue;
			}
			LOTRLevelData.getData(sharedPlayerUUID).renameSharedCustomWaypoint(shareCopy, newName);
		}
	}

	public void renameCustomWaypointClient(LOTRCustomWaypoint waypoint, String newName) {
		waypoint.rename(newName);
	}

	public void renameFellowship(LOTRFellowship fs, String name) {
		if (fs.isOwner(playerUUID)) {
			fs.setName(name);
		}
	}

	public void renameSharedCustomWaypoint(LOTRCustomWaypoint waypoint, String newName) {
		if (!waypoint.isShared()) {
			FMLLog.warning("LOTR: Warning! Tried to rename a shared custom waypoint with no owner!");
			return;
		}
		waypoint.rename(newName);
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketRenameCWPClient packet = waypoint.getClientRenamePacketShared();
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void revokePledgeFaction(EntityPlayer entityplayer, boolean intentional) {
		LOTRFaction wasPledge = pledgeFaction;
		float pledgeLvl = wasPledge.getPledgeAlignment();
		float prevAlign = getAlignment(wasPledge);
		float diff = prevAlign - pledgeLvl;
		float cd = diff / 5000.0f;
		cd = MathHelper.clamp_float(cd, 0.0f, 1.0f);
		int cdTicks = 36000;
		setPledgeFaction(null);
		setBrokenPledgeFaction(wasPledge);
		setPledgeBreakCooldown(cdTicks += Math.round(cd * 150.0f * 60.0f * 20.0f));
		World world = entityplayer.worldObj;
		if (!world.isRemote) {
			ChatComponentTranslation msg;
			LOTRFactionRank rank = wasPledge.getRank(prevAlign);
			LOTRFactionRank rankBelow = wasPledge.getRankBelow(rank);
			LOTRFactionRank rankBelow2 = wasPledge.getRankBelow(rankBelow);
			float newAlign = rankBelow2.alignment;
			float alignPenalty = (newAlign = Math.max(newAlign, pledgeLvl / 2.0f)) - prevAlign;
			if (alignPenalty < 0.0f) {
				LOTRAlignmentValues.AlignmentBonus penalty = LOTRAlignmentValues.createPledgePenalty(alignPenalty);
				double alignX = 0.0;
				double alignY = 0.0;
				double alignZ = 0.0;
				double lookRange = 2.0;
				Vec3 posEye = Vec3.createVectorHelper(entityplayer.posX, entityplayer.boundingBox.minY + entityplayer.getEyeHeight(), entityplayer.posZ);
				Vec3 look = entityplayer.getLook(1.0f);
				Vec3 posSight = posEye.addVector(look.xCoord * lookRange, look.yCoord * lookRange, look.zCoord * lookRange);
				MovingObjectPosition mop = world.rayTraceBlocks(posEye, posSight);
				if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					alignX = mop.blockX + 0.5;
					alignY = mop.blockY + 0.5;
					alignZ = mop.blockZ + 0.5;
				} else {
					alignX = posSight.xCoord;
					alignY = posSight.yCoord;
					alignZ = posSight.zCoord;
				}
				this.addAlignment(entityplayer, penalty, wasPledge, alignX, alignY, alignZ);
			}
			world.playSoundAtEntity(entityplayer, "lotr:event.unpledge", 1.0f, 1.0f);
			if (intentional) {
				msg = new ChatComponentTranslation("chat.lotr.unpledge", wasPledge.factionName());
			} else {
				msg = new ChatComponentTranslation("chat.lotr.autoUnpledge", wasPledge.factionName());
			}
			entityplayer.addChatMessage(msg);
			checkAlignmentAchievements(wasPledge, prevAlign);
		}
	}

	public void runAchievementChecks(EntityPlayer entityplayer, World world) {
		LOTRMaterial fullMaterial;
		int i = MathHelper.floor_double(entityplayer.posX);
		MathHelper.floor_double(entityplayer.boundingBox.minY);
		int k = MathHelper.floor_double(entityplayer.posZ);
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		if (biome instanceof LOTRBiome) {
			LOTRWaypoint.Region biomeRegion;
			LOTRBiome lotrbiome = (LOTRBiome) biome;
			LOTRAchievement ach = lotrbiome.getBiomeAchievement();
			if (ach != null) {
				addAchievement(ach);
			}
			biomeRegion = lotrbiome.getBiomeWaypoints();
			if (biomeRegion != null) {
				unlockFTRegion(biomeRegion);
			}
		}
		if (entityplayer.dimension == LOTRDimension.MIDDLE_EARTH.dimensionID) {
			addAchievement(LOTRAchievement.enterMiddleEarth);
		}
		if (entityplayer.dimension == LOTRDimension.UTUMNO.dimensionID) {
			addAchievement(LOTRAchievement.enterUtumnoIce);
			int y = MathHelper.floor_double(entityplayer.boundingBox.minY);
			LOTRUtumnoLevel level = LOTRUtumnoLevel.forY(y);
			if (level == LOTRUtumnoLevel.OBSIDIAN) {
				addAchievement(LOTRAchievement.enterUtumnoObsidian);
			} else if (level == LOTRUtumnoLevel.FIRE) {
				addAchievement(LOTRAchievement.enterUtumnoFire);
			}
		}
		if (entityplayer.inventory.hasItem(LOTRMod.pouch)) {
			addAchievement(LOTRAchievement.getPouch);
		}
		HashSet<Block> tables = new HashSet<>();
		int crossbowBolts = 0;
		for (ItemStack item : entityplayer.inventory.mainInventory) {
			Block block;
			if (item != null && item.getItem() instanceof ItemBlock && (block = Block.getBlockFromItem(item.getItem())) instanceof LOTRBlockCraftingTable) {
				tables.add(block);
			}
			if (item == null || !(item.getItem() instanceof LOTRItemCrossbowBolt)) {
				continue;
			}
			crossbowBolts += item.stackSize;
		}
		if (tables.size() >= 10) {
			addAchievement(LOTRAchievement.collectCraftingTables);
		}
		if (crossbowBolts >= 128) {
			addAchievement(LOTRAchievement.collectCrossbowBolts);
		}
		if (!hasAchievement(LOTRAchievement.hundreds) && pdTick % 20 == 0) {
			int hiredUnits = 0;
			List<LOTREntityNPC> nearbyNPCs = world.getEntitiesWithinAABB(LOTREntityNPC.class, entityplayer.boundingBox.expand(64.0, 64.0, 64.0));
			for (LOTREntityNPC npc : nearbyNPCs) {
				if (!npc.hiredNPCInfo.isActive || npc.hiredNPCInfo.getHiringPlayer() != entityplayer) {
					continue;
				}
				++hiredUnits;
			}
			if (hiredUnits >= 100) {
				addAchievement(LOTRAchievement.hundreds);
			}
		}
		if (biome instanceof LOTRBiomeGenMistyMountains && entityplayer.posY > 192.0) {
			addAchievement(LOTRAchievement.climbMistyMountains);
		}
		fullMaterial = isPlayerWearingFull(entityplayer);
		if (fullMaterial != null) {
			if (fullMaterial == LOTRMaterial.MITHRIL) {
				addAchievement(LOTRAchievement.wearFullMithril);
			} else if (fullMaterial == LOTRMaterial.FUR) {
				addAchievement(LOTRAchievement.wearFullFur);
			} else if (fullMaterial == LOTRMaterial.BLUE_DWARVEN) {
				addAchievement(LOTRAchievement.wearFullBlueDwarven);
			} else if (fullMaterial == LOTRMaterial.HIGH_ELVEN) {
				addAchievement(LOTRAchievement.wearFullHighElven);
			} else if (fullMaterial == LOTRMaterial.GONDOLIN) {
				addAchievement(LOTRAchievement.wearFullGondolin);
			} else if (fullMaterial == LOTRMaterial.GALVORN) {
				addAchievement(LOTRAchievement.wearFullGalvorn);
			} else if (fullMaterial == LOTRMaterial.RANGER) {
				addAchievement(LOTRAchievement.wearFullRanger);
			} else if (fullMaterial == LOTRMaterial.GUNDABAD_URUK) {
				addAchievement(LOTRAchievement.wearFullGundabadUruk);
			} else if (fullMaterial == LOTRMaterial.ARNOR) {
				addAchievement(LOTRAchievement.wearFullArnor);
			} else if (fullMaterial == LOTRMaterial.RIVENDELL) {
				addAchievement(LOTRAchievement.wearFullRivendell);
			} else if (fullMaterial == LOTRMaterial.ANGMAR) {
				addAchievement(LOTRAchievement.wearFullAngmar);
			} else if (fullMaterial == LOTRMaterial.WOOD_ELVEN_SCOUT) {
				addAchievement(LOTRAchievement.wearFullWoodElvenScout);
			} else if (fullMaterial == LOTRMaterial.WOOD_ELVEN) {
				addAchievement(LOTRAchievement.wearFullWoodElven);
			} else if (fullMaterial == LOTRMaterial.DOL_GULDUR) {
				addAchievement(LOTRAchievement.wearFullDolGuldur);
			} else if (fullMaterial == LOTRMaterial.DALE) {
				addAchievement(LOTRAchievement.wearFullDale);
			} else if (fullMaterial == LOTRMaterial.DWARVEN) {
				addAchievement(LOTRAchievement.wearFullDwarven);
			} else if (fullMaterial == LOTRMaterial.GALADHRIM) {
				addAchievement(LOTRAchievement.wearFullElven);
			} else if (fullMaterial == LOTRMaterial.HITHLAIN) {
				addAchievement(LOTRAchievement.wearFullHithlain);
			} else if (fullMaterial == LOTRMaterial.URUK) {
				addAchievement(LOTRAchievement.wearFullUruk);
			} else if (fullMaterial == LOTRMaterial.ROHAN) {
				addAchievement(LOTRAchievement.wearFullRohirric);
			} else if (fullMaterial == LOTRMaterial.ROHAN_MARSHAL) {
				addAchievement(LOTRAchievement.wearFullRohirricMarshal);
			} else if (fullMaterial == LOTRMaterial.DUNLENDING) {
				addAchievement(LOTRAchievement.wearFullDunlending);
			} else if (fullMaterial == LOTRMaterial.GONDOR) {
				addAchievement(LOTRAchievement.wearFullGondorian);
			} else if (fullMaterial == LOTRMaterial.DOL_AMROTH) {
				addAchievement(LOTRAchievement.wearFullDolAmroth);
			} else if (fullMaterial == LOTRMaterial.RANGER_ITHILIEN) {
				addAchievement(LOTRAchievement.wearFullRangerIthilien);
			} else if (fullMaterial == LOTRMaterial.LOSSARNACH) {
				addAchievement(LOTRAchievement.wearFullLossarnach);
			} else if (fullMaterial == LOTRMaterial.PELARGIR) {
				addAchievement(LOTRAchievement.wearFullPelargir);
			} else if (fullMaterial == LOTRMaterial.PINNATH_GELIN) {
				addAchievement(LOTRAchievement.wearFullPinnathGelin);
			} else if (fullMaterial == LOTRMaterial.BLACKROOT) {
				addAchievement(LOTRAchievement.wearFullBlackroot);
			} else if (fullMaterial == LOTRMaterial.LAMEDON) {
				addAchievement(LOTRAchievement.wearFullLamedon);
			} else if (fullMaterial == LOTRMaterial.MORDOR) {
				addAchievement(LOTRAchievement.wearFullOrc);
			} else if (fullMaterial == LOTRMaterial.MORGUL) {
				addAchievement(LOTRAchievement.wearFullMorgul);
			} else if (fullMaterial == LOTRMaterial.BLACK_URUK) {
				addAchievement(LOTRAchievement.wearFullBlackUruk);
			} else if (fullMaterial == LOTRMaterial.DORWINION) {
				addAchievement(LOTRAchievement.wearFullDorwinion);
			} else if (fullMaterial == LOTRMaterial.DORWINION_ELF) {
				addAchievement(LOTRAchievement.wearFullDorwinionElf);
			} else if (fullMaterial == LOTRMaterial.RHUN) {
				addAchievement(LOTRAchievement.wearFullRhun);
			} else if (fullMaterial == LOTRMaterial.RHUN_GOLD) {
				addAchievement(LOTRAchievement.wearFullRhunGold);
			} else if (fullMaterial == LOTRMaterial.NEAR_HARAD) {
				addAchievement(LOTRAchievement.wearFullNearHarad);
			} else if (fullMaterial == LOTRMaterial.GULF_HARAD) {
				addAchievement(LOTRAchievement.wearFullGulfHarad);
			} else if (fullMaterial == LOTRMaterial.CORSAIR) {
				addAchievement(LOTRAchievement.wearFullCorsair);
			} else if (fullMaterial == LOTRMaterial.UMBAR) {
				addAchievement(LOTRAchievement.wearFullUmbar);
			} else if (fullMaterial == LOTRMaterial.HARNEDOR) {
				addAchievement(LOTRAchievement.wearFullHarnedor);
			} else if (fullMaterial == LOTRMaterial.HARAD_NOMAD) {
				addAchievement(LOTRAchievement.wearFullNomad);
			} else if (fullMaterial == LOTRMaterial.BLACK_NUMENOREAN) {
				addAchievement(LOTRAchievement.wearFullBlackNumenorean);
			} else if (fullMaterial == LOTRMaterial.MOREDAIN) {
				addAchievement(LOTRAchievement.wearFullMoredain);
			} else if (fullMaterial == LOTRMaterial.TAUREDAIN) {
				addAchievement(LOTRAchievement.wearFullTauredain);
			} else if (fullMaterial == LOTRMaterial.TAUREDAIN_GOLD) {
				addAchievement(LOTRAchievement.wearFullTaurethrimGold);
			} else if (fullMaterial == LOTRMaterial.HALF_TROLL) {
				addAchievement(LOTRAchievement.wearFullHalfTroll);
			} else if (fullMaterial == LOTRMaterial.UTUMNO) {
				addAchievement(LOTRAchievement.wearFullUtumno);
			}
		}
	}

	public void runAlignmentDraining(EntityPlayerMP entityplayer) {
		if (LOTRConfig.alignmentDrain && pdTick % 1000 == 0) {
			ArrayList<LOTRFaction> drainFactions = new ArrayList<>();
			List<LOTRFaction> allFacs = LOTRFaction.getPlayableAlignmentFactions();
			for (LOTRFaction fac1 : allFacs) {
				for (LOTRFaction fac2 : allFacs) {
					if (!doFactionsDrain(fac1, fac2)) {
						continue;
					}
					float align1 = getAlignment(fac1);
					float align2 = getAlignment(fac2);
					if (align1 <= 0.0f || align2 <= 0.0f) {
						continue;
					}
					if (!drainFactions.contains(fac1)) {
						drainFactions.add(fac1);
					}
					if (drainFactions.contains(fac2)) {
						continue;
					}
					drainFactions.add(fac2);
				}
			}
			if (!drainFactions.isEmpty()) {
				for (LOTRFaction fac : drainFactions) {
					float align = getAlignment(fac);
					float alignLoss = 5.0f;
					alignLoss = Math.min(alignLoss, align - 0.0f);
					setAlignment(fac, align -= alignLoss);
				}
				sendMessageIfNotReceived(LOTRGuiMessageTypes.ALIGN_DRAIN);
				LOTRPacketAlignDrain packet = new LOTRPacketAlignDrain(drainFactions.size());
				LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
			}
		}
	}

	public void save(NBTTagCompound playerData) {
		NBTTagList alignmentTags = new NBTTagList();
		for (Map.Entry<LOTRFaction, Float> entry : alignments.entrySet()) {
			LOTRFaction faction = entry.getKey();
			float alignment = entry.getValue();
			NBTTagCompound nbt6 = new NBTTagCompound();
			nbt6.setString("Faction", faction.codeName());
			nbt6.setFloat("AlignF", alignment);
			alignmentTags.appendTag(nbt6);
		}
		playerData.setTag("AlignmentMap", alignmentTags);
		NBTTagList factionDataTags = new NBTTagList();
		for (Map.Entry<LOTRFaction, LOTRFactionData> entry : factionDataMap.entrySet()) {
			LOTRFaction faction = entry.getKey();
			LOTRFactionData data = entry.getValue();
			NBTTagCompound nbt4 = new NBTTagCompound();
			nbt4.setString("Faction", faction.codeName());
			data.save(nbt4);
			factionDataTags.appendTag(nbt4);
		}
		playerData.setTag("FactionData", factionDataTags);
		playerData.setString("CurrentFaction", viewingFaction.codeName());
		NBTTagList prevRegionFactionTags = new NBTTagList();
		for (Map.Entry<LOTRDimension.DimensionRegion, LOTRFaction> entry : prevRegionFactions.entrySet()) {
			LOTRDimension.DimensionRegion region = entry.getKey();
			LOTRFaction faction = entry.getValue();
			NBTTagCompound nbt3 = new NBTTagCompound();
			nbt3.setString("Region", region.codeName());
			nbt3.setString("Faction", faction.codeName());
			prevRegionFactionTags.appendTag(nbt3);
		}
		playerData.setTag("PrevRegionFactions", prevRegionFactionTags);
		playerData.setBoolean("HideAlignment", hideAlignment);
		NBTTagList takenRewardsTags = new NBTTagList();
		for (LOTRFaction faction : takenAlignmentRewards) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Faction", faction.codeName());
			takenRewardsTags.appendTag(nbt);
		}
		playerData.setTag("TakenAlignmentRewards", takenRewardsTags);
		if (pledgeFaction != null) {
			playerData.setString("PledgeFac", pledgeFaction.codeName());
		}
		playerData.setInteger("PledgeKillCD", pledgeKillCooldown);
		playerData.setInteger("PledgeBreakCD", pledgeBreakCooldown);
		playerData.setInteger("PledgeBreakCDStart", pledgeBreakCooldownStart);
		if (brokenPledgeFaction != null) {
			playerData.setString("BrokenPledgeFac", brokenPledgeFaction.codeName());
		}
		playerData.setBoolean("Pre35Align", hasPre35Alignments);
		playerData.setBoolean("Chosen35Align", chosenUnwantedAlignments);
		playerData.setBoolean("HideOnMap", hideOnMap);
		playerData.setBoolean("AdminHideMap", adminHideMap);
		playerData.setBoolean("ShowWP", showWaypoints);
		playerData.setBoolean("ShowCWP", showCustomWaypoints);
		playerData.setBoolean("ShowHiddenSWP", showHiddenSharedWaypoints);
		playerData.setBoolean("ConquestKills", conquestKills);
		NBTTagList achievementTags = new NBTTagList();
		for (LOTRAchievement achievement : achievements) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Category", achievement.category.name());
			nbt.setInteger("ID", achievement.ID);
			achievementTags.appendTag(nbt);
		}
		playerData.setTag("Achievements", achievementTags);
		if (shield != null) {
			playerData.setString("Shield", shield.name());
		}
		playerData.setBoolean("FriendlyFire", friendlyFire);
		playerData.setBoolean("HiredDeathMessages", hiredDeathMessages);
		if (deathPoint != null) {
			playerData.setInteger("DeathX", deathPoint.posX);
			playerData.setInteger("DeathY", deathPoint.posY);
			playerData.setInteger("DeathZ", deathPoint.posZ);
			playerData.setInteger("DeathDim", deathDim);
		}
		playerData.setInteger("Alcohol", alcoholTolerance);
		NBTTagList miniquestTags = new NBTTagList();
		for (LOTRMiniQuest quest : miniQuests) {
			NBTTagCompound nbt7 = new NBTTagCompound();
			quest.writeToNBT(nbt7);
			miniquestTags.appendTag(nbt7);
		}
		playerData.setTag("MiniQuests", miniquestTags);
		NBTTagList miniquestCompletedTags = new NBTTagList();
		for (LOTRMiniQuest quest : miniQuestsCompleted) {
			NBTTagCompound nbt8 = new NBTTagCompound();
			quest.writeToNBT(nbt8);
			miniquestCompletedTags.appendTag(nbt8);
		}
		playerData.setTag("MiniQuestsCompleted", miniquestCompletedTags);
		playerData.setInteger("MQCompleteCount", completedMiniquestCount);
		playerData.setInteger("MQCompletedBounties", completedBountyQuests);
		if (trackingMiniQuestID != null) {
			playerData.setString("MiniQuestTrack", trackingMiniQuestID.toString());
		}
		NBTTagList bountyTags = new NBTTagList();
		for (LOTRFaction fac : bountiesPlaced) {
			String fName = fac.codeName();
			bountyTags.appendTag(new NBTTagString(fName));
		}
		playerData.setTag("BountiesPlaced", bountyTags);
		if (lastWaypoint != null) {
			String lastWPName = lastWaypoint.getCodeName();
			playerData.setString("LastWP", lastWPName);
		}
		if (lastBiome != null) {
			int lastBiomeID = lastBiome.biomeID;
			playerData.setShort("LastBiome", (short) lastBiomeID);
		}
		NBTTagList sentMessageTags = new NBTTagList();
		for (Map.Entry<LOTRGuiMessageTypes, Boolean> entry : sentMessageTypes.entrySet()) {
			LOTRGuiMessageTypes message = entry.getKey();
			boolean sent = entry.getValue();
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Message", message.getSaveName());
			nbt.setBoolean("Sent", sent);
			sentMessageTags.appendTag(nbt);
		}
		playerData.setTag("SentMessageTypes", sentMessageTags);
		if (playerTitle != null) {
			playerData.setString("PlayerTitle", playerTitle.getTitle().getTitleName());
			playerData.setInteger("PlayerTitleColor", playerTitle.getColor().getFormattingCode());
		}
		playerData.setBoolean("FemRankOverride", femRankOverride);
		playerData.setInteger("FTSince", ftSinceTick);
		if (uuidToMount != null) {
			playerData.setString("MountUUID", uuidToMount.toString());
		}
		playerData.setInteger("MountUUIDTime", uuidToMountTime);
		playerData.setLong("LastOnlineTime", lastOnlineTime);
		NBTTagList unlockedFTRegionTags = new NBTTagList();
		for (LOTRWaypoint.Region region : unlockedFTRegions) {
			NBTTagCompound nbt9 = new NBTTagCompound();
			nbt9.setString("Name", region.name());
			unlockedFTRegionTags.appendTag(nbt9);
		}
		playerData.setTag("UnlockedFTRegions", unlockedFTRegionTags);
		NBTTagList customWaypointTags = new NBTTagList();
		for (LOTRCustomWaypoint waypoint : customWaypoints) {
			NBTTagCompound nbt = new NBTTagCompound();
			waypoint.writeToNBT(nbt, this);
			customWaypointTags.appendTag(nbt);
		}
		playerData.setTag("CustomWaypoints", customWaypointTags);
		NBTTagList cwpSharedUnlockedTags = new NBTTagList();
		for (CWPSharedKey key : cwpSharedUnlocked) {
			NBTTagCompound nbt10 = new NBTTagCompound();
			nbt10.setString("SharingPlayer", key.sharingPlayer.toString());
			nbt10.setInteger("CustomID", key.waypointID);
			cwpSharedUnlockedTags.appendTag(nbt10);
		}
		playerData.setTag("CWPSharedUnlocked", cwpSharedUnlockedTags);
		NBTTagList cwpSharedHiddenTags = new NBTTagList();
		for (CWPSharedKey key : cwpSharedHidden) {
			NBTTagCompound nbt11 = new NBTTagCompound();
			nbt11.setString("SharingPlayer", key.sharingPlayer.toString());
			nbt11.setInteger("CustomID", key.waypointID);
			cwpSharedHiddenTags.appendTag(nbt11);
		}
		playerData.setTag("CWPSharedHidden", cwpSharedHiddenTags);
		NBTTagList wpUseTags = new NBTTagList();
		for (Map.Entry<LOTRWaypoint, Integer> e : wpUseCounts.entrySet()) {
			LOTRAbstractWaypoint wp = e.getKey();
			int count = e.getValue();
			NBTTagCompound nbt12 = new NBTTagCompound();
			nbt12.setString("WPName", wp.getCodeName());
			nbt12.setInteger("Count", count);
			wpUseTags.appendTag(nbt12);
		}
		playerData.setTag("WPUses", wpUseTags);
		NBTTagList cwpUseTags = new NBTTagList();
		for (Map.Entry<Integer, Integer> e : cwpUseCounts.entrySet()) {
			int ID = e.getKey();
			int count = e.getValue();
			NBTTagCompound nbt5 = new NBTTagCompound();
			nbt5.setInteger("CustomID", ID);
			nbt5.setInteger("Count", count);
			cwpUseTags.appendTag(nbt5);
		}
		playerData.setTag("CWPUses", cwpUseTags);
		NBTTagList cwpSharedUseTags = new NBTTagList();
		for (Map.Entry<CWPSharedKey, Integer> e : cwpSharedUseCounts.entrySet()) {
			CWPSharedKey key = e.getKey();
			int count = e.getValue();
			NBTTagCompound nbt2 = new NBTTagCompound();
			nbt2.setString("SharingPlayer", key.sharingPlayer.toString());
			nbt2.setInteger("CustomID", key.waypointID);
			nbt2.setInteger("Count", count);
			cwpSharedUseTags.appendTag(nbt2);
		}
		playerData.setTag("CWPSharedUses", cwpSharedUseTags);
		playerData.setInteger("NextCWPID", nextCwpID);
		NBTTagList fellowshipTags = new NBTTagList();
		for (UUID fsID : fellowshipIDs) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("ID", fsID.toString());
			fellowshipTags.appendTag(nbt);
		}
		playerData.setTag("Fellowships", fellowshipTags);
		NBTTagList fellowshipInviteTags = new NBTTagList();
		for (LOTRFellowshipInvite invite : fellowshipInvites) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("ID", invite.fellowshipID.toString());
			if (invite.inviterID != null) {
				nbt.setString("InviterID", invite.inviterID.toString());
			}
			fellowshipInviteTags.appendTag(nbt);
		}
		playerData.setTag("FellowshipInvites", fellowshipInviteTags);
		if (chatBoundFellowshipID != null) {
			playerData.setString("ChatBoundFellowship", chatBoundFellowshipID.toString());
		}
		playerData.setBoolean("StructuresBanned", structuresBanned);
		playerData.setBoolean("TeleportedME", teleportedME);
		NBTTagCompound questNBT = new NBTTagCompound();
		questData.save(questNBT);
		playerData.setTag("QuestData", questNBT);
		needsSave = false;
	}

	public List<LOTRMiniQuest> selectMiniQuests(MiniQuestSelector selector) {
		ArrayList<LOTRMiniQuest> ret = new ArrayList<>();
		ArrayList<LOTRMiniQuest> threadSafe = new ArrayList<>(miniQuests);
		for (LOTRMiniQuest quest : threadSafe) {
			if (!selector.include(quest)) {
				continue;
			}
			ret.add(quest);
		}
		return ret;
	}

	public void send35AlignmentChoice(EntityPlayerMP entityplayer, World world) {
		if (LOTRConfig.alignmentDrain && hasPre35Alignments && !chosenUnwantedAlignments) {
			LOTRPacketAlignmentChoiceOffer pkt = new LOTRPacketAlignmentChoiceOffer();
			LOTRPacketHandler.networkWrapper.sendTo(pkt, entityplayer);
		}
	}

	public void sendAchievementPacket(EntityPlayerMP entityplayer, LOTRAchievement achievement, boolean display) {
		LOTRPacketAchievement packet = new LOTRPacketAchievement(achievement, display);
		LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
	}

	public void sendAchievementRemovePacket(EntityPlayerMP entityplayer, LOTRAchievement achievement) {
		LOTRPacketAchievementRemove packet = new LOTRPacketAchievementRemove(achievement);
		LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
	}

	public void sendAlignmentBonusPacket(LOTRAlignmentValues.AlignmentBonus source, LOTRFaction faction, float prevMainAlignment, LOTRAlignmentBonusMap factionMap, float conqBonus, double posX, double posY, double posZ) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null) {
			LOTRPacketAlignmentBonus packet = new LOTRPacketAlignmentBonus(faction, prevMainAlignment, factionMap, conqBonus, posX, posY, posZ, source);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void sendFellowshipInvitePacket(LOTRFellowship fs) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketFellowship packet = new LOTRPacketFellowship(this, fs, true);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void sendFellowshipInviteRemovePacket(LOTRFellowship fs) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketFellowshipRemove packet = new LOTRPacketFellowshipRemove(fs, true);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void sendFellowshipPacket(LOTRFellowship fs) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketFellowship packet = new LOTRPacketFellowship(this, fs, false);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void sendFellowshipRemovePacket(LOTRFellowship fs) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketFellowshipRemove packet = new LOTRPacketFellowshipRemove(fs, false);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void sendFTBouncePacket(EntityPlayerMP entityplayer) {
		LOTRPacketFTBounceClient packet = new LOTRPacketFTBounceClient();
		LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
	}

	public void sendFTPacket(EntityPlayerMP entityplayer, LOTRAbstractWaypoint waypoint, int startX, int startZ) {
		LOTRPacketFTScreen packet = new LOTRPacketFTScreen(waypoint, startX, startZ);
		LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
	}

	public void sendMessageIfNotReceived(LOTRGuiMessageTypes message) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			Boolean sent = sentMessageTypes.get(message);
			if (sent == null) {
				sent = false;
				sentMessageTypes.put(message, sent);
			}
			if (!sent.booleanValue()) {
				sentMessageTypes.put(message, true);
				markDirty();
				LOTRPacketMessage packet = new LOTRPacketMessage(message);
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		}
	}

	public void sendMiniQuestPacket(EntityPlayerMP entityplayer, LOTRMiniQuest quest, boolean completed) throws IOException {
		NBTTagCompound nbt = new NBTTagCompound();
		quest.writeToNBT(nbt);
		LOTRPacketMiniquest packet = new LOTRPacketMiniquest(nbt, completed);
		LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
	}

	public void sendOptionsPacket(int option, boolean flag) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketOptions packet = new LOTRPacketOptions(option, flag);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void sendPlayerData(EntityPlayerMP entityplayer) throws IOException {
		NBTTagCompound nbt = new NBTTagCompound();
		save(nbt);
		nbt.removeTag("Achievements");
		nbt.removeTag("MiniQuests");
		nbt.removeTag("MiniQuestsCompleted");
		nbt.removeTag("CustomWaypoints");
		nbt.removeTag("Fellowships");
		nbt.removeTag("FellowshipInvites");
		LOTRPacketLoginPlayerData packet = new LOTRPacketLoginPlayerData(nbt);
		LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
		for (LOTRAchievement achievement : achievements) {
			sendAchievementPacket(entityplayer, achievement, false);
		}
		for (LOTRMiniQuest quest : miniQuests) {
			sendMiniQuestPacket(entityplayer, quest, false);
		}
		for (LOTRMiniQuest quest : miniQuestsCompleted) {
			sendMiniQuestPacket(entityplayer, quest, true);
		}
		for (LOTRCustomWaypoint waypoint : customWaypoints) {
			LOTRPacketCreateCWPClient cwpPacket = waypoint.getClientPacket();
			LOTRPacketHandler.networkWrapper.sendTo(cwpPacket, entityplayer);
		}
		for (UUID fsID : fellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null) {
				continue;
			}
			sendFellowshipPacket(fs);
			fs.doRetroactiveWaypointSharerCheckIfNeeded();
			checkIfStillWaypointSharerForFellowship(fs);
		}
		HashSet<LOTRFellowshipInvite> staleFellowshipInvites = new HashSet<>();
		for (LOTRFellowshipInvite invite : fellowshipInvites) {
			LOTRFellowship fs = LOTRFellowshipData.getFellowship(invite.fellowshipID);
			if (fs != null) {
				sendFellowshipInvitePacket(fs);
				continue;
			}
			staleFellowshipInvites.add(invite);
		}
		if (!staleFellowshipInvites.isEmpty()) {
			fellowshipInvites.removeAll(staleFellowshipInvites);
			markDirty();
		}
		addSharedCustomWaypointsFromAllFellowships();
	}

	public void setAdminHideMap(boolean flag) {
		adminHideMap = flag;
		markDirty();
	}

	public void setAlcoholTolerance(int i) {
		EntityPlayer entityplayer;
		alcoholTolerance = i;
		markDirty();
		if (alcoholTolerance >= 250 && (entityplayer = getPlayer()) != null && !entityplayer.worldObj.isRemote) {
			addAchievement(LOTRAchievement.gainHighAlcoholTolerance);
		}
	}

	public void setAlignment(LOTRFaction faction, float alignment) {
		EntityPlayer entityplayer = getPlayer();
		if (faction.isPlayableAlignmentFaction()) {
			float prevAlignment = getAlignment(faction);
			alignments.put(faction, alignment);
			markDirty();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRLevelData.sendAlignmentToAllPlayersInWorld(entityplayer, entityplayer.worldObj);
			}
			checkAlignmentAchievements(faction, prevAlignment);
		}
		if (entityplayer != null && !entityplayer.worldObj.isRemote && pledgeFaction != null && !canPledgeTo(pledgeFaction)) {
			revokePledgeFaction(entityplayer, false);
		}
	}

	public void setAlignmentFromCommand(LOTRFaction faction, float set) {
		setAlignment(faction, set);
	}

	public void setBrokenPledgeFaction(LOTRFaction f) {
		brokenPledgeFaction = f;
		markDirty();
	}

	public void setChatBoundFellowship(LOTRFellowship fs) {
		setChatBoundFellowshipID(fs.getFellowshipID());
	}

	public void setChatBoundFellowshipID(UUID fsID) {
		chatBoundFellowshipID = fsID;
		markDirty();
	}

	public void setDeathDimension(int dim) {
		deathDim = dim;
		markDirty();
	}

	public void setDeathPoint(int i, int j, int k) {
		deathPoint = new ChunkCoordinates(i, j, k);
		markDirty();
	}

	public void setEnableConquestKills(boolean flag) {
		conquestKills = flag;
		markDirty();
		sendOptionsPacket(5, flag);
	}

	public void setEnableHiredDeathMessages(boolean flag) {
		hiredDeathMessages = flag;
		markDirty();
		sendOptionsPacket(1, flag);
	}

	public void setFellowshipAdmin(LOTRFellowship fs, UUID player, boolean flag, String granterUsername) {
		if (fs.isOwner(playerUUID)) {
			fs.setAdmin(player, flag);
			EntityPlayer subjectPlayer = getOtherPlayer(player);
			if (subjectPlayer != null && !subjectPlayer.worldObj.isRemote) {
				if (flag) {
					fs.sendNotification(subjectPlayer, "lotr.gui.fellowships.notifyOp", granterUsername);
				} else {
					fs.sendNotification(subjectPlayer, "lotr.gui.fellowships.notifyDeop", granterUsername);
				}
			}
		}
	}

	public void setFellowshipIcon(LOTRFellowship fs, ItemStack itemstack) {
		if (fs.isOwner(playerUUID) || fs.isAdmin(playerUUID)) {
			fs.setIcon(itemstack);
		}
	}

	public void setFellowshipPreventHiredFF(LOTRFellowship fs, boolean prevent) {
		if (fs.isOwner(playerUUID) || fs.isAdmin(playerUUID)) {
			fs.setPreventHiredFriendlyFire(prevent);
		}
	}

	public void setFellowshipPreventPVP(LOTRFellowship fs, boolean prevent) {
		if (fs.isOwner(playerUUID) || fs.isAdmin(playerUUID)) {
			fs.setPreventPVP(prevent);
		}
	}

	public void setFellowshipShowMapLocations(LOTRFellowship fs, boolean show) {
		if (fs.isOwner(playerUUID)) {
			fs.setShowMapLocations(show);
		}
	}

	public void setFemRankOverride(boolean flag) {
		femRankOverride = flag;
		markDirty();
		sendOptionsPacket(4, flag);
	}

	public void setFriendlyFire(boolean flag) {
		friendlyFire = flag;
		markDirty();
		sendOptionsPacket(0, flag);
	}

	public void setHideAlignment(boolean flag) {
		hideAlignment = flag;
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRLevelData.sendAlignmentToAllPlayersInWorld(entityplayer, entityplayer.worldObj);
		}
	}

	public void setHideMapLocation(boolean flag) {
		hideOnMap = flag;
		markDirty();
		sendOptionsPacket(3, flag);
	}

	public void setPlayerTitle(LOTRTitle.PlayerTitle title) {
		playerTitle = title;
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketTitle packet = new LOTRPacketTitle(playerTitle);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
		for (UUID fsID : fellowshipIDs) {
			LOTRFellowship fs = LOTRFellowshipData.getActiveFellowship(fsID);
			if (fs == null) {
				continue;
			}
			fs.updateForAllMembers(new FellowshipUpdateType.UpdatePlayerTitle(playerUUID, playerTitle));
		}
	}

	public void setPledgeBreakCooldown(int i) {
		boolean bigChange;
		EntityPlayer entityplayer;
		int preCD = pledgeBreakCooldown;
		LOTRFaction preBroken = brokenPledgeFaction;
		pledgeBreakCooldown = i = Math.max(0, i);
		bigChange = (pledgeBreakCooldown == 0 || preCD == 0) && pledgeBreakCooldown != preCD;
		if (pledgeBreakCooldown > pledgeBreakCooldownStart) {
			setPledgeBreakCooldownStart(pledgeBreakCooldown);
			bigChange = true;
		}
		if (pledgeBreakCooldown <= 0 && preBroken != null) {
			setPledgeBreakCooldownStart(0);
			setBrokenPledgeFaction(null);
			bigChange = true;
		}
		if (bigChange || LOTRPlayerData.isTimerAutosaveTick()) {
			markDirty();
		}
		if ((bigChange || pledgeBreakCooldown % 5 == 0) && (entityplayer = getPlayer()) != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketBrokenPledge packet = new LOTRPacketBrokenPledge(pledgeBreakCooldown, pledgeBreakCooldownStart, brokenPledgeFaction);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
		if (pledgeBreakCooldown == 0 && preCD != pledgeBreakCooldown && (entityplayer = getPlayer()) != null && !entityplayer.worldObj.isRemote) {
			String brokenName = preBroken == null ? StatCollector.translateToLocal("lotr.gui.factions.pledgeUnknown") : preBroken.factionName();
			ChatComponentTranslation msg = new ChatComponentTranslation("chat.lotr.pledgeBreakCooldown", brokenName);
			entityplayer.addChatMessage(msg);
		}
	}

	public void setPledgeBreakCooldownStart(int i) {
		pledgeBreakCooldownStart = i;
		markDirty();
	}

	public void setPledgeFaction(LOTRFaction fac) {
		EntityPlayer entityplayer;
		pledgeFaction = fac;
		pledgeKillCooldown = 0;
		markDirty();
		if (fac != null) {
			checkAlignmentAchievements(fac, getAlignment(fac));
			addAchievement(LOTRAchievement.pledgeService);
		}
		entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			if (fac != null) {
				World world = entityplayer.worldObj;
				world.playSoundAtEntity(entityplayer, "lotr:event.pledge", 1.0f, 1.0f);
			}
			LOTRPacketPledge packet = new LOTRPacketPledge(fac);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void setRegionLastViewedFaction(LOTRDimension.DimensionRegion region, LOTRFaction fac) {
		if (region.factionList.contains(fac)) {
			prevRegionFactions.put(region, fac);
			markDirty();
		}
	}

	public void setShield(LOTRShields lotrshield) {
		shield = lotrshield;
		markDirty();
	}

	public void setShowCustomWaypoints(boolean flag) {
		showCustomWaypoints = flag;
		markDirty();
	}

	public void setShowHiddenSharedWaypoints(boolean flag) {
		showHiddenSharedWaypoints = flag;
		markDirty();
	}

	public void setShowWaypoints(boolean flag) {
		showWaypoints = flag;
		markDirty();
	}

	public void setSiegeActive(int duration) {
		siegeActiveTime = Math.max(siegeActiveTime, duration);
	}

	public void setStructuresBanned(boolean flag) {
		structuresBanned = flag;
		markDirty();
	}

	public void setTargetFTWaypoint(LOTRAbstractWaypoint wp) {
		targetFTWaypoint = wp;
		markDirty();
		if (wp != null) {
			setTicksUntilFT(ticksUntilFT_max);
		} else {
			setTicksUntilFT(0);
		}
	}

	public void setTeleportedME(boolean flag) {
		teleportedME = flag;
		markDirty();
	}

	public void setTicksUntilFT(int i) {
		if (ticksUntilFT != i) {
			ticksUntilFT = i;
			if (ticksUntilFT == ticksUntilFT_max || ticksUntilFT == 0) {
				markDirty();
			}
		}
	}

	public void setTimeSinceFT(int i) {
		this.setTimeSinceFT(i, false);
	}

	public void setTimeSinceFT(int i, boolean forceUpdate) {
		boolean bigChange;
		EntityPlayer entityplayer;
		int preTick = ftSinceTick;
		ftSinceTick = i = Math.max(0, i);
		bigChange = (ftSinceTick == 0 || preTick == 0) && ftSinceTick != preTick || preTick < 0 && ftSinceTick >= 0;
		if (bigChange || LOTRPlayerData.isTimerAutosaveTick() || forceUpdate) {
			markDirty();
		}
		if ((bigChange || ftSinceTick % 5 == 0 || forceUpdate) && (entityplayer = getPlayer()) != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketFTTimer packet = new LOTRPacketFTTimer(ftSinceTick);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void setTimeSinceFTWithUpdate(int i) {
		this.setTimeSinceFT(i, true);
	}

	public void setTrackingMiniQuest(LOTRMiniQuest quest) {
		setTrackingMiniQuestID(quest == null ? null : quest.questUUID);
	}

	public void setTrackingMiniQuestID(UUID npcID) {
		trackingMiniQuestID = npcID;
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketMiniquestTrackClient packet = new LOTRPacketMiniquestTrackClient(trackingMiniQuestID);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void setUUIDToMount(UUID uuid) {
		uuidToMount = uuid;
		uuidToMountTime = uuidToMount != null ? 10 : 0;
		markDirty();
	}

	public void setViewingFaction(LOTRFaction faction) {
		if (faction != null) {
			viewingFaction = faction;
			markDirty();
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketUpdateViewingFaction packet = new LOTRPacketUpdateViewingFaction(viewingFaction);
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		}
	}

	public void setWPUseCount(LOTRAbstractWaypoint wp, int count) {
		if (wp instanceof LOTRCustomWaypoint) {
			LOTRCustomWaypoint cwp = (LOTRCustomWaypoint) wp;
			int ID = cwp.getID();
			if (cwp.isShared()) {
				UUID sharingPlayer = cwp.getSharingPlayerID();
				CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
				cwpSharedUseCounts.put(key, count);
			} else {
				cwpUseCounts.put(ID, count);
			}
		} else {
			wpUseCounts.put((LOTRWaypoint) wp, count);
		}
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketWaypointUseCount packet = new LOTRPacketWaypointUseCount(wp, count);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public boolean showCustomWaypoints() {
		return showCustomWaypoints;
	}

	public boolean showHiddenSharedWaypoints() {
		return showHiddenSharedWaypoints;
	}

	public boolean showWaypoints() {
		return showWaypoints;
	}

	public void transferFellowship(LOTRFellowship fs, UUID player, String prevOwnerUsername) {
		if (fs.isOwner(playerUUID)) {
			fs.setOwner(player);
			EntityPlayer newOwner = getOtherPlayer(player);
			if (newOwner != null && !newOwner.worldObj.isRemote) {
				fs.sendNotification(newOwner, "lotr.gui.fellowships.notifyTransfer", prevOwnerUsername);
			}
		}
	}

	public void unlockFTRegion(LOTRWaypoint.Region region) {
		if (isSiegeActive()) {
			return;
		}
		if (unlockedFTRegions.add(region)) {
			markDirty();
			EntityPlayer entityplayer = getPlayer();
			if (entityplayer != null && !entityplayer.worldObj.isRemote) {
				LOTRPacketWaypointRegion packet = new LOTRPacketWaypointRegion(region, true);
				LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
			}
		}
	}

	public void unlockSharedCustomWaypoint(LOTRCustomWaypoint waypoint) {
		if (!waypoint.isShared()) {
			FMLLog.warning("LOTR: Warning! Tried to unlock a shared custom waypoint with no owner!");
			return;
		}
		waypoint.setSharedUnlocked();
		CWPSharedKey key = CWPSharedKey.keyFor(waypoint.getSharingPlayerID(), waypoint.getID());
		cwpSharedUnlocked.add(key);
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			LOTRPacketCWPSharedUnlockClient packet = waypoint.getClientSharedUnlockPacket();
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void unlockSharedCustomWaypoints(EntityPlayer entityplayer) {
		if (pdTick % 20 == 0 && entityplayer.dimension == LOTRDimension.MIDDLE_EARTH.dimensionID) {
			ArrayList<LOTRCustomWaypoint> unlockWaypoints = new ArrayList<>();
			for (LOTRCustomWaypoint waypoint : customWaypointsShared) {
				if (!waypoint.isShared() || waypoint.isSharedUnlocked() || !waypoint.canUnlockShared(entityplayer)) {
					continue;
				}
				unlockWaypoints.add(waypoint);
			}
			for (LOTRCustomWaypoint waypoint : unlockWaypoints) {
				unlockSharedCustomWaypoint(waypoint);
			}
		}
	}

	public void unshareFellowshipFromOwnCustomWaypoints(LOTRFellowship fs) {
		for (LOTRCustomWaypoint waypoint : customWaypoints) {
			if (!waypoint.hasSharedFellowship(fs)) {
				continue;
			}
			customWaypointRemoveSharedFellowship(waypoint, fs);
		}
	}

	public void updateFactionData(LOTRFaction faction, LOTRFactionData factionData) {
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			markDirty();
			NBTTagCompound nbt = new NBTTagCompound();
			factionData.save(nbt);
			LOTRPacketFactionData packet = new LOTRPacketFactionData(faction, nbt);
			LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
		}
	}

	public void updateFastTravelClockFromLastOnlineTime(EntityPlayerMP player, World world) {
		int ftClockIncrease;
		int diff;
		if (lastOnlineTime <= 0L) {
			return;
		}
		MinecraftServer server = MinecraftServer.getServer();
		if (!server.isSinglePlayer() && (ftClockIncrease = (int) ((diff = (int) (getCurrentOnlineTime() - lastOnlineTime)) * 0.1)) > 0) {
			setTimeSinceFTWithUpdate(ftSinceTick + ftClockIncrease);
			ChatComponentTranslation msg = new ChatComponentTranslation("chat.lotr.ft.offlineTick", LOTRLevelData.getHMSTime_Ticks(diff), LOTRLevelData.getHMSTime_Ticks(ftClockIncrease));
			player.addChatMessage(msg);
		}
	}

	public void updateFellowship(LOTRFellowship fs, FellowshipUpdateType updateType) {
		List<UUID> playersToCheckSharedWaypointsFrom;
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			IMessage updatePacket = updateType.createUpdatePacket(this, fs);
			if (updatePacket != null) {
				LOTRPacketHandler.networkWrapper.sendTo(updatePacket, (EntityPlayerMP) entityplayer);
			} else {
				LOTRLog.logger.error("No associated packet for fellowship update type " + updateType.getClass().getName());
			}
		}
		playersToCheckSharedWaypointsFrom = updateType.getPlayersToCheckSharedWaypointsFrom(fs);
		if (playersToCheckSharedWaypointsFrom != null && !playersToCheckSharedWaypointsFrom.isEmpty()) {
			addSharedCustomWaypointsFrom(fs.getFellowshipID(), playersToCheckSharedWaypointsFrom);
			checkCustomWaypointsSharedBy(playersToCheckSharedWaypointsFrom);
		}
	}

	public void updateMiniQuest(LOTRMiniQuest quest) {
		markDirty();
		EntityPlayer entityplayer = getPlayer();
		if (entityplayer != null && !entityplayer.worldObj.isRemote) {
			try {
				sendMiniQuestPacket((EntityPlayerMP) entityplayer, quest, false);
			} catch (IOException e) {
				FMLLog.severe("Error sending miniquest packet to player " + entityplayer.getCommandSenderName());
				e.printStackTrace();
			}
		}
	}

	public boolean useFeminineRanks() {
		if (femRankOverride) {
			return true;
		}
		if (playerTitle != null) {
			LOTRTitle title = playerTitle.getTitle();
			return title.isFeminineRank();
		}
		return false;
	}

	public static boolean isTimerAutosaveTick() {
		return MinecraftServer.getServer() != null && MinecraftServer.getServer().getTickCounter() % 200 == 0;
	}

	public static class CWPSharedKey extends Pair<UUID, Integer> {
		public UUID sharingPlayer;
		public int waypointID;

		public CWPSharedKey(UUID player, int id) {
			sharingPlayer = player;
			waypointID = id;
		}

		@Override
		public UUID getLeft() {
			return sharingPlayer;
		}

		@Override
		public Integer getRight() {
			return waypointID;
		}

		@Override
		public Integer setValue(Integer value) {
			throw new UnsupportedOperationException();
		}

		public static CWPSharedKey keyFor(UUID player, int id) {
			return new CWPSharedKey(player, id);
		}
	}

}
