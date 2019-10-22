#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Process firewalld services
Parse the result
"""

import logging
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
        self._logger = logging.getLogger(__name__)

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
                abs_path = service.get('absolute_path')
                directory = service.get('directory')
                name = service['name']

                if abs_path:
                    template_path = abs_path
                    if directory:
                        self._logger.debug(
                            'both absolute_path and directory provided for %s,'
                            ' using absolute_path' % (name,)
                        )
                else:
                    template_path = os.path.join(
                        osetupcons.FileLocations.OVIRT_FIREWALLD_CONFIG,
                        directory,
                        '%s.xml.in' % name,
                    )

                self.environment[
                    otopicons.NetEnv.FIREWALLD_SERVICE_PREFIX +
                    name
                ] = outil.processTemplate(
                    template=template_path,
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
