package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.NicConfiguration;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmType;

@ValidatedClass(clazz = Vm.class)
public class VmValidator implements Validator<Vm> {

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
    private MigrationOptionsValidator migrationOptionsValidator = new MigrationOptionsValidator();

    @Override
    public void validateEnums(Vm vm) {
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
            for (Payload payload : vm.getPayloads().getPayloads()) {
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
            for (NicConfiguration nic : vm.getInitialization().getNicConfigurations().getNicConfigurations()) {
                if (nic.isSetBootProtocol()) {
                    guestNicConfigurationValidator.validateEnums(nic);
                }
            }
        }
        if (vm.isSetMigration()) {
            migrationOptionsValidator.validateEnums(vm.getMigration());
        }
    }
}
