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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Action;

public class V3ActionInAdapter implements V3Adapter<V3Action, Action> {
    @Override
    public Action adapt(V3Action from) {
        Action to = new Action();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetAsync()) {
            to.setAsync(from.isAsync());
        }
        if (from.isSetBricks()) {
            to.setBricks(adaptIn(from.getBricks()));
        }
        if (from.isSetCertificates()) {
            to.setCertificates(adaptIn(from.getCertificates()));
        }
        if (from.isSetCheckConnectivity()) {
            to.setCheckConnectivity(from.isCheckConnectivity());
        }
        if (from.isSetClone()) {
            to.setClone(from.isClone());
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetConnectivityTimeout()) {
            to.setConnectivityTimeout(from.getConnectivityTimeout());
        }
        if (from.isSetDiscardSnapshots()) {
            to.setDiscardSnapshots(from.isDiscardSnapshots());
        }
        if (from.isSetDisk()) {
            to.setDisk(adaptIn(from.getDisk()));
        }
        if (from.isSetDisks()) {
            to.setDisks(adaptIn(from.getDisks()));
        }
        if (from.isSetExclusive()) {
            to.setExclusive(from.isExclusive());
        }
        if (from.isSetFault()) {
            to.setFault(adaptIn(from.getFault()));
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
            to.setGracePeriod(adaptIn(from.getGracePeriod()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
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
            to.setIscsi(adaptIn(from.getIscsi()));
        }
        if (from.isSetIscsiTargets()) {
            to.setIscsiTargets(new Action.IscsiTargetsList());
            to.getIscsiTargets().getIscsiTargets().addAll(from.getIscsiTargets());
        }
        if (from.isSetJob()) {
            to.setJob(adaptIn(from.getJob()));
        }
        if (from.isSetLogicalUnits()) {
            to.setLogicalUnits(adaptIn(from.getLogicalUnits()));
        }
        if (from.isSetMaintenanceEnabled()) {
            to.setMaintenanceEnabled(from.isMaintenanceEnabled());
        }
        if (from.isSetModifiedBonds()) {
            to.setModifiedBonds(adaptIn(from.getModifiedBonds()));
        }
        if (from.isSetModifiedLabels()) {
            to.setModifiedLabels(adaptIn(from.getModifiedLabels()));
        }
        if (from.isSetModifiedNetworkAttachments()) {
            to.setModifiedNetworkAttachments(adaptIn(from.getModifiedNetworkAttachments()));
        }
        if (from.isSetOption()) {
            to.setOption(adaptIn(from.getOption()));
        }
        if (from.isSetPause()) {
            to.setPause(from.isPause());
        }
        if (from.isSetPowerManagement()) {
            to.setPowerManagement(adaptIn(from.getPowerManagement()));
        }
        if (from.isSetProxyTicket()) {
            to.setProxyTicket(adaptIn(from.getProxyTicket()));
        }
        if (from.isSetReason()) {
            to.setReason(from.getReason());
        }
        if (from.isSetRemovedBonds()) {
            to.setRemovedBonds(adaptIn(from.getRemovedBonds()));
        }
        if (from.isSetRemovedLabels()) {
            to.setRemovedLabels(adaptIn(from.getRemovedLabels()));
        }
        if (from.isSetRemovedNetworkAttachments()) {
            to.setRemovedNetworkAttachments(adaptIn(from.getRemovedNetworkAttachments()));
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
            to.setSnapshot(adaptIn(from.getSnapshot()));
        }
        if (from.isSetSsh()) {
            to.setSsh(adaptIn(from.getSsh()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(from.getStatus().getState());
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptIn(from.getStorageDomain()));
        }
        if (from.isSetStorageDomains()) {
            to.setStorageDomains(adaptIn(from.getStorageDomains()));
        }
        if (from.isSetSucceeded()) {
            to.setSucceeded(from.isSucceeded());
        }
        if (from.isSetSynchronizedNetworkAttachments()) {
            to.setSynchronizedNetworkAttachments(adaptIn(from.getSynchronizedNetworkAttachments()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetTicket()) {
            to.setTicket(adaptIn(from.getTicket()));
        }
        if (from.isSetUseCloudInit()) {
            to.setUseCloudInit(from.isUseCloudInit());
        }
        if (from.isSetUseSysprep()) {
            to.setUseSysprep(from.isUseSysprep());
        }
        if (from.isSetVirtualFunctionsConfiguration()) {
            to.setVirtualFunctionsConfiguration(adaptIn(from.getVirtualFunctionsConfiguration()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
