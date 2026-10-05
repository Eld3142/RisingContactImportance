package eld_rci.data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import lunalib.lunaSettings.LunaSettings;

import java.util.HashSet;
import java.util.Set;

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

    public static boolean MIN_CREDITS       = true;
    public static int MIN_CREDITS_VERY_HIGH = 500000;
    public static int MIN_CREDITS_HIGH      = 300000;
    public static int MIN_CREDITS_MEDIUM    = 100000;
    public static int MIN_CREDITS_LOW       = 50000;
    public static int MIN_CREDITS_VERY_LOW  = 20000;

    public static boolean FAVOR  = true;
    public static int FAVOR_COST = 2;

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

            MIN_CREDITS           = LunaSettings.getBoolean("eld_rci", "eld_RCI_MinCredits");
            MIN_CREDITS_VERY_HIGH = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_VERY_HIGH");
            MIN_CREDITS_HIGH      = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_HIGH");
            MIN_CREDITS_MEDIUM    = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_MEDIUM");
            MIN_CREDITS_LOW       = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_LOW");
            MIN_CREDITS_VERY_LOW  = LunaSettings.getInt("eld_rci", "eld_RCI_MinCredits_VERY_LOW");

            FAVOR      = LunaSettings.getBoolean("eld_rci", "eld_RCI_Favor");
            FAVOR_COST = LunaSettings.getInt("eld_rci", "eld_RCI_Favor_Cost");


            CONTACT_BLACKLIST = parseBlacklist(LunaSettings.getString("eld_rci", "eld_RCI_Blacklist"));
        }
    }


    private static final String DEFAULT_BLACKLIST = "sebestyen";

    public static Set<String> CONTACT_BLACKLIST = parseBlacklist(DEFAULT_BLACKLIST);

    public static boolean isBlacklisted(PersonAPI person) {
        if (person == null || CONTACT_BLACKLIST.isEmpty()) return false;
        for (String entry : CONTACT_BLACKLIST) {
            if (entry.equals(person.getId())) return true; // for ID
            if (entry.equalsIgnoreCase(person.getNameString())) return true; // for Display Name (unstable)
        }
        return false;
    }

    private static Set<String> parseBlacklist(String raw) {
        Set<String> result = new HashSet<String>();
        if (raw == null) return result;
        for (String entry : raw.split(",")) {
            String t = entry.trim();
            if (!t.isEmpty()) result.add(t);
        }
        return result;
    }

    public static void syncBlacklistKey(PersonAPI person) {
        if (person == null) return;
        if (isBlacklisted(person)) {
            person.getMemoryWithoutUpdate().set("$eld_RCI_Blacklisted", true);
        } else if (person.getMemoryWithoutUpdate().contains("$eld_RCI_Blacklisted")) {
            person.getMemoryWithoutUpdate().unset("$eld_RCI_Blacklisted");
        }
    }

}
