package eld_rci.data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eld_rci.data.campaign.eld_RCI_Threshold.*;

public class eld_RCI_MissionTracker implements CampaignEventListener {

    private static final String MISSION_COUNTER  = "$eld_rci_missionCount";
    private static final String MISSION_COOLDOWN = "$eld_rci_missionCooldown";

    private final Set<BaseHubMission> track_mission = new HashSet<>();

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
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
                    Long cooldown_End = person.getMemoryWithoutUpdate().getLong(MISSION_COOLDOWN);
                    if (cooldown_End != null && Global.getSector().getClock().getTimestamp() < cooldown_End) {
                        track_mission.add(mission);
                        continue;
                    }

                    PersonImportance importance = person.getImportance();

                    if (importance != null && importance != PersonImportance.VERY_HIGH) {
                        int count = person.getMemoryWithoutUpdate().getInt(MISSION_COUNTER);
                        count++;

                        if (count >= getThreshold(person) &&
                                person.getMarket().getSize() >= getMarketSizeReq(person)) {
                            person.setImportance(importance.next());
                            count = 0;

                            Global.getSector().getIntelManager().queueIntel(new eld_RCI_UpImportanceIntel(person));

                            long cooldown_end = Global.getSector().getClock().getTimestamp() +
                                    (long) (COOLDOWN_DAYS * Global.getSector().getClock().getSecondsPerDay());
                            person.getMemoryWithoutUpdate().set(MISSION_COOLDOWN, cooldown_end);
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

    private int getMarketSizeReq(PersonAPI person) {
        if (!MARKET_SIZE_REQ) {
            return 0;
        } else if (person.getImportance() == PersonImportance.HIGH) {
            return MARKET_SIZE_REQ_VERY_HIGH;
        } else if (person.getImportance() == PersonImportance.MEDIUM) {
            return MARKET_SIZE_REQ_HIGH;
        } else return 0;
    }

    public void clear() {
        track_mission.clear();
    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

    }

    @Override
    public void reportPlayerReputationChange(PersonAPI person, float delta) {

    }

    @Override
    public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {

    }

    @Override
    public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {

    }

    @Override
    public void reportPlayerDumpedCargo(CargoAPI cargo) {

    }

    @Override
    public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {

    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {

    }

    @Override
    public void reportPlayerOpenedMarket(com.fs.starfarer.api.campaign.econ.MarketAPI market) {

    }

    @Override
    public void reportPlayerClosedMarket(com.fs.starfarer.api.campaign.econ.MarketAPI market) {

    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {

    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {

    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {

    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {

    }

    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {

    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {

    }

    @Override
    public void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity) {

    }

    @Override
    public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpPointAPI.JumpDestination to) {

    }

}
