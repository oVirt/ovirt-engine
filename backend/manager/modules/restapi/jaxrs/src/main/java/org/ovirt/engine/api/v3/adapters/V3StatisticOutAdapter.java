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

import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Statistic;
import org.ovirt.engine.api.v3.types.V3StatisticType;
import org.ovirt.engine.api.v3.types.V3StatisticUnit;
import org.ovirt.engine.api.v3.types.V3ValueType;
import org.ovirt.engine.api.v3.types.V3Values;

public class V3StatisticOutAdapter implements V3Adapter<Statistic, V3Statistic> {
    @Override
    public V3Statistic adapt(Statistic from) {
        V3Statistic to = new V3Statistic();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBrick()) {
            to.setBrick(adaptOut(from.getBrick()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisk()) {
            to.setDisk(adaptOut(from.getDisk()));
        }
        if (from.isSetGlusterVolume()) {
            to.setGlusterVolume(adaptOut(from.getGlusterVolume()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetHostNic()) {
            to.setHostNic(adaptOut(from.getHostNic()));
        }
        if (from.isSetHostNumaNode()) {
            to.setHostNumaNode(adaptOut(from.getHostNumaNode()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetKind()) {
            switch (from.getKind()) {
                case COUNTER:
                    to.setType(V3StatisticType.COUNTER);
                    break;
                case GAUGE:
                    to.setType(V3StatisticType.GAUGE);
                    break;
            }
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNic()) {
            to.setNic(adaptOut(from.getNic()));
        }
        if (from.isSetStep()) {
            to.setStep(adaptOut(from.getStep()));
        }
        if (from.isSetUnit()) {
            switch (from.getUnit()) {
                case BITS_PER_SECOND:
                    to.setUnit(V3StatisticUnit.BITS_PER_SECOND);
                    break;
                case BYTES:
                    to.setUnit(V3StatisticUnit.BYTES);
                    break;
                case BYTES_PER_SECOND:
                    to.setUnit(V3StatisticUnit.BYTES_PER_SECOND);
                    break;
                case COUNT_PER_SECOND:
                    to.setUnit(V3StatisticUnit.COUNT_PER_SECOND);
                    break;
                case NONE:
                    to.setUnit(V3StatisticUnit.NONE);
                    break;
                case PERCENT:
                    to.setUnit(V3StatisticUnit.PERCENT);
                    break;
                case SECONDS:
                    to.setUnit(V3StatisticUnit.SECONDS);
                    break;
            }
        }
        if (from.isSetValues()) {
            V3Values toValues = new V3Values();
            toValues.getValues().addAll(adaptOut(from.getValues().getValues()));
            if (from.isSetType()) {
                switch (from.getType()) {
                case DECIMAL:
                    toValues.setType(V3ValueType.DECIMAL);
                    break;
                case INTEGER:
                    toValues.setType(V3ValueType.INTEGER);
                    break;
                case STRING:
                    toValues.setType(V3ValueType.STRING);
                    break;
                }
            }
            to.setValues(toValues);
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
