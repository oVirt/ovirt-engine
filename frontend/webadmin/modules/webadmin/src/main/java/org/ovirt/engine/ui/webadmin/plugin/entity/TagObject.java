package org.ovirt.engine.ui.webadmin.plugin.entity;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsMutableObjectWithProperties;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public final class TagObject extends JsMutableObjectWithProperties {
    protected TagObject() {}

    /**
     * Generate a Javascript TagObject from a GWT TagModel object, including the child nodes.
     * @param model The GWT model
     * @return A Javascript TagObject representing the information in from the parameter model.
     */
    public static TagObject from(TagModel model) {
        return from(model, true);
    }

    private static TagObject from(TagModel model, boolean includeChildren) {
        TagObject result = JavaScriptObject.createObject().cast();
        result.setValueAsString("description", model.getDescription().getEntity()); //$NON-NLS-1$
        result.setValueAsString("name", model.getName().getEntity()); //$NON-NLS-1$
        result.setValueAsBoolean("selected", model.getSelection()); //$NON-NLS-1$
        if (includeChildren) {
            JsArray<TagObject> children = JavaScriptObject.createArray().cast();
            for (TagModel child : model.getChildren()) {
                children.push(TagObject.from(child, true));
            }
            result.setValueAsJavaScriptObject("children", children); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Generate a JsArray of TagObjects that contains only the ACTIVE selected Tags from the tree.
     * @param model The ROOT node of the Tag tree.
     * @return A JsArray of active TagObjects.
     */
    public static JsArray<TagObject> activeTagArray(List<TagModel> activeTags) {
        JsArray<TagObject> result = JavaScriptObject.createArray().cast();
        for (TagModel activeTag: activeTags) {
            result.push(from(activeTag, false));
        }
        return result;
    }
}
