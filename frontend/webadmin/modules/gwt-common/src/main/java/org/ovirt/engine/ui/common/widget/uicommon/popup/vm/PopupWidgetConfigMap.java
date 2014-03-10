package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

public class PopupWidgetConfigMap extends HashMap<Widget, PopupWidgetConfig> {

    private static final long serialVersionUID = 7118052938701174692L;

    public PopupWidgetConfigMap update(Widget widget, PopupWidgetConfig newConfig) {
        remove(widget);
        return putOne(widget, newConfig);
    }

    public PopupWidgetConfigMap putAll(List<Widget> widgets, PopupWidgetConfig config) {
        for (Widget widget : widgets) {
            // each needs it's own copy to be possible to edit the application level visibility
            put(widget, config.copy());
        }

        return this;
    }

    public PopupWidgetConfigMap putOne(Widget widget, PopupWidgetConfig newConfig) {
        put(widget, newConfig);
        return this;
    }

    public PopupWidgetConfigMap getAll() {
        return filter(new Predicate() {
            @Override
            public boolean apply(PopupWidgetConfig config) {
                return true;
            }
        });
    }

    // will be used as soon as the server supports this flag
    public PopupWidgetConfigMap getVisibleForAdminOnly() {
        return filter(new Predicate() {
            @Override
            public boolean apply(PopupWidgetConfig config) {
                return config.isAdminOnly();
            }
        });
    }

    public PopupWidgetConfigMap getVisibleInAdvanceMode() {
        return filter(new Predicate() {
            @Override
            public boolean apply(PopupWidgetConfig config) {
                return config.isVisibleOnlyInAdvanced() && config.isApplicationLevelVisible();
            }
        });
    }

    public PopupWidgetConfigMap getAlwaysHidden() {
        return filter(new Predicate() {
            @Override
            public boolean apply(PopupWidgetConfig config) {
                return config.isAlwaysHidden();
            }
        });
    }

    public PopupWidgetConfigMap getDetachables() {
        return filter(new Predicate() {
            @Override
            public boolean apply(PopupWidgetConfig config) {
                return config.isDetachable();
            }
        });
    }

    private PopupWidgetConfigMap filter(Predicate predicate) {
        PopupWidgetConfigMap res = new PopupWidgetConfigMap();

        for (Map.Entry<Widget, PopupWidgetConfig> entry : this.entrySet()) {
            if (predicate.apply(entry.getValue())) {
                res.put(entry.getKey(), entry.getValue());
            }
        }
        return res;
    }

    private interface Predicate {
        boolean apply(PopupWidgetConfig config);
    }
}
