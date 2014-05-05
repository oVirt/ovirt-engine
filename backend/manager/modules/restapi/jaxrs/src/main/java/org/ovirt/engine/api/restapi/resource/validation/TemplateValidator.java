package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmType;

@ValidatedClass(clazz = Template.class)
public class TemplateValidator implements Validator<Template> {

    private UsbValidator usbValidator = new UsbValidator();
    private OsValidator osValidator = new OsValidator();
    private DisplayValidator displayValidator = new DisplayValidator();
    private CPUValidator cpuValidator = new CPUValidator();
    private RngDeviceValidator rngDeviceValidator = new RngDeviceValidator();

    @Override
    public void validateEnums(Template template) {
        if (template.isSetType()) {
            validateEnum(VmType.class, template.getType(), true);
        }
        if (template.isSetUsb()) {
            usbValidator.validateEnums(template.getUsb());
        }
        if (template.isSetOs()) {
            osValidator.validateEnums(template.getOs());
        }
        if (template.isSetDisplay()) {
            displayValidator.validateEnums(template.getDisplay());
        }
        if (template.isSetCpu()) {
            cpuValidator.validateEnums(template.getCpu());
        }
        if (template.isSetRngDevice()) {
            rngDeviceValidator.validateEnums(template.getRngDevice());
        }
    }

}
