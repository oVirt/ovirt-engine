package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;


public class VmPayload implements Serializable {
    private static final long serialVersionUID = -3665087594884425768L;
    private static String SpecParamsPayload = "vmPayload";
    private static String SpecParamsFileType = "file";

    private VmDeviceType type;
    private String fileName;
    private String content;

    public VmPayload() {
        this.type = VmDeviceType.CDROM;
        this.fileName = SpecParamsPayload;
        this.content = "";
    }

    public VmPayload(VmDeviceType type, Map<String, Object> specParams) {
        this.type = type;

        Map<String, Object> payload = (Map<String, Object>)specParams.get(SpecParamsPayload);
        Map<String, Object> files = (Map<String, Object>)payload.get(SpecParamsFileType);
        // for now we use only one file and one content...
        for (Map.Entry<String, Object> entry : files.entrySet()) {
            this.fileName = entry.getKey();
            this.content = entry.getValue().toString();
        }
    }

    public static boolean isPayload(Map<String, Object> specParams) {
        return specParams == null ? false : specParams.containsKey(SpecParamsPayload);
    }

    public static boolean isPayloadSizeLegal(String payload) {
        return payload.length() <= Config.<Integer> GetValue(ConfigValues.PayloadSize);
    }

    public VmDeviceType getType() {
        return this.type;
    }

    public void setType(VmDeviceType type) {
        this.type = type;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getSpecParams() {
        // function produce something like that:
        // vmPayload={file:{filename:content}}
        Map<String, Object> specParams = new HashMap<String, Object>();
        Map<String, Object> fileTypeList = new HashMap<String, Object>();
        Map<String, Object> fileList = new HashMap<String, Object>();

        specParams.put(SpecParamsPayload, fileTypeList);
        fileTypeList.put(SpecParamsFileType, fileList);
        fileList.put(this.fileName, this.content);

        return specParams;
    }
}
