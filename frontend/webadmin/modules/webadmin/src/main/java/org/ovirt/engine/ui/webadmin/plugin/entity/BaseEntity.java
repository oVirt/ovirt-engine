package org.ovirt.engine.ui.webadmin.plugin.entity;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.NGuid;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type representing an entity passed through the plugin API as native JS object.
 */
// TODO(vszocs) use restapi-types to map backend business entity types
// into restapi-definition types, and export these types for use with
// JavaScript (plugin API)
public class BaseEntity extends JavaScriptObject {

    protected BaseEntity() {
    }

    /**
     * Returns JSON-like representation of the given business entity:
     * <pre>
     * { entityId: "[BusinessEntityGuidAsString]" }
     * </pre>
     */
    public static native BaseEntity from(BusinessEntity<? extends NGuid> businessEntity) /*-{
        var guid = businessEntity.@org.ovirt.engine.core.common.businessentities.BusinessEntity::getId()();
        var guidAsString = guid.@org.ovirt.engine.core.compat.NGuid::toString()();
        return { entityId: guidAsString };
    }-*/;

    public static <T extends BusinessEntity<? extends NGuid>> JsArray<BaseEntity> arrayFrom(List<T> businessEntityList) {
        JsArray<BaseEntity> result = JavaScriptObject.createArray().cast();
        for (T businessEntity : businessEntityList) {
            result.push(BaseEntity.from(businessEntity));
        }
        return result;
    }

}
