package org.ovirt.engine.core.vdsbroker.libvirt;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ovirt.engine.core.common.businessentities.VmDevice;

/**
 * This class represents an XML in the form of:
 * <hotunplug>
 *   <devices>
 *     <disk>
 *       <alias name='mydisk'/>
 *     </disk>
 *   </devices>
 * </hotunplug>
 */
@XmlRootElement
public class Hotunplug {
    private Devices devices;

    public Devices getDevices() {
        return devices;
    }

    @XmlElement
    public Hotunplug setDevices(Devices devices) {
        this.devices = devices;
        return this;
    }

    public static class Devices  {
        private Device nic;
        private Device disk;

        public Device getInterface() {
            return nic;
        }

        @XmlElement
        public Devices setInterface(Device nic) {
            this.nic = nic;
            return this;
        }

        public Device getDisk() {
            return disk;
        }

        @XmlElement
        public Devices setDisk(Device disk) {
            this.disk = disk;
            return this;
        }
    }

    public static class Device {
        private Alias alias;

        public Device(VmDevice device) {
            this.alias = new Alias(device.getAlias());
        }

        public Alias getAlias() {
            return alias;
        }

        @XmlElement
        public void setAlias(Alias alias) {
            this.alias = alias;
        }
    }

    private static class Alias {
        private String name;

        public Alias(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @XmlAttribute
        public void setName(String name) {
            this.name = name;
        }
    }
}
