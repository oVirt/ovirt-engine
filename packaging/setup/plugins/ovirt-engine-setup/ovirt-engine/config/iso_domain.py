#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
ISO domain configuration plugin.
"""

import datetime
import gettext
import hashlib
import os
import re
import uuid

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import domains as osetupdomains
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


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
        'MASTER_VERSION': 0,
    }
    RE_NOT_ALPHANUMERIC = re.compile(r"[^-\w]")

    def _generate_md_content(self, sdUUID, description):
        self.logger.debug('Generating ISO Domain metadata')
        md = self.DEFAULT_MD.copy()
        md['SDUUID'] = sdUUID
        md['DESCRIPTION'] = description
        md['REMOTE_PATH'] = '%s:%s' % (
            self.environment[osetupcons.ConfigEnv.FQDN],
            self.environment[oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT]
        )

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
                    try:
                        if(
                            os.path.isdir(os.path.join(path, entry)) and
                            uuid.UUID(entry).version == 4
                        ):
                            self.logger.debug(
                                'Using existing uuid for ISO domain'
                            )
                            sd_uuid = entry
                        else:
                            raise RuntimeError(
                                _(
                                    'Error: directory {path} is not empty'
                                ).format(
                                    path=path,
                                )
                            )
                    except ValueError:
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
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='iso_domain',
            description='ISO domain layout',
            optional=True
        ).addFiles(
            group='iso_domain',
            fileList=uninstall_files,
        )
        if os.path.exists(path):
            self.logger.debug(
                'Enforcing ownership and access bits on {path}'.format(
                    path=path,
                )
            )
            os.chown(
                path,
                osetuputil.getUid(
                    self.environment[oengcommcons.SystemEnv.USER_VDSM]
                ),
                osetuputil.getGid(
                    self.environment[oengcommcons.SystemEnv.GROUP_KVM]
                )
            )
            os.chmod(path, 0o755)

        self.logger.debug('Generating a new uuid for ISO domain')
        sdUUID = str(uuid.uuid4())
        description = self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_NAME
        ]
        self.logger.debug(
            'Creating ISO domain for {path}. uuid: {uuid}'.format(
                path=path,
                uuid=sdUUID
            )
        )
        # Create images directory tree
        basePath = os.path.join(path, sdUUID)
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=os.path.join(
                    basePath,
                    'images',
                    oenginecons.Const.ISO_DOMAIN_IMAGE_UID,
                    '.keep',
                ),
                content=[],
                mode=0o644,
                dmode=0o755,
                owner=self.environment[oengcommcons.SystemEnv.USER_VDSM],
                group=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                downer=self.environment[
                    oengcommcons.SystemEnv.USER_VDSM
                ],
                dgroup=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                modifiedList=uninstall_files,
            )
        )
        # Create dom_md directory tree
        domMdDir = os.path.join(basePath, 'dom_md')
        for name in ('ids', 'inbox', 'outbox'):
            filename = os.path.join(domMdDir, name)
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=filename,
                    content=[],
                    mode=0o660,
                    dmode=0o755,
                    owner=self.environment[oengcommcons.SystemEnv.USER_VDSM],
                    group=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                    downer=self.environment[
                        oengcommcons.SystemEnv.USER_VDSM
                    ],
                    dgroup=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                    modifiedList=uninstall_files,
                )
            )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=os.path.join(domMdDir, 'leases'),
                content=b'\x00' * 512,
                binary=True,
                mode=0o660,
                dmode=0o755,
                owner=self.environment[oengcommcons.SystemEnv.USER_VDSM],
                group=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                downer=self.environment[
                    oengcommcons.SystemEnv.USER_VDSM
                ],
                dgroup=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                modifiedList=uninstall_files,
            )
        )
        metadata = os.path.join(domMdDir, 'metadata')
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=metadata,
                mode=0o644,
                dmode=0o755,
                owner=self.environment[oengcommcons.SystemEnv.USER_VDSM],
                group=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                downer=self.environment[oengcommcons.SystemEnv.USER_VDSM],
                dgroup=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                content=self._generate_md_content(sdUUID, description),
                modifiedList=uninstall_files,
            )
        )

        return sdUUID

    def _validateAndGetDomain(self, path):
        self._checker.check_valid_path(path)
        self._checker.check_base_writable(path)
        self._checker.check_available_space(
            path,
            oenginecons.Const.MINIMUM_SPACE_ISODOMAIN_MB
        )
        return self._get_domain(path)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ISO_DOMAIN_SD_UUID,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ISO_DOMAIN_NAME,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT,
            oenginecons.FileLocations.ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ISO_DOMAIN_EXISTS,
            False
        )

        self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR
        ] = None

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._checker = osetupdomains.DomainChecker()

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            oenginecons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        ),
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[oenginecons.SystemEnv.NFS_CONFIG_ENABLED] and
            not self.environment[oenginecons.ConfigEnv.ISO_DOMAIN_EXISTS]
        ),
    )
    def _customization(self):
        """
        If the user want to use NFS for ISO domain ask how to configure it.
        """
        interactive = self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
        ] is None

        validDomain = False
        while not validDomain:
            try:
                if self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ] is None:
                    default_mount_point = self.environment[
                        oenginecons.ConfigEnv.
                        ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT
                    ]
                    if os.path.exists(default_mount_point):
                        default_mount_point += '-%s' % (
                            datetime.datetime.utcnow().strftime('%Y%m%d%H%M%S')
                        )

                    self.environment[
                        oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ] = self.dialog.queryString(
                        name='NFS_MOUNT_POINT',
                        note=_('Local ISO domain path [@DEFAULT@]: '),
                        prompt=True,
                        caseSensitive=True,
                        default=default_mount_point,
                    )

                self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_SD_UUID
                ] = self._validateAndGetDomain(
                    path=self.environment[
                        oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ]
                )

                validDomain = True

            except (ValueError, RuntimeError) as e:
                if interactive:
                    if e.message == 'SIG2':
                        raise
                    self.logger.error(
                        _(
                            'Cannot access mount point '
                            '{mountPoint}: {error}'
                        ).format(
                            mountPoint=self.environment[
                                oenginecons.ConfigEnv.
                                ISO_DOMAIN_NFS_MOUNT_POINT
                            ],
                            error=e,
                        )
                    )
                    self.environment[
                        oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ] = None
                else:
                    raise

        path = self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
        ].rstrip('/')
        self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS].append({
            'type': 'public_content_rw_t',
            'pattern': '%s(/.*)?' % path,
        })
        self.environment[
            osetupcons.SystemEnv.SELINUX_RESTORE_PATHS
        ].append(path)

        if self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_NFS_ACL
        ] is None:
            self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_NFS_ACL
            ] = self.dialog.queryString(
                name='ISO_DOMAIN_ACL',
                note=_(
                    '\nPlease provide the ACL for the Local ISO domain.\n'
                    'See the exports(5) manpage for the format.\n'
                    'Examples:\n'
                    '- To allow access for host1, host2 and host3, input: '
                    'host1(rw) host2(rw) host3(rw)\n'
                    '- To allow access to the entire Internet, input: *(rw)\n'
                    '\nFor more information, see: '
                    'http://www.ovirt.org/Troubleshooting_NFS_Storage_Issues\n'
                    '\nLocal ISO domain ACL: '
                ),
                # No default, user must input something.
                # see https://bugzilla.redhat.com/1110740
                prompt=True,
                caseSensitive=True,
            )

        if self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_NAME
        ] is None:
            validName = False
            while not validName:
                self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_NAME
                ] = self.dialog.queryString(
                    name='ISO_DOMAIN_NAME',
                    note=_('Local ISO domain name [@DEFAULT@]: '),
                    prompt=True,
                    caseSensitive=True,
                    default=oenginecons.Defaults.DEFAULT_ISO_DOMAIN_NAME,
                )

                if self.RE_NOT_ALPHANUMERIC.search(
                        self.environment[
                            oenginecons.ConfigEnv.ISO_DOMAIN_NAME
                        ]
                ):
                    self.logger.error(
                        _(
                            'Domain name can only consist of alphanumeric '
                            'characters.'
                        )
                    )
                else:
                    validName = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.CONFIG_ISO_DOMAIN_AVAILABLE,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[oenginecons.SystemEnv.NFS_CONFIG_ENABLED] and
            not self.environment[oenginecons.ConfigEnv.ISO_DOMAIN_EXISTS]
        ),
    )
    def _add_iso_domain_to_db(self):
        """
        Add iso domain to DB
        """
        if self.environment[oenginecons.ConfigEnv.ISO_DOMAIN_SD_UUID] is None:
            self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_SD_UUID
            ] = self._prepare_new_domain(
                self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ]
            )
        self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR
        ] = os.path.join(
            self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
            ],
            self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_SD_UUID
            ],
            'images',
            oenginecons.Const.ISO_DOMAIN_IMAGE_UID,
        )
        self.logger.debug('Adding ISO domain into DB')
        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
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
                    oenginecons.ConfigEnv.ISO_DOMAIN_SD_UUID
                ],
                name=self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_NAME
                ],
                connection_id=str(uuid.uuid4()),
                connection='%s:%s' % (
                    self.environment[osetupcons.ConfigEnv.FQDN],
                    self.environment[
                        oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ]
                ),
                available=0,
                used=0,
            ),
        )
        self.environment[oenginecons.ConfigEnv.ISO_DOMAIN_EXISTS] = True


# vim: expandtab tabstop=4 shiftwidth=4
