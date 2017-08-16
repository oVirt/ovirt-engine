package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
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

public class SubTabTemplatePermissionPresenter
    extends AbstractSubTabTemplatePresenter<PermissionListModel<VmTemplate>, SubTabTemplatePermissionPresenter.ViewDef,
    SubTabTemplatePermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.templatePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabTemplatePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = TemplateSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.TEMPLATE_PERMISSION;
    }

    @Inject
    public SubTabTemplatePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, TemplateMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, TemplateListModel, PermissionListModel<VmTemplate>> modelProvider) {
        // View uses PermissionWithInheritedPermissionListModelTable to get action panel elsewhere passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                TemplateSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
