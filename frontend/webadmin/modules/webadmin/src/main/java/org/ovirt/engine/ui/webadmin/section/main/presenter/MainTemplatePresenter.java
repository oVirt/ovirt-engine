package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.TemplateBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTemplatePresenter extends AbstractMainWithDetailsPresenter<VmTemplate, TemplateListModel, MainTemplatePresenter.ViewDef, MainTemplatePresenter.ProxyDef> {

    @GenEvent
    public class TemplateSelectionChange {

        List<VmTemplate> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.templateMainPlace)
    public interface ProxyDef extends ProxyPlace<MainTemplatePresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<VmTemplate> {
    }

    @Inject
    public MainTemplatePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VmTemplate, TemplateListModel> modelProvider,
            SearchPanelPresenterWidget<VmTemplate, TemplateListModel> searchPanelPresenterWidget,
            TemplateBreadCrumbsPresenterWidget breadCrumbs,
            TemplateActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        TemplateSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.templateMainPlace);
    }

}
