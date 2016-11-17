package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class MoveOrCopyParameters extends StorageDomainParametersBase implements Serializable {
    private static final long serialVersionUID = 1051590893103934441L;

    private Map<Guid, Guid> imageToDestinationDomainMap;
    private boolean importAsNewEntity;
    private boolean allowPartialImport;
    private boolean imagesExistOnTargetStorageDomain;
    private Guid cpuProfileId;

    public MoveOrCopyParameters(Guid containerId, Guid storageDomainId) {
        super(storageDomainId);
        setContainerId(containerId);
        setTemplateMustExists(false);
        setForceOverride(false);
    }

    private Guid privateContainerId;

    public Guid getContainerId() {
        return privateContainerId;
    }

    public void setContainerId(Guid value) {
        privateContainerId = value;
    }

    private boolean privateCopyCollapse;

    public boolean getCopyCollapse() {
        return privateCopyCollapse;
    }

    public void setCopyCollapse(boolean value) {
        privateCopyCollapse = value;
    }

    private boolean privateTemplateMustExists;

    public boolean getTemplateMustExists() {
        return privateTemplateMustExists;
    }

    public void setTemplateMustExists(boolean value) {
        privateTemplateMustExists = value;
    }

    private boolean privateForceOverride;

    public boolean getForceOverride() {
        return privateForceOverride;
    }

    public void setForceOverride(boolean value) {
        privateForceOverride = value;
    }

    public MoveOrCopyParameters() {
        privateContainerId = Guid.Empty;
    }

    public void setImageToDestinationDomainMap(Map<Guid, Guid> map) {
        imageToDestinationDomainMap = map;
    }

    public Map<Guid, Guid> getImageToDestinationDomainMap() {
        return imageToDestinationDomainMap;
    }

    public boolean isImportAsNewEntity() {
        return importAsNewEntity;
    }

    public void setImportAsNewEntity(boolean importAsNewEntity) {
        this.importAsNewEntity = importAsNewEntity;
    }

    public boolean isAllowPartialImport() {
        return allowPartialImport;
    }

    public void setAllowPartialImport(boolean allowPartialImport) {
        this.allowPartialImport = allowPartialImport;
    }

    public boolean isImagesExistOnTargetStorageDomain() {
        return imagesExistOnTargetStorageDomain;
    }

    public void setImagesExistOnTargetStorageDomain(boolean imagesExistOnTargetStorageDomain) {
        this.imagesExistOnTargetStorageDomain = imagesExistOnTargetStorageDomain;
    }

    public Guid getCpuProfileId() {
        return cpuProfileId;
    }

    public void setCpuProfileId(Guid cpuProfileId) {
        this.cpuProfileId = cpuProfileId;
    }
}
