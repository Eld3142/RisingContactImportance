package eld_rci.data.scripts;

import eld_rci.data.campaign.eld_RCI_Threshold;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class eld_RCI_LunaListener implements LunaSettingsListener {

    int threshold_High     = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_HIGH");
    int threshold_Medium   = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_MEDIUM");
    int threshold_Low      = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_LOW");
    int threshold_Very_Low = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_VERY_LOW");

    //Gets called whenever settings are saved in the campaign or the main menu.
    @Override
    public void settingsChanged(String modID) {
        threshold_High     = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_HIGH");
        threshold_Medium   = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_MEDIUM");
        threshold_Low      = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_LOW");
        threshold_Very_Low = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_VERY_LOW");
        eld_RCI_Threshold.threshold();
    }

}
