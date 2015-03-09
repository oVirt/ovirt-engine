#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""
Process firewalld services
Parse the result
"""

import os
import libxml2

from otopi import constants as otopicons
from otopi import util
from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons


@util.export
class Process(object):

    _instance = None

    def __init__(self, environment):
        self._processed = False
        self._environment = environment

    @classmethod
    def getInstance(clz, environment):
        if clz._instance is None:
            clz._instance = Process(environment=environment)
        return clz._instance

    @property
    def environment(self):
        return self._environment

    def process_firewalld_services(self):
        if not self._processed:
            for service in self.environment[
                osetupcons.NetEnv.FIREWALLD_SERVICES
            ]:
                self.environment[
                    otopicons.NetEnv.FIREWALLD_SERVICE_PREFIX +
                    service['name']
                ] = outil.processTemplate(
                    template=os.path.join(
                        osetupcons.FileLocations.OVIRT_FIREWALLD_CONFIG,
                        service['directory'],
                        '%s.xml.in' % service['name'],
                    ),
                    subst=self.environment[osetupcons.NetEnv.FIREWALLD_SUBST],
                )
            self._processed = True

    def parseFirewalld(self, format, portSeparator='-'):
        self.process_firewalld_services()

        ret = ''
        for content in [
            content
            for key, content in self.environment.items()
            if key.startswith(
                otopicons.NetEnv.FIREWALLD_SERVICE_PREFIX
            )
        ]:
            doc = None
            ctx = None
            try:
                doc = libxml2.parseDoc(content)
                ctx = doc.xpathNewContext()
                nodes = ctx.xpathEval("/service/port")
                for node in nodes:
                    ret += format.format(
                        protocol=node.prop('protocol'),
                        port=node.prop('port').replace('-', portSeparator),
                    )
            finally:
                if doc is not None:
                    doc.freeDoc()
                if ctx is not None:
                    ctx.xpathFreeContext()

        return ret


# vim: expandtab tabstop=4 shiftwidth=4
