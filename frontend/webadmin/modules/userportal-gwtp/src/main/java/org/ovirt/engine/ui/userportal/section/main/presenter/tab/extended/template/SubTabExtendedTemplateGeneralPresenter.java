package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedTemplateSelectionChangeEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.template.TemplateGeneralModelProvider;

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

public class SubTabExtendedTemplateGeneralPresenter extends AbstractSubTabPresenter<VmTemplate, UserPortalTemplateListModel, TemplateGeneralModel, SubTabExtendedTemplateGeneralPresenter.ViewDef, SubTabExtendedTemplateGeneralPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedTempplateGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedTemplateGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
        void editTemplate(VmTemplate entity);
    }

    @TabInfo(container = ExtendedTemplateSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedTemplateGeneralSubTabLabel(), 0);
    }

    @Inject
    public SubTabExtendedTemplateGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, TemplateGeneralModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, ExtendedTemplateSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.extendedTemplateSideTabPlace);
    }

    @ProxyEvent
    public void onExtendedTemplateSelectionChange(ExtendedTemplateSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

    @Override
    protected void onDetailModelEntityChange(Object entity) {
        if (entity instanceof VmTemplate) {
            getView().editTemplate((VmTemplate) entity);
        }
    }

}
