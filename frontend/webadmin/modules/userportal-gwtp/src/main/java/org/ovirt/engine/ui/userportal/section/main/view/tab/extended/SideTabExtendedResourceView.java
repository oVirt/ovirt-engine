package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedResourcePresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.resources.ResourcesModelProvider;
import org.ovirt.engine.ui.userportal.widget.PercentageProgressBar;
import org.ovirt.engine.ui.userportal.widget.ToStringEntityModelLabel;
import org.ovirt.engine.ui.userportal.widget.resources.VmTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SideTabExtendedResourceView extends AbstractView implements SideTabExtendedResourcePresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SideTabExtendedResourceView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<ResourcesModel, SideTabExtendedResourceView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedResourceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("DefinedVMs.Entity")
    @WithElementId
    ToStringEntityModelLabel definedVms;

    @UiField
    @Path("RunningVMs.Entity")
    @WithElementId
    ToStringEntityModelLabel runningVms;

    @UiField
    @Path("RunningVMsPercentage.Entity")
    PercentageProgressBar vmsProgressBar;

    @UiField
    @Path("DefinedCPUs.Entity")
    @WithElementId
    ToStringEntityModelLabel definedCpus;

    @UiField
    @Path("UsedCPUs.Entity")
    @WithElementId
    ToStringEntityModelLabel usedCpus;

    @UiField
    @Path("UsedCPUsPercentage.Entity")
    PercentageProgressBar cpusProgressBar;

    @UiField
    @Path("DefinedMemory.Entity")
    @WithElementId
    ToStringEntityModelLabel definedMemory;

    @UiField
    @Path("UsedMemory.Entity")
    @WithElementId
    ToStringEntityModelLabel memoryUsage;

    @UiField
    @Path("UsedMemoryPercentage.Entity")
    PercentageProgressBar memoryProgressBar;

    @UiField
    @Path("TotalDisksSize.Entity")
    @WithElementId
    ToStringEntityModelLabel totalSize;

    @UiField
    @Path("NumOfSnapshots.Entity")
    @WithElementId
    ToStringEntityModelLabel numOfSnapshots;

    @UiField
    @Path("TotalSnapshotsSize.Entity")
    @WithElementId
    ToStringEntityModelLabel totalSizeOfSnapshots;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    VmTable vmTable;

    @Inject
    public SideTabExtendedResourceView(ResourcesModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            SubTableResources headerResources, ApplicationResources resources, ApplicationConstants constans) {
        vmTable = new VmTable(modelProvider, headerResources, resources, constans);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
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
