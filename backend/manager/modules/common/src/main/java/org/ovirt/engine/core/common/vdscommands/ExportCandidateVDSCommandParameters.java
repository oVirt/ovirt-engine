package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.compat.Guid;

public class ExportCandidateVDSCommandParameters extends StorageDomainIdParametersBase {
    private Guid privateVmGUID = new Guid();

    public Guid getVmGUID() {
        return privateVmGUID;
    }

    private void setVmGUID(Guid value) {
        privateVmGUID = value;
    }

    private HashMap<String, ArrayList<Guid>> privateListOfImages;

    public HashMap<String, ArrayList<Guid>> getListOfImages() {
        return privateListOfImages;
    }

    private void setListOfImages(HashMap<String, ArrayList<Guid>> value) {
        privateListOfImages = value;
    }

    private String privateVmMeta;

    public String getVmMeta() {
        return privateVmMeta;
    }

    private void setVmMeta(String value) {
        privateVmMeta = value;
    }

    private Guid privateVmTemplateGUID = new Guid();

    public Guid getVmTemplateGUID() {
        return privateVmTemplateGUID;
    }

    private void setVmTemplateGUID(Guid value) {
        privateVmTemplateGUID = value;
    }

    private HashMap<String, Guid> privateVmTemplateImageGUIDs;

    public HashMap<String, Guid> getVmTemplateImageGUIDs() {
        return privateVmTemplateImageGUIDs;
    }

    private void setVmTemplateImageGUIDs(HashMap<String, Guid> value) {
        privateVmTemplateImageGUIDs = value;
    }

    private String privateVmTemplateMeta;

    public String getVmTemplateMeta() {
        return privateVmTemplateMeta;
    }

    private void setVmTemplateMeta(String value) {
        privateVmTemplateMeta = value;
    }

    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    private boolean privateCollapse;

    public boolean getCollapse() {
        return privateCollapse;
    }

    private void setCollapse(boolean value) {
        privateCollapse = value;
    }

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    public ExportCandidateVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid vmGUID,
            HashMap<String, ArrayList<Guid>> listOfImages, String vmMeta, Guid vmTemplateGUID,
            HashMap<String, Guid> vmTemplateImageGUIDs, String vmTemplateMeta, String path, boolean collapse,
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
