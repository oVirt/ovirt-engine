package org.ovirt.engine.ui.webadmin.widget.tab;

import java.util.HashMap;
import java.util.Map;

public class WebadminMenuLayout {

    Map<String, MenuLayoutMenuDetails> layoutMap = new HashMap<>();

    public WebadminMenuLayout() {
        for (DefaultMenuLayout layout: DefaultMenuLayout.values()) {
            if (layout.getHref() != null) {
                // Main menu without sub items.
                MenuLayoutMenuDetails details = new MenuLayoutMenuDetails();
                details.setPrimaryPriority(layout.getIndex());
                details.setPrimaryTitle(layout.getTitle());
                details.setIcon(layout.getIcon());
                layoutMap.put(layout.getHref(), details);
            }
        }
        for (DefaultSubMenu subMenu: DefaultSubMenu.values()) {
            MenuLayoutMenuDetails details = new MenuLayoutMenuDetails();
            details.setPrimaryPriority(subMenu.getPrimaryMenu().getIndex());
            details.setPrimaryTitle(subMenu.getPrimaryMenu().getTitle());
            details.setSecondaryPriority(subMenu.getPriority());
            details.setSecondaryTitle(subMenu.getTitle());
            details.setIcon(subMenu.getIcon());
            layoutMap.put(subMenu.getHref(), details);
        }
    }

    public MenuLayoutMenuDetails getDetails(String itemPlace) {
        return layoutMap.get(itemPlace);
    }
}
