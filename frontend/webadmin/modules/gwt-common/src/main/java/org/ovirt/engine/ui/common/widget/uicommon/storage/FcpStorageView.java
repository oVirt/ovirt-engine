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
    static final double panelHeight = 365;

    public FcpStorageView(boolean multiSelection) {
        this(multiSelection, panelHeight);
    }

    public FcpStorageView(boolean multiSelection, double panelHeight) {
        super(multiSelection);

        contentPanel.getElement().getStyle().setHeight(panelHeight, Unit.PX);
    }

    @Override
    protected void initLists(SanStorageModelBase object) {
        // Create and update storage list
        sanStorageLunToTargetList = new SanStorageLunToTargetList(object, true, multiSelection);
        sanStorageLunToTargetList.activateItemsUpdate();

        // Set tree style
        sanStorageLunToTargetList.setTreeContainerStyleName(style.treePanel());

        // Add view widget to panel
        listPanel.add(sanStorageLunToTargetList);
    }
}
