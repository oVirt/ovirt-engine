package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.queries.ImportCandidateSourceEnum;
import org.ovirt.engine.core.compat.Guid;

public class AddImageFromImportParameters extends AddImageFromScratchParameters {

    private static final long serialVersionUID = 4373704503946067322L;

    private String candidateID;
    private String path;
    private ImportCandidateSourceEnum source = ImportCandidateSourceEnum.KVM;
    private boolean force;

    public AddImageFromImportParameters() {
    }

    public AddImageFromImportParameters(Guid imageId, Guid vmTemplateId, DiskImageBase diskInfo, String candidateID,
            String path, ImportCandidateSourceEnum source, boolean force) {
        super(imageId, vmTemplateId, diskInfo);
        setCandidateID(candidateID);
        setPath(path);
        setSource(source);
        setForce(force);
    }

    public String getCandidateID() {
        return candidateID;
    }

    public void setCandidateID(String value) {
        candidateID = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        path = value;
    }

    public ImportCandidateSourceEnum getSource() {
        return source;
    }

    public void setSource(ImportCandidateSourceEnum value) {
        source = value;
    }

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean value) {
        force = value;
    }
}
