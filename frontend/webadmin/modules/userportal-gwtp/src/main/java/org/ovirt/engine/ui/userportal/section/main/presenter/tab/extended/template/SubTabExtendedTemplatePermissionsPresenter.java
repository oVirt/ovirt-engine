package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedTemplateSelectionChangeEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.template.PermissionListModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedTemplatePermissionsPresenter
        extends AbstractSubTabPresenter<VmTemplate, UserPortalTemplateListModel, PermissionListModel, SubTabExtendedTemplatePermissionsPresenter.ViewDef, SubTabExtendedTemplatePermissionsPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedTempplatePersmissionsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedTemplatePermissionsPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = ExtendedTemplateSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedTemplatePermissionsSubTabLabel(), 4);
    }

    @Inject
    public SubTabExtendedTemplatePermissionsPresenter(EventBus eventBus,
            ViewDef view,
            ProxyDef proxy,
            PlaceManager placeManager,
            PermissionListModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.extendedTemplateSideTabPlace);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, ExtendedTemplateSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @ProxyEvent
    public void onExtendedTemplateSelectionChange(ExtendedTemplateSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }
}
