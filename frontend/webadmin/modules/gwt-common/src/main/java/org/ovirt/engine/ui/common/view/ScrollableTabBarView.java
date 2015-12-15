package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.ScrollableTabBarPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractCompositeTab;
import org.ovirt.engine.ui.common.widget.tab.RepeatingPushButton;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class ScrollableTabBarView extends AbstractView implements ScrollableTabBarPresenterWidget.ViewDef {

    public interface ViewUiBinder extends UiBinder<Widget, ScrollableTabBarView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    /**
     * element style name of 'min-width' that gets manipulated in the view.
     */
    private static final String MIN_WIDTH_STYLE = "minWidth"; //$NON-NLS-1$
    /**
     * element style name of 'max-width' that gets manipulated in the view.
     */
    private static final String MAX_WIDTH_STYLE = "maxWidth"; //$NON-NLS-1$
    /**
     * element style name of 'margin-top' that gets manipulated in the view.
     */
    private static final String MARGIN_TOP = "marginTop"; //$NON-NLS-1$

    /**
     * The CSS styles available to be modified inside the view.
     */
    interface Style extends CssResource {
        /**
         * The style associated with each drop down item.
         *
         * @return The style name as a string regardless of obfuscation level.
         */
        String dropdownItem();

        /**
         * The style of the outer item container.
         * @return The style name as a string regardless of obfuscation level.
         */
        String dropdownItemContainer();
    }

    /**
     * The style object using the UI-binder.
     */
    @UiField
    Style style;

    /**
     * The tab bar.
     */
    @UiField
    FlowPanel widgetBar;

    /**
     * The panel that is scrolled.
     */
    @UiField
    FlowPanel scrollPanel;

    /**
     * The left scroll button.
     */
    @UiField
    RepeatingPushButton scrollLeftButton;

    /**
     * The right scroll button.
     */
    @UiField
    RepeatingPushButton scrollRightButton;

    /**
     * The drop-down button for the menu list.
     */
    @UiField
    PushButton dropdownButton;

    /**
     * The drop-down menu list.
     */
    @UiField
    FlowPanel dropdownPanel;
    /**
     * The pop-up panel containing the drop-down menu list.
     */
    @UiField
    PopupPanel dropdownPopupPanel;

    /**
     * The minimum width needed to display all the widgets.
     */
    private int widgetMinWidth;

    /**
     * The number of pixels to scroll when clicking the left/right scroll buttons.
     */
    private int scrollDistance;

    /**
     * Constructor.
     */
    public ScrollableTabBarView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        configureAutoHidePartners();
        asWidget().addAttachHandler(new AttachEvent.Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

                    @Override
                    public void execute() {
                        recalculateSize();
                        showScrollButtons();
                    }
                });
            }
        });
    }

    /**
     * Configure the auto hide partner of the menu drop down button.
     */
    private void configureAutoHidePartners() {
        dropdownPopupPanel.addAutoHidePartner(dropdownButton.getElement());
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        Widget listWidget = copyWidgetAsDropdownItem(tabWidget);
        if (listWidget != null) {
            widgetBar.insert(tabWidget, index);
            dropdownPanel.insert(listWidget, index);
            recalculateWidgetBarMinWidth();
            showScrollButtons();
        }
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        int widgetIndex = widgetBar.getWidgetIndex(tabWidget);
        widgetBar.remove(tabWidget);
        dropdownPanel.remove(widgetIndex);
        recalculateWidgetBarMinWidth();
        showScrollButtons();
    }

    /**
     * Copy the passed in widget WITHOUT the event handlers. Then add a click handler to the new widget
     * and add the 'dropdownItem' style to them. The new widget is created from the original passed in widget
     * without the original style sheet. New style sheets are added to make it look proper in the drop down
     * list.
     *
     * @param widget The original widget, which is unchanged after this call.
     * @return The new widget with click handler and dropdownItems style.
     */
    private Widget copyWidgetAsDropdownItem(final IsWidget widget) {
        HTML newWidget = null;
        if (widget != null) {
            newWidget = new HTML();
            newWidget.setHTML(widget.asWidget().getElement().getString().replaceAll("class=\".*?\"", //$NON-NLS-1$
                    "class=\"" + style.dropdownItem() + "\"")); //$NON-NLS-1$ //$NON-NLS-2$
            newWidget.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    dropdownPopupPanel.hide();
                    widget.asWidget().getElement().scrollIntoView();
                    adjustButtons();
                }
            });
            newWidget.addStyleName(style.dropdownItemContainer());
        }
        return newWidget;
    }

    /**
     * Set the scroll distance in pixels.
     *
     * @param distance
     *            The distance to scroll.
     */
    @Override
    public void setScrollDistance(int distance) {
        this.scrollDistance = distance;
    }

    /**
     * Calculate the minimum width needed to display all the tabs on the bar. This works even if there are some
     * right floating tabs.
     */
    private void recalculateWidgetBarMinWidth() {
        widgetBar.getElement().getStyle().setProperty(MIN_WIDTH_STYLE, calculateWidgetMinWidthNeeded(), Unit.PX);
    }

    /**
     * Retrieve the cached minimum width needed. This is purely for performance reasons so we don't have to keep
     * calculating the needed with based on the width of all the tabs.
     * @return The current minimum width needed.
     */
    private int getWidgetMinWidthNeeded() {
        return widgetMinWidth;
    }

    /**
     * Calculate the actual width needed based on the number and width of the tabs. This iterates over the tabs
     * and collects the width of each. This method also caches the value so we don't have to iterate each time.
     * The cached value can be retrieved with getWidgetMinWidthNeeded().
     * @return The minimum width needed to display all the tabs.
     */
    private int calculateWidgetMinWidthNeeded() {
        int minWidth = 0;
        if (widgetBar.getWidgetCount() > 0) {
            for (int i = 0; i < widgetBar.getWidgetCount(); i++) {
                Widget widget = widgetBar.getWidget(i);
                if (widget.isVisible()) {
                    minWidth += widget.getElement().getOffsetWidth();
                }
            }
        }
        // Add 1 for browsers that don't report width properly.
        minWidth++;
        // Store this in a variable so we don't have to calculate it all the time.
        // This assumes that when resizes/etc happen this gets called to recalculate everything.
        widgetMinWidth = minWidth;
        return minWidth;
    }

    /**
     * Calculate the maximum width of the scrolling panel (the panel that contains the panel that has the buttons).
     * The logic to determine the width of the scrolling panel:
     * <ol>
     *   <li>Determine the width of the buttons. The width is 0 if they are not displayed.</li>
     *   <li>Determine the width of the panel containing the scrolling panel.</li>
     *   <li>The width needed is the width of the panel - the width of the buttons</li>
     * </ol>
     */
    private void recalculateScrollPanelMaxWidth() {
        int leftArrowWidth = scrollLeftButton.isVisible() ? scrollLeftButton.getOffsetWidth() : 0;
        int rightArrowWidth = scrollRightButton.isVisible() ? scrollRightButton.getOffsetWidth() : 0;
        int dropdownWidth = dropdownButton.isVisible() ? dropdownButton.getOffsetWidth() : 0;
        int maxScrollPanelWidth = asWidget().getOffsetWidth() - leftArrowWidth - rightArrowWidth - dropdownWidth;
        if (maxScrollPanelWidth > 0) {
            scrollPanel.getElement().getStyle().setProperty(MAX_WIDTH_STYLE, maxScrollPanelWidth, Unit.PX);
        } else {
            scrollPanel.getElement().getStyle().clearProperty(MAX_WIDTH_STYLE);
        }
    }

    /**
     * Position the scroll buttons to be properly positioned in the container.
     */
    private void positionScrollButtons() {
        // Calculate how far from the top the button needs to be to be centered.
        int marginTop = (asWidget().getOffsetHeight() - scrollLeftButton.getOffsetHeight()) / 2;
        scrollLeftButton.getElement().getStyle().setProperty(MARGIN_TOP, marginTop, Unit.PX);
        scrollRightButton.getElement().getStyle().setProperty(MARGIN_TOP, marginTop, Unit.PX);
        dropdownButton.getElement().getStyle().setProperty(MARGIN_TOP, marginTop, Unit.PX);
    }

    /**
     * Left scroll bar button click handler.
     * @param event The click event.
     */
    @UiHandler("scrollLeftButton")
    void handleLeftClick(ClickEvent event) {
        adjustScroll(-scrollDistance);
    }

    /**
     * Right scroll bar button click handler.
     * @param event The click event.
     */
    @UiHandler("scrollRightButton")
    void handleRightClick(ClickEvent event) {
        adjustScroll(scrollDistance);
    }

    /**
     * Drop-down button click handler.
     * @param event The click event.
     */
    @UiHandler("dropdownButton")
    void handleDropdownClick(ClickEvent event) {
        if (!dropdownPopupPanel.isShowing()) {
            for (int i = 0; i < widgetBar.getWidgetCount(); i++) {
                if (widgetBar.getWidget(i) instanceof AbstractCompositeTab) {
                    dropdownPanel.getWidget(i).setVisible(((AbstractCompositeTab) widgetBar.getWidget(i)).isAccessible());
                }
            }
            dropdownPanel.setVisible(true);
            dropdownPopupPanel.showRelativeTo(dropdownButton);
        } else {
            dropdownPanel.setVisible(false);
            dropdownPopupPanel.hide();
        }
    }

    @Override
    public void showScrollButtons() {
        boolean isScrolling = isScrollingNecessary();
        scrollRightButton.setVisible(isScrolling);
        scrollLeftButton.setVisible(isScrolling);
        dropdownButton.setVisible(isScrolling);
        if (isScrolling) {
            recalculateSize();
            positionScrollButtons();
        } else {
            scrollTo(0);
            scrollPanel.getElement().getStyle().setProperty(MAX_WIDTH_STYLE, asWidget().getOffsetWidth(), Unit.PX);
            widgetBar.getElement()
                    .getStyle()
                    .setProperty(MIN_WIDTH_STYLE, asWidget().getElement().getOffsetWidth(), Unit.PX);
        }
    }

    /**
     * Determine if the scroll buttons should be visible.
     * @return {@code true} if scrolling is necessary, false otherwise.
     */
    private boolean isScrollingNecessary() {
        int currentWidth = asWidget().getOffsetWidth();
        int minWidth = getWidgetMinWidthNeeded();
        return minWidth > 0 && currentWidth > 0 && currentWidth <= minWidth;
    }

    /**
     * Adjust the scroll by a number of pixels. The value can be positive or negative.
     * @param adjustment The number of pixels to adjust.
     */
    private void adjustScroll(int adjustment) {
        scrollTo(scrollPanel.getElement().getScrollLeft() + adjustment);
    }

    /**
     * Scroll to a specific position.
     * @param pos The position to scroll to.
     */
    private void scrollTo(int pos) {
        scrollPanel.getElement().setScrollLeft(pos);
        adjustButtons();
    }

    /**
     * Adjust the state of the scroll buttons based on the position of scroll panel. If you can't scroll more to the
     * left, disable the left button, if you can't scroll to the right disable the right button.
     */
    void adjustButtons() {
        if (scrollPanel.getElement().getScrollLeft() <= 0) {
            scrollLeftButton.setEnabled(false);
        } else {
            scrollLeftButton.setEnabled(true);
        }
        if ((scrollPanel.getElement().getScrollLeft() > 0
                || (widgetBar.getOffsetWidth() - scrollPanel.getOffsetWidth()) > 0)
                && scrollPanel.getElement().getScrollLeft()
                >= (widgetBar.getOffsetWidth() - scrollPanel.getOffsetWidth())) {
            scrollRightButton.setEnabled(false);
        } else {
            scrollRightButton.setEnabled(true);
        }
    }

    @Override
    public void recalculateSize() {
        recalculateScrollPanelMaxWidth();
        recalculateWidgetBarMinWidth();
    }

    @Override
    public void setOffset(int left, boolean wantsLeft) {
        if (wantsLeft) {
            asWidget().getElement().getStyle().setLeft(left, Unit.PX);
        }
        asWidget().getElement().getStyle().setWidth(Window.getClientWidth() - left, Unit.PX);
        recalculateSize();
        showScrollButtons();
    }

}
