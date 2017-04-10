package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.gwt.dom.client.ImageElement;

/**
 * It validates icons size, dimensions and file format.
 */
public class IconValidation implements IValidation {

    private final ImageElement imageElement;

    /**
     * @param imageElement the 'src' attribute of which is already set to string that will be validated from previous
     *                    event loop run
     */
    public IconValidation(ImageElement imageElement) {
        this.imageElement = imageElement;
    }

    @Override
    public ValidationResult validate(Object iconObject) {
        if (!(iconObject instanceof String)) {
            throw new IllegalArgumentException("Illegal argument type: " //$NON-NLS-1$
                    + (iconObject == null ? "null" : iconObject.getClass().toString())); //$NON-NLS-1$
        }
        final String iconUrl = (String) iconObject;
        if (!iconUrl.equals(imageElement.getSrc())) {
            throw new IllegalArgumentException("Validated url (" + iconUrl //$NON-NLS-1$
                    + ") doesn't match url of the image widget (" + imageElement.getSrc() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return validate(iconUrl);
    }

    private ValidationResult validate(String icon) {
        ValidationResult typeValidation = validateType(icon);
        if (!typeValidation.getSuccess()) {
            return typeValidation;
        }
        ValidationResult dimensionsValidation = validateBrowserParsabilityAndDimensions(icon);
        ValidationResult sizeValidation = validateSize(icon);
        if (dimensionsValidation.getSuccess() && sizeValidation.getSuccess()) {
            return ValidationResult.ok();
        }
        final List<String> reasons = new ArrayList<>(dimensionsValidation.getReasons());
        reasons.addAll(sizeValidation.getReasons());
        return new ValidationResult(false, reasons);
    }

    /**
     * HTMLImageElement.complete and Max width 150px, max height 120px
     */
    private ValidationResult validateBrowserParsabilityAndDimensions(String icon) {
        final int maxWidth = 150;
        final int maxHeight = 120;
        boolean complete = isComplete(imageElement);
        if (!complete) {
            return ValidationResult.fail(ConstantsManager.getInstance().getConstants().iconIsNotParsable());
        }
        boolean valid = imageElement.getWidth() <= maxWidth
                && imageElement.getHeight() <= maxHeight;
        if (valid) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(
                ConstantsManager.getInstance().getMessages().iconDimensionsTooLarge(
                        imageElement.getWidth(), imageElement.getWidth(), maxWidth, maxHeight));
    }

    /**
     *
     * @param element has to be instance of HTMLImageElement
     * @return {@code true} if image is successfully loaded and parsed, {@code false} otherwise
     */
    private native boolean isComplete(ImageElement element) /*-{
        return element.complete;
    }-*/;

    /**
     * The dataUri string has to fit in 32kB.
     * Ratio base64encoded/raw data is approx. 4/3.
     */
    private ValidationResult validateSize(String icon) {
        final int maxEncodedSize = 32 * 1024;
        final int maxRawSize = (int) (maxEncodedSize * (3d/4d) / 1024); // just estimate for users, in kB
        boolean valid = maxEncodedSize > icon.length();
        if (valid) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(
                ConstantsManager.getInstance().getMessages().iconFileTooLarge(maxRawSize));
    }

    /**
     * Magic numbers
     * <pre>
     *     png 89 50 4e 47 0d 0a 1a 0a
     *     jpg ff d8 ff
     *     gif 'GIF87a' or 'GIF89a'
     * </pre>
     */
    private ValidationResult validateType(String icon) {
        final String iconBase64Data = dataUriToBase64Data(icon);
        final String iconRawData = atob(iconBase64Data);
        final String[] magicNumbers = new String[] {
                "\u0089\u0050\u004e\u0047\r\n\u001a\n", //$NON-NLS-1$ // png
                "\u00ff\u00d8\u00ff", //$NON-NLS-1$ // png
                "GIF87a", //$NON-NLS-1$ // gif
                "GIF89a" //$NON-NLS-1$ // gif
        };
        for (String magicNumber : magicNumbers) {
            if (iconRawData.startsWith(magicNumber)) {
                return ValidationResult.ok();
            }
        }
        return ValidationResult.fail(ConstantsManager.getInstance().getMessages().invalidIconFormat("png, jpg, gif")); //$NON-NLS-1$
    }

    private native String atob(String encodedString) /*-{
        return atob(encodedString);
    }-*/;


    /**
     * Datauri format:
     * <pre>
     *     data:[&lt;MIME-type>][;charset=&lt;encoding&gt;][;base64],&lt;data&gt;
     * </pre>
     * @return base46 part of datauri
     */
    private String dataUriToBase64Data(String dataUri) {
        final int commaIndex = dataUri.indexOf(","); //$NON-NLS-1$
        return dataUri.substring(commaIndex + 1);
    }
}
