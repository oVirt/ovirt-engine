package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;
import org.ovirt.engine.ui.common.widget.renderer.NullableNumberRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacRangeModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;

public class MacRangeEditor extends AbstractModelBoundPopupWidget<MacRangeModel> implements HasValueChangeHandlers<MacRangeModel>, HasEnabled {

    public interface Driver extends UiCommonEditorDriver<MacRangeModel, MacRangeEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface WidgetUiBinder extends UiBinder<FlowPanel, MacRangeEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<MacRangeEditor> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    @UiField
    @Path(value = "leftBound.entity")
    @WithElementId
    StringEntityModelTextBoxEditor leftBound;

    @UiField
    @Path(value = "rightBound.entity")
    @WithElementId
    StringEntityModelTextBoxEditor rightBound;

    @UiField(provided = true)
    @Path(value = "macsCount.entity")
    @WithElementId
    EntityModelLabel<Integer> macsCount;

    private boolean enabled = true;

    public MacRangeEditor() {
        macsCount = new EntityModelLabel(new NullableNumberRenderer(NumberFormat.getDecimalFormat()),
                ToIntEntityModelParser.newTrimmingParser());
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(final MacRangeModel model) {
        driver.edit(model);
        IEventListener<EventArgs> textChangedListener =
                (ev, sender, args) -> {
                    ValueChangeEvent.fire(MacRangeEditor.this, model);
                    model.recalculateMacsCount();
                };
        model.getLeftBound().getEntityChangedEvent().addListener(textChangedListener);
        model.getRightBound().getEntityChangedEvent().addListener(textChangedListener);
    }

    @Override
    public MacRangeModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MacRangeModel> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        leftBound.setEnabled(enabled);
        rightBound.setEnabled(enabled);
    }

}
