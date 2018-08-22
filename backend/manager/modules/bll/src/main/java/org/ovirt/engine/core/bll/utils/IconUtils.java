package org.ovirt.engine.core.bll.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.dao.VmIconDefaultDao;

/**
 * Shared code related to Vm Icons
 */
@Singleton
public class IconUtils {

    @Inject
    private VmIconDao vmIconDao;

    @Inject
    private VmIconDefaultDao vmIconDefaultDao;

    /**
     *
     * @param dataUrl valid large icon in dataUrl form
     * @return small icon in dataUrl form
     */
    private static String computeSmallIcon(String dataUrl) {
        final byte[] rawData = fromDataUrl(dataUrl);
        final BufferedImage largeImage = loadImage(rawData);
        final BufferedImage smallImage = resizeToSmall(largeImage);
        final byte[] smallIconRawData = writeImage(smallImage, IconValidator.FileType.PNG);
        final String smallIcon = toDataUrl(smallIconRawData, IconValidator.FileType.PNG);
        return smallIcon;
    }

    private static BufferedImage resizeToSmall(BufferedImage largeImage) {
        if (IconValidator.DimensionsType.SMALL_CUSTOM_ICON.isInMaxBounds(largeImage)) {
            return largeImage;
        }
        final int targetWidth = Math.min(largeImage.getWidth(),
                IconValidator.DimensionsType.SMALL_CUSTOM_ICON.getMaxWidth());
        final int targetHeight = Math.min(largeImage.getHeight(),
                IconValidator.DimensionsType.SMALL_CUSTOM_ICON.getMaxHeight());
        final BufferedImage smallImage = new BufferedImage(targetWidth, targetHeight, largeImage.getType());
        final Graphics2D graphics = smallImage.createGraphics();
        graphics.drawImage(largeImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return smallImage;
    }

    private static byte[] fromDataUrl(String dataUrl) {
        final String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
        return DatatypeConverter.parseBase64Binary(base64Data);
    }

    private static BufferedImage loadImage(byte[] rawData) {
        try {
            return ImageIO.read(new ByteArrayInputStream(rawData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] writeImage(BufferedImage image, IconValidator.FileType fileType) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, fileType.getFormatName(), byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * It converts icon represented by raw bytes to dataurl format.
     * <p>
     *     Dataurl schema:
     *     <pre>
     *         data:[&lt;MIME-type>][;charset=&lt;encoding>][;base64],&lt;data>
     *     </pre>
     * </p>
     * @param data raw icon bytes
     * @param type icon file type descriptor
     * @return icon in form of dataurl
     */
    public static String toDataUrl(byte[] data, IconValidator.FileType type) {
        // data:[<MIME-type>][;charset=<encoding>][;base64],<data>
        final String base64Data = DatatypeConverter.printBase64Binary(data);
        final String dataUrl = new StringBuilder()
                .append("data:")
                .append(type.getMimeType())
                .append(";base64,")
                .append(base64Data)
                .toString();
        return dataUrl;
    }

    public VmIconIdSizePair ensureIconPairInDatabase(String largeIconDataUrl) {
        final String smallIconDataUrl = IconUtils.computeSmallIcon(largeIconDataUrl);
        Guid largeIconId = vmIconDao.ensureIconInDatabase(largeIconDataUrl);
        Guid smallIconId = vmIconDao.ensureIconInDatabase(smallIconDataUrl);
        return new VmIconIdSizePair(smallIconId, largeIconId);
    }

    /**
     * @return smallIconId
     */
    private Guid ensureSmallIconInDatabase(Guid largeIconId) {
        final String largeIconDataUrl = vmIconDao.get(largeIconId).getDataUrl();
        return ensureIconPairInDatabase(largeIconDataUrl).getSmall();
    }

    /**
     * @return list of icon ids that was previously used and are not used any more by this vm_static entity
     */
    public List<Guid> updateVmIcon(VmBase originalVmBase, VmBase newVmBase, String vmIconParameter) {
        if (vmIconParameter != null) {
            addNewIconPair(newVmBase, vmIconParameter);
        } else {
            if (newVmBase.getSmallIconId() == null) {
                computeSmallByLargeIconId(newVmBase);
            }
        }
        List<Guid> unusedIconIds = new ArrayList<>(2);
        if (newVmBase.getSmallIconId() != null && originalVmBase.getSmallIconId() != null &&
                !newVmBase.getSmallIconId().equals(originalVmBase.getSmallIconId())) {
            unusedIconIds.add(originalVmBase.getSmallIconId());
        }
        if (newVmBase.getLargeIconId() != null && originalVmBase.getLargeIconId() != null &&
                !newVmBase.getLargeIconId().equals(originalVmBase.getLargeIconId())) {
            unusedIconIds.add(originalVmBase.getLargeIconId());
        }
        return unusedIconIds;
    }

    private void computeSmallByLargeIconId(VmBase vmBase) {
        if (vmBase.getLargeIconId() == null) {
            return;
        }

        final List<VmIconDefault> iconDefaultsByLargeIconId = vmIconDefaultDao.getByLargeIconId(vmBase.getLargeIconId());
        if (!iconDefaultsByLargeIconId.isEmpty()) {
            vmBase.setSmallIconId(iconDefaultsByLargeIconId.get(0).getSmallIconId());
        } else {
            vmBase.setSmallIconId(ensureSmallIconInDatabase(vmBase.getLargeIconId()));
        }
    }

    private void addNewIconPair(VmBase vmBase, String largeIconDataUrl) {
        final VmIconIdSizePair iconIds = ensureIconPairInDatabase(largeIconDataUrl);
        vmBase.setLargeIconId(iconIds.getLarge());
        vmBase.setSmallIconId(iconIds.getSmall());
    }

    public void removeUnusedIcons(List<Guid> iconIds) {
        removeUnusedIcons(iconIds, null);
    }

    public void removeUnusedIcons(List<Guid> iconIds, CompensationContext compensationContext) {
        for (Guid iconId : iconIds) {
            if (compensationContext != null) {
                VmIcon icon = vmIconDao.get(iconId);
                if (icon != null) {
                    compensationContext.snapshotEntity(icon);
                }
            }

            vmIconDao.removeIfUnused(iconId);
        }
    }

    public static void preserveIcons(VmBase vmBaseFromOvf, VmBase vmBaseFromDb) {
        vmBaseFromOvf.setSmallIconId(vmBaseFromDb.getSmallIconId());
        vmBaseFromOvf.setLargeIconId(vmBaseFromDb.getLargeIconId());
    }
}
