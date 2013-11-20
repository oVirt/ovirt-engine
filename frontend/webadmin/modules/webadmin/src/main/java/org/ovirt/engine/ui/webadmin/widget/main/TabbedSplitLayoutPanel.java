package org.ovirt.engine.ui.webadmin.widget.main;

import org.ovirt.engine.ui.common.system.ClientStorage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabbedSplitLayoutPanel extends SplitLayoutPanel {

    /**
     * Style sheet interface.
     */
    public interface TabbedSplitLayoutCss extends CssResource {
        String sliderButton();
    }

    /**
     * Tabbed Split Layout panel resources interface.
     */
    public interface TabbedSplitLayoutResources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/TabbedSplitLayout.css")
        TabbedSplitLayoutCss taggedSplitLayoutCss();

        @Source("org/ovirt/engine/ui/webadmin/images/collapse_splitter.png")
        ImageResource collapeSplitterButton();

        @Source("org/ovirt/engine/ui/webadmin/images/expand_splitter.png")
        ImageResource expandSplitterButton();
    }

    private static final String WEST_SPLITTER_KEY = "MAIN_WEST_SPLITTER_WIDTH"; //$NON-NLS-1$
    private static final Double DEFAULT_STACK_PANEL_WIDTH = 235.0;

    /**
     * Tabbed Split Layout panel resources.
     */
    private static final TabbedSplitLayoutResources SPLIT_LAYOUT_RESOURCES =
            GWT.create(TabbedSplitLayoutResources.class);

    /**
     * The style.
     */
    private final TabbedSplitLayoutCss style;

    /**
     * Collapse button.
     */
    private final PushButton collapseLeft;
    /**
     * Expand button.
     */
    private final PushButton expandLeft;

    /**
     * {@code ScheduleCommand} that forces a layout when resizing the panel.
     */
    private ScheduledCommand layoutCommand;

    /**
     * Inserted WEST panel, that will contain the collapse button.
     */
    private LayoutPanel westPanel = null;
    /**
     * Inserted CENTER panel, that will contain the expand button.
     */
    private LayoutPanel centerPanel = null;

    /**
     * Client storage to store the current west panel width in.
     */
    final ClientStorage clientStorage;

    /**
     * Constructor
     * @param splitterSize Size width of the splitter bar.
     */
    public TabbedSplitLayoutPanel(int splitterSize, ClientStorage storage) {
        super(splitterSize);
        clientStorage = storage;
        style = SPLIT_LAYOUT_RESOURCES.taggedSplitLayoutCss();
        style.ensureInjected();
        collapseLeft = createButton(SPLIT_LAYOUT_RESOURCES.collapeSplitterButton());
        expandLeft = createButton(SPLIT_LAYOUT_RESOURCES.expandSplitterButton());
    }

    /**
     * If the direction is WEST and the west panel has not been inserted yet, then create a new {@code LayoutPanel},
     * and insert the passed in {@code Widget} into that panel, and insert the panel into the {@code SplitLayoutPanel}.
     * If the direction is CENTER and the center panel has not been inserted yet, then create a new {@code LayoutPanel},
     * and insert the passed in {@code Widget} into that panel, and insert the panel into the {@code SplitLayoutPanel}.
     */
    @Override
    public void insert(Widget child, Direction direction, double size, Widget before) {
        Widget insertedWidget = child;
        if (direction == Direction.WEST) {
            if (westPanel == null) {
                westPanel = new LayoutPanel();
                collapseLeft.setVisible(true);
                westPanel.add(collapseLeft);
                size = getStoredStackPanelWidth();
            }
            westPanel.add(child);
            insertedWidget = westPanel;
        } else if (direction == Direction.CENTER) {
            if (centerPanel == null) {
                centerPanel = new LayoutPanel();
                centerPanel.add(expandLeft);
            }
            centerPanel.add(child);
            insertedWidget = centerPanel;
        }
        super.insert(insertedWidget, direction, size, before);
    }

    /**
     * Create a new expand/collapse button.
     * @param imageResource The {@code ImageResource} to use to style the button.
     * @return A new {@code PushButton}.
     */
    private PushButton createButton(ImageResource imageResource) {
        PushButton result = new PushButton(new Image(imageResource), new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                toggleVisibleWestPanel();
            }

        });
        result.setVisible(false);
        result.addStyleName(style.sliderButton());
        return result;
    }

    /**
     * Toggle the west panel. If it is visible, hide it and vice versa.
     */
    private void toggleVisibleWestPanel() {
        LayoutData layout = (LayoutData) westPanel.getLayoutData();
        if (layout.size == 0) {
            double size = layout.oldSize;
            if (size == 0) {
                //The old size is 0, so we should restore the default size.
                size = DEFAULT_STACK_PANEL_WIDTH;
            }
            // Restore the old size.
            setWestPanelSize(size);
        } else {
             //Collapse to size 0.
            layout.oldSize = layout.size;
            setWestPanelSize(0);
        }
    }

    /**
     * Set the west panel size.
     * @param size The new size.
     */
    private void setWestPanelSize(double size) {
        LayoutData layout = (LayoutData) westPanel.getLayoutData();
        if (size == layout.size) {
            return;
        }

        layout.size = size;
        // Defer actually updating the layout, so that if we receive many
        // mouse events before layout/paint occurs, we'll only update once.
        if (layoutCommand == null) {
            layoutCommand = new ScheduledCommand() {
                @Override
                public void execute() {
                    layoutCommand = null;
                    forceLayout();
                }
            };
            Scheduler.get().scheduleDeferred(layoutCommand);
        }
    }

    /**
     * After load, set the the position of the collapse and expand buttons. Including visibility.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

            @Override
            public void execute() {
                determineButtonVisiblity();
                positionLeftCollapseButton();
            }
        });
    }

    @Override
    public void onResize() {
        super.onResize();
        double currentWidth = westPanel.getOffsetWidth();
        setStoredSplitterWidth(currentWidth);
        determineButtonVisiblity();
        positionLeftCollapseButton();
    }

    /**
     * Determine if the expand or collapse button should be visible.
     */
    private void determineButtonVisiblity() {
        if (westPanel.getOffsetWidth() == 0) { // Completely collapsed.
            expandLeft.setVisible(true);
            collapseLeft.setVisible(false);
        } else {
            expandLeft.setVisible(false);
            collapseLeft.setVisible(true);
        }
    }

    /**
     * Position the collapse button to be proper in relation to the splitter bar.
     */
    private void positionLeftCollapseButton() {
        collapseLeft.getElement().getStyle().setLeft(
                westPanel.getOffsetWidth() - collapseLeft.getOffsetWidth(), Unit.PX);
    }

    @Override
    public void setWidgetToggleDisplayAllowed(Widget widget, boolean allowed) {
        super.setWidgetToggleDisplayAllowed(getActualWidget(widget), allowed);
    }

    @Override
    public void setWidgetSize(Widget widget, double size) {
        super.setWidgetSize(getActualWidget(widget), size);
    }

    @Override
    public Double getWidgetSize(Widget widget) {
        return super.getWidgetSize(getActualWidget(widget));
    }

    /**
     * Determine if the passed in widget is part of the west or center panel, if so return the center or west panel
     * as the widget, so the super class will handle the operation properly.
     * @param widget The {@code Widget} to use to determine if we should return the panel or the widget.
     * @return A {@code Widget} that is either the original, or a panel that contains the widget.
     */
    private Widget getActualWidget(Widget widget) {
        Widget checkedWidget = widget;
        if (westPanel != null && widget.getParent() == westPanel) {
            checkedWidget = westPanel;
        } else if (centerPanel != null && widget.getParent() == centerPanel) {
            checkedWidget = centerPanel;
        }
        return checkedWidget;
    }

    /**
     * Retrieve the stored stack panel width.
     * @return The west stack panel width as a {@code double}
     */
    private double getStoredStackPanelWidth() {
        String widthString = clientStorage.getLocalItem(WEST_SPLITTER_KEY);
        double width = DEFAULT_STACK_PANEL_WIDTH; //In case there was no stored width, use the default.
        try {
            if (widthString != null) {
                width = Double.valueOf(widthString);
            }
        } catch (NumberFormatException nfe) {
            //Do nothing.
        }
        return width;
    }

    /**
     * Store the current width in the {@code ClientStorage} of the browser so we can recall it when we log in.
     * @param width The current width in pixels.
     */
    private void setStoredSplitterWidth(Double width) {
        clientStorage.setLocalItem(WEST_SPLITTER_KEY, width.toString());
    }


}
