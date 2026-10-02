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

import static eld_rci.data.campaign.eld_RCI_Settings.*;

public class eld_RCI_MissionTracker_EFS implements EveryFrameScript {

    private float check_Timer = 0f;
    private float check_Interval = 0.5f;

    private static final String MISSION_COUNTER = "$eld_rci_missionCount";
    private static final String MISSION_COOLDOWN = "$eld_rci_missionCooldown";

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
        check_Timer += amount;
        if (check_Timer < check_Interval) return;
        check_Timer = 0f;

        IntelManagerAPI intel = Global.getSector().getIntelManager();
        List<IntelInfoPlugin> missions = intel.getIntel(BaseHubMission.class);

        for (IntelInfoPlugin miss : missions) {
            if (!(miss instanceof BaseHubMission)) continue;
            BaseHubMission mission = (BaseHubMission) miss;
            int reward = mission.getCreditsReward();

            if (track_mission.contains(mission)) continue;

            BaseHubMission.HubMissionResult result = mission.getResult();
            if (result != null) {
                PersonAPI person = mission.getPerson();

                if (person != null  && result.success) {
                    if (COOLDOWN) {
                        long cooldown_End = person.getMemoryWithoutUpdate().getLong(MISSION_COOLDOWN);
                        if (Global.getSector().getClock().getTimestamp() < cooldown_End) {
                            track_mission.add(mission);
                            continue;
                        }
                    }

                    PersonImportance importance = person.getImportance();

                    if (importance != null && importance != PersonImportance.VERY_HIGH &&
                            reward >= getMinCredits(person)) {
                        int count = person.getMemoryWithoutUpdate().getInt(MISSION_COUNTER);
                        count++;

                        if (count >= getThreshold(person) &&
                                person.getMarket().getSize() >= getMarketSizeReq(person)) {
                            person.setImportance(importance.next());
                            count = 0;

                            Global.getSector().getIntelManager().queueIntel(new eld_RCI_UpImportanceIntel(person));

                            if (COOLDOWN) {
                                long cooldown_end = Global.getSector().getClock().getTimestamp() +
                                        (long) (COOLDOWN_DAYS * Global.getSector().getClock().getSecondsPerDay());
                                person.getMemoryWithoutUpdate().set(MISSION_COOLDOWN, cooldown_end);
                            }
                        }

                        person.getMemoryWithoutUpdate().set(MISSION_COUNTER, count);
                    }
                }
                track_mission.remove(mission);
            }
            track_mission.add(mission);
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

    private int getMarketSizeReq(PersonAPI person) {
        if (!MARKET_SIZE_REQ) {
            return 0;
        } else if (person.getImportance() == PersonImportance.HIGH) {
            return MARKET_SIZE_REQ_VERY_HIGH;
        } else if (person.getImportance() == PersonImportance.MEDIUM) {
            return MARKET_SIZE_REQ_HIGH;
        } else return 0;
    }

    private int getMinCredits(PersonAPI person) {
        PersonImportance importance = person.getImportance();
        if (!MIN_CREDITS) {
            return 0;
        } else if (importance == PersonImportance.HIGH) {
            return MIN_CREDITS_HIGH;
        } else if (importance == PersonImportance.MEDIUM) {
            return MIN_CREDITS_MEDIUM;
        } else if (importance == PersonImportance.LOW) {
            return MIN_CREDITS_LOW;
        } else if (importance == PersonImportance.VERY_LOW) {
            return MIN_CREDITS_VERY_LOW;
        } else return 0;
    }

    public void clear() {
        track_mission.clear();
    }
}
