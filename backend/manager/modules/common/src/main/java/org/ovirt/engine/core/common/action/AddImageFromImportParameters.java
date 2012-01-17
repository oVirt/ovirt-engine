package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddImageFromImportParameters")
public class AddImageFromImportParameters extends AddImageFromScratchParameters {
    private static final long serialVersionUID = 4373704503946067322L;
    @XmlElement(name = "CandidateID")
    private String privateCandidateID;

    public String getCandidateID() {
        return privateCandidateID;
    }

    private void setCandidateID(String value) {
        privateCandidateID = value;
    }

    @XmlElement(name = "Path")
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    @XmlElement(name = "Source")
    private ImportCandidateSourceEnum privateSource = ImportCandidateSourceEnum.forValue(0);

    public ImportCandidateSourceEnum getSource() {
        return privateSource;
    }

    private void setSource(ImportCandidateSourceEnum value) {
        privateSource = value;
    }

    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    private void setForce(boolean value) {
        privateForce = value;
    }

    public AddImageFromImportParameters(Guid imageId, Guid vmTemplateId, DiskImageBase diskInfo, String candidateID,
            String path, ImportCandidateSourceEnum source, boolean force) {
        super(imageId, vmTemplateId, diskInfo);
        setCandidateID(candidateID);
        setPath(path);
        setSource(source);
        setForce(force);
    }

    public AddImageFromImportParameters() {
    }
}
