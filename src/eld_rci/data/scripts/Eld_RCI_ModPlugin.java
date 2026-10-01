package eld_rci.data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import eld_rci.data.campaign.eld_RCI_MissionTracker;
import eld_rci.data.campaign.eld_RCI_Threshold;
import lunalib.lunaSettings.LunaSettings;

public class Eld_RCI_ModPlugin extends BaseModPlugin {

//    private eld_RCI_MissionTrackerOld eld_rci_tracker;
//    private boolean eld_rci_trackerAdded = false;

    /*This method is run right at the end of starsectors loading.
    * It is most useful for loading data that only really needs to be setup once. */
    @Override
    public void onApplicationLoad() throws Exception {

        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.addSettingsListener(new eld_RCI_LunaListener());
        }
        eld_RCI_Threshold.threshold();

    }

    /*This method is run in two cases:
    * - At the end of the creation of a new save
    * - When an existing save finished loading
    * This method is most useful for adding transient listeners/scripts and for enabling mid-save compatibility,
    * like adding star systems to an existing save if the mod was just added. */
    @Override
    public void onGameLoad(boolean newGame) {

//        Use EveryFrameScripts
//        if (eld_rci_tracker == null) {
//            eld_rci_tracker = new eld_RCI_MissionTrackerOld();
//        }

//        eld_rci_tracker.clear();

//        if (!eld_rci_trackerAdded) {
//            Global.getSector().addScript(eld_rci_tracker);
//            eld_rci_trackerAdded = true;
//        }

        Global.getSector().addListener(new eld_RCI_MissionTracker());

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
