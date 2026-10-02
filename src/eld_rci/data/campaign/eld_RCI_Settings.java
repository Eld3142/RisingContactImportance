package eld_rci.data.campaign;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

public class eld_RCI_Settings {

    public static int THRESHOLD_HIGH = 10;
    public static int THRESHOLD_MEDIUM = 5;
    public static int THRESHOLD_LOW = 3;
    public static int THRESHOLD_VERY_LOW = 2;

    public static boolean MARKET_SIZE_REQ = false;
    public static int MARKET_SIZE_REQ_VERY_HIGH = 6;
    public static int MARKET_SIZE_REQ_HIGH = 4;

    public static boolean COOLDOWN = true;
    public static int COOLDOWN_DAYS = 180;

    public static boolean MIN_CREDITS      = true;
    public static int MIN_CREDITS_HIGH     = 300000;
    public static int MIN_CREDITS_MEDIUM   = 100000;
    public static int MIN_CREDITS_LOW      = 50000;
    public static int MIN_CREDITS_VERY_LOW = 20000;

    public static void getSettings()
    {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            THRESHOLD_HIGH     = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_HIGH");
            THRESHOLD_MEDIUM   = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_MEDIUM");
            THRESHOLD_LOW      = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_LOW");
            THRESHOLD_VERY_LOW = LunaSettings.getInt("eld_rci", "eld_RCI_Threshold_VERY_LOW");

            MARKET_SIZE_REQ           = LunaSettings.getBoolean("eld_rci", "eld_RCI_MarketSizeReq");
            MARKET_SIZE_REQ_VERY_HIGH = LunaSettings.getInt("eld_rci", "eld_RCI_MarketSizeReq_VERY_HIGH");
            MARKET_SIZE_REQ_HIGH      = LunaSettings.getInt("eld_rci", "eld_RCI_MarketSizeReq_HIGH");

            COOLDOWN      = LunaSettings.getBoolean("eld_rci", "eld_RCI_Cooldown");
            COOLDOWN_DAYS = LunaSettings.getInt("eld_rci", "eld_RCI_Cooldown_Days");

            MIN_CREDITS          = LunaSettings.getBoolean("eld_rci", "eld_RCI_MinCredits");
            MIN_CREDITS_HIGH     = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_HIGH");
            MIN_CREDITS_MEDIUM   = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_MEDIUM");
            MIN_CREDITS_LOW      = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_LOW");
            MIN_CREDITS_VERY_LOW = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_VERY_LOW");
        }
    }

}
