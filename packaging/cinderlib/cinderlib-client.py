#!/usr/bin/env python
#
# Copyright 2018 Red Hat, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
#
# Refer to the README and COPYING files for full details of the license
#
"""

"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import argparse
import json
import logging
import os
import sys
import traceback

import requests


from requests.packages.urllib3.exceptions import InsecureRequestWarning

import config


from ovirt_engine import configfile

try:
    import cinderlib as cl
except ImportError:
    cl = None

# Silence SSL warnings for older versions
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

logger = None


class UsageError(Exception):
    """ Raised when usage is wrong """

conf = configfile.ConfigFile([config.ENGINE_DEFAULTS])


def get_ssh_known_hosts():
    ssh_dir = os.path.join(conf.get('ENGINE_ETC'), 'cinderlib')
    ssh_file = os.path.join(ssh_dir, 'ssh_known_hosts')

    return ssh_file


def main(args=None):
    if cl is None:
        sys.stderr.write("cinderlib package not available")
        sys.exit(1)

    parser = argparse.ArgumentParser(description="oVirt Cinder Library client")
    subparsers = parser.add_subparsers(title="commands")
    create_parser = subparsers.add_parser("create_volume",
                                          help="Create a volume."
                                               " return CinderLib metadata")
    create_parser.set_defaults(command=create_volume)
    create_parser.add_argument("driver", help="The driver parameters")
    create_parser.add_argument("db_url", help="The database url")
    create_parser.add_argument("volume_id", help="The volume id")
    create_parser.add_argument("size", help="The size needed for the volume")

    delete_parser = subparsers.add_parser("delete_volume",
                                          help="Delete a volume."
                                               " return CinderLib metadata")
    delete_parser.set_defaults(command=delete_volume)
    delete_parser.add_argument("driver", help="The driver parameters")
    delete_parser.add_argument("db_url", help="The database url")
    delete_parser.add_argument("volume_id", help="The volume id")

    connect_parser = subparsers.add_parser("connect_volume",
                                           help="Get volume connection info")
    connect_parser.set_defaults(command=connect_volume)
    connect_parser.add_argument("driver", help="The driver parameters")
    connect_parser.add_argument("db_url", help="The database url")
    connect_parser.add_argument("volume_id", help="The volume id")
    connect_parser.add_argument("connector_info",
                                help="The connector information")

    disconnect_parser = subparsers.add_parser("disconnect_volume",
                                              help="disconnect a volume")
    disconnect_parser.set_defaults(command=disconnect_volume)
    disconnect_parser.add_argument("driver", help="The driver parameters")
    disconnect_parser.add_argument("db_url", help="The database url")
    disconnect_parser.add_argument("volume_id", help="The volume id")

    extend_parser = subparsers.add_parser("extend_volume",
                                          help="Extend a volume")
    extend_parser.set_defaults(command=extend_volume)
    extend_parser.add_argument("driver", help="The driver parameters")
    extend_parser.add_argument("db_url", help="The database url")
    extend_parser.add_argument("volume_id", help="The volume id")
    extend_parser.add_argument("size", help="The size needed for the volume")

    storage_stats_parser = subparsers.add_parser("storage_stats",
                                                 help="Get the storage status")
    storage_stats_parser.set_defaults(command=storage_stats)
    storage_stats_parser.add_argument("driver", help="The driver parameters")
    storage_stats_parser.add_argument("db_url", help="The database url")
    storage_stats_parser.add_argument("refresh",
                                      help="True if latest data is required")

    save_device_parser = subparsers.add_parser("save_device",
                                               help="save a device")
    save_device_parser.set_defaults(command=save_device)
    save_device_parser.add_argument("driver", help="The driver parameters")
    save_device_parser.add_argument("db_url", help="The database url")
    save_device_parser.add_argument("volume_id", help="The volume id")
    save_device_parser.add_argument("device", help="The device")

    connection_info_parser = subparsers.add_parser("get_connection_info",
                                                   help="retrieve volume "
                                                        "attachment connection"
                                                        " info")
    connection_info_parser.set_defaults(command=get_connection_info)
    connection_info_parser.add_argument("driver",
                                        help="The driver parameters")
    connection_info_parser.add_argument("db_url", help="The database url")
    connection_info_parser.add_argument("volume_id", help="The volume id")

    clone_parser = subparsers.add_parser("clone_volume",
                                         help="Clone a volume")
    clone_parser.set_defaults(command=clone_volume)
    clone_parser.add_argument("driver", help="The driver parameters")
    clone_parser.add_argument("db_url", help="The database url")
    clone_parser.add_argument("volume_id", help="The source volume id")
    clone_parser.add_argument("cloned_vol_id", help="The cloned volume id")

    create_snapshot_parser = subparsers.add_parser("create_snapshot",
                                                   help="create snapshot ")
    create_snapshot_parser.set_defaults(command=create_snapshot)
    create_snapshot_parser.add_argument("driver",
                                        help="The driver parameters")
    create_snapshot_parser.add_argument("db_url", help="The database url")
    create_snapshot_parser.add_argument("volume_id", help="The volume id")

    remove_snapshot_parser = subparsers.add_parser("remove_snapshot",
                                                   help="remove a snapshot ")
    remove_snapshot_parser.set_defaults(command=remove_snapshot)
    remove_snapshot_parser.add_argument("driver",
                                        help="The driver parameters")
    remove_snapshot_parser.add_argument("db_url", help="The database url")
    remove_snapshot_parser.add_argument("snapshot_id", help="The snapshot id")
    remove_snapshot_parser.add_argument("volume_id", help="Snapshots's "
                                                          "volume id")
    create_volume_from_snapshot_parser = \
        subparsers.add_parser("create_volume_from_snapshot",
                              help="create a volume from a snapshot")
    create_volume_from_snapshot_parser.set_defaults(
        command=create_volume_from_snapshot)
    create_volume_from_snapshot_parser.add_argument("driver",
                                                    help="The driver "
                                                         "parameters")
    create_volume_from_snapshot_parser.add_argument("db_url",
                                                    help="The database url")
    create_volume_from_snapshot_parser.add_argument("volume_id",
                                                    help="Snapshots's "
                                                         "volume id")
    create_volume_from_snapshot_parser.add_argument("snapshot_id",
                                                    help="The snapshot id")

    args = parser.parse_args()
    try:
        args.command(args)
        sys.exit(0)
    except Exception as e:
        setup_logger()
        logger.error("Failure occurred when trying to run command '%s': %s",
                     sys.argv[1], e)
        sys.stderr.write(traceback.format_exc(e))
        sys.stderr.flush()
        sys.exit(1)


def setup_logger():
    logging.log_file = os.path.join(conf.get('ENGINE_LOG'),
                                    'cinderlib', 'cinderlib.log')
    logging.config.fileConfig("logger.conf", disable_existing_loggers=True)
    logging.captureWarnings(True)
    global logger
    logger = logging.getLogger()


def load_backend(args):
    persistence_config = {'storage': 'db', 'connection': args.db_url}
    cl.setup(file_locks_path=conf.get('ENGINE_TMP'),
             persistence_config=persistence_config,
             ssh_hosts_key_file=get_ssh_known_hosts(),
             disable_logs=False)

    # Setup logging here to not have our logger overridden by cinderlib's
    setup_logger()

    return cl.Backend(**json.loads(args.driver))


def create_volume(args):
    backend = load_backend(args)
    logger.info("Creating volume '%s', with size '%s' GB",
                args.volume_id, args.size)
    backend.create_volume(int(args.size), id=args.volume_id)
    backend.refresh()


def delete_volume(args):
    backend = load_backend(args)
    logger.info("Deleting volume '%s'")
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    vol.delete()


def connect_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    logger.info("Connecting volume '%s', to host with info %r", args.volume_id,
                args.connector_info)

    # check if we're already connected
    for c in vol.connections:
        provided_host = json.loads(args.connector_info)['host']
        if provided_host == c.connector_info['host']:
            logger.info("Volume '%s' already connected to host '%s'",
                        args.volume_id, provided_host)
            conn = c.conn_info
            break
    else:
        conn = (vol.connect(json.loads(args.connector_info))
                .connection_info['conn'])

    sys.stdout.write(json.dumps(conn))
    sys.stdout.flush()


def disconnect_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    logger.info("Disconnecting volume '%s'", args.volume_id)

    for c in vol.connections:
        c.disconnect()


def extend_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    logger.info("Extending volume '%s' by %s GB", args.volume_id, args.size)
    vol.extend(int(args.size))
    backend.refresh()


def storage_stats(args):
    backend = load_backend(args)
    logger.info("Fetch backend stats")
    sys.stdout.write(
        json.dumps(backend.stats(refresh=args.refresh)))
    sys.stdout.flush()


def save_device(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    conn = vol.connections[0]
    logger.info("Saving connection %r for volume '%s'", conn, vol.id)
    conn.device_attached(json.loads(args.device))


def get_connection_info(args):
    backend = load_backend(args)
    logger.info("Fetch volume '%s' connetion info", args.volume_id)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    conn = vol.connections[0]

    sys.stdout.write(json.dumps(conn.connection_info))
    sys.stdout.flush()


def clone_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    logger.info("Cloning volume '%s' to '%s'", vol.id, args.cloned_vol_id)
    vol.clone(id=args.cloned_vol_id)
    backend.refresh()


def create_snapshot(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    logger.info("Creating snapshot for volume '%s'", args.volume_id)
    snap = None
    try:
        snap = vol.create_snapshot()
    except:
        if snap and snap.status == 'error':
            logger.error("failed to create snapshot '%s', reverting", snap.id)
            snap.delete()
        raise

    logger.info("Created snapshot id: '%s'", snap.id)
    sys.stdout.write(snap.id)
    sys.stdout.flush()
    backend.refresh()


def remove_snapshot(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    logger.info("Removing volume '%s' snapshot '%s'",
                args.volume_id, args.snapshot_id)
    snap = [s for s in vol.snapshots if s.id == args.snapshot_id][0]
    snap.delete()


def create_volume_from_snapshot(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    snap = [s for s in vol.snapshots if s.id == args.snapshot_id][0]
    logger.info("Creating new volume from snapshot '%s' of volume '%s'",
                args.snapshot_id, args.volume_id)
    new_vol = snap.create_volume()
    logger.info("Created volume id: '%s'", new_vol.id)
    sys.stdout.write(new_vol.id)
    sys.stdout.flush()

if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
