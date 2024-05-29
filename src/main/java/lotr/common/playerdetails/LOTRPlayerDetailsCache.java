package lotr.common.playerdetails;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;

public class LOTRPlayerDetailsCache {
	public Map<UUID, PlayerDetails> detailsForPlayer = new HashMap<>();
	public PlayerDetailsApiClient apiClient = new PlayerDetailsApiClient();
	public Set<UUID> apiRequestedPlayers = new HashSet<>();

	public void fireRequest(UUID playerId) {
		if (!apiRequestedPlayers.contains(playerId)) {
			apiRequestedPlayers.add(playerId);
			apiClient.getPlayerDetailsAsync(playerId, new PlayerDetailsApiClient.Callback() {

				@Override
				public void accept(PlayerDetails details) {
					detailsForPlayer.put(playerId, details);
				}
			});
		}
	}

	public PlayerDetails getPlayerDetails(EntityPlayer player) {
		return this.getPlayerDetails(player.getUniqueID());
	}

	public PlayerDetails getPlayerDetails(UUID playerId) {
		if (detailsForPlayer.containsKey(playerId)) {
			return detailsForPlayer.get(playerId);
		}
		fireRequest(playerId);
		return PlayerDetails.getPlaceholder(playerId);
	}

}
