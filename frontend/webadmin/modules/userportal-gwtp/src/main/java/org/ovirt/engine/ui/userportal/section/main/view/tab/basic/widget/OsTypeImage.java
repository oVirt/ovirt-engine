package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemResources;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;

public class OsTypeImage extends Image implements IsEditor<TakesValueEditor<VmOsType>>, TakesValue<VmOsType> {

    private static final String LARGE = "Large";

    private static final String IMAGE = "Image";

    private MainTabBasicListItemResources resources;

    private VmOsType value;

    @UiConstructor
    public OsTypeImage(MainTabBasicListItemResources resources) {
        this.resources = resources;
    }

    @Override
    public TakesValueEditor<VmOsType> asEditor() {
        return TakesValueEditor.of(this);
    }

    @Override
    public void setValue(VmOsType value) {
        setUrl(getImage(value).getURL());
        this.value = value;
    }

    @Override
    public VmOsType getValue() {
        return value;
    }

    private ImageResource getImage(VmOsType value) {
        ResourcePrototype resource = resources.getResource(value.name() + LARGE + IMAGE);
        if (resource == null || !(resource instanceof ImageResource)) {
            return resources.otherOsLargeImage();
        }

        return (ImageResource) resource;
    }

}
