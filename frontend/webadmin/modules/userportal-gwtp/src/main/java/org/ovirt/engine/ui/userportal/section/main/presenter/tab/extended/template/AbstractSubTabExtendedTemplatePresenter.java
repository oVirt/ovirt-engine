package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedTemplateSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public abstract class AbstractSubTabExtendedTemplatePresenter<D extends EntityModel, V extends AbstractSubTabPresenter.ViewDef<VmTemplate>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<VmTemplate, UserPortalTemplateListModel, D, V, P> {

    public AbstractSubTabExtendedTemplatePresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<UserPortalTemplateListModel, D> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                ExtendedTemplateSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(UserPortalApplicationPlaces.extendedTemplateSideTabPlace);
    }

    @ProxyEvent
    public void onExtendedTemplateSelectionChange(ExtendedTemplateSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
