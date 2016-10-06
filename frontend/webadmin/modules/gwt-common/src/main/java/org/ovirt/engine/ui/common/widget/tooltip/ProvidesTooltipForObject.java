package org.ovirt.engine.ui.common.widget.tooltip;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface ProvidesTooltipForObject<T> {

    SafeHtml getTooltip(T object);

}
