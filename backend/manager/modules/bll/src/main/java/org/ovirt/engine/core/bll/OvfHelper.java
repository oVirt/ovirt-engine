package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/**
 * OvfHelper is a helper class that encapsulates the bll logic that needs
 * to be done on the ovf read data before using it in the bll project.
 */
public class OvfHelper {
    private OvfManager ovfManager;

    public OvfHelper() {
        ovfManager = new OvfManager();
    }

    /**
     * parses a given ovf to a vm, intialized with images and interfaces.
     * @param ovf
     * @return
     *        VM that represents the given ovf data
     * @throws OvfReaderException
     */
    public VM readVmFromOvf(String ovf) throws OvfReaderException {
        VM vm = new VM();
        ArrayList<DiskImage> diskImages = new ArrayList<DiskImage>();
        ArrayList<VmNetworkInterface> interfaces  = new ArrayList<VmNetworkInterface>();
        ovfManager.ImportVm(ovf, vm, diskImages, interfaces);

        // add images
        vm.setImages(diskImages);
        // add interfaces
        vm.setInterfaces(interfaces);

        // add disk map
        Map<Guid, List<DiskImage>> images = ImagesHandler
                .getImagesLeaf(diskImages);
        for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
            List<DiskImage> list = entry.getValue();
            vm.getDiskMap().put(entry.getKey(), list.get(list.size() - 1));
        }
        return vm;
    }

    public boolean isOvfTemplate(String ovfstring) {
        return ovfManager.IsOvfTemplate(ovfstring);
    }
}
