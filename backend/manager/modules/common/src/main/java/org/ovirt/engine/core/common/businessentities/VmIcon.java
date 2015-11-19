package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

/**
 * Entity corresponding to <strong>vm_icons</strong> database table.
 */
public class VmIcon implements BusinessEntity<Guid> {

    private Guid id;
    private String dataUrl;

    public VmIcon () {
    }

    public VmIcon(Guid id, String dataUrl) {
        this.id = id;
        this.dataUrl = dataUrl;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public void setTypeAndData(String mediaType, String data) {
        this.dataUrl = typeAndDataToDataUrl(mediaType, data);
    }

    public Pair<String, String> getTypeAndData() {
        return dataUrlToTypeAndData(this.dataUrl);
    }

    /**
     * It converts icon from dataurl form to separate media type and data.
     * @param dataUrl icon in dataurl form
     * @return icon in form of (media_type, base64 encoded data)
     */
    public static Pair<String, String> dataUrlToTypeAndData(String dataUrl) {
        final String dataUrlRegex = "^data:(\\w+/\\w+);base64,([\\w+/]+={0,2})$";
        final Matcher matcher = Pattern.compile(dataUrlRegex).matcher(dataUrl);
        final boolean matches = matcher.find();
        if (!matches) {
            throw new IllegalStateException("DataUrl has invalid format.");
        }
        final String mimeType = matcher.group(1);
        final String base64Data = matcher.group(2);
        return new Pair<>(mimeType, base64Data);
    }

    /**
     * It converts icon from couple (media type, base64 data) to dataurl form.
     * @param mediaType mime type
     * @param data base64 encoded icon data
     * @return icon in dataurl
     */
    public static String typeAndDataToDataUrl(String mediaType, String data) {
        return  "data:" + mediaType + ";base64," + data;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "VmIcon{" +
                "id=" + id +
                ", dataUrl='" + printDataUrl(dataUrl) + '\'' +
                '}';
    }

    private static String printDataUrl(String dataUrl) {
        if (dataUrl == null) {
            return "null";
        }
        final int maxLength = 32;
        if (dataUrl.length() > maxLength) {
            return dataUrl.substring(0, maxLength) + '…';
        }
        return dataUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VmIcon)) {
            return false;
        }
        VmIcon vmIcon = (VmIcon) o;
        return Objects.equals(id, vmIcon.id)
                && Objects.equals(dataUrl, vmIcon.dataUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                dataUrl
        );
    }
}
