package org.ovirt.engine.ui.common.widget.uicommon.instanceimages;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.InstanceImageLineModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

    private String elementId = DOM.createUniqueId();

    public interface Driver extends SimpleBeanEditorDriver<InstanceImageLineModel, InstanceImageLineEditor> {
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

        createEditButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createEditButton.getCommand().execute();
            }
        });

        attachButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attachButton.getCommand().execute();
            }
        });

        updateButtonText(model);

        model.getDiskModel().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ValueChangeEvent.fire(InstanceImageLineEditor.this, model);
                updateButtonText(model);
            }
        });
    }

    public void setupElementIds(InstanceImageLineModel model) {
        String diskAlias = model.getDisk() != null ? model.getDisk().getDiskAlias() : ""; //$NON-NLS-1$
        String composedId = ElementIdUtils.createElementId(elementId, diskAlias);

        nameLabel.getElement().setId(composedId);
        attachButton.getElement().setId(ElementIdUtils.createElementId(composedId, "attach")); //$NON-NLS-1$
        createEditButton.getElement().setId(ElementIdUtils.createElementId(composedId, "createEdit")); //$NON-NLS-1$
    }

    private void updateButtonText(InstanceImageLineModel model) {
        String text = model.getDiskModel().getEntity() != null ? constants.editInstanceImages() : constants.addInstanceImages();
        createEditButton.setLabel(text);
    }

    @Override
    public InstanceImageLineModel flush() {
        return driver.flush();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        createEditButton.setEnabled(enabled);
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
