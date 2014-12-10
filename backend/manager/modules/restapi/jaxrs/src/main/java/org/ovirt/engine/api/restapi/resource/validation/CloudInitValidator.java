package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.PayloadEncoding;

@ValidatedClass(clazz = CloudInit.class)
public class CloudInitValidator implements Validator<CloudInit> {

    @Override
    public void validateEnums(CloudInit model) {
        if (model != null) {
            if (model.isSetNetworkConfiguration()) {
                Nics nics = model.getNetworkConfiguration().getNics();
                if (nics != null && !nics.getNics().isEmpty()) {
                    for (NIC nic : nics.getNics()) {
                        if (nic.isSetBootProtocol()) {
                            validateEnum(BootProtocol.class, nic.getBootProtocol(), true);
                        }
                    }
                }
            }
            Files files = model.getFiles();
            if (files != null && !files.getFiles().isEmpty()) {
                for (File file : files.getFiles()) {
                    if (file.isSetType()) {
                        validateEnum(PayloadEncoding.class, file.getType(), true);
                    }
                }
            }
        }
    }
}
