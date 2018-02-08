package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
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

public class DiskSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<DiskSubTabPanelPresenter.ViewDef, DiskSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<DiskSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<Disk, DiskListModel> modelProvider;

    @Inject
    public DiskSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            DiskMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        DiskListModel mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.DISKS_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.DISKS_VMS, mainModel.getVmListModel());
        mapping.put(DetailTabDataIndex.DISKS_TEMPLATES, mainModel.getTemplateListModel());
        mapping.put(DetailTabDataIndex.DISKS_STORAGE, mainModel.getStorageListModel());
        mapping.put(DetailTabDataIndex.DISKS_PERMISSIONS, mainModel.getPermissionListModel());
    }

}
