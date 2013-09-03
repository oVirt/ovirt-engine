package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.CustomButton;

/**
 * A class to be used for buttons of a custom shape, as opposed to the usual, rectangular buttons (e.g.
 * {@link SimpleDialogButton}) where only the text or image on the button's face may be customised. The shape
 * customisation is achieved by passing proper images for each of the button's states (i.e. normal, pressed, hovered
 * upon or disabled).
 */
public class ShapedButton extends CustomButton {

    /**
     * @param normal
     *            an image that defines how the button normally looks, when it's neither pressed nor hovered upon.
     * @param click
     *            an image that defines how the button looks when it's pressed.
     * @param hover
     *            an image that defines how the button looks when it's hovered upon.
     * @param disabled
     *            an image that defines how the button looks when it's disabled.
     */
    public ShapedButton(ImageResource normal, ImageResource click, ImageResource hover, ImageResource disabled) {
        getUpFace().setHTML(AbstractImagePrototype.create(normal).getSafeHtml());
        getDownFace().setHTML(AbstractImagePrototype.create(click).getSafeHtml());
        getUpHoveringFace().setHTML(AbstractImagePrototype.create(hover).getSafeHtml());
        getUpDisabledFace().setHTML(AbstractImagePrototype.create(disabled).getSafeHtml());
    }

    @Override
    protected void onClick() {
        setDown(false);
        super.onClick();
    }

    @Override
    protected void onClickCancel() {
        setDown(false);
    }

    @Override
    protected void onClickStart() {
        setDown(true);
    }

}
