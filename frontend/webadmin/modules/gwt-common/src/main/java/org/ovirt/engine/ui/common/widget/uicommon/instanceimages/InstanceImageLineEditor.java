package org.ovirt.engine.ui.common.widget.uicommon.instanceimages;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.InstanceImageLineModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;

public class InstanceImageLineEditor extends AbstractModelBoundPopupWidget<InstanceImageLineModel> implements HasValueChangeHandlers<InstanceImageLineModel>, HasEnabled, HasElementId {

    @UiField
    @Path("name.entity")
    Label nameLabel;

    @UiField
    @Ignore
    UiCommandButton createEditButton;

    @UiField
    @Ignore
    UiCommandButton attachButton;

    private boolean enabled = true;

    private boolean createEditButtonEnabled = true;

    private String elementId = DOM.createUniqueId();

    public interface Driver extends UiCommonEditorDriver<InstanceImageLineModel, InstanceImageLineEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    static final CommonApplicationConstants constants = AssetProvider.getConstants();

    interface WidgetUiBinder extends UiBinder<FlowPanel, InstanceImageLineEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<InstanceImageLineEditor> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    public InstanceImageLineEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);

        driver.initialize(this);
    }

    @Override
    public void edit(final InstanceImageLineModel model) {
        driver.edit(model);
        setupElementIds(model);

        createEditButton.setCommand(model.getCreateEditCommand());
        attachButton.setCommand(model.getAttachCommand());

        createEditButton.addClickHandler(event -> createEditButton.getCommand().execute());

        attachButton.addClickHandler(event -> attachButton.getCommand().execute());

        updateCreateEditButtonText(model);
        updateCreateEditButtonEnablement(model);

        model.getDiskModel().getEntityChangedEvent().addListener((ev, sender, args) -> {
            ValueChangeEvent.fire(InstanceImageLineEditor.this, model);
            updateCreateEditButtonText(model);
            updateCreateEditButtonEnablement(model);
        });

        model.getCreateEditCommand().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            updateCreateEditButtonEnablement(model);
        });
    }

    public void setupElementIds(InstanceImageLineModel model) {
        String diskAlias = model.getDisk() != null ? model.getDisk().getDiskAlias() : ""; //$NON-NLS-1$
        String composedId = ElementIdUtils.createElementId(elementId, diskAlias);

        nameLabel.getElement().setId(composedId);
        attachButton.getElement().setId(ElementIdUtils.createElementId(composedId, "attach")); //$NON-NLS-1$
        createEditButton.getElement().setId(ElementIdUtils.createElementId(composedId, "createEdit")); //$NON-NLS-1$
    }

    private void updateCreateEditButtonText(InstanceImageLineModel model) {
        String text = model.getDiskModel().getEntity() != null ? constants.editInstanceImages() : constants.addInstanceImages();
        createEditButton.setLabel(text);
    }

    private void updateCreateEditButtonEnablement(InstanceImageLineModel model) {
        createEditButtonEnabled =
                model.getDiskModel().getEntity() != null ? model.getCreateEditCommand().isEditAllowed()
                        : model.getCreateEditCommand().isCreateAllowed();
        createEditButton.setEnabled(createEditButtonEnabled);
    }

    @Override
    public InstanceImageLineModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        createEditButton.setEnabled(enabled && createEditButtonEnabled);
        attachButton.setEnabled(enabled);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<InstanceImageLineModel> valueChangeHandler) {
        return addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
