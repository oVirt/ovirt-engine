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

import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticKind;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.model.Values;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Statistic;
import org.ovirt.engine.api.v3.types.V3Values;

public class V3StatisticInAdapter implements V3Adapter<V3Statistic, Statistic> {
    @Override
    public Statistic adapt(V3Statistic from) {
        Statistic to = new Statistic();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBrick()) {
            to.setBrick(adaptIn(from.getBrick()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisk()) {
            to.setDisk(adaptIn(from.getDisk()));
        }
        if (from.isSetGlusterVolume()) {
            to.setGlusterVolume(adaptIn(from.getGlusterVolume()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetHostNic()) {
            to.setHostNic(adaptIn(from.getHostNic()));
        }
        if (from.isSetHostNumaNode()) {
            to.setHostNumaNode(adaptIn(from.getHostNumaNode()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.getType() != null) {
            switch (from.getType()) {
                case COUNTER:
                    to.setKind(StatisticKind.COUNTER);
                    break;
                case GAUGE:
                    to.setKind(StatisticKind.GAUGE);
                    break;
            }
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNic()) {
            to.setNic(adaptIn(from.getNic()));
        }
        if (from.isSetStep()) {
            to.setStep(adaptIn(from.getStep()));
        }
        if (from.isSetUnit()) {
            switch (from.getUnit()) {
                case NONE:
                    to.setUnit(StatisticUnit.NONE);
                    break;
                case PERCENT:
                    to.setUnit(StatisticUnit.PERCENT);
                    break;
                case BYTES:
                    to.setUnit(StatisticUnit.BYTES);
                    break;
                case SECONDS:
                    to.setUnit(StatisticUnit.SECONDS);
                    break;
                case BYTES_PER_SECOND:
                    to.setUnit(StatisticUnit.BYTES_PER_SECOND);
                    break;
                case BITS_PER_SECOND:
                    to.setUnit(StatisticUnit.BITS_PER_SECOND);
                    break;
                case COUNT_PER_SECOND:
                    to.setUnit(StatisticUnit.COUNT_PER_SECOND);
                    break;
            }
        }
        if (from.isSetValues()) {
            V3Values fromValues = from.getValues();
            if (fromValues.isSetType()) {
                switch (fromValues.getType()) {
                    case DECIMAL:
                        to.setType(ValueType.DECIMAL);
                        break;
                    case INTEGER:
                        to.setType(ValueType.INTEGER);
                        break;
                    case STRING:
                        to.setType(ValueType.STRING);
                        break;
                }
            }
            to.setValues(new Values());
            to.getValues().getValues().addAll(adaptIn(fromValues.getValues()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
