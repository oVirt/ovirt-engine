#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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
Local state directory upgrade cleanups plugin
"""

import os
import shutil
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil


@util.export
class Plugin(plugin.PluginBase):
    """
    Local state directory upgrade cleanups plugin.
    Previous versions mixed root/ovirt ownership in local state directories
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        uid = osetuputil.getUid(
            self.environment[osetupcons.SystemEnv.USER_ENGINE]
        )
        gid = osetuputil.getGid(
            self.environment[osetupcons.SystemEnv.GROUP_ENGINE]
        )
        if os.path.exists(osetupcons.FileLocations.OVIRT_ENGINE_TMP_DIR):
            # clean the directory only if it contains at least one file
            # not owned by engine
            rm_tmp_dir = False
            for root, dirs, files in os.walk(
                top=osetupcons.FileLocations.OVIRT_ENGINE_TMP_DIR,
                followlinks=False,
            ):
                for name in dirs + files:
                    if os.stat(os.path.join(root, name)).st_uid != uid:
                        rm_tmp_dir = True
                        break
                if rm_tmp_dir:
                    break
            if rm_tmp_dir:
                self.logger.debug(
                    'Cleaning {tmpdir}'.format(
                        tmpdir=osetupcons.FileLocations.OVIRT_ENGINE_TMP_DIR,
                    )
                )
                shutil.rmtree(osetupcons.FileLocations.OVIRT_ENGINE_TMP_DIR)

        for root, dirs, files in os.walk(
            top=osetupcons.FileLocations.OVIRT_ENGINE_DEPLOYMENTS_DIR,
            followlinks=False,
        ):
            os.chown(root, uid, gid)
            for name in dirs + files:
                os.chown(os.path.join(root, name), uid, gid)


# vim: expandtab tabstop=4 shiftwidth=4
