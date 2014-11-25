package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabTemplateInterfacePresenter extends AbstractSubTabPresenter<VmTemplate, TemplateListModel, TemplateInterfaceListModel, SubTabTemplateInterfacePresenter.ViewDef, SubTabTemplateInterfacePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.templateInterfaceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabTemplateInterfacePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = TemplateSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.templateInterfaceSubTabLabel(), 2, modelProvider);
    }

    @Inject
    public SubTabTemplateInterfacePresenter(EventBus eventBus,
            ViewDef view,
            ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                TemplateSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.templateMainTabPlace);
    }

    @ProxyEvent
    public void onTemplateSelectionChange(TemplateSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
