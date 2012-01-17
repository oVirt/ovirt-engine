package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ExportCandidateVDSCommandParameters")
public class ExportCandidateVDSCommandParameters extends StorageDomainIdParametersBase {
    @XmlElement(name = "VmGUID")
    private Guid privateVmGUID = new Guid();

    public Guid getVmGUID() {
        return privateVmGUID;
    }

    private void setVmGUID(Guid value) {
        privateVmGUID = value;
    }

    @XmlElement
    private java.util.HashMap<String, java.util.ArrayList<Guid>> privateListOfImages;

    public java.util.HashMap<String, java.util.ArrayList<Guid>> getListOfImages() {
        return privateListOfImages;
    }

    private void setListOfImages(java.util.HashMap<String, java.util.ArrayList<Guid>> value) {
        privateListOfImages = value;
    }

    @XmlElement(name = "VmMeta")
    private String privateVmMeta;

    public String getVmMeta() {
        return privateVmMeta;
    }

    private void setVmMeta(String value) {
        privateVmMeta = value;
    }

    @XmlElement(name = "VmTemplateGUID")
    private Guid privateVmTemplateGUID = new Guid();

    public Guid getVmTemplateGUID() {
        return privateVmTemplateGUID;
    }

    private void setVmTemplateGUID(Guid value) {
        privateVmTemplateGUID = value;
    }

    @XmlElement
    private java.util.HashMap<String, Guid> privateVmTemplateImageGUIDs;

    public java.util.HashMap<String, Guid> getVmTemplateImageGUIDs() {
        return privateVmTemplateImageGUIDs;
    }

    private void setVmTemplateImageGUIDs(java.util.HashMap<String, Guid> value) {
        privateVmTemplateImageGUIDs = value;
    }

    @XmlElement(name = "VmTemplateMeta")
    private String privateVmTemplateMeta;

    public String getVmTemplateMeta() {
        return privateVmTemplateMeta;
    }

    private void setVmTemplateMeta(String value) {
        privateVmTemplateMeta = value;
    }

    @XmlElement(name = "Path")
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    @XmlElement(name = "Collapse")
    private boolean privateCollapse;

    public boolean getCollapse() {
        return privateCollapse;
    }

    private void setCollapse(boolean value) {
        privateCollapse = value;
    }

    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    public ExportCandidateVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid vmGUID,
            java.util.HashMap<String, java.util.ArrayList<Guid>> listOfImages, String vmMeta, Guid vmTemplateGUID,
            java.util.HashMap<String, Guid> vmTemplateImageGUIDs, String vmTemplateMeta, String path, boolean collapse,
            boolean force) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
        setVmGUID(vmGUID);
        setListOfImages(listOfImages);
        setVmMeta(vmMeta);
        setVmTemplateGUID(vmTemplateGUID);
        setVmTemplateImageGUIDs(vmTemplateImageGUIDs);
        setVmTemplateMeta(vmTemplateMeta);
        setPath(path);
        setCollapse(collapse);
        setForce(force);
    }

    public ExportCandidateVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmGUID = %s, listOfImages = %s, vmMeta = %s, vmTemplateGUID = %s, " +
                "vmTemplateImageGUIDs = %s, vmTemplateMeta = %s, path = %s, collapse = %s, force = %s",
                super.toString(),
                getVmGUID(),
                getListOfImages(),
                getVmMeta(),
                getVmTemplateGUID(),
                getVmTemplateImageGUIDs(),
                getVmTemplateMeta(),
                getPath(),
                getCollapse(),
                getForce());
    }
}
