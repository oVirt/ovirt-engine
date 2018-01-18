package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelDetachableWidget;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;

import com.google.gwt.user.client.ui.HasEnabled;

public class EntityModelDetachableWidgetWithInfo extends EntityModelWidgetWithInfo
        implements HasDetachable, HasEnabled {

    public EntityModelDetachableWidgetWithInfo(EnableableFormLabel label,
                                               AbstractValidatedWidgetWithLabel<?, ?> contentWidget) {
        super(label, new EntityModelDetachableWidget(contentWidget));
    }

    @Override
    public void setDetachableIconVisible(boolean visible) {
        getContentWidget().setDetachableIconVisible(visible);
    }

    @Override
    public void setAttached(boolean attached) {
        ((HasDetachable) contentWidget).setAttached(attached);
    }

    @Override
    public void setUsePatternFly(boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            contentWidget.addStyleName(ColumnSize.SM_1.getCssName());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        label.setEnabled(enabled);
        ((HasEnabled) contentWidget).setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return ((HasEnabled) contentWidget).isEnabled();
    }

    public EntityModelDetachableWidget getContentWidget() {
        return (EntityModelDetachableWidget) contentWidget;
    }

}
