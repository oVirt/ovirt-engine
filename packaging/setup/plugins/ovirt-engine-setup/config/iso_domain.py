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
ISO domain configuration plugin.
"""

import datetime
import gettext
import hashlib
import os
import uuid
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import domains as osetupdomains


@util.export
class Plugin(plugin.PluginBase):
    """
    ISO domain configuration plugin.
    """

    SHA_CKSUM_TAG = '_SHA_CKSUM'
    DEFAULT_MD = {
        'CLASS': 'Iso',
        'DESCRIPTION': 'isofun',
        'IOOPTIMEOUTSEC': 1,
        'LEASERETRIES': 3,
        'LEASETIMESEC': 5,
        'LOCKPOLICY': '',
        'LOCKRENEWALINTERVALSEC': 5,
        'POOL_UUID': '',
        'REMOTE_PATH': 'no.one.reads.this:/rhev',
        'ROLE': 'Regular',
        'SDUUID': '',
        'TYPE': 'NFS',
        'VERSION': 0,
    }

    def _generate_md_content(self, sdUUID, description):
        self.logger.debug('Generating ISO Domain metadata')
        md = self.DEFAULT_MD.copy()
        md['SDUUID'] = sdUUID,
        md['DESCRIPTION'] = description

        lines = ['%s=%s' % (key, md[key]) for key in sorted(md.keys())]
        checksum = hashlib.sha1()
        checksum.update(''.join(lines).encode('ascii', 'xmlcharrefreplace'))
        lines.append('%s=%s' % (self.SHA_CKSUM_TAG, checksum.hexdigest()))
        return lines

    def _get_domain(self, path):
        """
        Ensure that the path contains a valid domain
        """
        sd_uuid = None
        if os.path.exists(path):
            directory_content = []
            if not os.path.isdir(path):
                raise RuntimeError(
                    _('Error: directory {path} is not empty').format(
                        path=path,
                    )
                )

            directory_content = os.listdir(path)
            if directory_content:
                if len(directory_content) == 1:
                    entry = directory_content[0]
                    if(
                        os.path.isdir(os.path.join(path, entry)) and
                        uuid.UUID(entry).version == 4
                    ):
                        self.logger.debug('Using existing uuid for ISO domain')
                        sd_uuid = entry
                    else:
                        raise RuntimeError(
                            _('Error: directory {path} is not empty').format(
                                path=path,
                            )
                        )
                else:
                    raise RuntimeError(
                        _('Error: directory {path} is not empty').format(
                            path=path,
                        )
                    )

        return sd_uuid

    def _prepare_new_domain(self, path):
        self.logger.debug('Generating a new uuid for ISO domain')
        sdUUID = str(uuid.uuid4())
        description = self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_NAME
        ]
        self.logger.debug(
            'Creating ISO domain for {path}. uuid: {uuid}'.format(
                path=path,
                uuid=sdUUID
            )
        )
        #Create images directory tree
        basePath = os.path.join(path, sdUUID)
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=os.path.join(
                    basePath,
                    'images',
                    osetupcons.Const.ISO_DOMAIN_IMAGE_UID,
                    '.keep',
                ),
                content='',
                mode=0o644,
                dmode=0o755,
                owner=self.environment[osetupcons.SystemEnv.USER_VDSM],
                group=self.environment[osetupcons.SystemEnv.GROUP_KVM],
                downer=self.environment[
                    osetupcons.SystemEnv.USER_VDSM
                ],
                dgroup=self.environment[osetupcons.SystemEnv.GROUP_KVM],
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        #Create dom_md directory tree
        domMdDir = os.path.join(basePath, 'dom_md')
        for name in ('ids', 'inbox', 'leases', 'outbox'):
            filename = os.path.join(domMdDir, name)
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=filename,
                    content='',
                    mode=0o644,
                    dmode=0o755,
                    owner=self.environment[osetupcons.SystemEnv.USER_VDSM],
                    group=self.environment[osetupcons.SystemEnv.GROUP_KVM],
                    downer=self.environment[
                        osetupcons.SystemEnv.USER_VDSM
                    ],
                    dgroup=self.environment[osetupcons.SystemEnv.GROUP_KVM],
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )
            self.environment[
                osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
            ].append(filename)
        metadata = os.path.join(domMdDir, 'metadata')
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=metadata,
                mode=0o755,
                dmode=0o755,
                owner=self.environment[osetupcons.SystemEnv.USER_VDSM],
                group=self.environment[osetupcons.SystemEnv.GROUP_KVM],
                downer=self.environment[osetupcons.SystemEnv.USER_VDSM],
                dgroup=self.environment[osetupcons.SystemEnv.GROUP_KVM],
                content=self._generate_md_content(sdUUID, description),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(metadata)

        return sdUUID

    def _validateAndGetDomain(self, path):
        self._checker.check_valid_path(path)
        self._checker.check_base_writable(path)
        self._checker.check_available_space(
            path,
            osetupcons.Const.MINIMUM_SPACE_ISODOMAIN_MB
        )
        return self._get_domain(path)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.ISO_DOMAIN_NAME,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT,
            osetupcons.FileLocations.ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT
        )

        self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR
        ] = None

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._checker = osetupdomains.DomainChecker()

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=[
            osetupcons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        ],
        condition=lambda self: self.environment[
            osetupcons.SystemEnv.NFS_CONFIG_ENABLED
        ],
    )
    def _customization(self):
        """
        If the user want to use NFS for ISO domain ask how to configure it.
        """
        interactive = self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
        ]

        validDomain = False
        while not validDomain:
            try:
                default_mount_point = self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT
                ]
                if os.path.exists(default_mount_point):
                    default_mount_point += '-%s' % (
                        datetime.datetime.utcnow().strftime('%Y%m%d%H%M%S')
                    )

                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ] = self.dialog.queryString(
                    name='NFS_MOUNT_POINT',
                    note=_('Local ISO domain path [@DEFAULT@]: '),
                    prompt=True,
                    caseSensitive=True,
                    default=default_mount_point,
                )

                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
                ] = self._validateAndGetDomain(
                    path=self.environment[
                        osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ]
                )

                validDomain = True

            except (ValueError, RuntimeError) as e:
                if interactive:
                    self.logger.error(
                        _(
                            'Cannot access mount point '
                            '{mountPoint}: {error}'
                        ).format(
                            mountPoint=self.environment[
                                osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                            ],
                            error=e,
                        )
                    )
                else:
                    raise

        path = self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
        ].rstrip('/')
        self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS].append({
            'type': 'public_content_rw_t',
            'pattern': '%s(/.*)?' % path,
        })
        self.environment[
            osetupcons.SystemEnv.SELINUX_RESTORE_PATHS
        ].append(path)

        if self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_NAME
        ] is None:
            self.environment[
                osetupcons.ConfigEnv.ISO_DOMAIN_NAME
            ] = self.dialog.queryString(
                name='ISO_DOMAIN_NAME',
                note=_('Local ISO domain name [@DEFAULT@]: '),
                prompt=True,
                caseSensitive=True,
                default=osetupcons.Defaults.DEFAULT_ISO_DOMAIN_NAME,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.CONFIG_ISO_DOMAIN_AVAILABLE,
        after=[
            osetupcons.Stages.DB_CONNECTION_AVAILABLE,
        ],
        condition=lambda self: self.environment[
            osetupcons.SystemEnv.NFS_CONFIG_ENABLED
        ],
    )
    def _add_iso_domain_to_db(self):
        """
        Add iso domain to DB
        """
        if self.environment[osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID] is None:
            self.environment[
                osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
            ] = self._prepare_new_domain(
                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ]
            )
        self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR
        ] = os.path.join(
            self.environment[
                osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
            ],
            self.environment[
                osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
            ],
            'images',
            osetupcons.Const.ISO_DOMAIN_IMAGE_UID,
        )
        self.logger.debug('Adding ISO domain into DB')
        self.environment[osetupcons.DBEnv.STATEMENT].execute(
            statement="""
                select inst_add_iso_storage_domain(
                    %(storage_domain_id)s,
                    %(name)s,
                    %(connection_id)s,
                    %(connection)s,
                    %(available)s,
                    %(used)s
                )
            """,
            args=dict(
                storage_domain_id=self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
                ],
                name=self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_NAME
                ],
                connection_id=str(uuid.uuid4()),
                connection='%s:%s' % (
                    self.environment[osetupcons.ConfigEnv.FQDN],
                    self.environment[
                        osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ]
                ),
                available=0,
                used=0,
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
