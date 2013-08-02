package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.AbstractRenderer;

public class ToStringEntityModelRenderer<T> extends AbstractRenderer<T> {

    @Override
    public String render(T entity) {
        return entity == null ? null : entity.toString();
    }

}
