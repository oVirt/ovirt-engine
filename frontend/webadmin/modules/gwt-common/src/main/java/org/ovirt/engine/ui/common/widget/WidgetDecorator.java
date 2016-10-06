package org.ovirt.engine.ui.common.widget;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Decorates a {@link Widget}.
 * <p>
 * {@code WidgetDecorator} implements {@link IsWidget#asWidget asWidget}
 * by returning the decorated widget itself. This means the decorator is
 * transparent to GWT Widget parent/child hierarchy, and thus transparent
 * to physical DOM element hierarchy.
 * <p>
 * For example, the following UiBinder template:
 *
 * <pre>
 * &lt;FlowPanel&gt;
 *   &lt;WidgetDecorator&gt;
 *     &lt;Button/&gt;
 *   &lt;/WidgetDecorator&gt;
 * &lt;/FlowPanel&gt;
 * </pre>
 *
 * renders into the following HTML markup:
 *
 * <pre>
 * &lt;div&gt; &lt;!-- FlowPanel --&gt;
 *   &lt;button/&gt; &lt;!-- Button --&gt;
 * &lt;/div&gt;
 * </pre>
 *
 * <p>
 * This effectively allows implementing the "decorator" design pattern:
 * implement {@link #decorateWidget} and apply desired functionality to
 * the decorated widget.
 */
public abstract class WidgetDecorator implements IsWidget, HasWidgets, HasOneWidget {

    private Widget widget;

    public WidgetDecorator() {
    }

    public WidgetDecorator(Widget w) {
        setWidget(w);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public Widget getWidget() {
        return asWidget();
    }

    @Override
    public void setWidget(Widget w) {
        // Validate.
        if (w == widget) {
            return;
        }

        // Detach new child.
        if (w != null) {
            w.removeFromParent();
        }

        // Remove old child.
        if (widget != null) {
            remove(widget);
        }

        // Logical attach.
        widget = w;

        // Decorator hook.
        if (widget != null) {
            decorateWidget(widget);
        }
    }

    protected abstract void decorateWidget(Widget w);

    @Override
    public void setWidget(IsWidget w) {
        setWidget(w.asWidget());
    }

    @Override
    public void add(Widget w) {
        if (getWidget() != null) {
            throw new IllegalStateException("Can only contain one child widget"); //$NON-NLS-1$
        }
        setWidget(w);
    }

    @Override
    public boolean remove(Widget w) {
        // Validate.
        if (w != widget) {
            return false;
        }

        // Logical detach.
        clear();
        return true;
    }

    @Override
    public void clear() {
        setWidget(null);
    }

    @Override
    public Iterator<Widget> iterator() {
        // Simple iterator for the single widget.
        return new Iterator<Widget>() {

            boolean hasElement = widget != null;
            Widget returned = null;

            @Override
            public boolean hasNext() {
                return hasElement;
            }

            @Override
            public Widget next() {
                if (!hasElement || (widget == null)) {
                    throw new NoSuchElementException();
                }
                hasElement = false;
                returned = widget;
                return returned;
            }

            @Override
            public void remove() {
                if (returned != null) {
                    WidgetDecorator.this.remove(returned);
                }
            }

        };
    }

}
