package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedTemplateSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public abstract class BasicSubTabExtendedTemplatePresenter<M extends ListWithDetailsModel, D extends EntityModel, V extends AbstractSubTabPresenter.ViewDef<VmTemplate>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<VmTemplate, M, D, V, P> {

    public BasicSubTabExtendedTemplatePresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<M, D> modelProvider) {
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
