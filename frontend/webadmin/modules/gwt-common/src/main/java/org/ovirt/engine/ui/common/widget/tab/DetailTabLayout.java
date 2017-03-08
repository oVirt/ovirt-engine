package org.ovirt.engine.ui.common.widget.tab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gwtplatform.mvp.client.TabData;

public class DetailTabLayout {
    List<DetailTabInfo> detailLayout = new ArrayList<>();

    /**
     * Adds a new GroupedTabData to the list of details and then returns the INDEX of that item in the
     * list
     * @param tabData The new Grouped data.
     * @return The index of the grouped data.
     */
    public int addGroupedTabData(TabData tabData) {
        DetailTabInfo tabDetails = new DetailTabInfo();
        tabDetails.setDetailPriority((int)tabData.getPriority());
        tabDetails.setDetailTitle(tabData.getLabel());
        if (!detailLayout.contains(tabDetails)) {
            detailLayout.add(tabDetails);
        }
        detailLayout.sort(Comparator.comparing(DetailTabInfo::getDetailPriority));
        return detailLayout.indexOf(tabDetails);
    }
}
