package org.ovirt.engine.ui.common.widget.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;

public class MenuLayout {
    private final Comparator<GroupedTabData> tabDataComparator =
            Comparator.comparing(t -> t.getGroupPriority());

    private SortedMap<GroupedTabData, List<GroupedTabData>> menuMap = new TreeMap<>(tabDataComparator);

    public void addMenuItem(GroupedTabData tabData) {
        if (tabData.getGroupTitle() == null || !primaryMenuContains(tabData)) {
            // This tab is NOT part of a group, make it is own menu item.
            menuMap.put(tabData, new ArrayList<>());
        }

        if (tabData.getGroupTitle() != null && !tabData.getGroupTitle().equals(tabData.getLabel())) {
            // But this one also contains a sub item.
            addSecondaryMenu(tabData);
        }
    }

    private void addSecondaryMenu(GroupedTabData tabDef) {
        List<GroupedTabData> secondaryMenu = getPrimaryTabData(tabDef);
        if (secondaryMenu != null) {
            secondaryMenu.add(tabDef);
            Collections.sort(secondaryMenu, tabDataComparator);
        }
    }

    public String getPrimaryGroupTitle(String secondaryLabel) {
        for (Entry<GroupedTabData, List<GroupedTabData>> entry: menuMap.entrySet()) {
            List<GroupedTabData> secondaryMenus = entry.getValue();
            for (GroupedTabData data: secondaryMenus) {
                if (data.getLabel().equals(secondaryLabel)) {
                    return entry.getKey().getGroupTitle();
                }
            }
        }
        return null;
    }

    private boolean primaryMenuContains(GroupedTabData tabData) {
        return menuMap.keySet().stream().anyMatch(t -> tabData.getGroupTitle().equals(t.getGroupTitle()));
    }

    private List<GroupedTabData> getPrimaryTabData(GroupedTabData tabData) {
        GroupedTabData key = null;

        List<GroupedTabData> matchingKeys = menuMap.keySet().stream().filter(t ->
            tabData.getGroupTitle().equals(t.getGroupTitle())).collect(Collectors.toList());
        if (!matchingKeys.isEmpty()) {
            key = matchingKeys.get(0);
            return menuMap.get(key);
        } else {
            return null;
        }
    }

    public int getMenuIndex(GroupedTabData newTab) {
        int menuIndex = menuMap.keySet().contains(newTab) ?
                ((SortedSet<GroupedTabData>)menuMap.keySet()).headSet(newTab).size(): -1;

        if (menuIndex == -1) {
            // Haven't found the item in the primary menu, try the secondary menu.
            for (List<GroupedTabData> secondaryMenuList: menuMap.values()) {
                menuIndex = secondaryMenuList.indexOf(newTab);
                if (menuIndex != -1) {
                    return menuIndex;
                }
            }
            menuIndex = 0;
        }
        return menuIndex;
    }
}
