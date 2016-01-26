package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

/**
 * Presenter for the sub-sub tab (Hosts > General > Errata) that contains errata (singular: Erratum)
 * for the selected Host.
 */
public class SubTabHostGeneralHostErrataPresenter
    extends AbstractSubTabHostPresenter<HostErrataCountModel,
        SubTabHostGeneralHostErrataPresenter.ViewDef, SubTabHostGeneralHostErrataPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostGeneralErrataSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostGeneralHostErrataPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
        AbstractUiCommandButton getTotalSecurity();
        AbstractUiCommandButton getTotalBugFix();
        AbstractUiCommandButton getTotalEnhancement();
        void showErrorMessage(SafeHtml errorMessage);
        void showCounts(ErrataCounts counts);
        void showProgress();
    }

    @TabInfo(container = HostGeneralSubTabPanelPresenter.class)
    static TabData getTabData(DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel> errataCountModelProvider) {
        return new ModelBoundTabData(constants.hostGeneralErrataSubTabLabel(), 7, errataCountModelProvider);
    }

    private final HostErrataCountModel errataCountModel;

    @Inject
    public SubTabHostGeneralHostErrataPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,  HostMainTabSelectedItems selectedItems,
            DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel> errataCountModelProvider) {
        super(eventBus, view, proxy, placeManager, errataCountModelProvider, selectedItems,
                HostGeneralSubTabPanelPresenter.TYPE_SetTabContent);

        errataCountModel = getModelProvider().getModel();
    }

    @Override
    public void itemChanged(VDS item) {
        super.itemChanged(item);
        if (isVisible()) {
            updateModel();
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        updateModel();
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter#initializeHandlers()
     */
    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        registerHandler(getView().getTotalSecurity().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().getTotalSecurity().getCommand().execute();
            }
        }));

        registerHandler(getView().getTotalBugFix().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().getTotalBugFix().getCommand().execute();
            }
        }));

        registerHandler(getView().getTotalEnhancement().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().getTotalEnhancement().getCommand().execute();
            }
        }));


        // Handle the counts changing -> simple view update.
        //
        errataCountModel.addErrataCountsChangeListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                // bus published message that the counts changed. update view.
                ErrataCounts counts = errataCountModel.getErrataCounts();
                getView().showCounts(counts);
            }
        });

        // Handle the count model getting a query error -> simple view update.
        //
        errataCountModel.addPropertyChangeListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("Message".equals(args.propertyName)) { //$NON-NLS-1$
                    if (errataCountModel.getMessage() != null && !errataCountModel.getMessage().isEmpty()) {
                        // bus published message that an error occurred communicating with Katello. Show the alert panel.
                        getView().showErrorMessage(SafeHtmlUtils.fromString(errataCountModel.getMessage()));
                    }
                } else if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                    if (errataCountModel.getProgress() != null) {
                        getView().showProgress();
                    }
                }
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();

        getView().getTotalSecurity().setCommand(errataCountModel.getShowSecurityCommand());
        getView().getTotalBugFix().setCommand(errataCountModel.getShowBugsCommand());
        getView().getTotalEnhancement().setCommand(errataCountModel.getShowEnhancementsCommand());
    }


    private void updateModel() {
        VDS currentSelectedHost = getSelectedMainItems().getSelectedItem();
        if (currentSelectedHost != null) {
            errataCountModel.setGuid(currentSelectedHost.getId());
            errataCountModel.setEntity(currentSelectedHost);
            errataCountModel.runQuery(currentSelectedHost.getId());
        }
    }
}
