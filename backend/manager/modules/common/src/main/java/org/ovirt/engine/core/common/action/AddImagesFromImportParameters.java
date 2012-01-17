package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddImagesFromImportParameters")
public class AddImagesFromImportParameters extends AddImageFromImportParameters {
    private static final long serialVersionUID = -5062837812098816810L;
    @XmlElement(name = "BaseID")
    private Guid privateBaseID = new Guid();

    public Guid getBaseID() {
        return privateBaseID;
    }

    private void setBaseID(Guid value) {
        privateBaseID = value;
    }

    @XmlElement
    private java.util.HashMap<String, Guid> privateBaseImageIDs;

    public java.util.HashMap<String, Guid> getBaseImageIDs() {
        return privateBaseImageIDs == null ? new HashMap<String, Guid>() : privateBaseImageIDs;
    }

    private void setBaseImageIDs(java.util.HashMap<String, Guid> value) {
        privateBaseImageIDs = value;
    }

    private Map<String, List<DiskImage>> privateImportedImages;

    public Map<String, List<DiskImage>> getImportedImages() {
        return privateImportedImages == null ? new HashMap<String, List<DiskImage>>() : privateImportedImages;
    }

    private void setImportedImages(Map<String, List<DiskImage>> value) {
        privateImportedImages = value;
    }

    public AddImagesFromImportParameters(String candidateID, Guid baseID, java.util.HashMap<String, Guid> baseImageIDs,
            String path, ImportCandidateSourceEnum source, boolean force, Map<String, List<DiskImage>> importedImages) {
        super(Guid.Empty, Guid.Empty, new DiskImageBase(), candidateID, path, source, force);
        setBaseID(baseID);
        setBaseImageIDs(baseImageIDs);
        setImportedImages(importedImages);
    }

    public AddImagesFromImportParameters() {
    }
}
