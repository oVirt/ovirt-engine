package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.MenuLayoutMenuDetails;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Presenter for the main tab that contains errata (singular: Erratum) for the engine itself.
 * Note: this tab is only show when 'Errata' is selected in the System Tree.
 */
public class MainTabEngineErrataPresenter extends AbstractMainTabWithDetailsPresenter<Erratum,
    EngineErrataListModel, MainTabEngineErrataPresenter.ViewDef, MainTabEngineErrataPresenter.ProxyDef> {

    @GenEvent
    public class ErrataSelectionChange {
        List<Erratum> selectedItems;
    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.errataMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabEngineErrataPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<Erratum> {
        void showErrorMessage(SafeHtml errorMessage);
        void clearErrorMessage();
        public ErrataFilterPanel getErrataFilterPanel();
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(WebadminMenuLayout menuLayout) {
        MenuLayoutMenuDetails menuDetails = menuLayout.getDetails(
                WebAdminApplicationPlaces.errataMainTabPlace);
        return new GroupedTabData(menuDetails);
    }

    @Inject
    public MainTabEngineErrataPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Erratum, EngineErrataListModel> modelProvider,
            OvirtBreadCrumbsPresenterWidget<Erratum, EngineErrataListModel> breadCrumbs) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, null, breadCrumbs, null);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.errataMainTabPlace);
    }

    @Override
    protected PlaceRequest getSubTabRequest() {
        return PlaceRequestFactory.get( WebAdminApplicationPlaces.errataDetailsSubTabPlace);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        ErrataSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected void onBind() {
        super.onBind();

        // Handle the list model getting a query error -> simple view update.
        //
        getModel().addErrorMessageChangeListener((ev, sender, args) -> {

            if (getModel().getMessage() != null && !getModel().getMessage().isEmpty()) {
                // bus published message that an error occurred communicating with Katello. Show the alert panel.
                getView().showErrorMessage(SafeHtmlUtils.fromString(getModel().getMessage()));
            }
            else if (getModel().getMessage() == null || getModel().getMessage().isEmpty()) {
                getView().clearErrorMessage();
            }
        });

        // Handle the filter panel value changing -> simple view update (re-filter).
        //
        getView().getErrataFilterPanel().addValueChangeHandler(event -> {
            getModel().setItemsFilter(event.getValue());
            getModel().reFilter();
        });
    }

}
