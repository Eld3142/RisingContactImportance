package eld_rci.data.scripts;

import eld_rci.data.campaign.eld_RCI_Settings;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class eld_RCI_LunaListener implements LunaSettingsListener {

    int threshold_High     = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_HIGH");
    int threshold_Medium   = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_MEDIUM");
    int threshold_Low      = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_LOW");
    int threshold_Very_Low = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_VERY_LOW");

    boolean marketsizereq       = LunaSettings.getBoolean("eld_rci", "eld_RCI_MarketSizeReq");
    int marketsizereq_Very_High = LunaSettings.getInt("eld_rci", "eld_RCI_MarketSizeReq_VERY_HIGH");
    int marketsizereq_High      = LunaSettings.getInt("eld_rci", "eld_RCI_MarketSizeReq_HIGH");

    boolean cooldown  = LunaSettings.getBoolean("eld_rci", "eld_RCI_Cooldown");
    int cooldown_days = LunaSettings.getInt("eld_rci", "eld_RCI_Cooldown_Days");

    boolean mincredits      = LunaSettings.getBoolean("eld_rci", "eld_RCI_MinCredits");
    int mincredits_High     = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_HIGH");
    int mincredits_Medium   = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_MEDIUM");
    int mincredits_Low      = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_LOW");
    int mincredits_Very_Low = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_VERY_LOW");

    //Gets called whenever settings are saved in the campaign or the main menu.
    @Override
    public void settingsChanged(String modID) {
        threshold_High     = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_HIGH");
        threshold_Medium   = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_MEDIUM");
        threshold_Low      = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_LOW");
        threshold_Very_Low = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_VERY_LOW");

        marketsizereq           = LunaSettings.getBoolean("eld_rci", "eld_RCI_MarketSizeReq");
        marketsizereq_Very_High = LunaSettings.getInt("eld_rci", "eld_RCI_MarketSizeReq_VERY_HIGH");
        marketsizereq_High      = LunaSettings.getInt("eld_rci", "eld_RCI_MarketSizeReq_HIGH");

        cooldown      = LunaSettings.getBoolean("eld_rci", "eld_RCI_Cooldown");
        cooldown_days = LunaSettings.getInt("eld_rci", "eld_RCI_Cooldown_Days");

        mincredits          = LunaSettings.getBoolean("eld_rci", "eld_RCI_MinCredits");
        mincredits_High     = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_HIGH");
        mincredits_Medium   = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_MEDIUM");
        mincredits_Low      = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_LOW");
        mincredits_Very_Low = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_VERY_LOW");

        eld_RCI_Settings.getSettings();
    }

}
