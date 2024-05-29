package lotr.common.world.map;

import java.io.*;
import java.nio.charset.Charset;
import java.text.*;
import java.util.*;

import com.google.common.io.Files;

import cpw.mods.fml.common.FMLLog;
import lotr.common.LOTRConfig;
import lotr.common.fellowship.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;

public class LOTRCustomWaypointLogger {
	public static Charset CHARSET = Charset.forName("UTF-8");
	public static DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM");
	public static DateFormat MONTH_DATE_FORMAT = new SimpleDateFormat("MM-dd");
	public static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public static void log(String function, EntityPlayer entityplayer, LOTRCustomWaypoint cwp) {
		if (!LOTRConfig.cwpLog) {
			return;
		}
		try {
			File logFile;
			File dupeLogDir;
			Date date = Calendar.getInstance().getTime();
			StringBuilder logLine = new StringBuilder().append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", MONTH_DATE_FORMAT.format(date), TIME_FORMAT.format(date), function, entityplayer.getCommandSenderName(), entityplayer.getPersistentID(), cwp.getCodeName(), cwp.getXCoord(), cwp.getYCoordSaved(), cwp.getZCoord(), cwp.isShared(), cwp.isShared() ? cwp.getSharingPlayerName() : "N/A", cwp.isShared() ? cwp.getSharingPlayerID() : "N/A"));
			if (cwp.isShared()) {
				List<UUID> fsIDs = cwp.getSharedFellowshipIDs();
				for (UUID id : fsIDs) {
					LOTRFellowship fellowship = LOTRFellowshipData.getActiveFellowship(id);
					if (fellowship == null || !fellowship.containsPlayer(entityplayer.getUniqueID())) {
						continue;
					}
					logLine.append(",");
					logLine.append(fellowship.getName());
				}
			}
			if (!(dupeLogDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "lotr_cwp_logs")).exists()) {
				dupeLogDir.mkdirs();
			}
			if (!(logFile = new File(dupeLogDir, DATE_FORMAT.format(date) + ".csv")).exists()) {
				Files.append("date,time,function,username,UUID,wp_name,x,y,z,shared,sharer_name,sharer_UUID,common_fellowships" + System.lineSeparator(), logFile, CHARSET);
			}
			Files.append(logLine.append(System.lineSeparator()).toString(), logFile, CHARSET);
		} catch (IOException e) {
			FMLLog.warning("Error logging custom waypoint activities");
			e.printStackTrace();
		}
	}

	public static void logCreate(EntityPlayer entityplayer, LOTRCustomWaypoint cwp) {
		LOTRCustomWaypointLogger.log("CREATE", entityplayer, cwp);
	}

	public static void logDelete(EntityPlayer entityplayer, LOTRCustomWaypoint cwp) {
		LOTRCustomWaypointLogger.log("DELETE", entityplayer, cwp);
	}

	public static void logRename(EntityPlayer entityplayer, LOTRCustomWaypoint cwp) {
		LOTRCustomWaypointLogger.log("RENAME", entityplayer, cwp);
	}

	public static void logTravel(EntityPlayer entityplayer, LOTRCustomWaypoint cwp) {
		LOTRCustomWaypointLogger.log("TRAVEL", entityplayer, cwp);
	}
}
