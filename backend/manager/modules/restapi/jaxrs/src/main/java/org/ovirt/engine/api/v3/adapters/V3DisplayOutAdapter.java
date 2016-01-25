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

import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Display;

public class V3DisplayOutAdapter implements V3Adapter<Display, V3Display> {
    @Override
    public V3Display adapt(Display from) {
        V3Display to = new V3Display();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetAllowOverride()) {
            to.setAllowOverride(from.isAllowOverride());
        }
        if (from.isSetCertificate()) {
            to.setCertificate(adaptOut(from.getCertificate()));
        }
        if (from.isSetCopyPasteEnabled()) {
            to.setCopyPasteEnabled(from.isCopyPasteEnabled());
        }
        if (from.isSetDisconnectAction()) {
            to.setDisconnectAction(from.getDisconnectAction());
        }
        if (from.isSetFileTransferEnabled()) {
            to.setFileTransferEnabled(from.isFileTransferEnabled());
        }
        if (from.isSetKeyboardLayout()) {
            to.setKeyboardLayout(from.getKeyboardLayout());
        }
        if (from.isSetMonitors()) {
            to.setMonitors(from.getMonitors());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetProxy()) {
            to.setProxy(from.getProxy());
        }
        if (from.isSetSecurePort()) {
            to.setSecurePort(from.getSecurePort());
        }
        if (from.isSetSingleQxlPci()) {
            to.setSingleQxlPci(from.isSingleQxlPci());
        }
        if (from.isSetSmartcardEnabled()) {
            to.setSmartcardEnabled(from.isSmartcardEnabled());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
