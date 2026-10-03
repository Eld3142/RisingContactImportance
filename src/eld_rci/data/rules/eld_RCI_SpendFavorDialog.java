package eld_rci.data.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

import static eld_rci.data.campaign.eld_RCI_Settings.FAVOR_COST;
import static eld_rci.data.scripts.eld_RCI_CleanUp.cleanMissionRefs;

public class eld_RCI_SpendFavorDialog extends BaseCommandPlugin implements InteractionDialogPlugin {

    public static int getFavorCost() {
        return FAVOR_COST;
    }

    private static final String LEAVE   = "eld_RCI_Favor_Leave";
    private static final String ACCEPT  = "eld_RCI_Favor_Accept";
    private static final String DECLINE = "eld_RCI_Favor_Decline";

    private String pendingSpecId = null;
    private HubMission pendingMission = null;

    private final InteractionDialogPlugin plugin;
    private final PersonAPI person;
    private final Map<String, MemoryAPI> memoryMap;

    private InteractionDialogAPI dialog;
    private final Map<String, HubMission> viable = new LinkedHashMap<>();

    public eld_RCI_SpendFavorDialog() {
        this.plugin = null;
        this.person = null;
        this.memoryMap = null;
    }

    public eld_RCI_SpendFavorDialog(InteractionDialogPlugin plugin,
                                 PersonAPI person, Map<String, MemoryAPI> memoryMap) {
        this.plugin = plugin;
        this.person = person;
        this.memoryMap = memoryMap;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || dialog.getInteractionTarget() == null) return false;

        PersonAPI person = dialog.getInteractionTarget().getActivePerson();
        if (person == null) return false;

        InteractionDialogPlugin plugin = dialog.getPlugin();
        eld_RCI_SpendFavorDialog menu = new eld_RCI_SpendFavorDialog(plugin, person, memoryMap);
        dialog.setPlugin(menu);
        menu.init(dialog);
        return true;
    }

    private int getFavor() {
        return person.getMemoryWithoutUpdate().getInt("$eld_rci_missionCount");
    }

    private void discardAll() {
        try {
            MissionHub hub = person != null ? BaseMissionHub.get(person) : null;
            if (hub != null) ((BaseMissionHub) hub).getOfferedMissions().removeAll(viable.values());
        } catch (Exception ignored) {

        }

        for (HubMission mis : viable.values()) {
            try { mis.abort(); } catch (Exception ignored) {

            }
        }
        viable.clear();
    }

    private HubMission generate(MissionHub hub, PersonMissionSpec spec) {
        try {
            HubMissionCreator creator = new BaseHubMissionCreator(spec);
            creator.setSeed(Misc.genRandomSeed());
            creator.updateRandom();

            HubMission mission = creator.createHubMission(hub);
            if (mission == null) return null;

            mission.setHub(hub);
            mission.setCreator(creator);
            mission.setGenRandom(creator.getGenRandom());

            mission.createAndAbortIfFailed(person.getMarket(), false);
            if (mission.isMissionCreationAborted()) return null;

            return mission;
        } catch (Exception e) {
            Global.getLogger(this.getClass()).warn("[RCI Debug] Failed to generate "
                    + spec.getMissionId(), e);
            return null;
        }
    }

    @Override
    public void init(InteractionDialogAPI dialog) { //Leave response stuff
        this.dialog = dialog;
        declineBranch(null);
    }

    private void declineBranch(String notice) { //Decline response stuff
        discardAll();
        cleanMissionRefs(person);
        TextPanelAPI text = dialog.getTextPanel();
        OptionPanelAPI opts = dialog.getOptionPanel();
        opts.clearOptions();

        if (notice != null) {
            text.addPara(notice, Misc.getHighlightColor());
        }
        text.addPara(person.getNameString() + " owes you "
                + getFavor() + " favor. Each arrangement costs "
                + getFavorCost() + " favor.", Misc.getTextColor());

        MissionHub hub = BaseMissionHub.get(person);
        if (hub == null || person.getMarket() == null) {
            text.addPara("\"I can't arrange any mission for you right now.\"",
                    Misc.getNegativeHighlightColor());
        } else {
            List<PersonMissionSpec> specs = BaseMissionHub.getMissionsForPerson(person);
            for (PersonMissionSpec spec : specs) {
                if (spec.hasTag(Tags.MISSION_NON_REPEATABLE)) continue;
                if (!hasAllowedTag(spec)) continue;
                if (spec.getMissionId().equals("cpc")) continue; // custom production not supported yet, sorry

                HubMission mission = generate(hub, spec);
                if (mission == null) continue;

                String name = spec.getMissionId();
                if (mission instanceof BaseHubMission) {
                    name = ((BaseHubMission) mission).getBaseName();
                }

                viable.put(spec.getMissionId(), mission);
                opts.addOption(name + "  (" + getFavorCost() + " favor if accepted)", spec.getMissionId());
                if (getFavor() < getFavorCost()) {
                    opts.setEnabled(spec.getMissionId(), false);
                    opts.setTooltip(spec.getMissionId(),
                            "Requires " + getFavorCost() + " favor (you have " + getFavor() + " Favor currently).");
                }
            }

            if (viable.isEmpty()) {
                text.addPara("\"I don't have any mission that can be arranged for now.\"",
                        Misc.getNegativeHighlightColor());
            }
        }
        opts.addOption("Leave", LEAVE);
    }

    private void acceptBranch() { //Accept response stuff
        String specId = pendingSpecId;
        HubMission mission = pendingMission;
        pendingSpecId = null;
        pendingMission = null;

        if (mission == null) {
            declineBranch("That arrangement is no longer available.");
            return;
        }

        int before = getFavor();
        if (before < getFavorCost()) {
            declineBranch("Not enough favor.");
            return;
        }

        MissionHub hub = BaseMissionHub.get(person);
        if (hub == null) {
            declineBranch("That arrangement is no longer available.");
            return;
        }

        try {
            ((BaseMissionHub) hub).getOfferedMissions().add(mission);
            person.getMemoryWithoutUpdate().set("$eld_rci_missionCount", before - getFavorCost());

            ((BaseMissionHub) hub).accept(dialog, memoryMap, specId);
        } catch (Exception e) {
            person.getMemoryWithoutUpdate().set("$eld_rci_missionCount", before);
            Global.getLogger(this.getClass()).warn("[RCI Debug] grant failed for " + specId, e);

            declineBranch("\"I can't get this mission for you for some reason.\""
                    + "\n\nThe arrangement fell through (maybe you already did this type of mission recently?). "
                    + "Naturally, you didn't use any favor.");
            return;
        }

        viable.remove(specId);
        discardAll();
        String name = specId;
        if (mission instanceof BaseHubMission) {
            name = ((BaseHubMission) mission).getBaseName();
        }

        dialog.getTextPanel().addPara("Arranged: " + name + ".", Misc.getPositiveHighlightColor());
        dialog.getOptionPanel().clearOptions();
        dialog.getOptionPanel().addOption("Leave", LEAVE);
    }

    @Override
    public void optionSelected(String optionText, Object option) { //Evil and intimidating options stuff
        if (LEAVE.equals(option)) {
            dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
            dialog.getVisualPanel().showPersonInfo(person);
            discardAll();

            dialog.getOptionPanel().clearOptions();
            dialog.setPlugin(plugin);
            plugin.init(dialog);
            return;
        }

        if (DECLINE.equals(option)) {
            dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
            dialog.getVisualPanel().showPersonInfo(person);

            pendingSpecId = null; pendingMission = null;
            declineBranch(null);
            return;
        }

        if (ACCEPT.equals(option)) {
            dialog.getVisualPanel().removeMapMarkerFromPersonInfo();

            acceptBranch();
            return;
        }

        if (isContactAccept(option)) {
            dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
            acceptBranch();
            return;
        }

        if (isContactDecline(option)) {
            dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
            pendingSpecId = null; pendingMission = null;
            declineBranch(null);
            return;
        }

        String specId = (String) option;
        HubMission mission = viable.get(specId);

        if (mission == null) {
            if (!forwardToRules(option)) {
                declineBranch("That arrangement is no longer available.");
            }
            return;
        }

        if (getFavor() < getFavorCost()) {
            declineBranch("Not enough favor.");
            return;
        }

        pendingSpecId = specId;
        pendingMission = mission;

        dialog.getOptionPanel().clearOptions();
        mission.updateInteractionData(dialog, memoryMap);

        boolean entered = false;
        try {
            entered = FireBest.fire(null, dialog, memoryMap, mission.getTriggerPrefix() + "_option");
        } catch (Exception e) {
            Global.getLogger(this.getClass()).warn("[RCI Debug] entry rules failed", e);
        }

        if (entered) {
            dialog.getOptionPanel().addOption("Decline", DECLINE);
            dialog.getOptionPanel().addOption("Leave", LEAVE);
            return;
        }

        try {
            if (mission instanceof BaseHubMission) {
                List<Misc.Token> showMap = new ArrayList<>();
                showMap.add(new Misc.Token("showMap", Misc.TokenType.LITERAL));
                showMap.add(new Misc.Token("Target: " + ((BaseHubMission) mission).getBaseName(),
                        Misc.TokenType.LITERAL));
                ((BaseHubMission) mission).callEvent("eld_RCI_FavorConfirm", dialog, showMap, memoryMap);
            }
        } catch (Exception e) {
            Global.getLogger(this.getClass()).warn("[RCI Debug] showMap failed, continuing without map", e);
        }

        boolean shown = FireBest.fire(null, dialog, memoryMap,
                mission.getTriggerPrefix() + "_blurb true");
        if (!shown) {
            @SuppressWarnings("deprecation")
            String blurb = mission.getBlurbText();
            if (blurb != null) dialog.getTextPanel().addPara(blurb, Misc.getTextColor());
            else dialog.getTextPanel().addPara("The contact can arrange "
                    + mission.getMissionId() + ".", Misc.getTextColor());
        }

        if (mission instanceof BaseHubMission) {
            int reward = ((BaseHubMission) mission).getCreditsReward();
            if (reward > 0) dialog.getTextPanel().addPara("Reward: "
                    + Misc.getWithDGS(reward) + " credits.", Misc.getHighlightColor());
        }

        dialog.getTextPanel().addPara("Arrangement cost: " + getFavorCost()
                + " favor (you currently have " + getFavor() + ").", Misc.getTextColor());

        dialog.getOptionPanel().addOption("Accept arrangement (" + getFavorCost() + " favor)", ACCEPT);
        dialog.getOptionPanel().addOption("Decline", DECLINE);
        dialog.getOptionPanel().addOption("Leave", LEAVE);
    }

    private boolean hasAllowedTag(PersonMissionSpec spec) {
        return containsAllowedTag(spec.getTagsAny()) || containsAllowedTag(spec.getTagsAll());
    }

    private boolean containsAllowedTag(Set<String> tags) {
        for (String tag : tags) {
            String t = tag.trim().toLowerCase();
            if (t.equals("trade") || t.equals("military") || t.equals("underworld")) return true;
        }
        return false;
    }

    private boolean isContactAccept(Object option) {
        if (!(option instanceof String)) return false;
        String id = (String) option;
        return id.startsWith("contact_accept"); // || id.equals("aseb_accept"); for Alviss missions
    }

    private boolean isContactDecline(Object option) {
        return option instanceof String && ((String) option).startsWith("contact_decline");
    }

    private boolean forwardToRules(Object option) {
        dialog.addOptionSelectedText(option);
        dialog.getOptionPanel().clearOptions();
        memoryMap.get(MemKeys.LOCAL).set("$option", option);

        boolean fired = false;
        try {
            fired = FireBest.fire(null, dialog, memoryMap, "DialogOptionSelected");
        } catch (Exception e) {
            Global.getLogger(this.getClass()).warn("[RCI Debug] rules forward failed", e);
        }

        if (fired) dialog.getOptionPanel().addOption("Leave", LEAVE);
        return fired;
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return memoryMap;
    }

}
