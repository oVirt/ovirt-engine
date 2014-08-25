package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public class FcpStorageView extends AbstractSanStorageView {

    @UiField
    @Path(value = "GetLUNsFailure")
    Label errorMessage;

    SanStorageLunToTargetList sanStorageLunToTargetList;

    double panelHeight = 378;
    double listHeight = 340;

    public FcpStorageView(boolean multiSelection) {
        super(multiSelection);
    }

    public FcpStorageView(boolean multiSelection, double panelHeight, double listHeight) {
        super(multiSelection);

        this.panelHeight = panelHeight;
        this.listHeight = listHeight;
    }

    @Override
    protected void initLists(SanStorageModelBase object) {
        // Create and update storage list
        sanStorageLunToTargetList = new SanStorageLunToTargetList(object, true, multiSelection);
        sanStorageLunToTargetList.activateItemsUpdate();

        // Update style
        sanStorageLunToTargetList.setTreeContainerHeight(listHeight);
        contentPanel.getElement().getStyle().setHeight(panelHeight, Unit.PX);

        // Add view widget to panel
        contentPanel.setWidget(sanStorageLunToTargetList);
    }
}
