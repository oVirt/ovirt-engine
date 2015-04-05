package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RegisterCinderDiskParameters extends AddDiskParameters {

    private static final long serialVersionUID = -6020756467875901455L;

    private CinderDisk cinderDisk;

    public RegisterCinderDiskParameters() {
    }

    public RegisterCinderDiskParameters(CinderDisk cinderDisk, Guid storageDomainId) {
        this.cinderDisk = cinderDisk;
        setStorageDomainId(storageDomainId);
    }

    public CinderDisk getCinderDisk() {
        return cinderDisk;
    }
}
