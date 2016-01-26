package org.ovirt.engine.ui.webadmin.widget.errata;

import org.ovirt.engine.ui.uicommonweb.models.ErrataFilterValue;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Composite panel that renders three checkboxes that allow the user to (client-side) filter
 * a grid of errata by errata Type.
 */
public class ErrataFilterPanel extends Composite {

    interface ViewUiBinder extends UiBinder<FlowPanel, ErrataFilterPanel> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String errataSummaryLabel();
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    CheckBox securityCheckbox;

    @UiField
    CheckBox bugCheckbox;

    @UiField
    CheckBox enhancementCheckbox;

    @UiField
    Image securityCheckboxImage;

    @UiField
    Image bugCheckboxImage;

    @UiField
    Image enhancementCheckboxImage;

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
        securityCheckbox.setValue(security);
        bugCheckbox.setValue(bugs);
        enhancementCheckbox.setValue(enhancements);
        showPanelItems(true);
    }

    private void showPanelItems(boolean show) {
        securityCheckbox.setVisible(show);
        bugCheckbox.setVisible(show);
        enhancementCheckbox.setVisible(show);
        enhancementCheckboxImage.setVisible(show);
        bugCheckboxImage.setVisible(show);
        securityCheckboxImage.setVisible(show);
    }

    public void addValueChangeHandler(final ValueChangeHandler<ErrataFilterValue> handler) {

        ValueChangeHandler<Boolean> internalHandler = new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                // one of the checkboxes changed, but get all three checkbox values for the event
                ErrataFilterValue value = new ErrataFilterValue(securityCheckbox.getValue(),
                        bugCheckbox.getValue(), enhancementCheckbox.getValue());

                handler.onValueChange(new ValueChangeEvent<ErrataFilterValue>(value) {});
            }

        };

        securityCheckbox.addValueChangeHandler(internalHandler);
        bugCheckbox.addValueChangeHandler(internalHandler);
        enhancementCheckbox.addValueChangeHandler(internalHandler);
    }

    private void localize() {
        securityCheckbox.setText(constants.security());
        bugCheckbox.setText(constants.bugs());
        enhancementCheckbox.setText(constants.enhancements());
    }

}
