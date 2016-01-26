package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.refresh.RefreshPanel;
import org.ovirt.engine.ui.common.widget.refresh.SimpleRefreshManager;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedResourcePresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;
import org.ovirt.engine.ui.userportal.widget.QuotaCPUProgressBar;
import org.ovirt.engine.ui.userportal.widget.QuotaMemoryProgressBar;
import org.ovirt.engine.ui.userportal.widget.QuotaProgressBar;
import org.ovirt.engine.ui.userportal.widget.QuotaStorageProgressBar;
import org.ovirt.engine.ui.userportal.widget.ToStringEntityModelLabel;
import org.ovirt.engine.ui.userportal.widget.resources.VmTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SideTabExtendedResourceView extends AbstractView implements SideTabExtendedResourcePresenter.ViewDef {

    private ResourcesModel model;
    private static final int INFO_BOX_UPPER_PART_HEIGHT = 150;
    private static final int STORAGE_BOX_UPPER_PART_HEIGHT = 350;

    interface ViewUiBinder extends UiBinder<Widget, SideTabExtendedResourceView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<ResourcesModel, SideTabExtendedResourceView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedResourceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    interface Style extends CssResource {
        String quotaNameText();
    }

    @UiField
    Style style;

    @UiField(provided = true)
    @Path("usedCPUsPercentage.entity")
    @WithElementId("usedCpusPercentage")
    QuotaProgressBar cpusProgressBar;

    @UiField
    @WithElementId
    VerticalPanel cpusQuotasList;

    @UiField(provided = true)
    @Path("usedMemoryPercentage.entity")
    @WithElementId("memoryUsagePercentage")
    QuotaProgressBar memoryProgressBar;

    @UiField
    @WithElementId
    VerticalPanel memoryQuotasList;

    @UiField(provided = true)
    @Path("usedMemoryPercentage.entity")
    @WithElementId("storageUsagePercentage")
    QuotaProgressBar storageProgressBar;

    @UiField
    @WithElementId
    VerticalPanel storageQuotasList;

    @UiField
    @Path("totalDisksSize.entity")
    @WithElementId
    ToStringEntityModelLabel totalSize;

    @UiField
    @Path("numOfSnapshots.entity")
    @WithElementId
    ToStringEntityModelLabel numOfSnapshots;

    @UiField
    @Path("totalSnapshotsSize.entity")
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
    FlowPanel infoBoxMemory;

    @UiField
    @Ignore
    FlowPanel infoBoxCpu;

    @UiField
    @Ignore
    FlowPanel bottomLayoutPanel;

    @UiField(provided = true)
    RefreshPanel refreshPanel;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public SideTabExtendedResourceView(UserPortalDataBoundModelProvider<VM, ResourcesModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            SubTableResources headerResources) {
        vmTable = new VmTable(modelProvider, headerResources);

        SimpleRefreshManager refreshManager = new SimpleRefreshManager(modelProvider, eventBus, clientStorage);
        refreshPanel = refreshManager.getRefreshPanel();

        cpusProgressBar = new QuotaCPUProgressBar();
        memoryProgressBar = new QuotaMemoryProgressBar();
        storageProgressBar = new QuotaStorageProgressBar();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();

        modelProvider.getModel().getUsedQuotaPercentage().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                initQuotaList(model);
            }
        });

        setResizeHandler();

        vcpuExpander.initWithContent(vcpuExpanderContent.getElement());
        memoryExpander.initWithContent(memoryExpanderContent.getElement());
        storageExpander.initWithContent(storageExpanderContent.getElement());
    }

    private void setResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                vcpuExpanderContent.setHeight(numOrZero(infoBoxCpu.getOffsetHeight() - INFO_BOX_UPPER_PART_HEIGHT) + "px"); //$NON-NLS-1$
                memoryExpanderContent.setHeight(numOrZero(infoBoxMemory.getOffsetHeight() - INFO_BOX_UPPER_PART_HEIGHT) + "px"); //$NON-NLS-1$
                vmTable.setHeight(numOrZero(bottomLayoutPanel.getOffsetHeight() - STORAGE_BOX_UPPER_PART_HEIGHT) + "px"); //$NON-NLS-1$
            }
        });
    }

    private int numOrZero(int num) {
        if (num < 0) {
            return 0;
        } else {
            return num;
        }
    }

    private void localize() {
        vcpuExpander.setTitleWhenCollapsed(constants.showQuotaDistribution());
        vcpuExpander.setTitleWhenExpanded(constants.hideQuotaDistribution());

        memoryExpander.setTitleWhenCollapsed(constants.showQuotaDistribution());
        memoryExpander.setTitleWhenExpanded(constants.hideQuotaDistribution());

        storageExpander.setTitleWhenCollapsed(constants.showQuotaDistribution());
        storageExpander.setTitleWhenExpanded(constants.hideQuotaDistribution());
    }

    public void initQuotaList(ResourcesModel model) {
        List<QuotaUsagePerUser> list = (List<QuotaUsagePerUser>) model.getUsedQuotaPercentage().getEntity();
        QuotaUsagePerUser aggregatedUsage = new QuotaUsagePerUser(Guid.Empty, "", 0, 0, 0, 0, 0, 0);

        if (list != null) {
            cpusQuotasList.clear();
            memoryQuotasList.clear();
            storageQuotasList.clear();

            cpusQuotasList.setSpacing(7);
            memoryQuotasList.setSpacing(7);
            storageQuotasList.setSpacing(7);

            for (QuotaUsagePerUser quotaPerUserUsageEntity : list) {
                aggregate(aggregatedUsage, quotaPerUserUsageEntity);

                addQuotaToVcpuQuotaList(quotaPerUserUsageEntity);
                addQuotaToMemoryQuotaList(quotaPerUserUsageEntity);
                addQuotaToStorageQuotaList(quotaPerUserUsageEntity);
            }

            cpusProgressBar.setQuotaUsagePerUser(aggregatedUsage);
            memoryProgressBar.setQuotaUsagePerUser(aggregatedUsage);
            storageProgressBar.setQuotaUsagePerUser(aggregatedUsage);

            String title = constants.showQuotaDistribution() + " (" + list.size() + ")"; //$NON-NLS-1$  //$NON-NLS-2$
            vcpuExpander.setTitleWhenCollapsed(title);
            memoryExpander.setTitleWhenCollapsed(title);
            storageExpander.setTitleWhenCollapsed(title);
        }
    }

    private void addQuotaToVcpuQuotaList(QuotaUsagePerUser quotaPerUserUsageEntity) {
        QuotaProgressBar vcpuQuotaProgressBar = new QuotaCPUProgressBar(quotaPerUserUsageEntity);
        addQuotaRow(cpusQuotasList, quotaPerUserUsageEntity.getQuotaName(), vcpuQuotaProgressBar);
    }

    private void addQuotaToMemoryQuotaList(QuotaUsagePerUser quotaPerUserUsageEntity) {
        QuotaProgressBar memoryQuotaProgressBar = new QuotaMemoryProgressBar(quotaPerUserUsageEntity);
        addQuotaRow(memoryQuotasList, quotaPerUserUsageEntity.getQuotaName(), memoryQuotaProgressBar);
    }

    private void addQuotaToStorageQuotaList(QuotaUsagePerUser quotaPerUserUsageEntity) {
        QuotaProgressBar storageQuotaProgressBar = new QuotaStorageProgressBar(quotaPerUserUsageEntity);
        addQuotaRow(storageQuotasList, quotaPerUserUsageEntity.getQuotaName(), storageQuotaProgressBar);
    }

    private void addQuotaRow(VerticalPanel list, String quotaName, QuotaProgressBar progressBar) {
        FlowPanel flowPanel = new FlowPanel();
        Label quotaNameLabel = new Label();
        quotaNameLabel.setText(quotaName);
        quotaNameLabel.setStyleName(style.quotaNameText());
        flowPanel.add(quotaNameLabel);
        flowPanel.add(progressBar);
        list.add(flowPanel);
    }

    private void aggregate(QuotaUsagePerUser aggregatedUsage, QuotaUsagePerUser quotaPerUserUsageEntity) {
        if (quotaPerUserUsageEntity.isUnlimitedVcpu() || aggregatedUsage.isUnlimitedVcpu()) {
            aggregatedUsage.setVcpuLimit(QuotaProgressBar.UNLIMITED);
        } else {
            aggregatedUsage.setVcpuLimit(quotaPerUserUsageEntity.getVcpuLimit() + aggregatedUsage.getVcpuLimit());
        }
        aggregatedUsage.setVcpuUsageForUser(quotaPerUserUsageEntity.getVcpuUsageForUser()
                + aggregatedUsage.getVcpuUsageForUser());
        aggregatedUsage.setVcpuTotalUsage(quotaPerUserUsageEntity.getVcpuTotalUsage()
                + aggregatedUsage.getVcpuTotalUsage());

        if (quotaPerUserUsageEntity.isUnlimitedMemory() || aggregatedUsage.isUnlimitedMemory()) {
            aggregatedUsage.setMemoryLimit(QuotaProgressBar.UNLIMITED);
        } else {
            aggregatedUsage.setMemoryLimit(quotaPerUserUsageEntity.getMemoryLimit() + aggregatedUsage.getMemoryLimit());
        }
        aggregatedUsage.setMemoryUsageForUser(quotaPerUserUsageEntity.getMemoryUsageForUser()
                + aggregatedUsage.getMemoryUsageForUser());
        aggregatedUsage.setMemoryTotalUsage(quotaPerUserUsageEntity.getMemoryTotalUsage()
                + aggregatedUsage.getMemoryTotalUsage());

        if (quotaPerUserUsageEntity.isUnlimitedStorage() || aggregatedUsage.isUnlimitedStorage()) {
            aggregatedUsage.setStorageLimit(QuotaProgressBar.UNLIMITED);
        } else {
            aggregatedUsage.setStorageLimit(quotaPerUserUsageEntity.getStorageLimit()
                    + aggregatedUsage.getStorageLimit());
        }
        aggregatedUsage.setStorageUsageForUser(quotaPerUserUsageEntity.getStorageUsageForUser()
                + aggregatedUsage.getStorageUsageForUser());
        aggregatedUsage.setStorageTotalUsage(quotaPerUserUsageEntity.getStorageTotalUsage()
                + aggregatedUsage.getStorageTotalUsage());
    }

    @Override
    public void edit(ResourcesModel model) {
        vmTable.edit(model);
        driver.edit(model);
        this.model = model;
    }

    @Override
    public ResourcesModel flush() {
        return driver.flush();
    }

}
