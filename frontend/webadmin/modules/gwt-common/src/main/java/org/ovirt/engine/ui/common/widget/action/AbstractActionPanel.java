package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class used to implement action panel widgets.
 *
 * @param <T> List model item type.
 */
public abstract class AbstractActionPanel<T> extends Composite implements HasElementId, ProvidesElementId {

    private final SearchableModelProvider<T, ?> dataProvider;

    @UiField
    public DropDown menuContainer = new DropDown();
    @UiField
    public Button clickButton = new Button();
    @UiField
    public DropDownMenu menu = new DropDownMenu();

    private String elementId = DOM.createUniqueId();

    /**
     * Constructor.
     * @param dataProvider The data provider.
     */
    public AbstractActionPanel(SearchableModelProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Returns the model data provider.
     * @return The {@code SearchableModelProvider}.
     */
    protected SearchableModelProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        // Hide the button, we need it to set the attributes on so jquery can manipulate it.
        clickButton.setDataToggle(Toggle.DROPDOWN);
        clickButton.setVisible(false);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
