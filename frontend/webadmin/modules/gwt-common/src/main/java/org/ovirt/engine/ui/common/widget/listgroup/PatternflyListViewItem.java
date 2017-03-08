package org.ovirt.engine.ui.common.widget.listgroup;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public abstract class PatternflyListViewItem<T> extends ListGroupItem implements ClickHandler, HasClickHandlers{

    private static final String LIST_GROUP_ITEM_CONTAINER = "list-group-item-container"; // $NON-NLS-1$

    private T entity;

    private List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public PatternflyListViewItem(String name, T entity) {
        add(createCheckBoxPanel());
        add(createMainInfoPanel(name, entity));
        this.entity = entity;
    }

    private IsWidget createCheckBoxPanel() {
        FlowPanel checkBoxPanel = new FlowPanel();
        checkBoxPanel.addStyleName(PatternflyConstants.PF_LIST_VIEW_CHECKBOX);
        return checkBoxPanel;
    }

    private IsWidget createMainInfoPanel(String header, T entity) {
        FlowPanel mainInfoPanel = new FlowPanel();
        mainInfoPanel.addStyleName(PatternflyConstants.PF_LIST_VIEW_MAIN_INFO);
        mainInfoPanel.add(createIconPanel());
        mainInfoPanel.add(createBodyPanel(header, entity));
        return mainInfoPanel;
    }

    protected Container createItemContainerPanel(Row content) {
        Container panel = new Container();
        panel.addStyleName(LIST_GROUP_ITEM_CONTAINER);
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
        panel.add(content);
        return panel;
    }

    protected void addDetailItem(SafeHtml label, String value, DListElement parent) {
        Element dt = Document.get().createElement("dt"); // $NON-NLS-1$
        dt.setInnerSafeHtml(label); // $NON-NLS-1$
        parent.appendChild(dt);

        Element dd = Document.get().createElement("dd"); // $NON-NLS-1$
        dd.setInnerText(value);
        parent.appendChild(dd);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public void onClick(ClickEvent event) {
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

    public abstract void restoreStateFromViewItem(PatternflyListViewItem<T> originalViewItem);

    protected abstract IsWidget createIconPanel();
    protected abstract IsWidget createBodyPanel(String header, T entity);
    protected abstract void hideAllDetails();
    protected abstract void toggleExpanded();
}
