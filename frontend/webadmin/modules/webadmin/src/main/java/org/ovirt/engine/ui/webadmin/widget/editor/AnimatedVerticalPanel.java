package org.ovirt.engine.ui.webadmin.widget.editor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AnimatedVerticalPanel extends VerticalPanel {

    private static final int ANIM_DELAY = 500;
    private static final int ADD_DELAY = 100;
    private final Queue<Widget> pendingWidgtes = new LinkedList<>();

    public void addAll(Collection<? extends Widget> list, boolean fadeIn) {
        for (Widget widget : list) {
            if (fadeIn) {
                addPending(widget);
            } else {
                add(widget);
            }
        }
        if (fadeIn) {
            renderPending();
        }
    }

    public void addFadeIn(Widget w) {
        final Style widgetStyle = w.getElement().getStyle();
        widgetStyle.setOpacity(0);
        add(w);
        new Animation() {

            @Override
            protected void onUpdate(double progress) {
                widgetStyle.setOpacity(progress);
            }

        }.run(ANIM_DELAY);

    }

    public void addPending(Widget w) {
        pendingWidgtes.add(w);
    }

    public void renderPending() {
        Scheduler.get().scheduleFixedDelay(() -> {
            if (pendingWidgtes.isEmpty()) {
                return false;
            }
            addFadeIn(pendingWidgtes.remove());
            return !pendingWidgtes.isEmpty();
        }, ADD_DELAY);
    }

    @Override
    public void clear() {
        super.clear();
        pendingWidgtes.clear();
    }
}
