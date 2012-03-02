package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueLabel;

public class UserPortalEntityModelLabel extends ValueLabel<Object> {

    protected UserPortalEntityModelLabel() {
        super(new AbstractRenderer<Object>() {

            @Override
            public String render(Object object) {
                if (object == null) {
                    return "";
                }
                return object.toString();
            }
        });
    }

}
