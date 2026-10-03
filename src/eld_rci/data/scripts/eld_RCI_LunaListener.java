package eld_rci.data.scripts;

import com.fs.starfarer.api.Global;
import eld_rci.data.campaign.eld_RCI_Settings;
import lunalib.lunaSettings.LunaSettingsListener;

public class eld_RCI_LunaListener implements LunaSettingsListener {

    //Gets called whenever settings are saved in the campaign or the main menu.
    @Override
    public void settingsChanged(String modID) {
        if (!"eld_rci".equals(modID)) {
            return;
        }

        eld_RCI_Settings.getSettings();
        if (Global.getSector() != null) {
            Global.getSector().getMemoryWithoutUpdate().set("$eld_RCI_favorEnabled", eld_RCI_Settings.FAVOR);
        }
    }

}
