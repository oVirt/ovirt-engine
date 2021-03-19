package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;

public class FullEntityOvfData implements Serializable {
    private VM vm;
    private VmTemplate vmTemplate;
    private List<DiskImage> diskImages = new ArrayList<>();
    private List<LunDisk> lunDisks = new ArrayList<>();
    private List<VmNetworkInterface> interfaces = new ArrayList<>();
    private VmBase vmBase = new VmBase();
    private String clusterName = "";
    private List<AffinityGroup> affinityGroups = new ArrayList<>();
    private Set<DbUser> dbUsers = new HashSet<>();
    private Map<String, Set<String>> userToRoles = new HashMap<>();
    private List<Label> affinityLabelsNames = new ArrayList<>();
    private Map<VmExternalDataKind, String> vmExternalData = new EnumMap<>(VmExternalDataKind.class);

    public FullEntityOvfData() {
    }

    public FullEntityOvfData(VM vm) {
        this.setVm(vm);
        this.setVmBase(vm.getStaticData());
        this.setClusterName(vm.getClusterName());
    }

    public FullEntityOvfData(VmTemplate vmTemplate) {
        this.setVmTemplate(vmTemplate);
        this.setVmBase(vmTemplate);
        this.setClusterName(vmTemplate.getClusterName());
    }

    public FullEntityOvfData(List<DiskImage> diskImages,
            List<LunDisk> lunDisks,
            List<VmNetworkInterface> interfaces,
            VmBase vmBase,
            String clusterName,
            List<AffinityGroup> affinityGroups,
            Set<DbUser> dbUsers,
            Map<String, Set<String>> userToRoles,
            List<Label> affinityLabelsNames) {
        this.diskImages = diskImages;
        this.lunDisks = lunDisks;
        this.interfaces = interfaces;
        this.vmBase = vmBase;
        this.clusterName = clusterName;
        this.affinityGroups = affinityGroups;
        this.dbUsers = dbUsers;
        this.userToRoles = userToRoles;
        this.affinityLabelsNames = affinityLabelsNames;
    }

    public VmBase getVmBase() {
        return vmBase;
    }

    public void setVmBase(VmBase vmBase) {
        this.vmBase = vmBase;
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public VmTemplate getVmTemplate() {
        return vmTemplate;
    }

    public void setVmTemplate(VmTemplate vmTemplate) {
        this.vmTemplate = vmTemplate;
    }

    public List<DiskImage> getDiskImages() {
        return diskImages;
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        this.diskImages = diskImages;
    }

    public List<LunDisk> getLunDisks() {
        return lunDisks;
    }

    public void setLunDisks(List<LunDisk> lunDisks) {
        this.lunDisks = lunDisks;
    }

    public List<VmNetworkInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<VmNetworkInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<AffinityGroup> getAffinityGroups() {
        return affinityGroups;
    }

    public void setAffinityGroups(List<AffinityGroup> affinityGroups) {
        this.affinityGroups = affinityGroups;
    }

    public Set<DbUser> getDbUsers() {
        return dbUsers;
    }

    public void setDbUsers(Set<DbUser> dbUsers) {
        this.dbUsers = dbUsers;
    }

    public Map<String, Set<String>> getUserToRoles() {
        return userToRoles;
    }

    public void setUserToRoles(Map<String, Set<String>> userToRoles) {
        this.userToRoles = userToRoles;
    }

    public List<Label> getAffinityLabels() {
        return affinityLabelsNames;
    }

    public void setAffinityLabels(List<Label> affinityLabelsNames) {
        this.affinityLabelsNames = affinityLabelsNames;
    }

    public Map<VmExternalDataKind, String> getVmExternalData() {
        return vmExternalData;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FullEntityOvfData)) {
            return false;
        }
        FullEntityOvfData other = (FullEntityOvfData) obj;
        return super.equals(obj)
                && Objects.equals(diskImages, other.diskImages)
                && Objects.equals(lunDisks, other.lunDisks)
                && Objects.equals(interfaces, other.interfaces)
                && Objects.equals(vmBase, other.vmBase)
                && Objects.equals(clusterName, other.clusterName)
                && Objects.equals(affinityGroups, other.affinityGroups)
                && Objects.equals(dbUsers, other.dbUsers)
                && Objects.equals(userToRoles, other.userToRoles)
                && Objects.equals(affinityLabelsNames, other.affinityLabelsNames)
                && Objects.equals(vmExternalData, other.vmExternalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                diskImages,
                lunDisks,
                interfaces,
                vmBase,
                clusterName,
                affinityGroups,
                dbUsers,
                userToRoles,
                affinityLabelsNames,
                vmExternalData);
    }
}
