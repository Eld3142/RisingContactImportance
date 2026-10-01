package eld_rci.data.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eld_rci.data.campaign.eld_RCI_Threshold.*;

public class eld_RCI_MissionTrackerOld implements EveryFrameScript {

    private static final String MISSION_COUNTER = "$eld_rci_missionCount";

    private final Set<BaseHubMission> track_mission = new HashSet<>();

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        IntelManagerAPI intel = Global.getSector().getIntelManager();
        List<IntelInfoPlugin> missions = intel.getIntel(BaseHubMission.class);

        for (IntelInfoPlugin miss : missions) {
            if (!(miss instanceof BaseHubMission)) continue;
            BaseHubMission mission = (BaseHubMission) miss;

            if (track_mission.contains(mission)) continue;

            BaseHubMission.HubMissionResult result = mission.getResult();
            if (result != null && result.success) {
                PersonAPI person = mission.getPerson();

                if (person != null) {
                    PersonImportance importance = person.getImportance();

                    if (importance != null && importance != PersonImportance.VERY_HIGH) {
                        int count = person.getMemoryWithoutUpdate().getInt(MISSION_COUNTER);
                        count++;

                        if (count >= getThreshold(person)) {
                            person.setImportance(importance.next());
                            count = 0;
                        }

                        person.getMemoryWithoutUpdate().set(MISSION_COUNTER, count);
                    }
                }
                track_mission.add(mission);
            }
        }
    }

    private int getThreshold(PersonAPI person) {
        if (person.getImportance() == PersonImportance.HIGH) {
            return THRESHOLD_HIGH;
        } else if (person.getImportance() == PersonImportance.MEDIUM) {
            return THRESHOLD_MEDIUM;
        } else if (person.getImportance() == PersonImportance.LOW) {
            return THRESHOLD_LOW;
        } else if (person.getImportance() == PersonImportance.VERY_LOW) {
            return THRESHOLD_VERY_LOW;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public void clear() {
        track_mission.clear();
    }
}
