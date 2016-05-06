/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3ActionOutAdapter implements V3Adapter<Action, V3Action> {
    @Override
    public V3Action adapt(Action from) {
        V3Action to = new V3Action();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetAsync()) {
            to.setAsync(from.isAsync());
        }
        if (from.isSetBricks()) {
            to.setBricks(adaptOut(from.getBricks()));
        }
        if (from.isSetCertificates()) {
            to.setCertificates(adaptOut(from.getCertificates()));
        }
        if (from.isSetCheckConnectivity()) {
            to.setCheckConnectivity(from.isCheckConnectivity());
        }
        if (from.isSetClone()) {
            to.setClone(from.isClone());
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
        }
        if (from.isSetConnectivityTimeout()) {
            to.setConnectivityTimeout(from.getConnectivityTimeout());
        }
        if (from.isSetDiscardSnapshots()) {
            to.setDiscardSnapshots(from.isDiscardSnapshots());
        }
        if (from.isSetDisk()) {
            to.setDisk(adaptOut(from.getDisk()));
        }
        if (from.isSetDisks()) {
            to.setDisks(adaptOut(from.getDisks()));
        }
        if (from.isSetExclusive()) {
            to.setExclusive(from.isExclusive());
        }
        if (from.isSetFault()) {
            to.setFault(adaptOut(from.getFault()));
        }
        if (from.isSetFenceType()) {
            to.setFenceType(from.getFenceType());
        }
        if (from.isSetFixLayout()) {
            to.setFixLayout(from.isFixLayout());
        }
        if (from.isSetForce()) {
            to.setForce(from.isForce());
        }
        if (from.isSetGracePeriod()) {
            to.setGracePeriod(adaptOut(from.getGracePeriod()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetImage()) {
            to.setImage(from.getImage());
        }
        if (from.isSetImportAsTemplate()) {
            to.setImportAsTemplate(from.isImportAsTemplate());
        }
        if (from.isSetIsAttached()) {
            to.setIsAttached(from.isIsAttached());
        }
        if (from.isSetIscsi()) {
            to.setIscsi(adaptOut(from.getIscsi()));
        }
        if (from.isSetIscsiTargets()) {
            to.getIscsiTargets().addAll(from.getIscsiTargets().getIscsiTargets());
        }
        if (from.isSetJob()) {
            to.setJob(adaptOut(from.getJob()));
        }
        if (from.isSetLogicalUnits()) {
            to.setLogicalUnits(adaptOut(from.getLogicalUnits()));
        }
        if (from.isSetMaintenanceEnabled()) {
            to.setMaintenanceEnabled(from.isMaintenanceEnabled());
        }
        if (from.isSetModifiedBonds()) {
            to.setModifiedBonds(adaptOut(from.getModifiedBonds()));
        }
        if (from.isSetModifiedLabels()) {
            to.setModifiedLabels(adaptOut(from.getModifiedLabels()));
        }
        if (from.isSetModifiedNetworkAttachments()) {
            to.setModifiedNetworkAttachments(adaptOut(from.getModifiedNetworkAttachments()));
        }
        if (from.isSetOption()) {
            to.setOption(adaptOut(from.getOption()));
        }
        if (from.isSetPause()) {
            to.setPause(from.isPause());
        }
        if (from.isSetPowerManagement()) {
            to.setPowerManagement(adaptOut(from.getPowerManagement()));
        }
        if (from.isSetProxyTicket()) {
            to.setProxyTicket(adaptOut(from.getProxyTicket()));
        }
        if (from.isSetReason()) {
            to.setReason(from.getReason());
        }
        if (from.isSetRemovedBonds()) {
            to.setRemovedBonds(adaptOut(from.getRemovedBonds()));
        }
        if (from.isSetRemovedLabels()) {
            to.setRemovedLabels(adaptOut(from.getRemovedLabels()));
        }
        if (from.isSetRemovedNetworkAttachments()) {
            to.setRemovedNetworkAttachments(adaptOut(from.getRemovedNetworkAttachments()));
        }
        if (from.isSetResolutionType()) {
            to.setResolutionType(from.getResolutionType());
        }
        if (from.isSetRestoreMemory()) {
            to.setRestoreMemory(from.isRestoreMemory());
        }
        if (from.isSetRootPassword()) {
            to.setRootPassword(from.getRootPassword());
        }
        if (from.isSetSnapshot()) {
            to.setSnapshot(adaptOut(from.getSnapshot()));
        }
        if (from.isSetSsh()) {
            to.setSsh(adaptOut(from.getSsh()));
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus());
            to.setStatus(status);
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptOut(from.getStorageDomain()));
        }
        if (from.isSetStorageDomains()) {
            to.setStorageDomains(adaptOut(from.getStorageDomains()));
        }
        if (from.isSetSucceeded()) {
            to.setSucceeded(from.isSucceeded());
        }
        if (from.isSetSynchronizedNetworkAttachments()) {
            to.setSynchronizedNetworkAttachments(adaptOut(from.getSynchronizedNetworkAttachments()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetTicket()) {
            to.setTicket(adaptOut(from.getTicket()));
        }
        if (from.isSetUseCloudInit()) {
            to.setUseCloudInit(from.isUseCloudInit());
        }
        if (from.isSetUseSysprep()) {
            to.setUseSysprep(from.isUseSysprep());
        }
        if (from.isSetVirtualFunctionsConfiguration()) {
            to.setVirtualFunctionsConfiguration(adaptOut(from.getVirtualFunctionsConfiguration()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
