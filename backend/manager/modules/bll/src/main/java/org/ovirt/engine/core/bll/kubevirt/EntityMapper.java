package org.ovirt.engine.core.bll.kubevirt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.kubevirt.Units;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1ObjectMeta;
import kubevirt.io.V1CPU;
import kubevirt.io.V1Memory;
import kubevirt.io.V1ResourceRequirements;
import kubevirt.io.V1VirtualMachine;
import openshift.io.V1Template;

public class EntityMapper {

    public static VmStatic toOvirtVm(V1VirtualMachine source, Guid clusterId) {
        VmStatic vmStatic = new VmStatic();
        mapGenericData(vmStatic, source, clusterId);
        mapMemory(vmStatic, source);
        mapCPU(vmStatic, source);
        mapDisplayType(vmStatic, source);
        return vmStatic;
    }

    public static VmStatic toOvirtVm(V1Template template, Guid clusterId) {
        LinkedTreeMap<String, ?> vmAsMap = template.getVms().get(0);
        LinkedTreeMap<String, Object> metadata = (LinkedTreeMap) vmAsMap.get("metadata");
        // the following metadata fields have to be set to pass serialization
        metadata.put("name", "");
        metadata.put("uid", "");
        Gson gson = new Gson();
        JsonObject o = gson.toJsonTree(vmAsMap).getAsJsonObject();
        V1VirtualMachine vm = gson.fromJson(o, V1VirtualMachine.class);
        vm.getMetadata().setCreationTimestamp(template.getMetadata().getCreationTimestamp());

        VmStatic vmStatic = toOvirtVm(vm, clusterId);
        vmStatic.setPredefinedProperties(new JSON().serialize(vm));
        return vmStatic;
    }

    public static V1VirtualMachine toKubevirtVm(VmTemplate template, VmStatic vm, PVCDisk rootDisk) {
        String templateConf = template.getPredefinedProperties();
        UnaryOperator<String> escape = str -> String.format("\\$\\{%s\\}", str);
        String conf = templateConf
                .replaceAll(escape.apply("NAME"), vm.getName())
                .replaceAll(escape.apply("PVCNAME"), rootDisk.getName());
        V1VirtualMachine result = new JSON().deserialize(conf, V1VirtualMachine.class);
        Map<String, String> annotations = new HashMap<>();
        annotations.put(KubevirtUtils.DESCRIPTION_ANNOTATION, vm.getDescription());
        annotations.put(KubevirtUtils.COMMENT_ANNOTATION, vm.getComment());
        result.getMetadata().setAnnotations(annotations);
        result.getMetadata().setNamespace(rootDisk.getNamespace());
        result.getMetadata().setName(vm.getName());

        V1CPU cpuTopology = result.getSpec().getTemplate().getSpec().getDomain().getCpu();
        if (cpuTopology == null) {
            cpuTopology = new V1CPU();
        }
        cpuTopology.setSockets(vm.getNumOfSockets());
        cpuTopology.setCores(vm.getCpuPerSocket());
        cpuTopology.setThreads(vm.getThreadsPerCpu());
        result.getSpec().getTemplate().getSpec().getDomain().setCpu(cpuTopology);

        V1Memory memory = result.getSpec().getTemplate().getSpec().getDomain().getMemory();
        if (memory == null) {
            memory = new V1Memory();
        }
        memory.setGuest(vm.getMemSizeMb() + "M");
        result.getSpec().getTemplate().getSpec().getDomain().setMemory(memory);

        V1ResourceRequirements resources = result.getSpec().getTemplate().getSpec().getDomain().getResources();
        if (resources == null) {
            resources = new V1ResourceRequirements();
        }
        Map<String, String> requests = resources.getRequests();
        if (requests == null) {
            requests = new HashMap<>();
        }
        requests.put("memory", vm.getMinAllocatedMem() + "M");
        resources.setRequests(requests);
        Map<String, String> limits = resources.getLimits();
        if (limits == null) {
            limits = new HashMap<>();
        }
        limits.put("memory", vm.getMaxMemorySizeMb() + "M");
        resources.setLimits(limits);
        result.getSpec().getTemplate().getSpec().getDomain().setResources(resources);

        return result;
    }

    private static void mapGenericData(VmStatic vmStatic, V1VirtualMachine source, Guid clusterId) {
        V1ObjectMeta metadata = source.getMetadata();

        vmStatic.setId(new Guid(metadata.getUid()));
        vmStatic.setName(metadata.getName());
        vmStatic.setCreationDate(metadata.getCreationTimestamp().toDate());
        vmStatic.setClusterId(clusterId);
        // for kubevirt, cluster id and provider id are equals
        vmStatic.setProviderId(clusterId);
        vmStatic.setOrigin(OriginType.KUBEVIRT);
        vmStatic.setOriginalTemplateName(source.getSpec().getTemplate().getMetadata().getName());
        vmStatic.setNamespace(metadata.getNamespace());


        Map<String, String> annotations = metadata.getAnnotations();
        if (annotations != null) {
            String description = annotations.get(KubevirtUtils.DESCRIPTION_ANNOTATION);
            if (description != null) {
                vmStatic.setDescription(description);
            }
            String comment = annotations.get(KubevirtUtils.COMMENT_ANNOTATION);
            if (comment != null) {
                vmStatic.setComment(comment);
            }
        }
    }

    private static void mapMemory(VmStatic vmStatic, V1VirtualMachine source) {
        Integer memoryRequest = null;
        Integer memoryLimit = null;
        V1ResourceRequirements resources = source.getSpec().getTemplate().getSpec().getDomain().getResources();
        if (resources != null) {
            Map<String, String> requests = resources.getRequests();
            if (requests != null) {
                memoryRequest = Units.parse(requests.get("memory"));
            }
            Map<String, String> limits = resources.getLimits();
            if (limits != null) {
                memoryLimit = Units.parse(limits.get("memory"));
            }
        }

        V1Memory memory = source.getSpec().getTemplate().getSpec().getDomain().getMemory();
        Integer guestMemory = memory != null ? Units.parse(memory.getGuest()) : null;

        vmStatic.setMemSizeMb(Objects.requireNonNullElse(guestMemory, memoryRequest));
        vmStatic.setMinAllocatedMem(Objects.requireNonNullElse(memoryRequest, guestMemory));
        vmStatic.setMaxMemorySizeMb(Objects.requireNonNullElse(memoryLimit, vmStatic.getMemSizeMb()));
        // TODO: when only memory limits are defined
    }

    private static void mapCPU(VmStatic vmStatic, V1VirtualMachine source) {
        V1CPU cpuTopology = source.getSpec().getTemplate().getSpec().getDomain().getCpu();
        if (cpuTopology != null) {
            Integer value;
            if ((value = cpuTopology.getSockets()) != null) {
                vmStatic.setNumOfSockets(value);
            }
            if ((value = cpuTopology.getCores()) != null) {
                vmStatic.setCpuPerSocket(value);
            }
            if ((value = cpuTopology.getThreads()) != null) {
                vmStatic.setThreadsPerCpu(value);
            }
            vmStatic.setCustomCpuName(cpuTopology.getModel());
        }
        // TODO if CPU topology is not defined, it should be set according to resource requests/limits
    }

    private static void mapDisplayType(VmStatic vmStatic, V1VirtualMachine source) {
        vmStatic.setDefaultDisplayType(DisplayType.none);
        if (!Boolean.FALSE.equals(source.getSpec()
                .getTemplate()
                .getSpec()
                .getDomain()
                .getDevices()
                .isAutoattachGraphicsDevice())) {
            vmStatic.setDefaultDisplayType(DisplayType.vga);
        }
    }
}
