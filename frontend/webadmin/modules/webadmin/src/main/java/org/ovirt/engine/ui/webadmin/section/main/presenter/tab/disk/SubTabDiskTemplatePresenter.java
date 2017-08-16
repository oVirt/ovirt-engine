package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabDiskTemplatePresenter
    extends AbstractSubTabDiskPresenter<DiskTemplateListModel, SubTabDiskTemplatePresenter.ViewDef,
        SubTabDiskTemplatePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.diskTemplateSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDiskTemplatePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Disk> {
    }

    @TabInfo(container = DiskSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.DISKS_TEMPLATES;
    }

    @Inject
    public SubTabDiskTemplatePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DiskMainSelectedItems selectedItems,
            SearchableDetailModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel> modelProvider) {
        // No action panel on disk template view, can pass null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                DiskSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
