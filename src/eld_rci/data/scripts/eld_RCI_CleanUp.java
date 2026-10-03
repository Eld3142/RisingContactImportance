package eld_rci.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionHub;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import eld_rci.data.campaign.eld_RCI_MissionTracker;

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

}
