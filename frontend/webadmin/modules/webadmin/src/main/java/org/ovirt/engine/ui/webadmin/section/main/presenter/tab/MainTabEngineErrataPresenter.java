package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.ErrataFilterValue;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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

    private static final ApplicationConstants constants = AssetProvider.getConstants();

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
    static TabData getTabData(
            MainModelProvider<Erratum, EngineErrataListModel> modelErrata) {
        return new ModelBoundTabData(constants.errataMainTabLabel(), 1, modelErrata);
    }

    @Inject
    public MainTabEngineErrataPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Erratum, EngineErrataListModel> modelErrata) {
        super(eventBus, view, proxy, placeManager, modelErrata);
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
        getModel().addErrorMessageChangeListener(new IEventListener<PropertyChangedEventArgs>(){
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {

                if (getModel().getMessage() != null && !getModel().getMessage().isEmpty()) {
                    // bus published message that an error occurred communicating with Katello. Show the alert panel.
                    getView().showErrorMessage(SafeHtmlUtils.fromString(getModel().getMessage()));
                }
                else if (getModel().getMessage() == null || getModel().getMessage().isEmpty()) {
                    getView().clearErrorMessage();
                }
            }
        });

        // Handle the filter panel value changing -> simple view update (re-filter).
        //
        getView().getErrataFilterPanel().addValueChangeHandler(new ValueChangeHandler<ErrataFilterValue>() {
            @Override
            public void onValueChange(ValueChangeEvent<ErrataFilterValue> event) {
                getModel().setItemsFilter(event.getValue());
                getModel().reFilter();
            }
        });
    }

}
