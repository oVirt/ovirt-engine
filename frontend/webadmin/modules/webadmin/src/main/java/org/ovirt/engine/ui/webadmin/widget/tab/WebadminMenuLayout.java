package org.ovirt.engine.ui.webadmin.widget.tab;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

public class WebadminMenuLayout {

    Map<String, MenuLayoutMenuDetails> layoutMap = new HashMap<>();

    public WebadminMenuLayout() {
        setUiMode(UiModeData.getUiMode());
    }

    private void initLayoutMap(PrimaryMenuItem[] items, SecondaryMenuItem[] secondaryItems) {
        for (PrimaryMenuItem layout : items) {
            if (layout.getHref() != null) {
                // Main menu without sub items.
                MenuLayoutMenuDetails details = new MenuLayoutMenuDetails();
                details.setPrimaryPriority(layout.getIndex());
                // This is to make sure there is no sub menu, don't set the primary title.
                details.setSecondaryTitle(layout.getTitle());
                details.setIcon(layout.getIcon());
                layoutMap.put(layout.getHref(), details);
            }
        }
        for (SecondaryMenuItem subMenu : secondaryItems) {
            MenuLayoutMenuDetails details = new MenuLayoutMenuDetails();
            details.setPrimaryPriority(subMenu.getPrimaryMenu().getIndex());
            details.setPrimaryTitle(subMenu.getPrimaryMenu().getTitle());
            details.setSecondaryPriority(subMenu.getIndex());
            details.setSecondaryTitle(subMenu.getTitle());
            details.setIcon(subMenu.getIcon());
            layoutMap.put(subMenu.getHref(), details);
        }
    }

    public void setUiMode(ApplicationMode applicationMode) {
        switch (applicationMode) {
        case AllModes:
            initLayoutMap(AllModesMenuLayout.values(), AllModesSubMenu.values());
            break;
        case GlusterOnly:
            initLayoutMap(GlusterModeMenuLayout.values(), GlusterModeSubMenu.values());
            break;
        case VirtOnly:
            initLayoutMap(AllModesMenuLayout.values(), AllModesSubMenu.values());
            break;
        default:
            break;
        }
    }

    public MenuLayoutMenuDetails getDetails(String itemPlace) {
        return layoutMap.get(itemPlace);
    }

}
