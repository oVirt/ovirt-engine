package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ApplySearchStringEvent;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HasHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
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
public abstract class AbstractMainTabWithDetailsPresenter<T, M extends ListWithDetailsModel,
    V extends AbstractMainTabWithDetailsPresenter.ViewDef<T>, P extends TabContentProxyPlace<?>>
        extends AbstractMainTabPresenter<T, M, V, P> implements DetailsTransitionHandler<T> {

    public interface ViewDef<T> extends View, HasActionTable<T> {
        void setDetailPlaceTransitionHandler(DetailsTransitionHandler<T> handler);
        void setBreadCrumbs(OvirtBreadCrumbs<T, ?> breadCrumbs);
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<>();

    private final SearchPanelPresenterWidget<T, M> searchPanelPresenterWidget;

    private final OvirtBreadCrumbs<T, M> breadCrumbs;

    @Inject
    private SearchStringCollector searchStringCollector;

    public AbstractMainTabWithDetailsPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, MainModelProvider<T, M> modelProvider,
            SearchPanelPresenterWidget<T, M> searchPanelPresenterWidget,
            OvirtBreadCrumbs<T, M> breadCrumbs) {
        super(eventBus, view, proxy, placeManager, modelProvider);
        this.searchPanelPresenterWidget = searchPanelPresenterWidget;
        this.breadCrumbs = breadCrumbs;
    }

    @Override
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

                    if (!isVisible()) {
                        handlePlaceTransition();
                    }
                }));
        registerHandler(getEventBus().addHandler(ApplySearchStringEvent.getType(), event -> {
            applySearchString(event.getSearchString());
        }));
        getView().setDetailPlaceTransitionHandler(this);
        String searchString = searchStringCollector.getSearchStringPrefix(modelProvider.getModel().getSearchString());
        if (searchString != null) {
            // Someone set search string before we were instantiated, update the search string.
            applySearchString(searchString);
        }
    }

    private void applySearchString(String searchString) {
        if (modelProvider.getModel() instanceof SearchableListModel) {
            @SuppressWarnings("unchecked")
            SearchableListModel<?, ? extends EntityModel<?>> listModel = modelProvider.getModel();
            if (!StringUtils.isEmpty(searchString)
                    && searchString.startsWith(listModel.getDefaultSearchString())) {
                // search string for this model found.
                listModel.setSearchString(searchString);
                listModel.getSearchCommand().execute();
                MainModelSelectionChangeEvent.fire((HasHandlers) getEventBus(), listModel);
            }
        }
    }

    public void handlePlaceTransition() {
        if (hasSelection() && hasSelectionDetails()) {
            // Sub tab panel is shown upon revealing the sub tab, in order to avoid
            // the 'flicker' effect due to the panel still showing previous content
            placeManager.revealPlace(getSubTabRequest());
        } else {
            // Hide sub tab panel when there is nothing selected
            setSubTabPanelVisible(false);
            getBreadCrumbs().clearActiveSubTab();
            placeManager.revealPlace(getMainTabRequest());
        }
    }

    protected boolean hasSelectionDetails() {
        return true;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setSubTabPanelVisible(false);
        getBreadCrumbs().clearActiveSubTab();
        getTable().resetScrollPosition();
        getView().setBreadCrumbs(getBreadCrumbs());
        if (hasSearchPanelPresenterWidget()) {
            setInSlot(TYPE_SetSearchPanel, searchPanelPresenterWidget);
        }
    }

    /**
     * Subclasses should fire an event to indicate that the table selection has changed.
     */
    protected abstract void fireTableSelectionChangeEvent();

    protected PlaceRequest getSubTabRequest() {
        String subTabName = modelProvider.getModel().getActiveDetailModel().getHashName();
        String requestToken = getMainTabRequest().getNameToken() + WebAdminApplicationPlaces.SUB_TAB_PREFIX + subTabName;
        return PlaceRequestFactory.get(requestToken);
    }

    /**
     * Returns items currently selected in the table.
     */
    protected List<T> getSelectedItems() {
        return getTable().getSelectionModel().getSelectedList();
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

    public boolean hasSearchPanelPresenterWidget() {
        return getSearchPanelPresenterWidget() != null;
    }

    public OvirtBreadCrumbs<T, M> getBreadCrumbs() {
        return breadCrumbs;
    }

    protected void setTags(List<TagModel> tags) {
        if (hasSearchPanelPresenterWidget()) {
            searchPanelPresenterWidget.setTags(tags);
        }
    }
}
