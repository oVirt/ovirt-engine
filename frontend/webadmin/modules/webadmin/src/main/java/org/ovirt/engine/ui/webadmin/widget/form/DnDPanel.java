package org.ovirt.engine.ui.webadmin.widget.form;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Panel that sinks HTML5 drag & drop events.<BR>
 * To be draggable, or receive Drag Events, Widgets <B>must</B> extend this Panel.<BR>
 *
 * <B>When moving to GWT 2.4, this Panel and the DnD Events should be removed.</B>
 *
 * @see <a href="http://www.w3.org/TR/html5/dnd.html#dndevents">W3C HTML Specification</a>
 */
public class DnDPanel extends FocusPanel {

    public DnDPanel(Boolean allowDrag) {
        super();
        setDraggable(getElement(), allowDrag.toString());
    }

    public DnDPanel(Widget child, Boolean allowDrag) {
        super(child);
        setDraggable(getElement(), allowDrag.toString());
    }

    private native void setDraggable(Element elem, String draggable) /*-{
        elem.draggable = draggable;
    }-*/;

    @Override
    public void sinkBitlessEvent(String eventTypeName) {
        super.sinkBitlessEvent(eventTypeName);
        if (eventTypeName.startsWith("drag")) { //$NON-NLS-1$
            // extra work to register drag events
            sinkDragBitlessEventImpl(getElement(), eventTypeName);
        }
    }

    protected native void sinkDragBitlessEventImpl(Element elem, String eventTypeName) /*-{
        // Some drag events must call preventDefault to prevent native text selection.
        switch (eventTypeName) {
        case "drag":
            elem.ondrag = @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent;
            break;
        case "dragend":
            elem.ondragend = @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent;
            break;
        case "dragenter":
            elem.ondragenter = $entry(function(evt) {
                evt.preventDefault();
                @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent.call(this, evt);
            });
            break;
        case "dragleave":
            elem.ondragleave = @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent;
            break;
        case "dragover":
            elem.ondragover = $entry(function(evt) {
                evt.preventDefault();
                @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent.call(this, evt);
            });
            break;
        case "dragstart":
            elem.ondragstart = @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent;
            break;
        case "drop":
            elem.ondrop = @com.google.gwt.user.client.impl.DOMImplStandard::dispatchEvent;
            break;
        default:
            // catch missing cases
            throw "Trying to sink unknown event type " + eventTypeName;
        }
    }-*/;

}
