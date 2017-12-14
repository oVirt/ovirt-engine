package org.ovirt.engine.ui.webadmin.widget.alert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.action.ActionAnchorListItem;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.HasDataMinimalDelegate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class NotificationListWidget extends Composite implements ActionWidget {

    interface WidgetUiBinder extends UiBinder<Widget, NotificationListWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final String ARIA_EXPANDED = "aria-expanded"; // $NON-NLS-1$
    private static final String BTN_LINK = "btn-link"; // $NON-NLS-1$
    private static final String BTN_DEFAULT = "btn-default"; // $NON-NLS-1$
    private static final String MAX_HEIGHT = "maxHeight"; // $NON-NLS-1$

    String title;

    @UiField
    Panel content;

    private Anchor titleAnchor;
    private PanelHeader eventPanelHeading;
    private FlowPanel actionPanel;

    private PanelBody eventPanelBody;

    private Range range = new Range(0, 100);

    private Toggle toggle = Toggle.COLLAPSE;
    private String parentWidgetId;
    private String thisWidgetId;
    private boolean startCollapsed;

    private int rowCount;

    private HasData<AuditLog> hasDataDelegate = new HasDataMinimalDelegate<AuditLog>() {
        @Override
        public int getRowCount() {
            return rowCount;
        }

        @Override
        public void setRowData(int start, List<? extends AuditLog> values) {
            setInternalRowData(start, values);
        }

        @Override
        public Range getVisibleRange() {
            return range;
        }
    };

    private List<String> actionLabels = new ArrayList<>();
    private List<AuditLogActionCallback> auditLogActions = new ArrayList<>();
    private List<UICommand> actionCommand = new ArrayList<>();

    private String clearAllActionLabel;
    private AuditLogActionCallback clearAllActionCallback;
    private UICommand clearAllActionCommand;

    private String restoreAllActionLabel;
    private AuditLogActionCallback restoreAllActionCallback;
    private UICommand restoreAllActionCommand;

    private List<? extends AuditLog> currentValues;
    private int containerHeight = 0;

    public NotificationListWidget(String headerTitle) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        setHeaderTitle(headerTitle);
    }

    public void setHeaderTitle(String title) {
        this.title = title;
        this.thisWidgetId = title.replace(" ", "_").toLowerCase(); // $NON-NLS-1$ $NON-NLS-2$
     }

    @Override
    public void addAction(String buttonLabel, UICommand command, AuditLogActionCallback callback) {
        addActionCallback(buttonLabel, command, callback);
    }

    @Override
    public void addClearAllAction(String label, UICommand command, AuditLogActionCallback callback) {
        addClearAllActionCallback(label, command, callback);
    }

    @Override
    public void addRestoreAllAction(String label, UICommand command, AuditLogActionCallback callback) {
        addRestoreAllActionCallback(label, command, callback);
    }

    private void setInternalRowData(int start, List<? extends AuditLog> values) {
        // Compare the new values with the ones currently displayed, if no changes, don't refresh.
        if (values != null && !valuesEquals(values)) {
            rowCount = values.size();
            boolean collapsed = checkIfCollapsed();
            currentValues = values;
            content.clear();
            eventPanelHeading = new PanelHeader();
            Heading titleHeading = new Heading(HeadingSize.H4);
            titleHeading.addStyleName(PatternflyConstants.PF_PANEL_TITLE);
            titleAnchor = new Anchor(hashString(thisWidgetId));
            titleAnchor.setDataParent(hashString(parentWidgetId));
            titleAnchor.setDataTarget(hashString(thisWidgetId));
            titleAnchor.setDataToggle(this.toggle);
            titleAnchor.setText(this.title);
            titleAnchor.addClickHandler(e -> e.preventDefault());
            if (collapsed) {
                titleAnchor.addStyleName(PatternflyConstants.COLLAPSED);
            }
            titleHeading.add(titleAnchor);
            eventPanelHeading.add(titleHeading);

            content.add(eventPanelHeading);
            PanelCollapse eventCollapse = new PanelCollapse();
            eventCollapse.setId(thisWidgetId);
            eventPanelBody = new PanelBody();
            if (this.containerHeight > 0) {
                eventPanelBody.getElement().getStyle().setProperty(MAX_HEIGHT, containerHeight + Unit.PX.getType());
                eventPanelBody.getElement().getStyle().setOverflowY(Overflow.AUTO);
            }
            eventCollapse.add(eventPanelBody);
            if (collapsed) {
                eventCollapse.getElement().setAttribute(ARIA_EXPANDED, String.valueOf(false));
                eventCollapse.getElement().getStyle().setHeight(0, Unit.PX);
            } else {
                eventCollapse.getElement().setAttribute(ARIA_EXPANDED, String.valueOf(true));
                eventCollapse.addStyleName(Styles.IN);
            }
            content.add(eventCollapse);
            for (final AuditLog auditLog: values) {
                DrawerNotification notification = new DrawerNotification(auditLog);
                for (int i = 0; i < actionLabels.size(); i++) {
                    final int index = i;
                    ActionAnchorListItem listItem = new ActionAnchorListItem(actionLabels.get(index));
                    listItem.addClickHandler(e ->
                        auditLogActions.get(index).executeCommand(actionCommand.get(index), auditLog)
                    );
                    notification.addActionButton(listItem);
                }
                eventPanelBody.add(notification);
            }
            if (clearAllActionLabel != null) {
                actionPanel = new FlowPanel();
                actionPanel.addStyleName(PatternflyConstants.PF_DRAWER_ACTION);
                eventCollapse.add(actionPanel);
                Button clearAllbutton = new Button(clearAllActionLabel);
                clearAllbutton.addStyleName(BTN_LINK);
                clearAllbutton.addStyleName(Styles.BTN_BLOCK);
                clearAllbutton.removeStyleName(BTN_DEFAULT);
                clearAllbutton.addClickHandler(event ->
                    clearAllActionCallback.executeCommand(clearAllActionCommand, null)
                );
                actionPanel.add(clearAllbutton);
                Button restoreAllbutton = new Button(restoreAllActionLabel);
                restoreAllbutton.addStyleName(BTN_LINK);
                restoreAllbutton.addStyleName(Styles.BTN_BLOCK);
                restoreAllbutton.removeStyleName(BTN_DEFAULT);
                restoreAllbutton.addClickHandler(event ->
                    restoreAllActionCallback.executeCommand(restoreAllActionCommand, null)
                );
                actionPanel.add(restoreAllbutton);
            }
        }
    }

    private boolean checkIfCollapsed() {
        boolean result = false;
        if (titleAnchor != null) {
            String styleString = titleAnchor.getStyleName();
            if (styleString != null) {
                String[] styles = styleString.split(" "); // $NON-NLS-1$
                Optional<String> found = Arrays.asList(styles).stream().filter(
                        s -> s.equals(PatternflyConstants.COLLAPSED)).findFirst();
                result = found.isPresent();
            }
        } else {
            result = startCollapsed;
        }
        return result;
    }

    private String hashString(String original) {
        return "#" + original; // $NON-NLS-1$
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

    private void addActionCallback(String label, UICommand command, AuditLogActionCallback callback) {
        actionLabels.add(label);
        actionCommand.add(command);
        auditLogActions.add(callback);
    }

    private void addClearAllActionCallback(String label, UICommand command, AuditLogActionCallback callback) {
        clearAllActionLabel = label;
        clearAllActionCommand = command;
        clearAllActionCallback = callback;
    }

    private void addRestoreAllActionCallback(String label, UICommand command, AuditLogActionCallback callback) {
        restoreAllActionLabel = label;
        restoreAllActionCommand = command;
        restoreAllActionCallback = callback;
    }

    public void setDataToggleInfo(Toggle toggle, String parentId) {
        this.toggle = toggle;
        this.parentWidgetId = parentId;
    }

    public void setContainerHeight(int height) {
        this.containerHeight  = height;
        eventPanelBody.getElement().getStyle().setProperty(MAX_HEIGHT, containerHeight + Unit.PX.getType());
        eventPanelBody.getElement().getStyle().setOverflowY(Overflow.AUTO);
    }

    public int getHeaderTitleHeight() {
        return eventPanelHeading.getOffsetHeight();
    }

    public int getFooterHeight() {
        return actionPanel.getOffsetHeight();
    }

    public void setStartCollapse(boolean value) {
        this.startCollapsed = value;
    }

    public HasData<AuditLog> asHasData() {
        return this.hasDataDelegate;
    }
}
