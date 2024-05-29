package lotr.client;

import java.util.*;

import lotr.common.*;
import lotr.common.fac.LOTRFaction;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRAlignmentTicker {
	public static Map<LOTRFaction, LOTRAlignmentTicker> allFactionTickers = new HashMap<>();
	public LOTRFaction theFac;
	public float oldAlign;
	public float newAlign;
	public int moveTick = 0;
	public int prevMoveTick = 0;
	public int flashTick;
	public int numericalTick;

	public LOTRAlignmentTicker(LOTRFaction f) {
		theFac = f;
	}

	public float getInterpolatedAlignment(float f) {
		if (moveTick == 0) {
			return oldAlign;
		}
		float tickF = prevMoveTick + (moveTick - prevMoveTick) * f;
		tickF /= 20.0f;
		tickF = 1.0f - tickF;
		return oldAlign + (newAlign - oldAlign) * tickF;
	}

	public void update(EntityPlayer entityplayer, boolean forceInstant) {
		float curAlign = LOTRLevelData.getData(entityplayer).getAlignment(theFac);
		if (forceInstant) {
			oldAlign = newAlign = curAlign;
			moveTick = 0;
			prevMoveTick = 0;
			flashTick = 0;
			numericalTick = 0;
		} else {
			if (newAlign != curAlign) {
				oldAlign = newAlign;
				newAlign = curAlign;
				moveTick = 20;
				prevMoveTick = 20;
				flashTick = 30;
				numericalTick = 200;
			}
			prevMoveTick = moveTick;
			if (moveTick > 0) {
				--moveTick;
				if (moveTick <= 0) {
					oldAlign = newAlign;
				}
			}
			if (flashTick > 0) {
				--flashTick;
			}
			if (numericalTick > 0) {
				--numericalTick;
			}
		}
	}

	public static LOTRAlignmentTicker forFaction(LOTRFaction fac) {
		LOTRAlignmentTicker ticker = allFactionTickers.get(fac);
		if (ticker == null) {
			ticker = new LOTRAlignmentTicker(fac);
			allFactionTickers.put(fac, ticker);
		}
		return ticker;
	}

	public static void updateAll(EntityPlayer entityplayer, boolean forceInstant) {
		for (LOTRDimension dim : LOTRDimension.values()) {
			for (LOTRFaction fac : dim.factionList) {
				LOTRAlignmentTicker.forFaction(fac).update(entityplayer, forceInstant);
			}
		}
	}
}
