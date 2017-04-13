package org.ovirt.engine.ui.webadmin.widget.errata;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.ovirt.engine.ui.uicommonweb.models.ErrataFilterValue;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;

/**
 * Composite panel that renders three check box buttons that allow the user to (client-side) filter
 * a grid of errata by errata Type.
 */
public class ErrataFilterPanel extends Composite {

    interface ViewUiBinder extends UiBinder<ButtonGroup, ErrataFilterPanel> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String errataSummaryLabel();
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    CheckBoxButton securityCheckbox;

    @UiField
    CheckBoxButton bugCheckbox;

    @UiField
    CheckBoxButton enhancementCheckbox;

    public ErrataFilterPanel() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        init();
        showPanelItems(false);
        localize();
    }

    public void init() {
        init(true, true, true);
    }

    public void init(boolean security, boolean bugs, boolean enhancements) {
        securityCheckbox.setActive(security);
        bugCheckbox.setActive(bugs);
        enhancementCheckbox.setActive(enhancements);
        showPanelItems(true);
    }

    private void showPanelItems(boolean show) {
        securityCheckbox.setVisible(show);
        bugCheckbox.setVisible(show);
        enhancementCheckbox.setVisible(show);
    }

    public void addValueChangeHandler(final ValueChangeHandler<ErrataFilterValue> handler) {

        ValueChangeHandler<Boolean> internalHandler = event -> {
            // Do this deferred to give the javascript time to activate/deactivate the buttons.
            Scheduler.get().scheduleDeferred(() -> {
                // one of the checkboxes changed, but get all three checkbox values for the event
                ErrataFilterValue value = new ErrataFilterValue(securityCheckbox.isActive(),
                        bugCheckbox.isActive(), enhancementCheckbox.isActive());

                handler.onValueChange(new ValueChangeEvent<ErrataFilterValue>(value) {});
            });
        };

        securityCheckbox.addValueChangeHandler(internalHandler);
        bugCheckbox.addValueChangeHandler(internalHandler);
        enhancementCheckbox.addValueChangeHandler(internalHandler);
    }

    private void localize() {
        securityCheckbox.setHTML(constants.security());
        bugCheckbox.setHTML(constants.bugs());
        enhancementCheckbox.setHTML(constants.enhancements());
    }

}
