package org.ovirt.engine.ui.common.widget.refresh;

import java.util.Set;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.renderer.MillisecondRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * A panel that shows a refresh button, with popup menu to select the refresh rate.<BR>
 * Works with an {@link AbstractRefreshManager}.
 */
public abstract class BaseRefreshPanel extends FocusPanel implements HasClickHandlers, HasElementId {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public interface BaseResources extends ClientBundle {

        ImageResource check_icon();

        ImageResource refresh_button();

        BaseRefreshPanelCss refreshPanelCss();

        ImageResource separator();

        ImageResource triangle_down();
    }

    private class RefreshRateOptionCommand implements Command {

        private RefreshRateOptionMenuItem menuItem;

        public RefreshRateOptionCommand() {
            super();
        }

        @Override
        public void execute() {
            menuPopup.hide();
            refreshMenuButton.setDown(false);
            refreshManager.setCurrentRefreshRate(menuItem.getRefreshRate());
        }

        public void setMenuItem(RefreshRateOptionMenuItem menuItem) {
            this.menuItem = menuItem;
        }

    }

    /**
     * A custom menu bar with check icon
     */
    private class RefreshRateOptionMenuItem extends MenuItem {

        private final Image checkItem;
        private final int refreshRate;

        public RefreshRateOptionMenuItem(int refreshRate, Command cmd) {
            super("", true, cmd); //$NON-NLS-1$

            this.refreshRate = refreshRate;

            checkItem = new Image(RESOURCES.check_icon());
            checkItem.setStylePrimaryName(style.checkitem());
            checkItem.setVisible(false);
            getElement().appendChild(checkItem.getElement());

            Anchor textItem = new Anchor();
            textItem.setText(MillisecondRenderer.getInstance().render(refreshRate));
            textItem.setStylePrimaryName(style.textItem());
            getElement().appendChild(textItem.getElement());
        }

        public int getRefreshRate() {
            return refreshRate;
        }

        public void select() {
            checkItem.setVisible(true);
        }

        @Override
        public void setSelectionStyle(boolean selected) {
            super.setSelectionStyle(selected);
        }

        public void unselect() {
            checkItem.setVisible(false);
        }

    }

    /**
     * A custom menu bar for 'RefreshRateOptionMenuItem' items.
     */
    private static class RefreshRateOptionsMenu extends MenuBar {
        public RefreshRateOptionsMenu(boolean vertical) {
            super(vertical);
        }

        public RefreshRateOptionMenuItem getItemByRefreshRate(int refreshRate) {
            // Find the menu item that is associated with the specified refresh rate value
            for (MenuItem item : this.getItems()) {
                if (item instanceof RefreshRateOptionMenuItem) {
                    RefreshRateOptionMenuItem refreshRateOptionMenuItem = (RefreshRateOptionMenuItem) item;
                    if (refreshRateOptionMenuItem.getRefreshRate() == refreshRate) {
                        return refreshRateOptionMenuItem;
                    }
                }
            }

            return null;
        }

        public void selectItem(RefreshRateOptionMenuItem menuItem) {

            if (menuItem == null) {
                return;
            }

            // Select the specified item after unselecting the others
            for (MenuItem item : this.getItems()) {
                if (item instanceof RefreshRateOptionMenuItem) {
                    RefreshRateOptionMenuItem refreshRateOptionMenuItem = (RefreshRateOptionMenuItem) item;
                    refreshRateOptionMenuItem.unselect();
                    refreshRateOptionMenuItem.setSelectionStyle(false);
                }
            }

            menuItem.select();
        }

    }

    private static BaseResources RESOURCES;

    private final DecoratedPopupPanel menuPopup;

    private PushButton refreshButton;

    private final AbstractRefreshManager<?> refreshManager;

    private ToggleButton refreshMenuButton;

    private final Image separator;

    private final RefreshRateOptionsMenu refreshOptionsMenu;

    private final Label statusLabel;

    private final BaseRefreshPanelCss style;

    private WidgetTooltip tooltip;


    /**
     * Create a Panel managed by the specified {@link RefreshManager}<BR>
     * used only by the Refresh Manager
     *
     */
    protected BaseRefreshPanel(AbstractRefreshManager<?> refreshManager) {
        RESOURCES = createResources();
        this.refreshManager = refreshManager;
        style = RESOURCES.refreshPanelCss();
        style.ensureInjected();

        // Set panel's attributes
        // setWidth("32px");
        // setHeight("18px");

        // Create menu popup
        menuPopup = new DecoratedPopupPanel(true, false);
        menuPopup.setStylePrimaryName(style.refreshRateMenuPopup());

        // Add mouse hover events
        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                // border: 1px solid #96B7D6;
                Style border = getElement().getStyle();
                border.setBorderWidth(1, Unit.PX);
                border.setBorderStyle(BorderStyle.SOLID);
                border.setBorderColor("#96B7D6"); //$NON-NLS-1$
            }

        });

        addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                // border: 1px solid transparent;
                Style border = getElement().getStyle();
                border.setBorderWidth(1, Unit.PX);
                border.setBorderStyle(BorderStyle.SOLID);
                border.setBorderColor("transparent"); //$NON-NLS-1$
            }
        });

        // Create refresh and refresh menu buttons
        createRefreshButton();
        createRefreshMenuButton();

        // Create refresh options menu
        refreshOptionsMenu = getRefreshOptionsMenu();

        // Add menu to the popup
        menuPopup.add(refreshOptionsMenu);
        menuPopup.addAutoHidePartner(refreshMenuButton.getElement());
        menuPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                refreshMenuButton.setDown(false);
            }
        });

        // Hide popup on window resize
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                menuPopup.hide();
            }
        });

        // Create panel separator
        ImageResource separatorImg = RESOURCES.separator();
        separator = new Image(separatorImg);

        // Add refresh button, refresh options menu button, and tooltip
        HorizontalPanel panel = new HorizontalPanel();
        statusLabel = new Label();
        panel.add(statusLabel);
        panel.add(refreshButton);
        panel.add(separator);
        panel.add(refreshMenuButton);
        setWidget(panel);
        tooltip = new WidgetTooltip(this);
        tooltip.setPlacement(Placement.BOTTOM);
        setTooltipText(refreshManager.getRefreshStatus());
    }

    public void hideRefreshMenuButton() {
        HorizontalPanel refreshPanel = (HorizontalPanel) getWidget();
        refreshPanel.remove(separator);
        refreshPanel.remove(refreshMenuButton);
        getElement().getStyle().setMarginRight(2, Unit.PX);
    }

    @Override
    public void setElementId(String elementId) {
        // Set refresh button element ID
        refreshButton.getElement().setId(
                ElementIdUtils.createElementId(elementId, "refreshButton")); //$NON-NLS-1$
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addHandler(handler, ClickEvent.getType());
    }

    public void setTooltipText(String status) {
        tooltip.setText(status);
        tooltip.reconfigure();
    }

    private void createRefreshButton() {
        Image refreshIcon = new Image(RESOURCES.refresh_button());
        refreshIcon.setPixelSize(11, 11);
        refreshIcon.setStylePrimaryName(style.refreshIcon());

        refreshButton = new PushButton(refreshIcon);
        refreshButton.setStylePrimaryName(style.refreshButton());
        refreshButton.addStyleName("brp_refreshButton_pfly_fix"); //$NON-NLS-1$
        refreshButton.setPixelSize(17, 17);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent(event);
            }
        });
    }

    private void createRefreshMenuButton() {
        Image refreshMenuIcon = new Image(RESOURCES.triangle_down());

        refreshMenuButton = new ToggleButton(refreshMenuIcon);
        refreshMenuButton.setStylePrimaryName(style.refreshMenuButton());
        refreshMenuButton.setPixelSize(13, 17);
        refreshMenuButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Show/Hide popup
                if (refreshMenuButton.isDown()) {
                    // Check selected item
                    int globalRefreshRate = refreshManager.getCurrentRefreshRate();
                    refreshOptionsMenu.selectItem(refreshOptionsMenu.getItemByRefreshRate(globalRefreshRate));
                    menuPopup.showRelativeTo(refreshButton);
                }
                else {
                    menuPopup.hide();
                }
            }
        });
    }

    private RefreshRateOptionsMenu getRefreshOptionsMenu() {
        // Create refresh rate options menu bar
        RefreshRateOptionsMenu refreshOptionsMenu = new RefreshRateOptionsMenu(true);
        refreshOptionsMenu.setStylePrimaryName(style.refreshRateMenu());

        // Create menu's title and add it as the first item
        MenuItem title = new MenuItem(constants.refreshRate(), new Command() {
            @Override
            public void execute() {
            }
        });
        title.setEnabled(false);
        title.setStylePrimaryName(style.refreshMenuTitle());
        refreshOptionsMenu.addItem(title);

        Set<Integer> refreshRates = AbstractRefreshManager.getRefreshRates();

        int itemIndex = 0;
        for (Integer refreshRate : refreshRates) {
            // Create command and menu item
            RefreshRateOptionCommand command = new RefreshRateOptionCommand();
            RefreshRateOptionMenuItem refreshRateOption = new RefreshRateOptionMenuItem(refreshRate, command);
            refreshRateOption.setStylePrimaryName(style.refreshRateOption());

            // Set command with the item it's associated with
            command.setMenuItem(refreshRateOption);

            // Add item (and a separator if needed)
            refreshOptionsMenu.addItem(refreshRateOption);
            if (itemIndex != refreshRates.size() - 1) {
                MenuItemSeparator separator = refreshOptionsMenu.addSeparator();
                separator.getElement().getStyle().setBackgroundColor("#E0E9F2"); //$NON-NLS-1$
            }
            itemIndex++;
        }

        return refreshOptionsMenu;
    }

    protected abstract BaseResources createResources();

}
