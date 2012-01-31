package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public class MilisecondRenderer extends AbstractRenderer<Integer> {

    private static final MilisecondRenderer INSTANCE = new MilisecondRenderer();

    public static MilisecondRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    public String render(Integer object) {
        return object / 1000 + " sec";
    }

}
