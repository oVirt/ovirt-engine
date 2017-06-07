package org.ovirt.engine.ui.common.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Breadcrumbs;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.base.HasHref;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSearchBox;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSelectedCallback;
import org.ovirt.engine.ui.common.widget.tab.MenuLayout;
import org.ovirt.engine.ui.common.widget.tooltip.OvirtPopover;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Bread crumbs that allows the user to see where they are in the application. There are 3 levels
 * of depth that can be displayed.
 * <OL>
 *   <li>Primary level, for instance Computer</li>
 *   <li>Secondary level, for instance VMs</li>
 *   <li>Tertiary level, the selected entity from the list model</li>
 * </OL>
 *
 * @param <T> Model type of the list model
 * @param <M> The ListModel providing the selection and the name of the secondary menu.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class OvirtBreadCrumbs<T, M extends SearchableListModel> extends Breadcrumbs
    implements ListModelSelectedCallback<T> {

    IsWidget currentSelectedItemWidget;

    private final MenuLayout menuLayout;
    private final MainModelProvider<T, M> listModelProvider;
    private OvirtPopover popover;
    private final SelectionModel<T> selectionModel;
    private ListModelSearchBox<T, ?> searchBox;
    private boolean updateToFirstRow = false;
    private boolean updateToLastRow = false;
    private boolean detailTabsShowing = false;

    @Inject
    public OvirtBreadCrumbs(EventBus eventBus, MainModelProvider<T, M> listModelProvider, MenuLayout menuLayout) {
        this.listModelProvider = listModelProvider;
        this.menuLayout = menuLayout;
        M listModel = listModelProvider.getModel();
        listModel.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateSelectedRows());
        listModel.getSelectedItemsChangedEvent().addListener((ev, sender, args) -> updateSelectedRows());
        selectionModel = listModelProvider.getModel().getSelectionModel();
        // Since the current selected item is set before this is instantiated, we need to look up the value
        // in the constructor.
        updateSelectedRows();
    }

    public void updateSelectedRows() {
        if (updateToFirstRow) {
            updateToFirstRow = false;
            Collection<T> items = listModelProvider.getModel().getItems();
            if (items instanceof List) {
                final List<T> itemsAsList = (List<T>)items;
                if (!itemsAsList.isEmpty()) {
                    Scheduler.get().scheduleDeferred(() -> {
                        T firstItem = itemsAsList.get(0);
                        selectionModel.setSelected(firstItem, true);
                        listModelProvider.getModel().setSelectedItem(firstItem);
                        currentSelectedItemWidget = createSelectionDropDown(getName(firstItem));
                    });
                }
            }
        } else if (updateToLastRow) {
            updateToLastRow = false;
            Collection<T> items = listModelProvider.getModel().getItems();
            if (items instanceof List) {
                final List<T> itemsAsList = (List<T>)items;
                if (!itemsAsList.isEmpty()) {
                    Scheduler.get().scheduleDeferred(() -> {
                        T lastItem = itemsAsList.get(itemsAsList.size() - 1);
                        selectionModel.setSelected(lastItem, true);
                        listModelProvider.getModel().setSelectedItem(lastItem);
                        currentSelectedItemWidget = createSelectionDropDown(getName(lastItem));
                    });
                }
            }
        }
        if (listModelProvider.getModel().getSelectedItem() != null && detailTabsShowing) {
            currentSelectedItemWidget = createSelectionDropDown(getName((T) listModelProvider.getModel().getSelectedItem()));
        }
        buildCrumbs();
    }

    private String getName(T item) {
        String result = "";
        if (item instanceof Nameable) {
            result = ((Nameable)item).getName();
        }
        return result;
    }

    public void clearActiveSubTab() {
        currentSelectedItemWidget = null;
        detailTabsShowing = false;
        buildCrumbs();
    }

    public void setActiveSubTab(String title) {
        detailTabsShowing = true;
        currentSelectedItemWidget = createSelectionDropDown(getName((T) listModelProvider.getModel().getSelectedItem()));
        buildCrumbs();
    }

    private void buildCrumbs() {
        // Clear the existing path.
        this.clear();
        // Add starting >>
        add(new AnchorListItem(""));

        // Add primary menu label.
        String primaryLabel = menuLayout.getPrimaryGroupTitle(listModelProvider.getModel().getTitle());
        if (primaryLabel != null) {
            add(new ListItem(primaryLabel));
        }

        // Add main model name.
        AnchorListItem mainModelAnchor = new AnchorListItem(listModelProvider.getModel().getTitle());
        mainModelAnchor.setHref("#" + listModelProvider.getModel().getApplicationPlace()); //$NON-NLS-1$
        add(mainModelAnchor);

        if (currentSelectedItemWidget != null) {
            add(currentSelectedItemWidget);
        }

    }

    private AnchorListItem createSelectionDropDown(String currentName) {
        AnchorListItem dropDown = new AnchorListItem();
        Icon exchange = new Icon(IconType.EXCHANGE);
        exchange.setBorder(true);
        exchange.getElement().getStyle().setColor("#0088CC"); // $NON-NLS-1$
        exchange.getElement().getStyle().setBorderColor("#0088CC"); // $NON-NLS-1$
        exchange.getElement().getStyle().setProperty("borderRadius", 0.2 + Unit.EM.getType()); // $NON-NLS-1$
        exchange.getElement().getStyle().setMarginLeft(6, Unit.PX);
        exchange.getElement().getStyle().setCursor(Cursor.POINTER);
        Anchor anchor = new Anchor(currentName, HasHref.EMPTY_HREF);
        anchor.addClickHandler(e -> {
            if (popover.isVisible()) {
                popover.hide();
            } else {
                popover.show();
            }
        });
        anchor.add(exchange);
        createPopover(anchor);
        dropDown.add(anchor);
        return dropDown;
    }

    private void createPopover(Anchor anchor) {
        popover = new OvirtPopover(anchor);
        popover.setTrigger(Trigger.MANUAL);
        popover.setPlacement(Placement.BOTTOM);
        popover.setContainer(anchor);
        if (searchBox == null) {
            searchBox = new ListModelSearchBox<>(listModelProvider);
            searchBox.setSelectionModel(selectionModel);
            searchBox.addModelSelectedCallback(this);
        }
        popover.addContent(searchBox, "searchPanel"); // $NON-NLS-1$
    }

    @Override
    public void modelSelected(T model) {
        popover.hide();
    }

    public void toggleSearchWidget() {
        if (popover != null && currentSelectedItemWidget != null) {
            if (popover.isVisible()) {
                popover.hide();
            } else {
                popover.show();
            }
        }
    }

    public boolean isSearchVisible() {
        return popover.isVisible();
    }

    private List<T> getItemsAsList(M searchableListModel) {
        return new ArrayList<>(searchableListModel.getItems());
    }

    public void nextEntity() {
        M searchableListModel = listModelProvider.getModel();

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
            selectionModel.setSelected(itemsAsList.get(newIndex), true);
        }
    }

    public void previousEntity() {
        M searchableListModel = listModelProvider.getModel();

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
            selectionModel.setSelected(itemsAsList.get(newIndex), true);
        }
    }
}
