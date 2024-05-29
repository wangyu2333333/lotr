package lotr.common.playerdetails;

public class ExclusiveGroup {
	public static ExclusiveGroup MOD_TEAM = ExclusiveGroup.of("mod_team");
	public static ExclusiveGroup CREATOR = ExclusiveGroup.of("creator");
	public static ExclusiveGroup VICEGERENT = ExclusiveGroup.of("vicegerent");
	public static ExclusiveGroup GRUK = ExclusiveGroup.of("gruk");
	public static ExclusiveGroup BOYD = ExclusiveGroup.of("boyd");
	public static ExclusiveGroup BAT = ExclusiveGroup.of("bat");
	public static ExclusiveGroup ELECTRICIAN = ExclusiveGroup.of("electrician");
	public static ExclusiveGroup OGRE_FINDER = ExclusiveGroup.of("ogre_finder");
	public static ExclusiveGroup TRANSLATOR = ExclusiveGroup.of("translator");
	public static ExclusiveGroup PATRON = ExclusiveGroup.of("patron");
	public static ExclusiveGroup BOWING_ELVES = ExclusiveGroup.of("bowing_elves");
	public static ExclusiveGroup BUILD_CONTEST_1_ELVEN = ExclusiveGroup.of("build_contest_1_elven");
	public static ExclusiveGroup BUILD_CONTEST_2_EVIL = ExclusiveGroup.of("build_contest_2_evil");
	public static ExclusiveGroup BUILD_CONTEST_3_SHIRE = ExclusiveGroup.of("build_contest_3_shire");
	public static ExclusiveGroup BUILD_CONTEST_4_GONDOR = ExclusiveGroup.of("build_contest_4_gondor");
	public static ExclusiveGroup BUILD_CONTEST_5_HARAD = ExclusiveGroup.of("build_contest_5_harad");
	public static ExclusiveGroup BUILD_CONTEST_6_RHUN = ExclusiveGroup.of("build_contest_6_rhun");
	public static ExclusiveGroup BUILD_CONTEST_7_RENEWED_ENTRANT = ExclusiveGroup.of("build_contest_7_renewed_entrant");
	public static ExclusiveGroup BUILD_CONTEST_7_RENEWED_WINNER = ExclusiveGroup.of("build_contest_7_renewed_winner");
	public static ExclusiveGroup STRUCTURE_CONTEST_1 = ExclusiveGroup.of("structure_contest_1");
	public static ExclusiveGroup LOREMASTER_2013 = ExclusiveGroup.of("loremaster_2013");
	public static ExclusiveGroup LOREMASTER_2014 = ExclusiveGroup.of("loremaster_2014");
	public static ExclusiveGroup LOREMASTER_2015 = ExclusiveGroup.of("loremaster_2015");
	public static ExclusiveGroup LOREMASTER_2016 = ExclusiveGroup.of("loremaster_2016");
	public String name;

	public ExclusiveGroup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ExclusiveGroup of(String name) {
		return new ExclusiveGroup(name);
	}
}
