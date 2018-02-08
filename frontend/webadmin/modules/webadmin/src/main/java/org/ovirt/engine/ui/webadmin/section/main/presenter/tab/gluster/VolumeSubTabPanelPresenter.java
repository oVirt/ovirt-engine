package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
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

public class VolumeSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<VolumeSubTabPanelPresenter.ViewDef, VolumeSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<VolumeSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<GlusterVolumeEntity, VolumeListModel> modelProvider;

    @Inject
    public VolumeSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            VolumeMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        VolumeListModel mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.GLUSTER_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.GLUSTER_BRICKS, mainModel.getBrickListModel());
        mapping.put(DetailTabDataIndex.GLUSTER_PARAMETERS, mainModel.getParameterListModel());
        mapping.put(DetailTabDataIndex.GLUSTER_PERMISSIONS, mainModel.getPermissionListModel());
        mapping.put(DetailTabDataIndex.GLUSTER_EVENTS, mainModel.getEventListModel());
        mapping.put(DetailTabDataIndex.GLUSTER_GEO_REP, mainModel.getGeoRepListModel());
        mapping.put(DetailTabDataIndex.GLUSTER_SNAPSHOTS, mainModel.getSnapshotListModel());
    }

}
