package org.ovirt.engine.ui.common.widget.uicommon.instanceimages;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.InstanceImageLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.InstanceImagesModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class InstanceImagesEditor extends AddRemoveRowWidget<InstanceImagesModel, InstanceImageLineModel, InstanceImageLineEditor> implements HasElementId {

    private InstanceImagesModel model;

    private String elementId = DOM.createUniqueId();

    interface WidgetUiBinder extends UiBinder<Widget, InstanceImagesEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<InstanceImagesEditor> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    public interface InstanceImagesWidgetStyle extends WidgetStyle {
        String titleStylePadding();
        String mainPanel();
        String contentPanel();
    }

    @UiField
    Label title;

    @UiField
    FlowPanel mainPanel;

    public InstanceImagesEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setUsePatternFly(boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            title.removeStyleName(((InstanceImagesWidgetStyle)style).titleStylePadding());
            mainPanel.removeStyleName(((InstanceImagesWidgetStyle)style).mainPanel());
            contentPanel.removeStyleName(((InstanceImagesWidgetStyle)style).contentPanel());
        }
    }

    @Override
    protected InstanceImageLineEditor createWidget(InstanceImageLineModel value) {
        InstanceImageLineEditor editor = new InstanceImageLineEditor();
        editor.setElementId(elementId);
        editor.edit(value);
        return editor;
    }

    @Override
    protected InstanceImageLineModel createGhostValue() {
        InstanceImageLineModel lineModel = new InstanceImageLineModel(model);
        // the getVm() is null on new VM - that is handled inside the line models
        lineModel.initialize(null, model.getVm());
        return lineModel;
    }

    @Override
    protected boolean isGhost(InstanceImageLineModel value) {
        return value.isGhost();
    }

    @Override
    protected boolean vetoRemoveWidget(final Pair<InstanceImageLineModel, InstanceImageLineEditor> item, final InstanceImageLineModel value, final InstanceImageLineEditor widget) {
        model.approveRemoveDisk(item.getFirst(), approved -> {
            if (approved) {
                doRemoveItem(item, value, widget);
            }
        });

        // will be eventually removed from the callback
        return true;
    }

    @Override
    public void edit(final InstanceImagesModel model) {
        this.model = model;
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                title.setVisible(model.getIsAvailable());
                mainPanel.setVisible(model.getIsAvailable());
                contentPanel.setVisible(model.getIsAvailable());
            }
        });
        super.edit(model);
    }

    @Override
    protected void doRemoveItem(Pair<InstanceImageLineModel, InstanceImageLineEditor> item,
            InstanceImageLineModel value,
            InstanceImageLineEditor widget) {
        super.doRemoveItem(item, value, widget);
        model.updateActionsAvailability();
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
