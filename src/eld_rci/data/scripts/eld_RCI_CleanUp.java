package eld_rci.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionHub;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import eld_rci.data.campaign.eld_RCI_MissionTracker;
import eld_rci.data.campaign.eld_RCI_UpImportanceIntel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class eld_RCI_CleanUp {

    public static void cleanMissionRefs(PersonAPI person) {
        if (person == null || Global.getSector() == null) return;
        try {
            IntelManagerAPI intel = Global.getSector().getIntelManager();
            MissionHub hub = BaseMissionHub.get(person);
            List<HubMission> offered = new ArrayList<HubMission>();

            if (hub instanceof BaseMissionHub) offered = ((BaseMissionHub) hub).getOfferedMissions();
            Set<Object> liveIntel = new HashSet<Object>(intel.getIntel(BaseHubMission.class));

            cleanRef(person.getMemoryWithoutUpdate(), person, offered, liveIntel);
            if (person.getMarket() != null)
                cleanRef(person.getMarket().getMemoryWithoutUpdate(), person, offered, liveIntel);
            if (person.getFaction() != null)
                cleanRef(person.getFaction().getMemoryWithoutUpdate(), person, offered, liveIntel);
        } catch (Exception e) {
            Global.getLogger(eld_RCI_MissionTracker.class).warn("[RCI Debug] ref cleaning failed", e);
        }
    }

    private static void cleanRef(MemoryAPI mem, PersonAPI person,
                                 List<HubMission> offered, Set<Object> liveIntel) {
        for (PersonMissionSpec spec : BaseMissionHub.getMissionsForPerson(person)) {
            String key = "$" + spec.getMissionId() + "_ref";
            if (!mem.contains(key)) continue;

            Object val = mem.get(key);
            if (val == null || (!offered.contains(val) && !liveIntel.contains(val))) {
                mem.unset(key);
            }
        }
    }

    public static void removalCleanUp() {
        if (Global.getSector() == null) return;
        try {
            IntelManagerAPI intel = Global.getSector().getIntelManager();

            // Clean up Intel
            for (IntelInfoPlugin old : new ArrayList<IntelInfoPlugin>(intel.getIntel(eld_RCI_UpImportanceIntel.class))) {
                intel.removeIntel(old);
            }

            // Clean up keys in each contact
            Set<PersonAPI> people = new HashSet<PersonAPI>();
            for (ImportantPeopleAPI.PersonDataAPI data :
                    Global.getSector().getImportantPeople().getPeopleCopy()) {
                if (data.getPerson() != null) people.add(data.getPerson());
            }
            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                people.addAll(market.getPeopleCopy());
            }
            for (PersonAPI person : people) {
                try {
                    MemoryAPI mem = person.getMemoryWithoutUpdate();
                    mem.unset("$eld_rci_missionCount");
                    mem.unset("$eld_rci_missionCooldown");
                    mem.unset("$eld_RCI_Blacklisted");
                    cleanMissionRefs(person);
                } catch (Exception ignored) {
                }
            }

            // Clean up key and listener in Sector
            Global.getSector().getMemoryWithoutUpdate().unset("$eld_RCI_favorEnabled");
            Global.getSector().getListenerManager().removeListenerOfClass(eld_RCI_MissionTracker.class);

        } catch (Exception e) {
            Global.getLogger(eld_RCI_MissionTracker.class).warn("[RCI Debug] Removal cleanup failed", e);
        }
    }

}
