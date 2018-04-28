package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSelectedCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.view.client.SelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class OvirtBreadCrumbsPresenterWidget<T, M extends SearchableListModel>
    extends PresenterWidget<OvirtBreadCrumbsPresenterWidget.ViewDef> implements ListModelSelectedCallback<T> {

    public interface ViewDef<T> extends View {

        void buildCrumbs(String modelTitle, String modelHref);
        void setCurrentSelectedNameForItem(T item);
        void hidePopover();
        boolean isSearchVisible();
        void toggleSearchWidget();
        void setSelectionCallback(ListModelSelectedCallback<T> selectionCallback);
        void hideSelectedWidget();
    }

    private final MainModelProvider<T, M> listModelProvider;

    private boolean updateToFirstRow = false;
    private boolean updateToLastRow = false;
    private boolean showSelectedName = true;

    @Inject
    public OvirtBreadCrumbsPresenterWidget(EventBus eventBus, ViewDef<T> view,
            MainModelProvider<T, M> listModelProvider) {
        super(eventBus, view);
        this.listModelProvider = listModelProvider;
        view.setSelectionCallback(this);
        // Since the current selected item is set before this is instantiated, we need to look up the value
        // in the constructor.
        updateSelectedRows();
    }

    private SelectionModel<T> getSelectionModel() {
       return getModel().getSelectionModel();
    }

    private M getModel() {
        return listModelProvider.getModel();
    }

    @Override
    protected void onBind() {
        super.onBind();
        M listModel = getModel();
        listModel.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateSelectedRows());
        listModel.getSelectedItemsChangedEvent().addListener((ev, sender, args) -> updateSelectedRows());
    }

    private void updateSelectedRows() {
        if (updateToFirstRow) {
            updateToFirstRow = false;
            final List<T> itemsAsList = getItemsAsList(getModel());
            if (!itemsAsList.isEmpty()) {
                Scheduler.get().scheduleDeferred(() -> {
                    T firstItem = itemsAsList.get(0);
                    getSelectionModel().setSelected(firstItem, true);
                    getView().setCurrentSelectedNameForItem(firstItem);
                });
            }
        } else if (updateToLastRow) {
            updateToLastRow = false;
            final List<T> itemsAsList = getItemsAsList(getModel());
            if (!itemsAsList.isEmpty()) {
                Scheduler.get().scheduleDeferred(() -> {
                    T lastItem = itemsAsList.get(itemsAsList.size() - 1);
                    getSelectionModel().setSelected(lastItem, true);
                    getView().setCurrentSelectedNameForItem(lastItem);
                });
            }
        }
        if (getModel().getSelectedItem() != null && showSelectedName) {
            getView().setCurrentSelectedNameForItem((T) getModel().getSelectedItem());
        }
        getView().buildCrumbs(getModel().getTitle(), getModel().getApplicationPlace());
    }

    public void hideSelectedName() {
        this.showSelectedName = false;
        getView().hideSelectedWidget();
    }

    private List<T> getItemsAsList(M searchableListModel) {
        Collection<T> items = searchableListModel.getItems();
        return items != null ? new ArrayList<>(searchableListModel.getItems()) : Collections.emptyList();
    }

    public void nextEntity() {
        M searchableListModel = getModel();

        T entity = (T) searchableListModel.getSelectedItem();
        List<T> itemsAsList = getItemsAsList(searchableListModel);
        int currentIndex = itemsAsList.indexOf(entity);
        int newIndex = currentIndex + 1;
        if (newIndex >= itemsAsList.size()) {
            if (searchableListModel.getSearchNextPageCommand().getIsExecutionAllowed() &&
                    searchableListModel.getSearchNextPageCommand().getIsAvailable()) {
                searchableListModel.executeCommand(searchableListModel.getSearchNextPageCommand());
                updateToFirstRow = true;
            }
        } else {
            getSelectionModel().setSelected(itemsAsList.get(newIndex), true);
        }
    }

    public void previousEntity() {
        M searchableListModel = getModel();

        T entity = (T) searchableListModel.getSelectedItem();
        List<T> itemsAsList = getItemsAsList(searchableListModel);
        int currentIndex = itemsAsList.indexOf(entity);
        int newIndex = currentIndex - 1;
        if (newIndex < 0) {
            if (searchableListModel.getSearchPreviousPageCommand().getIsExecutionAllowed() &&
                    searchableListModel.getSearchPreviousPageCommand().getIsAvailable()) {
                searchableListModel.executeCommand(searchableListModel.getSearchPreviousPageCommand());
                updateToLastRow = true;
            }
        } else {
            getSelectionModel().setSelected(itemsAsList.get(newIndex), true);
        }
    }

    public void toggleSearch() {
        getView().toggleSearchWidget();
    }

    public boolean isSearchVisible() {
        return getView().isSearchVisible();
    }

    @Override
    public void modelSelected(T model) {
        getView().hidePopover();
        getSelectionModel().setSelected(model, true);
    }

    public void rebuildBreadCrumbs() {
        getView().buildCrumbs(getModel().getTitle(), getModel().getApplicationPlace());
    }
}
