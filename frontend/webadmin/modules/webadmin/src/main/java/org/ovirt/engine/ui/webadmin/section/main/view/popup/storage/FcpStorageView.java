package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.webadmin.widget.storage.SanStorageLunToTargetList;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public class FcpStorageView extends AbstractSanStorageView {

    @UiField
    @Path(value = "GetLUNsFailure")
    Label errorMessage;

    SanStorageLunToTargetList sanStorageLunToTargetList;

    @Override
    protected void initLists(SanStorageModelBase object) {
        // Create and update storage list
        sanStorageLunToTargetList = new SanStorageLunToTargetList(object, true);
        sanStorageLunToTargetList.activateItemsUpdate();

        // Set tree style
        sanStorageLunToTargetList.setTreeContainerStyleName(style.treePanel());

        // Add view widget to panel
        listPanel.add(sanStorageLunToTargetList);
    }
}
