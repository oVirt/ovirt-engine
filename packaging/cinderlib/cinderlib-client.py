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
import sys

try:
    import cinderlib as cl
except ImportError:
    cl = None


class UsageError(Exception):
    """ Raised when usage is wrong """


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
    create_parser.add_argument("name", help="Name of the volume")
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

    args = parser.parse_args()
    try:
        args.command(args)
        sys.exit(0)
    except Exception as e:
        sys.stderr.write(str(e))
        sys.stderr.flush()
        sys.exit(1)


def load_backend(args):
    persistence_config = {'storage': 'db', 'connection': args.db_url}
    cl.setup(persistence_config=persistence_config)
    return cl.Backend(**json.loads(args.driver))


def create_volume(args):
    backend = load_backend(args)
    backend.create_volume(int(args.size), id=args.name)
    backend.refresh()


def delete_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    vol.delete()


def connect_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]

    # check if we're already connected
    for c in vol.connections:
        provided_host = json.loads(args.connector_info)['host']
        if provided_host == c.connector_info['host']:
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

    for c in vol.connections:
        c.disconnect()


def extend_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    vol.extend(int(args.size))
    backend.refresh()


def storage_stats(args):
    backend = load_backend(args)
    sys.stdout.write(
        json.dumps(backend.stats(refresh=args.refresh)))
    sys.stdout.flush()


def save_device(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    conn = vol.connections[0]
    conn.device_attached(json.loads(args.device))


def get_connection_info(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    conn = vol.connections[0]

    sys.stdout.write(json.dumps(conn.connection_info))
    sys.stdout.flush()


def clone_volume(args):
    backend = load_backend(args)
    vol = backend.volumes_filtered(volume_id=args.volume_id)[0]
    vol.clone(id=args.cloned_vol_id)
    backend.refresh()

if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
