package org.ovirt.engine.core.bll.validator;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.di.Injector;

public class IconValidator {

    private final DimensionsType dimensionsType;
    private String mimeType;
    private String base64Data;
    private byte[] rawImageData;
    private FileType imageType;
    private BufferedImage image;
    private ValidationResult validationResult = ValidationResult.VALID;

    private IconValidator(DimensionsType dimensionsType, String dataUrl) {
        this.dimensionsType = dimensionsType;
        validateDataUrlFormat(dataUrl);
        if (!validationResult.isValid()) {
            return;
        }
        validateBase64();
        validateImageType();
        if (!validationResult.isValid()) {
            return;
        }
        validateParsability();
        if (!validationResult.isValid()) {
            return;
        }
        validateMimeType();
        if (!validationResult.isValid()) {
            return;
        }
        validateDimensions();
    }

    public static ValidationResult validate(DimensionsType iconType, String dataUrl) {
        return new IconValidator(iconType, dataUrl).getValidationResult();
    }

    public static ValidationResult validateIconId(Guid iconId, String nameForErrorMessage) {
        if (Injector.get(VmIconDao.class).exists(iconId)) {
            return ValidationResult.VALID;
        }
        return new ValidationResult(EngineMessage.ICON_OF_PROVIDED_ID_DOES_NOT_EXIST,
                "$iconName " + nameForErrorMessage);
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    private void validateDataUrlFormat(String dataUrl) {
        final String dataUrlRegex = "^data:(\\w+/\\w+);base64,([\\w+/]+={0,2})$";
        final Matcher matcher = Pattern.compile(dataUrlRegex).matcher(dataUrl);
        final boolean matches = matcher.find();
        if (!matches) {
            validationResult = new ValidationResult(EngineMessage.VM_ICON_DATAURL_MALFORMED);
            return;
        }
        mimeType = matcher.group(1);
        base64Data = matcher.group(2);
    }

    private void validateBase64() {
        try {
            rawImageData = DatatypeConverter.parseBase64Binary(base64Data);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            validationResult = new ValidationResult(EngineMessage.VM_ICON_BASE64_PART_MALFORMED);
        }
    }

    private void validateImageType() {
        try {
            final InputStream inputStream = new ByteArrayInputStream(rawImageData);
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            if (!imageReaders.hasNext()) {
                validationResult = new ValidationResult(EngineMessage.PROVIDED_VM_ICON_OF_UNKNOWN_TYPE);
                return;
            }
            final String formatName = imageReaders.next().getFormatName();
            imageType = FileType.getByFormatName(formatName);
            if (imageType == null) {
                validationResult = new ValidationResult(EngineMessage.PROVIDED_VM_ICONS_OF_UNSUPPORTED_TYPE,
                        "$fileType " + formatName,
                        "$supportedFileTypes " + FileType.getSupportedTypes());
            }
        } catch (IOException e) {
            validationResult = new ValidationResult(EngineMessage.PROVIDED_VM_ICON_CANT_BE_READ);
        }
    }

    private void validateParsability() {
        try {
            image = ImageIO.read(new ByteArrayInputStream(rawImageData));
        } catch (RuntimeException | IOException e) {
            validationResult = new ValidationResult(EngineMessage.PROVIDED_VM_ICON_CANT_BE_READ);
        }
    }

    private void validateMimeType() {
        if (!imageType.getMimeType().equals(mimeType)) {
            validationResult = new ValidationResult(EngineMessage.VM_ICON_MIME_TYPE_DOESNT_MATCH_IMAGE_DATA,
                    "$mimeType " + mimeType,
                    "$imageType " + imageType.getMimeType());
        }
    }

    private void validateDimensions() {
        boolean dimensionsValid = image.getWidth() >= dimensionsType.getMinWidth()
                && image.getWidth() <= dimensionsType.getMaxWidth()
                && image.getHeight() >= dimensionsType.getMinHeight()
                && image.getHeight() <= dimensionsType.getMaxHeight();
        if (!dimensionsValid) {
            validationResult = new ValidationResult(EngineMessage.PROVIDED_VM_ICON_HAS_INVALID_DIMENSIONS,
                    "$allowedDimensions " + "from " + dimensionsType.getMinWidth() + "x" + dimensionsType.getMinHeight()
                        + " to " + dimensionsType.getMaxWidth() + "x" + dimensionsType.getMaxHeight(),
                    "$currentDimensions " + image.getWidth() + "x" + image.getHeight());
        }
    }

    public enum FileType {

        JPG(Arrays.asList("jpg", "jpeg"), "image/jpeg", "JPEG"),
        PNG(Arrays.asList("png"), "image/png", "png"),
        GIF(Arrays.asList("gif"), "image/gif", "gif");

        /**
         * lower case
         */
        private final List<String> extensions;
        private final String mimeType;
        /**
         * String used to identify image format by {@link javax.imageio.ImageWriter}s
         * and {@link javax.imageio.ImageReader}a
         */
        private final String formatName;

        FileType(List<String> extensions, String mimeType, String formatName) {
            this.extensions = extensions;
            this.mimeType = mimeType;
            this.formatName = formatName;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getFormatName() {
            return formatName;
        }

        public static FileType getByFormatName(String formatName) {
            for (FileType type : FileType.values()) {
                if (type.getFormatName().equals(formatName)) {
                    return type;
                }
            }
            return null;
        }

        public static String getSupportedTypes() {
            List<String> supportedTypeNames = new ArrayList<>();
            for (FileType type : FileType.values()) {
                supportedTypeNames.add(type.toString().toLowerCase());
            }
            return StringUtils.join(supportedTypeNames, ", ");
        }
    }

    public static enum DimensionsType {

        SMALL_PREDEFINED_ICON(43, 43, 43, 43),
        LARGE_PREDEFINED_ICON(150, 120, 150, 120),
        SMALL_CUSTOM_ICON(1, 1, 43, 43),
        LARGE_CUSTOM_ICON(1, 1, 150, 120);

        private final int minWidth;
        private final int minHeight;
        private final int maxWidth;
        private final int maxHeight;

        DimensionsType(int minWidth, int minHeight, int maxWidth, int maxHeight) {
            this.minWidth = minWidth;
            this.minHeight = minHeight;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        public int getMinWidth() {
            return minWidth;
        }

        public int getMinHeight() {
            return minHeight;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public boolean isInMaxBounds(RenderedImage image) {
            return image.getWidth() <= maxWidth
                    && image.getHeight() <= maxHeight;
        }
    }
}
