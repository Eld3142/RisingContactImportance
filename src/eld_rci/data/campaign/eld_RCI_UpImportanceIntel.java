package eld_rci.data.campaign;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Set;

public class eld_RCI_UpImportanceIntel extends BaseIntelPlugin {

    protected PersonAPI person;

    public eld_RCI_UpImportanceIntel(PersonAPI person) {
        this.person = person;
    }

    public String getName() {
        return "Contact's Importance Increased";
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

        info.addPara(person.getNameString() + "'s importance has risen to " +
                person.getImportance().getDisplayName() + ".", opad);

        addLogTimestamp(info, tc, opad);

        addDeleteButton(info, width);
    }

    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_CONTACTS);
        return tags;
    }

    public void reportPlayerClickedOn() {
        super.reportPlayerClickedOn();
        endAfterDelay(1f);
    }

}
