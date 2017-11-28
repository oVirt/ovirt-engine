package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ApplySearchStringEvent;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Base class for main tab presenters that work with {@link ListWithDetailsModel}.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Main model type.
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractMainWithDetailsPresenter<T, M extends ListWithDetailsModel,
    V extends AbstractMainWithDetailsPresenter.ViewDef<T>, P extends ProxyPlace<?>>
        extends AbstractMainPresenter<T, M, V, P> implements DetailsTransitionHandler<T> {

    public interface ViewDef<T> extends View, HasActionTable<T> {
        void setDetailPlaceTransitionHandler(DetailsTransitionHandler<T> handler);

        void resizeToFullHeight();

        HandlerRegistration addWindowResizeHandler(ResizeHandler handler);

    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetBreadCrumbs = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetActionPanel = new Type<>();

    private final SearchPanelPresenterWidget<T, M> searchPanelPresenterWidget;

    private final OvirtBreadCrumbsPresenterWidget<T, M> breadCrumbsPresenterWidget;

    private boolean resizing = false;

    @Inject
    private SearchStringCollector searchStringCollector;

    public AbstractMainWithDetailsPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider,
            SearchPanelPresenterWidget<T, M> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<T, M> breadCrumbsPresenterWidget,
            ActionPanelPresenterWidget<T, M> actionPanelPresenterWidget) {
        super(eventBus, view, proxy, placeManager, modelProvider, actionPanelPresenterWidget);
        this.searchPanelPresenterWidget = searchPanelPresenterWidget;
        this.breadCrumbsPresenterWidget = breadCrumbsPresenterWidget;
        this.breadCrumbsPresenterWidget.hideSelectedName();
    }

    protected ActionTable<T> getTable() {
        return getView().getTable();
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getTable().getSelectionModel()
                .addSelectionChangeHandler(event -> {
                    // Update main model selection
                    modelProvider.setSelectedItems(getSelectedItems());

                    // Let others know that the table selection has changed
                    fireTableSelectionChangeEvent();

                    // We need to be visible so we don't display ourselves when the detail tabs
                    // quick switch search is run.
                    if (isVisible()) {
                        handlePlaceTransition(false);
                    }
                }));
        registerHandler(getEventBus().addHandler(ApplySearchStringEvent.getType(), event -> {
            applySearchString(event.getSearchString());
        }));
        getView().setDetailPlaceTransitionHandler(this);
        registerHandler(getView().addWindowResizeHandler(e -> {
            if (!resizing) {
                Scheduler.get().scheduleDeferred(() -> {
                    getView().resizeToFullHeight();
                    resizing = false;
                });
                resizing = true;
            }
        }));
        String searchString = searchStringCollector.getSearchStringPrefix(modelProvider.getModel().getSearchString());
        if (searchString != null) {
            // Someone set search string before we were instantiated, update the search string.
            applySearchString(searchString);
        }
        if (hasSearchPanelPresenterWidget()) {
            setInSlot(TYPE_SetSearchPanel, searchPanelPresenterWidget);
        }
        if (hasActionPanelPresenterWidget()) {
            setInSlot(TYPE_SetActionPanel, getActionPanelPresenterWidget());
        }
    }

    private void applySearchString(String searchString) {
        if (modelProvider.getModel() instanceof SearchableListModel) {
            @SuppressWarnings("unchecked")
            SearchableListModel<?, ? extends EntityModel<?>> listModel = modelProvider.getModel();
            if (StringHelper.isNotNullOrEmpty(searchString)
                    && searchString.startsWith(listModel.getDefaultSearchString())) {
                // search string for this model found.
                listModel.setSearchString(searchString);
                listModel.getSearchCommand().execute();
                MainModelSelectionChangeEvent.fire((HasHandlers) getEventBus(), listModel);
            }
        }
    }

    @Override
    public void handlePlaceTransition(boolean linkClicked) {
        if (hasSelection() && hasSelectionDetails() && linkClicked) {
            // Sub tab panel is shown upon revealing the sub tab, in order to avoid
            // the 'flicker' effect due to the panel still showing previous content
            placeManager.revealPlace(getSubTabRequest());
        } else {
            placeManager.revealPlace(getMainViewRequest());
        }
    }

    protected boolean hasSelectionDetails() {
        return true;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetSearchPanel, searchPanelPresenterWidget);
        setInSlot(TYPE_SetBreadCrumbs, breadCrumbsPresenterWidget);
        if (hasActionPanelPresenterWidget()) {
            getTable().setActionMenus(getActionPanelPresenterWidget().getActionButtons());
        }
        breadCrumbsPresenterWidget.rebuildBreadCrumbs();
        getView().resizeToFullHeight();
    }

    @Override
    protected void onHide() {
        getTable().hideContextMenu();
        getView().resizeToFullHeight();
    }

    /**
     * Subclasses should fire an event to indicate that the table selection has changed.
     */
    protected abstract void fireTableSelectionChangeEvent();

    protected PlaceRequest getSubTabRequest() {
        String subTabName;
        modelProvider.getModel().ensureActiveDetailModel();
        subTabName = modelProvider.getModel().getActiveDetailModel().getHashName();
        String requestToken = getMainViewRequest().getNameToken() + WebAdminApplicationPlaces.SUB_TAB_PREFIX + subTabName;
        return PlaceRequestFactory.get(requestToken);
    }

    /**
     * Returns items currently selected in the table.
     */
    protected List<T> getSelectedItems() {
        return getTable().getSelectionModel().asMultiSelectionModel().getSelectedList();
    }

    /**
     * Returns {@code true} when there is at least one item selected in the table, {@code false} otherwise.
     */
    protected boolean hasSelection() {
        return !getSelectedItems().isEmpty();
    }

    /**
     * Deselects any items currently selected in the table.
     */
    protected void clearSelection() {
        getTable().getSelectionModel().clear();
    }

    public SearchPanelPresenterWidget<?, ?> getSearchPanelPresenterWidget() {
        return searchPanelPresenterWidget;
    }

    public OvirtBreadCrumbsPresenterWidget<T, M> getBreadCrumbs() {
        return breadCrumbsPresenterWidget;
    }

    public boolean hasSearchPanelPresenterWidget() {
        return getSearchPanelPresenterWidget() != null;
    }

    public boolean hasActionPanelPresenterWidget() {
        return getActionPanelPresenterWidget() != null;
    }

    protected void setTags(List<TagModel> tags) {
        searchPanelPresenterWidget.setTags(tags);
    }
}
