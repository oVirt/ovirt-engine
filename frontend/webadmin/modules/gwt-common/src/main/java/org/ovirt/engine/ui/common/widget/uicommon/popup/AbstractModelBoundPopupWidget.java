package org.ovirt.engine.ui.common.widget.uicommon.popup;

import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for widgets that represent the content of popup views bound to a UiCommon Window model.
 *
 * @param <T>
 *            Window model type.
 */
public abstract class AbstractModelBoundPopupWidget<T extends Model> extends Composite implements HasEditorDriver<T>, FocusableComponentsContainer {

    public void focusInput() {
        // No-op, override as necessary
    }

    public int setTabIndexes(int nextTabIndex) {
        // No-op, override as necessary
        return nextTabIndex;
    }

    private void setEnabled(Widget widget, boolean enable) {
        if (widget instanceof IndexedPanel) {
            for (int i = 0; i < ((IndexedPanel) widget).getWidgetCount(); i++) {
                setEnabled(((IndexedPanel) widget).getWidget(i), enable);
            }
        } else if (widget instanceof HasEnabled) {
            ((HasEnabled) widget).setEnabled(enable);
        }
    }

    public void enableWidget(Widget widget) {
        setEnabled(widget, true);
    }

    public void disableWidget(Widget widget) {
        setEnabled(widget, false);
    }
}
