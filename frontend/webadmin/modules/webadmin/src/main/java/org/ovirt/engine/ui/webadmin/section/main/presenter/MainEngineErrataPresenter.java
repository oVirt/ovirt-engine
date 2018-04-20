package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Presenter for the main tab that contains errata (singular: Erratum) for the engine itself.
 * Note: this tab is only show when 'Errata' is selected in the System Tree.
 */
public class MainEngineErrataPresenter extends AbstractMainWithDetailsPresenter<Erratum,
    EngineErrataListModel, MainEngineErrataPresenter.ViewDef, MainEngineErrataPresenter.ProxyDef> {

    @GenEvent
    public class ErrataSelectionChange {
        List<Erratum> selectedItems;
    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.errataMainPlace)
    public interface ProxyDef extends ProxyPlace<MainEngineErrataPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<Erratum> {
        void showErrorMessage(SafeHtml errorMessage);
        void clearErrorMessage();
        public ErrataFilterPanel getErrataFilterPanel();
    }

    @Inject
    public MainEngineErrataPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Erratum, EngineErrataListModel> modelProvider,
            OvirtBreadCrumbsPresenterWidget<Erratum, EngineErrataListModel> breadCrumbs) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, null, breadCrumbs, null);
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.errataMainPlace);
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
            } else if (getModel().getMessage() == null || getModel().getMessage().isEmpty()) {
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
