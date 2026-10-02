package eld_rci.data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Set;

import static eld_rci.data.campaign.eld_RCI_MissionTracker.getThreshold;
import static eld_rci.data.campaign.eld_RCI_Settings.COOLDOWN;

public class eld_RCI_UpImportanceIntel extends BaseIntelPlugin {

    protected PersonAPI person;

    public PersonAPI getPerson() {
        return person;
    }

    public eld_RCI_UpImportanceIntel(PersonAPI person) {
        this.person = person;
    }

    public String getName() {
        return person.getNameString() +  "'s Importance";
    }

    public String getIcon() {
        return person.getPortraitSprite();
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color tc = Misc.getTextColor();
        float opad = 10f;

        info.addPara(person.getNameString() + "'s importance is currently " +
                person.getImportance().getDisplayName() + ".", opad);

        addLogTimestamp(info, tc, opad);

        addDeleteButton(info, width);

        int currentCount = person.getMemoryWithoutUpdate().getInt("$eld_rci_missionCount");
        if (person.getImportance() == PersonImportance.VERY_HIGH) {
            info.addPara("Maximum importance reached", opad, Misc.getHighlightColor());
        } else {
            info.addPara("Missions completed to next increase: " + currentCount +
                    " / " + getThreshold(person), opad, Misc.getHighlightColor(), String.valueOf(currentCount));
        }


        if (COOLDOWN) {
            long cooldown_End = person.getMemoryWithoutUpdate().getLong("$eld_rci_missionCooldown");

            long remaining = cooldown_End - Global.getSector().getClock().getTimestamp();
            if (cooldown_End != 0 && remaining > 0) {
                double days = remaining / 86400000.0;
                info.addPara("Cooldown active: missions will not count toward the next importance increase for " +
                                Misc.getStringForDays((int) Math.max(1, Math.ceil(days))) + ".", opad, Misc.getHighlightColor(),
                        "" + Math.max(1, (int) Math.ceil(days)));
            } else {
                info.addPara("Cooldown inactive: missions will count toward the next importance increase.",
                        opad, Misc.getPositiveHighlightColor());
            }
        }
    }

    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_CONTACTS);
        return tags;
    }

}
