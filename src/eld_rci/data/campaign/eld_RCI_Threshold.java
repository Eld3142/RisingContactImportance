package eld_rci.data.campaign;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

public class eld_RCI_Threshold {

    public static int THRESHOLD_HIGH = 10;
    public static int THRESHOLD_MEDIUM = 5;
    public static int THRESHOLD_LOW = 3;
    public static int THRESHOLD_VERY_LOW = 2;

    public static void threshold()
    {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            THRESHOLD_HIGH     = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_HIGH");
            THRESHOLD_MEDIUM   = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_MEDIUM");
            THRESHOLD_LOW      = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_LOW");
            THRESHOLD_VERY_LOW = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_VERY_LOW");
        }
    }

}
