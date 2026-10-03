package eld_rci.data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import eld_rci.data.campaign.eld_RCI_MissionTracker;
import eld_rci.data.campaign.eld_RCI_Settings;
import eld_rci.data.campaign.eld_RCI_UpImportanceIntel;
import lunalib.lunaSettings.LunaSettings;

import static eld_rci.data.scripts.eld_RCI_CleanUp.cleanMissionRefs;

public class Eld_RCI_ModPlugin extends BaseModPlugin {

//    private eld_RCI_MissionTracker_EFS eld_rci_tracker;
//    private boolean eld_rci_trackerAdded = false;

    /*This method is run right at the end of starsectors loading.
    * It is most useful for loading data that only really needs to be setup once. */
    @Override
    public void onApplicationLoad() throws Exception {

        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.addSettingsListener(new eld_RCI_LunaListener());
        }
        eld_RCI_Settings.getSettings();

    }

    /*This method is run in two cases:
    * - At the end of the creation of a new save
    * - When an existing save finished loading
    * This method is most useful for adding transient listeners/scripts and for enabling mid-save compatibility,
    * like adding star systems to an existing save if the mod was just added. */
    @Override
    public void onGameLoad(boolean newGame) {

//        Use EveryFrameScripts (performance concern)
//        if (eld_rci_tracker == null) {
//            eld_rci_tracker = new eld_RCI_MissionTracker_EFS();
//        }

//        eld_rci_tracker.clear();

//        if (!eld_rci_trackerAdded) {
//            Global.getSector().addScript(eld_rci_tracker);
//            eld_rci_trackerAdded = true;
//        }

//        Use CampaignEventListener (unstable)
        Global.getSector().addListener(new eld_RCI_MissionTracker());
        Global.getSector().getMemoryWithoutUpdate().set("$eld_RCI_favorEnabled", eld_RCI_Settings.FAVOR);

        IntelManagerAPI intel = Global.getSector().getIntelManager();
        for (IntelInfoPlugin intelPlugin : intel.getIntel(ContactIntel.class)) {
            ContactIntel contact = (ContactIntel) intelPlugin;
            ContactIntel.ContactState state = contact.getState();

            if (state != ContactIntel.ContactState.NON_PRIORITY
                    && state != ContactIntel.ContactState.PRIORITY) continue;
            PersonAPI person = contact.getPerson();
            if (person == null) continue;

            cleanMissionRefs(person);

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

    /*Runs when a save is created.
    * This method specifically runs before procedural generation, so any base-game procedural content is not accessible yet.
    * It is recommended to start placing your modded star systems from here,
    * as starsectors procgen will avoid placing stars and hyperspace storms nearby existing systems, preventing overlap.*/
    @Override
    public void onNewGame() {

    }

    /*Runs after onNewGame, after the economy has finished loading.
    * This method can be useful for accessing other mods star systems, assuming those have placed their systems in onNewGame. */
    @Override
    public void onNewGameAfterEconomyLoad() {

    }

    @Override
    public void onNewGameAfterProcGen() {

    }

    @Override
    public void onNewGameAfterTimePass() {

    }


}
