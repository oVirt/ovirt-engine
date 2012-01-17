package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

import java.util.Map;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportCandidateVDSCommandParameters")
public class ImportCandidateVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    @XmlElement(name = "CandidateID")
    private String privateCandidateID;

    public String getCandidateID() {
        return privateCandidateID;
    }

    protected void setCandidateID(String value) {
        privateCandidateID = value;
    }

    @XmlElement(name = "BaseID")
    private Guid privateBaseID = new Guid();

    public Guid getBaseID() {
        return privateBaseID;
    }

    protected void setBaseID(Guid value) {
        privateBaseID = value;
    }

    @XmlElement
    private java.util.HashMap<String, Guid> privateBaseImageIDs;

    public Map<String, Guid> getBaseImageIDs() {
        return privateBaseImageIDs;
    }

    protected void setBaseImageIDs(java.util.HashMap<String, Guid> value) {
        privateBaseImageIDs = value;
    }

    @XmlElement(name = "CandidateSource")
    private ImportCandidateSourceEnum privateCandidateSource = ImportCandidateSourceEnum.forValue(0);

    public ImportCandidateSourceEnum getCandidateSource() {
        return privateCandidateSource;
    }

    protected void setCandidateSource(ImportCandidateSourceEnum value) {
        privateCandidateSource = value;
    }

    @XmlElement(name = "ImportPath")
    private String privateImportPath;

    public String getImportPath() {
        return privateImportPath;
    }

    protected void setImportPath(String value) {
        privateImportPath = value;
    }

    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    protected void setForce(boolean value) {
        privateForce = value;
    }

    public ImportCandidateVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            String candidateID, Guid baseID, java.util.HashMap<String, Guid> baseImageIDs,
            ImportCandidateSourceEnum candidateSource, String importPath, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId);
        setCandidateID(candidateID);
        setBaseID(baseID);
        setBaseImageIDs(baseImageIDs);
        setCandidateSource(candidateSource);
        setImportPath(importPath);
        setForce(force);
    }

    public ImportCandidateVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, candidateId = %s, baseId = %s, baseImageIDs = %s, candidateSource = %s, " +
                "importPath = %s, force = %s",
                super.toString(),
                getCandidateID(),
                getBaseID(),
                getBaseImageIDs(),
                getCandidateSource(),
                getImportPath(),
                getForce());
    }
}
