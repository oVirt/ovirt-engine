package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.GuestNicConfiguration;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmType;

@ValidatedClass(clazz = VM.class)
public class VmValidator implements Validator<VM> {

    private UsbValidator usbValidator = new UsbValidator();
    private OsValidator osValidator = new OsValidator();
    private DisplayValidator displayValidator = new DisplayValidator();
    private PlacementPolicyValidator placementPolicyValidator = new PlacementPolicyValidator();
    private PayloadValidator payloadValidator = new PayloadValidator();
    private ConfigurationValidator configurationValidator = new ConfigurationValidator();
    private CloudInitValidator cloudInitValidator = new CloudInitValidator();
    private CPUValidator cpuValidator = new CPUValidator();
    private RngDeviceValidator rngDeviceValidator = new RngDeviceValidator();
    private GuestNicConfigurationValidator guestNicConfigurationValidator = new GuestNicConfigurationValidator();

    @Override
    public void validateEnums(VM vm) {
        if (vm.isSetType()) {
            validateEnum(VmType.class, vm.getType(), true);
        }
        if (vm.isSetUsb()) {
            usbValidator.validateEnums(vm.getUsb());
        }
        if (vm.isSetOs()) {
            osValidator.validateEnums(vm.getOs());
        }
        if (vm.isSetDisplay()) {
            displayValidator.validateEnums(vm.getDisplay());
        }
        if (vm.isSetPlacementPolicy()) {
            placementPolicyValidator.validateEnums(vm.getPlacementPolicy());
        }
        if (vm.isSetCpu()) {
            cpuValidator.validateEnums(vm.getCpu());
        }
        if (vm.isSetPayloads()) {
            for (Payload payload : vm.getPayloads().getPayload()) {
                payloadValidator.validateEnums(payload);
            }
        }
        if (vm.isSetInitialization() && vm.getInitialization().isSetConfiguration()) {
            configurationValidator.validateEnums(vm.getInitialization().getConfiguration());
        }
        if (vm.isSetInitialization()) {
            if (vm.getInitialization().isSetCloudInit()) {
                cloudInitValidator.validateEnums(vm.getInitialization().getCloudInit());
            }
        }
        if (vm.isSetRngDevice()) {
            rngDeviceValidator.validateEnums(vm.getRngDevice());
        }
        if (vm.isSetInitialization() && vm.getInitialization().isSetNicConfigurations()) {
            for (GuestNicConfiguration nic : vm.getInitialization().getNicConfigurations().getNicConfigurations()) {
                if (nic.isSetBootProtocol()) {
                    guestNicConfigurationValidator.validateEnums(nic);
                }
            }
        }
    }
}
