package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.AbstractRenderer;

public class EntityModelRenderer extends AbstractRenderer<Object> {

    @Override
    public String render(Object entity) {
        return entity == null ? null : entity.toString();
    }

}
