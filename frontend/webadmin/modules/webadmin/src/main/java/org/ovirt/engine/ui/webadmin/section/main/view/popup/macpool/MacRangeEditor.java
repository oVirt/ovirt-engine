package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacRangeModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
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

    interface Style extends CssResource {
        String elementStyle();
    }

    @UiField
    @Path(value = "leftBound.entity")
    @WithElementId
    StringEntityModelTextBoxEditor leftBound;

    @UiField
    @Path(value = "rightBound.entity")
    @WithElementId
    StringEntityModelTextBoxEditor rightBound;

    @UiField
    Style style;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private boolean enabled = true;

    public MacRangeEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        leftBound.setLabel(constants.macPoolWidgetLeftBound());
        rightBound.setLabel(constants.macPoolWidgetRightBound());
        ((Element)leftBound.getElement().getChild(0)).addClassName(style.elementStyle());
        ((Element)leftBound.getElement().getChild(1)).addClassName(style.elementStyle());
        ((Element)rightBound.getElement().getChild(0)).addClassName(style.elementStyle());
        ((Element)rightBound.getElement().getChild(1)).addClassName(style.elementStyle());
    }

    @Override
    public void edit(final MacRangeModel model) {
        driver.edit(model);
        leftBound.fireValueChangeOnKeyDown();
        rightBound.fireValueChangeOnKeyDown();
        IEventListener<EventArgs> textChangedListener = new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ValueChangeEvent.fire(MacRangeEditor.this, model);
            }
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
