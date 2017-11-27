package org.ovirt.engine.ui.common.view;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Breadcrumbs;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSearchBox;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSelectedCallback;
import org.ovirt.engine.ui.common.widget.tooltip.OvirtPopover;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class OvirtBreadCrumbsView<T, M extends SearchableListModel> extends AbstractView
    implements OvirtBreadCrumbsPresenterWidget.ViewDef<T> {

    public interface ViewUiBinder extends UiBinder<Widget, OvirtBreadCrumbsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final String SEARCH_PANEL_CONTENT_ID = "searchPanel"; // $NON-NLS-1$
    private static final String QUICK_SWITCH = "quickswitch"; // $NON-NLS-1$

    @UiField
    Breadcrumbs breadCrumbs;

    @UiField
    FlowPanel container;

    private OvirtPopover popover;
    private ListModelSearchBox<T, ?> searchBox;
    private ListModelSelectedCallback<T> selectionCallback;

    private final MainModelProvider<T, M> listModelProvider;
    private final MenuDetailsProvider menuDetailsProvider;

    IsWidget currentSelectedItemWidget;
    private boolean hideSelectedWidget = false;

    @Inject
    public OvirtBreadCrumbsView(MainModelProvider<T, M> listModelProvider, MenuDetailsProvider menuDetailsProvider) {
        this.menuDetailsProvider = menuDetailsProvider;
        this.listModelProvider = listModelProvider;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void buildCrumbs(String modelTitle, String modelHref) {
        // Clear the existing path.
        container.clear();
        breadCrumbs = new Breadcrumbs();
        container.add(breadCrumbs);

        // Add primary menu label.
        String primaryLabel = menuDetailsProvider.getLabelFromHref(modelHref);
        if (primaryLabel != null) {
            breadCrumbs.add(new ListItem(primaryLabel));
        }
        menuDetailsProvider.setMenuActive(modelHref);

        // Add main model name.
        AnchorListItem mainModelAnchor = new AnchorListItem(modelTitle);
        mainModelAnchor.setHref("#" + modelHref); //$NON-NLS-1$
        breadCrumbs.add(mainModelAnchor);

        if (currentSelectedItemWidget != null && !hideSelectedWidget) {
            breadCrumbs.add(currentSelectedItemWidget);
        }
    }

    public void setCurrentSelectedNameForItem(T item) {
        currentSelectedItemWidget = createSelectionDropDown(getName(item));
    }

    // Can't make this a composite class since I can't put anything inside
    // an AnchorListItem besides text using ui.xml files. But manually I can
    // add widgets without issue.
    private AnchorListItem createSelectionDropDown(SafeHtml currentName) {
        OvirtAnchorListItem dropDown = new OvirtAnchorListItem();
        Button exchangeButton = new Button();
        exchangeButton.setIcon(IconType.EXCHANGE);
        exchangeButton.addStyleName(QUICK_SWITCH);
        exchangeButton.getElement().getStyle().setMarginLeft(10, Unit.PX);
        exchangeButton.getElement().getStyle().setMarginTop(-5, Unit.PX);
        Anchor anchor = dropDown.getAnchor();
        anchor.getElement().setInnerHTML(currentName.asString());
        anchor.getElement().getStyle().setFontSize(28, Unit.PX);
        anchor.addClickHandler(e -> {
            if (popover.isVisible()) {
                popover.hide();
            } else {
                popover.show();
            }
        });
        anchor.add(exchangeButton);
        createPopover(anchor);
        dropDown.add(anchor);
        return dropDown;
    }

    private void createPopover(Anchor anchor) {
        if (popover != null) {
            popover.destroy();
        }
        popover = new OvirtPopover(anchor);
        popover.setTrigger(Trigger.MANUAL);
        popover.setPlacement(Placement.BOTTOM);
        popover.setAutoClose(true);
        popover.setContainer(anchor);
        if (searchBox == null) {
            searchBox = createSearchBox();
            searchBox.addModelSelectedCallback(selectionCallback);
        }
        popover.addContent(searchBox, SEARCH_PANEL_CONTENT_ID);
    }

    // Need to keep this untyped so sub classes can override this.
    protected ListModelSearchBox createSearchBox() {
        return new ListModelSearchBox(listModelProvider) {
            @Override
            protected SafeHtml getName(Object item) {
                return OvirtBreadCrumbsView.this.getName((T) item);
            }
        };
    }

    public SafeHtml getName(T item) {
        String result = "";
        if (item instanceof Nameable) {
            result = ((Nameable)item).getName();
        }
        return SafeHtmlUtils.fromString(result);
    }

    @Override
    public void hidePopover() {
        popover.hide();
    }

    @Override
    public void setSelectionCallback(ListModelSelectedCallback<T> selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    @Override
    public void toggleSearchWidget() {
        if (popover != null && currentSelectedItemWidget != null) {
            if (popover.isVisible()) {
                popover.hide();
            } else {
                popover.show();
            }
        }
    }

    @Override
    public boolean isSearchVisible() {
        return popover != null && popover.isVisible();
    }

    private static class OvirtAnchorListItem extends AnchorListItem {
        public Anchor getAnchor() {
            return anchor;
        }
    }

    @Override
    public void hideSelectedWidget() {
        this.hideSelectedWidget = true;
    }
}
