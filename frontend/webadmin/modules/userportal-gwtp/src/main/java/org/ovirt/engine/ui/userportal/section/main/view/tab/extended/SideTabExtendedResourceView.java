package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedResourcePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.UserPortalEntityModelLabel;
import org.ovirt.engine.ui.userportal.uicommon.model.resources.ResourcesModelProvider;
import org.ovirt.engine.ui.userportal.widget.PercentageProgressBar;
import org.ovirt.engine.ui.userportal.widget.resources.VmTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SideTabExtendedResourceView extends AbstractView implements SideTabExtendedResourcePresenter.ViewDef {

    @UiField
    @Path("DefinedVMs.Entity")
    UserPortalEntityModelLabel definedVms;

    @UiField
    @Path("RunningVMs.Entity")
    UserPortalEntityModelLabel runningVms;

    @UiField
    @Path("RunningVMsPercentage.Entity")
    PercentageProgressBar vmsProgressBar;

    @UiField
    @Path("DefinedCPUs.Entity")
    UserPortalEntityModelLabel definedCpus;

    @UiField
    @Path("UsedCPUs.Entity")
    UserPortalEntityModelLabel usedCpus;

    @UiField
    @Path("UsedCPUsPercentage.Entity")
    PercentageProgressBar cpusProgressBar;

    @UiField
    @Path("DefinedMemory.Entity")
    UserPortalEntityModelLabel definedMemory;

    @UiField
    @Path("UsedMemory.Entity")
    UserPortalEntityModelLabel memoryUsage;

    @UiField
    @Path("UsedMemoryPercentage.Entity")
    PercentageProgressBar memoryProgressBar;

    @UiField
    @Path("TotalDisksSize.Entity")
    UserPortalEntityModelLabel totalSize;

    @UiField
    @Path("NumOfSnapshots.Entity")
    UserPortalEntityModelLabel numOfSnapshots;

    @UiField
    @Path("TotalSnapshotsSize.Entity")
    UserPortalEntityModelLabel totalSizeOfSnapshots;

    @UiField(provided = true)
    @Ignore
    VmTable vmTable;

    interface ViewUiBinder extends UiBinder<Widget, SideTabExtendedResourceView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<ResourcesModel, SideTabExtendedResourceView> {
        Driver driver = GWT.create(Driver.class);
    }

    @Inject
    public SideTabExtendedResourceView(ResourcesModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            SubTableResources headerResources, ApplicationResources resources) {

        vmTable = new VmTable(modelProvider, headerResources, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(ResourcesModel model) {
        vmTable.edit(model);
        Driver.driver.edit(model);
    }

    @Override
    public ResourcesModel flush() {
        return Driver.driver.flush();
    }

}
