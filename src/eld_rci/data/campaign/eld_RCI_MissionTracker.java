package eld_rci.data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eld_rci.data.campaign.eld_RCI_Settings.*;

public class eld_RCI_MissionTracker implements CampaignEventListener {

    private static final boolean DEBUG = true;
    private void debugLog(String log) {
        if (DEBUG) {
            Global.getLogger(this.getClass()).info("[RCI Debug] " + log);
        }
    }

    private static final String MISSION_COUNTER  = "$eld_rci_missionCount";
    private static final String MISSION_COOLDOWN = "$eld_rci_missionCooldown";

    private final Set<BaseHubMission> track_mission = new HashSet<>();

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        IntelManagerAPI intel = Global.getSector().getIntelManager();
        List<IntelInfoPlugin> missions = intel.getIntel(BaseHubMission.class);

        track_mission.retainAll(missions);

        for (IntelInfoPlugin miss : missions) {
            if (!(miss instanceof BaseHubMission)) continue;
            BaseHubMission mission = (BaseHubMission) miss;
            int reward = mission.getCreditsReward();

            if (track_mission.contains(mission)) {
                debugLog("Mission base name = " + mission.getBaseName()
                        + " | hash code = " + System.identityHashCode(mission)
                        + " | success = " + (mission.getResult() != null && mission.getResult().success)
                        + " | person = " + (mission.getPerson() != null ? mission.getPerson().getNameString() : "null"));
                continue;
            }

            BaseHubMission.HubMissionResult result = mission.getResult();
            if (result != null) {
                PersonAPI person = mission.getPerson();

                if (person != null  && result.success) {
                    if (COOLDOWN) {
                        long now = Global.getSector().getClock().getTimestamp();
                        long cooldown_End = person.getMemoryWithoutUpdate().getLong(MISSION_COOLDOWN);
                        long max_cooldown = COOLDOWN_DAYS * 86400000L;

                        if (cooldown_End != 0 && (cooldown_End - now) > max_cooldown) {
                            cooldown_End = now + max_cooldown;
                            person.getMemoryWithoutUpdate().set(MISSION_COOLDOWN, cooldown_End);
                        }

                        debugLog("Cooldown check for " + person.getNameString() +
                                " | stored cooldown_End = " + cooldown_End +
                                " | now = " + now);

                        if (cooldown_End != 0 && now < cooldown_End) {
                            debugLog("Mission skipped (cooldown) for " + person.getNameString() +
                                    " | count = " + person.getMemoryWithoutUpdate().getInt(MISSION_COUNTER));
                            continue;
                        } else if (cooldown_End != 0) {
                            person.getMemoryWithoutUpdate().set(MISSION_COOLDOWN, 0);
                            debugLog("Cooldown expired for " + person.getNameString() + ", reset to 0");
                        }
                    }

                    PersonImportance importance = person.getImportance();

                    if (importance != null && importance != PersonImportance.VERY_HIGH &&
                            reward >= getMinCredits(person)) {
                        int count = person.getMemoryWithoutUpdate().getInt(MISSION_COUNTER);
                        debugLog("Before increment: " + person.getNameString() +
                                " | importance = " + importance.getDisplayName() +
                                " | count = " + count +
                                " | threshold = " + getThreshold(person) +
                                " | reward = " + reward +
                                " | minCredits = " + getMinCredits(person));
                        count++;

                        int marketSize = person.getMarket() != null ? person.getMarket().getSize() : 0; // If somehow there is a market-less person
                        if (count >= getThreshold(person) && marketSize >= getMarketSizeReq(person)) {
                            person.setImportance(importance.next());
                            count = 0;

                            debugLog("Importance up: " + person.getNameString() +
                                    "'s importance to " + person.getImportance().getDisplayName() +
                                    " | count reset to 0");

                            IntelManagerAPI intelManager = Global.getSector().getIntelManager();
                            for (IntelInfoPlugin old : intelManager.getIntel(eld_RCI_UpImportanceIntel.class)) {
                                if (old instanceof eld_RCI_UpImportanceIntel
                                        && ((eld_RCI_UpImportanceIntel) old).getPerson() == person) {
                                    intelManager.removeIntel(old);
                                }
                            }
                            intelManager.addIntel(new eld_RCI_UpImportanceIntel(person));

                            if (COOLDOWN) {
                                long now = Global.getSector().getClock().getTimestamp();
                                long cooldown_end = now + (long) (COOLDOWN_DAYS * 86400000L);
                                person.getMemoryWithoutUpdate().set(MISSION_COOLDOWN, cooldown_end);
                            }
                        } else {
                            debugLog("Requirement not met: " + person.getNameString() +
                                    " | count = " + count +
                                    " | threshold = " + getThreshold(person) +
                                    " | marketSize = " + marketSize +
                                    " | marketReq = " + getMarketSizeReq(person));
                        }

                        person.getMemoryWithoutUpdate().set(MISSION_COUNTER, count);
                        debugLog("After increment: " + person.getNameString() + " | count = " + count);
                    } else {
                        debugLog("One or more requirement not met: " + person.getNameString() +
                                " | importance = " + (importance != null ? importance.getDisplayName() : "null") +
                                " | reward = " + reward +
                                " | minCredits = " + getMinCredits(person));
                    }
                    track_mission.add(mission);
                }
            }
        }

        for (IntelInfoPlugin intelPlugin : intel.getIntel(ContactIntel.class)) {
            ContactIntel contact = (ContactIntel) intelPlugin;
            ContactIntel.ContactState state = contact.getState();

            if (state != ContactIntel.ContactState.NON_PRIORITY
                    && state != ContactIntel.ContactState.PRIORITY) continue;
            PersonAPI person = contact.getPerson();
            if (person == null) continue;

            boolean exists = false;
            for (IntelInfoPlugin old : intel.getIntel(eld_RCI_UpImportanceIntel.class)) {
                if (old instanceof eld_RCI_UpImportanceIntel
                        && ((eld_RCI_UpImportanceIntel) old).getPerson() == person) {
                    exists = true; break;
                }
            }
            if (!exists) {
                intel.addIntel(new eld_RCI_UpImportanceIntel(person));
            }
        }

    }

    public static int getThreshold(PersonAPI person) {
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

    public static int getMarketSizeReq(PersonAPI person) {
        if (!MARKET_SIZE_REQ) {
            return 0;
        } else if (person.getImportance() == PersonImportance.HIGH) {
            return MARKET_SIZE_REQ_VERY_HIGH;
        } else if (person.getImportance() == PersonImportance.MEDIUM) {
            return MARKET_SIZE_REQ_HIGH;
        } else return 0;
    }

    public static int getMinCredits(PersonAPI person) {
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
