package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.*;
import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedResourcePresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.resources.ResourcesModelProvider;
import org.ovirt.engine.ui.userportal.widget.PercentageProgressBar;
import org.ovirt.engine.ui.userportal.widget.QuotaProgressBar;
import org.ovirt.engine.ui.userportal.widget.ToStringEntityModelLabel;
import org.ovirt.engine.ui.userportal.widget.resources.VmTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

import java.util.List;

public class SideTabExtendedResourceView extends AbstractView implements SideTabExtendedResourcePresenter.ViewDef {

    private ResourcesModel model;

    interface ViewUiBinder extends UiBinder<Widget, SideTabExtendedResourceView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<ResourcesModel, SideTabExtendedResourceView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedResourceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    private static final CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);
    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<Number>(DiskSizeRenderer.DiskSizeUnit.GIGABYTE);

    @UiField
    Style style;

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
    @WithElementId("runningVmsPercentage")
    PercentageProgressBar vmsProgressBar;

    @UiField
    @Path("UsedCPUsPercentage.Entity")
    @WithElementId("usedCpusPercentage")
    QuotaProgressBar cpusProgressBar;

    @UiField
    @WithElementId
    VerticalPanel cpusQuotasList;

    @UiField
    @Path("UsedMemoryPercentage.Entity")
    @WithElementId("memoryUsagePercentage")
    QuotaProgressBar memoryProgressBar;

    @UiField
    @WithElementId
    VerticalPanel memoryQuotasList;

    @UiField
    @Path("UsedMemoryPercentage.Entity")
    @WithElementId("storageUsagePercentage")
    QuotaProgressBar storageProgressBar;

    @UiField
    @WithElementId
    VerticalPanel storageQuotasList;

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

    @UiField
    @Ignore
    public AdvancedParametersExpander vcpuExpander;

    @UiField
    @Ignore
    public ScrollPanel vcpuExpanderContent;

    @UiField
    @Ignore
    public AdvancedParametersExpander memoryExpander;

    @UiField
    @Ignore
    public ScrollPanel memoryExpanderContent;

    @UiField
    @Ignore
    public AdvancedParametersExpander storageExpander;

    @UiField
    @Ignore
    public ScrollPanel storageExpanderContent;

    @UiField
    @Ignore
    FlowPanel infoBoxRight;

    @UiField
    @Ignore
    FlowPanel infoBoxMiddle;

    @UiField
    @Ignore
    FlowPanel infoBoxLeft;

    @UiField
    @Ignore
    DockLayoutPanel bottomLayoutPanel;

    @UiField
    @Ignore
    HorizontalPanel bottomInfoBox;

    @Inject
    public SideTabExtendedResourceView(ResourcesModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            SubTableResources headerResources, ApplicationResources resources, ApplicationConstants constans) {
        vmTable = new VmTable(modelProvider, headerResources, resources, constans);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();

        modelProvider.getModel().getUsedQuotaPercentage().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                initQuotaList(model);
            }
        });

        vcpuExpander.initWithContent(vcpuExpanderContent.getElement());
        memoryExpander.initWithContent(memoryExpanderContent.getElement());
        storageExpander.initWithContent(storageExpanderContent.getElement());

        ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (vcpuExpander.isDown() || memoryExpander.isDown()) {
                    infoBoxLeft.setStyleName(style.infoBoxLeftExtended());
                    infoBoxMiddle.setStyleName(style.infoBoxMiddleExtended());
                    infoBoxRight.setStyleName(style.infoBoxRightExtended());
                } else {
                    infoBoxLeft.setStyleName(style.infoBoxLeft());
                    infoBoxMiddle.setStyleName(style.infoBoxMiddle());
                    infoBoxRight.setStyleName(style.infoBoxRight());
                }
            }
        };
        vcpuExpander.addClickHandler(clickHandler);
        memoryExpander.addClickHandler(clickHandler);
        storageExpander.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (storageExpander.isDown()) {
                    bottomLayoutPanel.setWidgetSize(bottomInfoBox, 260);
                    bottomLayoutPanel.setHeight("400px"); //$NON-NLS-1$
                } else {
                    bottomLayoutPanel.setWidgetSize(bottomInfoBox, 130);
                    bottomLayoutPanel.setHeight("265px"); //$NON-NLS-1$
                }
            }
        });
    }

    private void localize() {
        vcpuExpander.setTitleWhenCollapsed(constants.showQuotaDistribution());
        vcpuExpander.setTitleWhenExpended(constants.hideQuotaDistribution());

        memoryExpander.setTitleWhenCollapsed(constants.showQuotaDistribution());
        memoryExpander.setTitleWhenExpended(constants.hideQuotaDistribution());

        storageExpander.setTitleWhenCollapsed(constants.showQuotaDistribution());
        storageExpander.setTitleWhenExpended(constants.hideQuotaDistribution());
    }


    public void initQuotaList(ResourcesModel model) {
        List<QuotaUsagePerUser> list = (List<QuotaUsagePerUser>) model.getUsedQuotaPercentage().getEntity();
        QuotaUsagePerUser aggregatedUsage = new QuotaUsagePerUser(Guid.Empty, "", 0, 0, 0, 0, 0, 0);

        boolean unlimitedVcpu = false;
        boolean unlimitedMem = false;
        boolean unlimitedStorage = false;

        if (list != null) {
            cpusQuotasList.clear();
            memoryQuotasList.clear();
            storageQuotasList.clear();

            cpusQuotasList.setSpacing(7);
            memoryQuotasList.setSpacing(7);
            storageQuotasList.setSpacing(7);

            for (QuotaUsagePerUser quotaPerUserUsageEntity : list) {
                unlimitedMem |= quotaPerUserUsageEntity.isUnlimitedMemory();
                unlimitedStorage |= quotaPerUserUsageEntity.isUnlimitedStorage();
                unlimitedVcpu |= quotaPerUserUsageEntity.isUnlimitedVcpu();

                aggregate(aggregatedUsage, quotaPerUserUsageEntity);

                addQuotaToVcpuQuotaList(quotaPerUserUsageEntity);
                addQuotaToMemoryQuotaList(quotaPerUserUsageEntity);
                addQuotaToStorageQuotaList(quotaPerUserUsageEntity);
            }

            if (unlimitedVcpu) {
                cpusProgressBar.setUnlimited(true);
            } else {
                cpusProgressBar.setValueA((int)aggregatedUsage.getOthersVcpuUsagePercentage());
                cpusProgressBar.setValueB((int) aggregatedUsage.getUserVcpuUsagePercentage());
                cpusProgressBar.setTitle(messages.quotaFreeCpus(aggregatedUsage.getFreeVcpu()));
            }

            if (unlimitedMem) {
                memoryProgressBar.setUnlimited(true);
            } else {
                memoryProgressBar.setValueA((int)aggregatedUsage.getOthersMemoryUsagePercentage());
                memoryProgressBar.setValueB((int)aggregatedUsage.getUserMemoryUsagePercentage());
                String freeMem = aggregatedUsage.getFreeMemory() > 4096 ?
                        aggregatedUsage.getFreeMemory()/1024 + "GB" //$NON-NLS-1$
                        : aggregatedUsage.getFreeMemory() + "MB"; //$NON-NLS-1$
                memoryProgressBar.setTitle(constants.freeMemory() + freeMem);
            }

            if (unlimitedStorage) {
                storageProgressBar.setUnlimited(true);
            } else {
                storageProgressBar.setValueA((int)aggregatedUsage.getOthersStorageUsagePercentage());
                storageProgressBar.setValueB((int)aggregatedUsage.getUserStorageUsagePercentage());
                String freeStorage = aggregatedUsage.getFreeStorage() == 0 ?
                        "0" : //$NON-NLS-1$
                        diskSizeRenderer.render(aggregatedUsage.getFreeStorage());
                storageProgressBar.setTitle(constants.freeStorage() + freeStorage);
            }

            vcpuExpander.setTitleWhenCollapsed(constants.showQuotaDistribution() + " (" + list.size() + ")"); //$NON-NLS-1$  //$NON-NLS-2$
        }
    }

    private void addQuotaToVcpuQuotaList(QuotaUsagePerUser quotaPerUserUsageEntity) {
        QuotaProgressBar vcpuQuotaProgressBar = new QuotaProgressBar();
        if (quotaPerUserUsageEntity.isUnlimitedVcpu()) {
            vcpuQuotaProgressBar.setUnlimited(true);
        } else {
            vcpuQuotaProgressBar.setValueA((int) quotaPerUserUsageEntity.getOthersVcpuUsagePercentage());
            vcpuQuotaProgressBar.setValueB((int) quotaPerUserUsageEntity.getUserVcpuUsagePercentage());
            vcpuQuotaProgressBar.setTitle(messages.quotaFreeCpus(quotaPerUserUsageEntity.getFreeVcpu()));
            if (quotaPerUserUsageEntity.getVcpuTotalUsage() == 0) {
                vcpuQuotaProgressBar.setZeroValue();
            }
        }

        addQuotaRow(cpusQuotasList, quotaPerUserUsageEntity.getQuotaName(), vcpuQuotaProgressBar);
    }

    private void addQuotaRow(VerticalPanel list, String quotaName, QuotaProgressBar progressBar) {
        VerticalPanel verticalPanel = new VerticalPanel();
        Label quotaNameLabel = new Label();
        quotaNameLabel.setText(quotaName);
        verticalPanel.add(quotaNameLabel);
        verticalPanel.add(progressBar);
        verticalPanel.setWidth("100%"); //$NON-NLS-1$
        list.add(verticalPanel);
    }

    private void addQuotaToMemoryQuotaList(QuotaUsagePerUser quotaPerUserUsageEntity) {
        QuotaProgressBar memoryQuotaProgressBar = new QuotaProgressBar();
        if (quotaPerUserUsageEntity.isUnlimitedMemory()) {
            memoryQuotaProgressBar.setUnlimited(true);
        } else {
            memoryQuotaProgressBar.setValueA((int) quotaPerUserUsageEntity.getOthersMemoryUsagePercentage());
            memoryQuotaProgressBar.setValueB((int) quotaPerUserUsageEntity.getUserMemoryUsagePercentage());
            String freeMem = quotaPerUserUsageEntity.getFreeMemory() > 4096 ?
                    quotaPerUserUsageEntity.getFreeMemory()/1024 + "GB" //$NON-NLS-1$
                    : quotaPerUserUsageEntity.getFreeMemory() + "MB"; //$NON-NLS-1$
            memoryQuotaProgressBar.setTitle(constants.freeMemory() + freeMem);
            if (quotaPerUserUsageEntity.getMemoryTotalUsage() == 0) {
                memoryQuotaProgressBar.setZeroValue();
            }
        }

        addQuotaRow(memoryQuotasList, quotaPerUserUsageEntity.getQuotaName(), memoryQuotaProgressBar);
    }

    private void addQuotaToStorageQuotaList(QuotaUsagePerUser quotaPerUserUsageEntity) {
        QuotaProgressBar storageQuotaProgressBar = new QuotaProgressBar();
        if (quotaPerUserUsageEntity.isUnlimitedStorage()) {
            storageQuotaProgressBar.setUnlimited(true);
        } else {
            storageQuotaProgressBar.setValueA((int) quotaPerUserUsageEntity.getOthersStorageUsagePercentage());
            storageQuotaProgressBar.setValueB((int) quotaPerUserUsageEntity.getUserStorageUsagePercentage());
            String freeStorage = quotaPerUserUsageEntity.getFreeStorage() == 0 ?
                    "0" : //$NON-NLS-1$
                    diskSizeRenderer.render(quotaPerUserUsageEntity.getFreeStorage());
            storageQuotaProgressBar.setTitle(constants.freeStorage() + freeStorage);
            if (quotaPerUserUsageEntity.getMemoryTotalUsage() == 0) {
                storageQuotaProgressBar.setZeroValue();
            }
        }

        addQuotaRow(storageQuotasList, quotaPerUserUsageEntity.getQuotaName(), storageQuotaProgressBar);
    }

    private void aggregate(QuotaUsagePerUser aggregatedUsage, QuotaUsagePerUser quotaPerUserUsageEntity) {
        aggregatedUsage.setVcpuLimit(quotaPerUserUsageEntity.getVcpuLimit() + aggregatedUsage.getVcpuLimit());
        aggregatedUsage.setVcpuUsageForUser(quotaPerUserUsageEntity.getVcpuUsageForUser() + aggregatedUsage.getVcpuUsageForUser());
        aggregatedUsage.setVcpuTotalUsage(quotaPerUserUsageEntity.getVcpuTotalUsage() + aggregatedUsage.getVcpuTotalUsage());

        aggregatedUsage.setMemoryLimit(quotaPerUserUsageEntity.getMemoryLimit() + aggregatedUsage.getMemoryLimit());
        aggregatedUsage.setMemoryUsageForUser(quotaPerUserUsageEntity.getMemoryUsageForUser() + aggregatedUsage.getMemoryUsageForUser());
        aggregatedUsage.setMemoryTotalUsage(quotaPerUserUsageEntity.getMemoryTotalUsage() + aggregatedUsage.getMemoryTotalUsage());

        aggregatedUsage.setStorageLimit(quotaPerUserUsageEntity.getStorageLimit() + aggregatedUsage.getStorageLimit());
        aggregatedUsage.setStorageUsageForUser(quotaPerUserUsageEntity.getStorageUsageForUser() + aggregatedUsage.getStorageUsageForUser());
        aggregatedUsage.setStorageTotalUsage(quotaPerUserUsageEntity.getStorageTotalUsage() + aggregatedUsage.getStorageTotalUsage());
    }

    @Override
    public void edit(ResourcesModel model) {
        vmTable.edit(model);
        Driver.driver.edit(model);
        this.model = model;
    }

    @Override
    public ResourcesModel flush() {
        return Driver.driver.flush();
    }

    interface Style extends CssResource {

        String infoBoxLeft();
        String infoBoxMiddle();
        String infoBoxRight();
        String infoBoxLeftExtended();
        String infoBoxMiddleExtended();
        String infoBoxRightExtended();
    }

}
