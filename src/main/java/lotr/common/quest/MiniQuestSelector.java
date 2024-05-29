package lotr.common.quest;

import java.util.UUID;

import com.google.common.base.Supplier;

import lotr.common.fac.LOTRFaction;

public interface MiniQuestSelector {
	boolean include(LOTRMiniQuest var1);

	public static class BountyActiveAnyFaction extends OptionalActive {
		public BountyActiveAnyFaction() {
			setActiveOnly();
		}

		@Override
		public boolean include(LOTRMiniQuest quest) {
			if (super.include(quest) && quest instanceof LOTRMiniQuestBounty) {
				LOTRMiniQuestBounty bQuest = (LOTRMiniQuestBounty) quest;
				return !bQuest.killed;
			}
			return false;
		}
	}

	public static class BountyActiveFaction extends BountyActiveAnyFaction {
		public Supplier<LOTRFaction> factionGet;

		public BountyActiveFaction(Supplier<LOTRFaction> sup) {
			factionGet = sup;
		}

		@Override
		public boolean include(LOTRMiniQuest quest) {
			return super.include(quest) && quest.entityFaction == factionGet.get();
		}
	}

	public static class EntityId extends OptionalActive {
		public UUID entityID;

		public EntityId(UUID id) {
			entityID = id;
		}

		@Override
		public boolean include(LOTRMiniQuest quest) {
			return super.include(quest) && quest.entityUUID.equals(entityID);
		}
	}

	public static class Faction extends OptionalActive {
		public Supplier<LOTRFaction> factionGet;

		public Faction(Supplier<LOTRFaction> sup) {
			factionGet = sup;
		}

		@Override
		public boolean include(LOTRMiniQuest quest) {
			return super.include(quest) && quest.entityFaction == factionGet.get();
		}
	}

	public static class OptionalActive implements MiniQuestSelector {
		public boolean activeOnly = false;

		@Override
		public boolean include(LOTRMiniQuest quest) {
			if (activeOnly) {
				return quest.isActive();
			}
			return true;
		}

		public OptionalActive setActiveOnly() {
			activeOnly = true;
			return this;
		}
	}

}
