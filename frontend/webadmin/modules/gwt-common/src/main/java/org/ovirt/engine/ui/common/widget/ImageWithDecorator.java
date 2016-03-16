package org.ovirt.engine.ui.common.widget;

import com.google.gwt.resources.client.ImageResource;

public class ImageWithDecorator {
    private ImageResource image;
    private ImageResource decorator;
    private int decoratorPositionLeft;
    private int decoratorPositionTop;
    private String status;

    public ImageWithDecorator(ImageResource image,
            ImageResource decorator,
            int decoratorPositionLeft,
            int decoratorPositionTop,
            String status) {
        this.image = image;
        this.decorator = decorator;
        this.decoratorPositionLeft = decoratorPositionLeft;
        this.decoratorPositionTop = decoratorPositionTop;
        this.status = status;
    }

    public ImageResource getImage() {
        return image;
    }

    public void setImage(ImageResource image) {
        this.image = image;
    }

    public ImageResource getDecorator() {
        return decorator;
    }

    public void setDecorator(ImageResource decorator) {
        this.decorator = decorator;
    }

    public int getDecoratorPositionLeft() {
        return decoratorPositionLeft;
    }

    public void setDecoratorPositionLeft(int decoratorPositionLeft) {
        this.decoratorPositionLeft = decoratorPositionLeft;
    }

    public int getDecoratorPositionTop() {
        return decoratorPositionTop;
    }

    public void setDecoratorPositionTop(int decoratorPositionTop) {
        this.decoratorPositionTop = decoratorPositionTop;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
