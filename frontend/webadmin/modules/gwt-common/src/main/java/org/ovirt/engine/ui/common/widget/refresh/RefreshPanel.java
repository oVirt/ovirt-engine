package org.ovirt.engine.ui.common.widget.refresh;

import java.util.Set;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.renderer.MillisecondRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A panel that shows a refresh button, with popup menu to select the refresh rate.<BR>
 * Works with an {@link AbstractRefreshManager}.
 */
public class RefreshPanel extends ButtonGroup implements HasClickHandlers, HasElementId {

    private final AbstractRefreshManager<?> refreshManager;

    private WidgetTooltip tooltip;

    private Button refreshButton;
    private Button dropdownButton;
    private DropDownMenu dropdownMenu;

    /**
     * Create a Panel managed by the specified {@link RefreshManager}<BR>
     * used only by the Refresh Manager
     */
    protected RefreshPanel(AbstractRefreshManager<?> refreshManager) {
        this.refreshManager = refreshManager;

        refreshButton = new Button("", IconType.REFRESH, new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                fireEvent(event);
            }

        });

        createRefreshMenuButton();
        createDropdownMenu();

        add(refreshButton);
        add(dropdownButton);
        add(dropdownMenu);

        tooltip = new WidgetTooltip(this);
        setTooltipText(refreshManager.getRefreshStatus());
    }

    /**
     * Set the element id directly on the refresh button itself and ignore the dropdown bits.
     */
    @Override
    public void setElementId(String elementId) {
        refreshButton.getElement().setId(
                ElementIdUtils.createElementId(elementId, "refreshButton")); //$NON-NLS-1$
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return refreshButton.addHandler(handler, ClickEvent.getType());
    }

    public void setTooltipText(String status) {
        tooltip.setText(status);
    }

    private void createRefreshMenuButton() {
        dropdownButton = new Button();
        dropdownButton.setToggleCaret(true);
        dropdownButton.setDataToggle(Toggle.DROPDOWN);
    }

    private void createDropdownMenu() {
        dropdownMenu = new DropDownMenu();
        dropdownMenu.setPull(Pull.LEFT);
        Set<Integer> refreshRates = AbstractRefreshManager.getRefreshRates();
        for (Integer refreshRate : refreshRates) {
            AnchorListItem refreshRateItem = new AnchorListItem();
            refreshRateItem.setText(MillisecondRenderer.getInstance().render(refreshRate));
            refreshRateItem.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    refreshManager.setCurrentRefreshRate(refreshRate);
                    removeActiveStyles();
                    refreshRateItem.addStyleName(Styles.ACTIVE);
                }

            });
            if (refreshManager.getCurrentRefreshRate() == refreshRate) {
                refreshRateItem.addStyleName(Styles.ACTIVE);
            }
            dropdownMenu.add(refreshRateItem);
        }
    }

    protected void removeActiveStyles() {
        for (int i = 0; i < dropdownMenu.getWidgetCount(); i++) {
            dropdownMenu.getWidget(i).removeStyleName(Styles.ACTIVE);
        }
    }
}
