package org.ovirt.engine.ui.common.widget.listgroup;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class PatternflyListViewItem<T> extends Composite implements ClickHandler, HasClickHandlers {

    private static final String LIST_GROUP_ITEM_CONTAINER = "list-group-item-container"; // $NON-NLS-1$
    protected static final String DOUBLE_SIZE = "fa-2x"; // $NON-NLS-1$
    protected static final String GREEN = "green"; // $NON-NLS-1$
    protected static final String RED = "red"; // $NON-NLS-1$
    protected static final String STACKED_DETAIL_ITEM = "stacked-detail-item"; // $NON-NLS-1$

    interface WidgetUiBinder extends UiBinder<Widget, PatternflyListViewItem<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    protected ListGroupItem listGroupItem;

    @UiField
    protected FlowPanel checkBoxPanel;

    @UiField
    protected FlowPanel mainInfoPanel;

    @UiField
    protected FlowPanel iconPanel;

    @UiField
    protected FlowPanel bodyPanel;

    @UiField
    protected FlowPanel descriptionPanel;

    @UiField
    protected FlowPanel additionalInfoPanel;

    @UiField
    protected FlowPanel descriptionHeaderPanel;

    @UiField
    protected FlowPanel statusPanel;

    private T entity;

    private List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public PatternflyListViewItem(String name, T entity) {
        this.entity = entity;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        createIcon();
        createBodyPanel(SafeHtmlUtils.fromString(name), entity);
    }

    protected FlowPanel getCheckBoxPanel() {
        return checkBoxPanel;
    }

    protected Container createItemContainerPanel(Row content) {
        return createItemContainerPanel(content, true);
    }

    protected Container createItemContainerPanel(Row content, boolean hidden) {
        Container panel = new Container();
        panel.addStyleName(LIST_GROUP_ITEM_CONTAINER);
        if (hidden) {
            panel.addStyleName(ExpandableListViewItem.HIDDEN);
            panel.setFluid(true);
            Button closeButton = new Button();
            closeButton.addStyleName(Styles.CLOSE);
            getClickHandlerRegistrations().add(closeButton.addClickHandler(this));
            Span icon = new Span();
            icon.addStyleName(PatternflyConstants.PFICON);
            icon.addStyleName(PatternflyConstants.PFICON_CLOSE);
            closeButton.add(icon);
            panel.add(closeButton);
        }
        panel.add(content);
        return panel;
    }

    public ListGroupItem asListGroupItem() {
        return listGroupItem;
    }

    protected void addReverseDetailItem(SafeHtml label, SafeHtml value, DListElement parent) {
        Element dt = Document.get().createElement("dt"); // $NON-NLS-1$
        dt.setInnerSafeHtml(value); // $NON-NLS-1$
        parent.appendChild(dt);

        Element dd = Document.get().createElement("dd"); // $NON-NLS-1$
        dd.setInnerSafeHtml(label);
        parent.appendChild(dd);
    }

    protected void addDetailItem(SafeHtml label, SafeHtml value, DListElement parent) {
        Element dt = Document.get().createElement("dt"); // $NON-NLS-1$
        dt.setInnerSafeHtml(label); // $NON-NLS-1$
        parent.appendChild(dt);

        Element dd = Document.get().createElement("dd"); // $NON-NLS-1$
        dd.setInnerSafeHtml(value);
        parent.appendChild(dd);
    }

    protected void addStackedDetailItem(SafeHtml label, SafeHtml value, DListElement parent) {
        Element dt = Document.get().createElement("dt"); // $NON-NLS-1$
        dt.setInnerSafeHtml(label); // $NON-NLS-1$
        parent.appendChild(dt);
        dt.addClassName(STACKED_DETAIL_ITEM);
        dt.getStyle().setFloat(Style.Float.LEFT);
        dt.getStyle().setPaddingRight(5, Unit.PX);

        Element dd = Document.get().createElement("dd"); // $NON-NLS-1$
        dd.setInnerSafeHtml(value);
        parent.appendChild(dd);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return listGroupItem.addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public void onClick(ClickEvent event) {
        event.preventDefault();
        event.stopPropagation();
        ExpandableListViewItem eventItem = null;
        if (event.getSource() instanceof ExpandableListViewItem) {
            eventItem = (ExpandableListViewItem)event.getSource();
        }
        if (eventItem != null) {
            boolean active = eventItem.isActive();
            // Hide all, then unhide the one clicked.
            hideAllDetails();
            eventItem.toggleExpanded(!active);
            toggleExpanded();
        } else {
            // Clicked a close icon
            hideAllDetails();
        }
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        for (HandlerRegistration registration: handlerRegistrations) {
            registration.removeHandler();
        }
    }

    protected List<HandlerRegistration> getClickHandlerRegistrations() {
        return handlerRegistrations;
    }

    protected ColumnSize calculateColSize(int index) {
        // For index 3 return 2 so we have enough room for the X.
        if (index == 3) {
            return ColumnSize.MD_2;
        }
        return ColumnSize.MD_3;
    }

    public abstract void restoreStateFromViewItem(PatternflyListViewItem<T> originalViewItem);

    protected abstract IsWidget createIcon();
    protected abstract IsWidget createBodyPanel(SafeHtml header, T entity);
    protected abstract void hideAllDetails();
    protected abstract void toggleExpanded();
    protected abstract void toggleExpanded(boolean expand);
}
