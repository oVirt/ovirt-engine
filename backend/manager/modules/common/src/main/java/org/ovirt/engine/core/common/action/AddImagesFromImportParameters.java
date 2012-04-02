package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.queries.ImportCandidateSourceEnum;
import org.ovirt.engine.core.compat.Guid;

public class AddImagesFromImportParameters extends AddImageFromImportParameters {
    private static final long serialVersionUID = -5062837812098816810L;

    private Guid baseID = new Guid();
    private HashMap<String, Guid> baseImageIDs;
    private Map<String, List<DiskImage>> importedImages;

    public AddImagesFromImportParameters() {
    }

    public AddImagesFromImportParameters(String candidateID, Guid baseID, HashMap<String, Guid> baseImageIDs,
            String path, ImportCandidateSourceEnum source, boolean force, Map<String, List<DiskImage>> importedImages) {
        super(Guid.Empty, Guid.Empty, new DiskImageBase(), candidateID, path, source, force);
        setBaseID(baseID);
        setBaseImageIDs(baseImageIDs);
        setImportedImages(importedImages);
    }

    public Guid getBaseID() {
        return baseID;
    }

    public void setBaseID(Guid value) {
        baseID = value;
    }

    public HashMap<String, Guid> getBaseImageIDs() {
        return baseImageIDs == null ? new HashMap<String, Guid>() : baseImageIDs;
    }

    public void setBaseImageIDs(HashMap<String, Guid> value) {
        baseImageIDs = value;
    }

    public Map<String, List<DiskImage>> getImportedImages() {
        return importedImages == null ? new HashMap<String, List<DiskImage>>() : importedImages;
    }

    public void setImportedImages(Map<String, List<DiskImage>> value) {
        importedImages = value;
    }
}
