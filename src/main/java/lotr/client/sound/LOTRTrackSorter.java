package lotr.client.sound;

import java.util.*;

import lotr.common.world.biome.LOTRMusicRegion;

public class LOTRTrackSorter {
	public static Filter forAny() {
		return new Filter() {

			@Override
			public boolean accept(LOTRMusicTrack track) {
				return true;
			}
		};
	}

	public static Filter forRegionAndCategory(LOTRMusicRegion reg, LOTRMusicCategory cat) {
		return new Filter() {

			@Override
			public boolean accept(LOTRMusicTrack track) {
				return track.getRegionInfo(reg).getCategories().contains(cat);
			}
		};
	}

	public static List<LOTRMusicTrack> sortTracks(List<LOTRMusicTrack> tracks, Filter filter) {
		ArrayList<LOTRMusicTrack> sorted = new ArrayList<>();
		for (LOTRMusicTrack track : tracks) {
			if (!filter.accept(track)) {
				continue;
			}
			sorted.add(track);
		}
		return sorted;
	}

	public interface Filter {
		boolean accept(LOTRMusicTrack var1);
	}

}
