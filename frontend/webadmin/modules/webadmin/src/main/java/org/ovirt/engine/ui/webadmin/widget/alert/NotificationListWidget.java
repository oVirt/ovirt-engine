package org.ovirt.engine.ui.webadmin.widget.alert;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.PanelGroup;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.action.ActionAnchorListItem;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;

public class NotificationListWidget extends Composite implements HasData<AuditLog> {

    interface WidgetUiBinder extends UiBinder<Widget, NotificationListWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final String BTN_LINK = "btn-link"; // $NON-NLS-1$
    private static final String BTN_DEFAULT = "btn-default"; // $NON-NLS-1$

    @UiField
    Heading title;

    @UiField
    PanelGroup contentPanel;

    @UiField
    Anchor toggleAnchor;

    private ToggleHandler toggleHandler;

    private int rowCount;

    private Range range = new Range(0, 10);

    private List<String> actionLabels = new ArrayList<>();
    private List<AuditLogActionCallback> auditLogActions = new ArrayList<>();
    private List<UICommand> actionCommand = new ArrayList<>();

    private String allActionLabel;
    private AuditLogActionCallback allActionCallback;
    private UICommand allActionCommand;

    private List<? extends AuditLog> currentValues;

    public NotificationListWidget(String headerTitle) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        setHeaderTitle(headerTitle);
    }

    public void setHeaderTitle(String title) {
        this.title.setText(title);
    }

    public void setToggleHandler(ToggleHandler handler) {
        this.toggleHandler = handler;
    }

    @UiHandler("toggleAnchor")
    void onToggleAnchor(ClickEvent event) {
        if (toggleHandler != null) {
            toggleHandler.toggle();
        }
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        return null;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Range getVisibleRange() {
        return range;
    }

    @Override
    public boolean isRowCountExact() {
        return true;
    }

    @Override
    public void setRowCount(int count) {
        this.rowCount = count;
        range = new Range(0, count);
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
        this.rowCount = count;
    }

    @Override
    public void setVisibleRange(int start, int length) {
        range = new Range(start, length);
    }

    @Override
    public void setVisibleRange(Range range) {
        this.range = range;
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(
            com.google.gwt.view.client.CellPreviewEvent.Handler<AuditLog> handler) {
        return null;
    }

    @Override
    public SelectionModel<? super AuditLog> getSelectionModel() {
        return null;
    }

    @Override
    public AuditLog getVisibleItem(int indexOnPage) {
        return null;
    }

    @Override
    public int getVisibleItemCount() {
        return 0;
    }

    @Override
    public Iterable<AuditLog> getVisibleItems() {
        return null;
    }

    @Override
    public void setRowData(int start, List<? extends AuditLog> values) {
        // Compare the new values with the ones currently displayed, if no changes, don't refresh.
        if (values != null && !valuesEquals(values)) {
            currentValues = values;
            contentPanel.clear();
            Panel eventPanel = new Panel();
            contentPanel.add(eventPanel);
            PanelHeader eventPanelHeading = new PanelHeader();
            eventPanelHeading.setText(this.title.getText());
            eventPanel.add(eventPanelHeading);
            PanelCollapse eventCollapse = new PanelCollapse();
            eventCollapse.removeStyleName(Styles.COLLAPSE);
            PanelBody eventPanelBody = new PanelBody();
            eventCollapse.add(eventPanelBody);
            eventPanel.add(eventCollapse);
            for (final AuditLog auditLog: values) {
                DrawerNotification notification = new DrawerNotification(auditLog);
                for (int i = 0; i < actionLabels.size(); i++) {
                    final int index = i;
                    ActionAnchorListItem listItem = new ActionAnchorListItem(actionLabels.get(index));
                    listItem.addClickHandler(e -> {
                        auditLogActions.get(index).executeCommand(actionCommand.get(index), auditLog);
                    });
                    notification.addActionButton(listItem);
                }
                eventPanelBody.add(notification);
            }
            if (allActionLabel != null) {
                FlowPanel actionPanel = new FlowPanel();
                actionPanel.addStyleName(PatternflyConstants.PF_DRAWER_ACTION);
                eventCollapse.add(actionPanel);
                Button button = new Button(allActionLabel);
                button.addStyleName(BTN_LINK);
                button.addStyleName(Styles.BTN_BLOCK);
                button.removeStyleName(BTN_DEFAULT);
                button.addClickHandler(event -> {
                    allActionCallback.executeCommand(allActionCommand, null);
                });
                actionPanel.add(button);
            }
        }
    }

    private boolean valuesEquals(List<? extends AuditLog> values) {
        boolean result = true;
        if (currentValues == null || currentValues.size() != values.size()) {
            result = false;
        } else {
            for (int i = 0; i < values.size(); i++) {
                // We know values and current values are the same size.
                if (!currentValues.get(i).equals(values.get(i))) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void setSelectionModel(SelectionModel<? super AuditLog> selectionModel) {
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        this.range = range;
    }

    public void addActionCallback(String label, UICommand command, AuditLogActionCallback callback) {
        actionLabels.add(label);
        actionCommand.add(command);
        auditLogActions.add(callback);
    }

    public void addAllActionCallback(String label, UICommand command, AuditLogActionCallback callback) {
        allActionLabel = label;
        allActionCommand = command;
        allActionCallback = callback;
    }
}
