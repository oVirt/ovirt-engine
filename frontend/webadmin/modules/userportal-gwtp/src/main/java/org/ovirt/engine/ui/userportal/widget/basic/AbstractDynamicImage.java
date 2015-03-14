package org.ovirt.engine.ui.userportal.widget.basic;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;

/**
 * Renders an image according to the value given. The specific image has to be in a ClientBundleWithLookup file. Can be
 * used together with @Path("...")
 *
 * @param <T>
 *            the object from which the specific image will be calculated
 * @param <R>
 *            the specific class of ClientBundleWithLookup where the resource will take place
 */
public abstract class AbstractDynamicImage<T, R extends ClientBundleWithLookup> extends Image implements IsEditor<TakesValueEditor<T>>, TakesValue<T> {

    private R bundle;

    private T value;

    public AbstractDynamicImage(R bundle) {
        this.bundle = bundle;
    }

    @Override
    public TakesValueEditor<T> asEditor() {
        return TakesValueEditor.of(this);
    }

    @Override
    public void setValue(T value) {
        setUrl(getImage(value).getURL());
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    private ImageResource getImage(T value) {
        ResourcePrototype resource = bundle.getResource(imageName(value));
        if (!(resource instanceof ImageResource)) {
            return (ImageResource) bundle.getResource(defaultImageName(value));
        }

        return (ImageResource) resource;
    }

    protected abstract String imageName(T value);

    protected abstract String defaultImageName(T value);
}
