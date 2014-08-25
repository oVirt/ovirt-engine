package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VnicProfileSelectionChangeEvent;

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

public class SubTabVnicProfileTemplatePresenter extends AbstractSubTabPresenter<VnicProfileView, VnicProfileListModel, VnicProfileTemplateListModel, SubTabVnicProfileTemplatePresenter.ViewDef, SubTabVnicProfileTemplatePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.vnicProfileTemplateSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVnicProfileTemplatePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VnicProfileView> {
    }

    @TabInfo(container = VnicProfileSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.vnicProfileTemplateSubTabLabel(), 1,
                modelProvider);
    }

    @Inject
    public SubTabVnicProfileTemplatePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, VnicProfileSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(ApplicationPlaces.templateMainTabPlace);
    }

    @ProxyEvent
    public void onVnicProfileSelectionChange(VnicProfileSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}

