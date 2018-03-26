package org.ovirt.engine.core.common.businessentities;

/**
 * https://libvirt.org/formatdomain.html#elementsControllers
 *
 * <p>
 *     ohci ~ usb1.1 <br>
 *     uhci ~ usb1.x <br>
 *     ehci ~ usb2.0 <br>
 *     xhci ~ usb3.1 <br>
 * </p>
 */
public enum UsbControllerModel {

    /**
     * Default, used if no controllers if specified.
     * https://www.redhat.com/archives/libvir-list/2011-August/msg00816.html
     */
    PIIX3_UHCI("piix3-uhci"),
    PIIX4_UHCI("piix4-uhci"),
    EHCI("ehci"),
    ICH9_EHCI1("ich9-ehci1"),
    ICH9_UHCI1("ich9-uhci1"),
    ICH9_UHCI2("ich9-uhci2"),
    ICH9_UHCI3("ich9-uhci3"),
    VT82C686B_UHCI("vt82c686b-uhci"),
    PCI_OHCI("pci-ohci"),
    NEC_XHCI("nec-xhci"),
    QEMU_XHCI("qemu-xhci"),
    /**
     * xen pvusb with qemu backend, version 1.1
     */
    QUSB1("qusb1"),
    /**
     * xen pvusb with qemu backend, version 2.0
     */
    QUSB2("qusb2"),
    NONE("none");

    public final String libvirtName;

    UsbControllerModel(String libvirtName) {
        this.libvirtName = libvirtName;
    }

    public static UsbControllerModel fromLibvirtName(String libvirtName) {
        for (UsbControllerModel usbControllerModel : values()) {
            if (usbControllerModel.libvirtName.equals(libvirtName)) {
                return usbControllerModel;
            }
        }
        throw new RuntimeException("Unknown libvirt name '" + libvirtName + "'");
    }
}
