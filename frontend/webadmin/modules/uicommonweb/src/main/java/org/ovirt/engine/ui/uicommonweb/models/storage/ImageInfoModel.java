package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.dom.client.Element;

public class ImageInfoModel extends EntityModel<String> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private VolumeFormat format;
    private long actualSize;
    private long virtualSize;
    private QemuCompat qcowCompat;
    private Boolean backingFile;
    private Boolean fileLoaded = false;
    private DiskContentType contentType;
    private String fileName;

    public VolumeFormat getFormat() {
        return format;
    }

    public void setFormat(VolumeFormat format) {
        this.format = format;
    }

    public long getActualSize() {
        return actualSize;
    }

    public void setActualSize(long actualSize) {
        this.actualSize = actualSize;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public QemuCompat getQcowCompat() {
        return qcowCompat;
    }

    public void setQcowCompat(QemuCompat qcowCompat) {
        this.qcowCompat = qcowCompat;
    }

    public Boolean isBackingFile() {
        return backingFile;
    }

    public void setBackingFile(Boolean backingFile) {
        this.backingFile = backingFile;
    }

    public Boolean getFileLoaded() {
        return fileLoaded;
    }

    public void setFileLoaded(Boolean fileLoaded) {
        this.fileLoaded = fileLoaded;
    }

    public DiskContentType getContentType() {
        return contentType;
    }

    public void setContentType(DiskContentType contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public native void initialize(Element imageFileUploadElement) /*-{
        var self = this;

        imageFileUploadElement.addEventListener('change', onChange);

        function onChange() {
            self.@org.ovirt.engine.ui.uicommonweb.models.storage.ImageInfoModel::clearModel()();

            // See http://git.qemu.org/?p=qemu.git;a=blob;f=docs/specs/qcow2.txt
            var file = imageFileUploadElement.files[0];
            var reader = new FileReader();
            reader.onload = function(e) {
                var header = reader.result;
                if (header.byteLength == 0) {
                    // can't open the file
                    return;
                }
                var isQcow = readString(header.slice(0, 4)) == "QFI\xfb";
                var info = {};
                info.format = isQcow ? "COW" : "RAW";
                info.actualSize = file.size;
                info.virtualSize = 0;
                info.backingFile = false;
                info.fileLoaded = true;
                info.contentType = 'DATA';

                if (isQcow) {
                    var version = readUint32(header.slice(4, 8));
                    info.qcowCompat = version == 2 ? "0.10" : version == 3 ? "1.1" : "";
                    var backingFileOffset = readUint64(header.slice(8, 16));
                    info.backingFile = backingFileOffset != 0;
                    info.virtualSize = readUint64(header.slice(24, 32));
                } else {
                    // An ISO file contains the literal 'CD001' in offset 8001h
                    var isISO = readString(header.slice(0x8001, 0x8001 + 5)) == "CD001";
                    if (isISO) {
                        info.contentType = 'ISO';
                        info.fileName = file.name;
                    }
                    info.virtualSize = info.actualSize;
                }

                self.@org.ovirt.engine.ui.uicommonweb.models.storage.ImageInfoModel::updateModel(*) (
                    info.format, info.actualSize, info.virtualSize, info.qcowCompat, info.backingFile, info.fileLoaded,
                    info.contentType, info.fileName);
            };
            // An ISO file contains the literal 'CD001' in offset 8001h thus 8006h bytes needed to be read
            var blob = file.slice(0, 0x8001 + 5);
            reader.readAsArrayBuffer(blob);
        };

        function readString(buf) {
            // Reading from array buffer, assuming big endian
            return String.fromCharCode.apply(null, new Uint8Array(buf));
        };

        function readUint32(buf) {
            return new DataView(buf).getUint32(0);
        };

        function readUint64(buf) {
            var dv = new DataView(buf);
            var high = dv.getUint32(0);
            var low = dv.getUint32(4);
            // There is no way to represent uint64, but double works.
            return high * 4294967296.0 + low;
        };
    }-*/;

    public void updateModel(String format, double actualSize, double virtualSize, String qcowCompat,
                            boolean backingFile, boolean fileLoaded, String contentType, String fileName) {
        setFormat(VolumeFormat.valueOf(format));
        setActualSize((long) actualSize);
        setVirtualSize((long) virtualSize);
        setQcowCompat(QemuCompat.forValue(qcowCompat));
        setBackingFile(backingFile);
        setFileLoaded(fileLoaded);
        setContentType(DiskContentType.valueOf(contentType));
        setFileName(fileName);

        getEntityChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public void clearModel() {
        setFormat(null);
        setActualSize(0);
        setVirtualSize(0);
        setQcowCompat(null);
        setBackingFile(null);
        setFileLoaded(false);
        setContentType(null);
        setFileName(null);

        setIsValid(true);
        getInvalidityReasons().clear();
        getEntityChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public boolean validate(StorageFormatType storageFormatType, long imageSize) {
        if (!fileLoaded) {
            getInvalidityReasons().add(constants.uploadImageCannotBeOpened());
            return false;
        }
        if (backingFile) {
            getInvalidityReasons().add(constants.uploadImageBackingFileUnsupported());
            return false;
        }

        if (qcowCompat != null && qcowCompat != ImageInfoModel.QemuCompat.V2) {
            switch (storageFormatType) {
                case V1:
                case V2:
                case V3:
                    getInvalidityReasons().add(messages.uploadImageQemuCompatUnsupported(
                            qcowCompat.getValue(), storageFormatType.name()));
                    return false;
            }
        }

        return true;
    }

    public enum QemuCompat {
        V2("0.10"), //$NON-NLS-1$
        V3("1.1"); //$NON-NLS-1$

        private final String value;
        private static final Map<String, QemuCompat> mappings = new HashMap<>();
        static {
            for (QemuCompat compat : values()) {
                mappings.put(compat.getValue(), compat);
            }
        }

        private QemuCompat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static QemuCompat forValue(String value) {
            return mappings.get(value);
        }
    }
}
