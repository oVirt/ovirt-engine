package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.PayloadEncoding;

@ValidatedClass(clazz = CloudInit.class)
public class CloudInitValidator implements Validator<CloudInit> {

    @Override
    public void validateEnums(CloudInit model) {
        if (model != null) {
            if (model.isSetNetworkConfiguration()) {
                if (model.getNetworkConfiguration().isSetNics()
                        && !model.getNetworkConfiguration().getNics().getNics().isEmpty()) {
                    for (NIC iface : model.getNetworkConfiguration().getNics().getNics()) {
                        validateEnum(BootProtocol.class, iface.getBootProtocol(), true);
                    }
                }
            }
            if (model.isSetFiles()
                    && model.getFiles().isSetFiles()
                    && !model.getFiles().getFiles().isEmpty()) {
                for (File file : model.getFiles().getFiles()) {
                    validateEnum(PayloadEncoding.class, file.getType(), true);
                }
            }
        }
    }
}
