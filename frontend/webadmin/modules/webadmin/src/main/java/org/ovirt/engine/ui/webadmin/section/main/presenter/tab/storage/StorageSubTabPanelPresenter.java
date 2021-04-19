package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class StorageSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<StorageSubTabPanelPresenter.ViewDef, StorageSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<StorageSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<StorageDomain, StorageListModel> modelProvider;

    @Inject
    public StorageSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            StorageMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        StorageListModel mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.STORAGE_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.STORAGE_DATA_CENTER, mainModel.getDcListModel());
        mapping.put(DetailTabDataIndex.STORAGE_REGISTER_VMS, mainModel.getVmRegisterListModel());
        mapping.put(DetailTabDataIndex.STORAGE_VM_BACKUP, mainModel.getVmBackupModel());
        mapping.put(DetailTabDataIndex.STORAGE_REGISTER_TEMPLATES, mainModel.getTemplateRegisterListModel());
        mapping.put(DetailTabDataIndex.STORAGE_TEMPLATE_BACKUP, mainModel.getTemplateBackupModel());
        mapping.put(DetailTabDataIndex.STORAGE_REGISTER_DISK_IMAGE, mainModel.getDiskImageRegisterListModel());
        mapping.put(DetailTabDataIndex.STORAGE_VMS, mainModel.getVmListModel());
        mapping.put(DetailTabDataIndex.STORAGE_TEMPLATES, mainModel.getTemplateListModel());
        mapping.put(DetailTabDataIndex.STORAGE_IMAGES, mainModel.getIsoListModel());
        mapping.put(DetailTabDataIndex.STORAGE_DISKS, mainModel.getDiskListModel());
        mapping.put(DetailTabDataIndex.STORAGE_SNAPSHOTS, mainModel.getSnapshotListModel());
        mapping.put(DetailTabDataIndex.STORAGE_LEASE, mainModel.getLeaseListModel());
        mapping.put(DetailTabDataIndex.STORAGE_DISK_PROFILES, mainModel.getDiskProfileListModel());
        mapping.put(DetailTabDataIndex.STORAGE_DR, mainModel.getDRListModel());
        mapping.put(DetailTabDataIndex.STORAGE_EVENTS, mainModel.getEventListModel());
        mapping.put(DetailTabDataIndex.STORAGE_PERMISSIONS, mainModel.getPermissionListModel());
    }

}
