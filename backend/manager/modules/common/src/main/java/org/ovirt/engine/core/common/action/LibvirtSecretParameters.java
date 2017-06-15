package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;

public class LibvirtSecretParameters extends ActionParametersBase {

    private static final long serialVersionUID = -5231418068819634608L;

    @Valid
    private LibvirtSecret libvirtSecret;

    public LibvirtSecretParameters() {
    }

    public LibvirtSecretParameters(LibvirtSecret libvirtSecret) {
        this.libvirtSecret = libvirtSecret;
    }

    public LibvirtSecret getLibvirtSecret() {
        return libvirtSecret;
    }

}
