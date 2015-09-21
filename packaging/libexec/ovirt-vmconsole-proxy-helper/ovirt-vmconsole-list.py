#!/usr/bin/python
#
# Copyright 2015 Red Hat
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import contextlib
import json
import logging
import os
import os.path
import sys
import urllib2
import urlparse

import ovirt_vmconsole_conf as config

from ovirt_engine import configfile, service, ticket


_HTTP_STATUS_CODE_SUCCESS = 200
_LOGGER_NAME = 'ovirt.engine.vmconsole.helper'


def make_ticket_encoder(cfg_file):
    return ticket.TicketEncoder(
        cfg_file.get('TOKEN_CERTIFICATE'),
        cfg_file.get('TOKEN_KEY'),
    )


def parse_args():
    parser = argparse.ArgumentParser(
        description='ovirt-vmconsole-proxy helper tool')
    parser.add_argument(
        '--debug', default=False, action='store_true',
        help='enable debug log',
    )
    parser.add_argument(
        '--version', metavar='V', type=int, nargs='?', default=1,
        help='version of the protocol to use',
    )
    subparsers = parser.add_subparsers(
        dest='entity',
        help='subcommand help',
    )

    parser_consoles = subparsers.add_parser(
        'consoles',
        help='list available consoles',
    )
    parser_consoles.add_argument(
        '--entityid', nargs='?', type=str, default='',
        help='entity ID where needed',
    )

    parser_keys = subparsers.add_parser(
        'keys',
        help='list available keys',
    )
    parser_keys.add_argument(
        '--keyfp', nargs='?', type=str, default='',
        help='list only the keys matching the given fingerprint',
    )
    parser_keys.add_argument(
        '--keytype', nargs='?', type=str, default='',
        help='list only the keys matching the given key type (e.g. ssh-rsa)',
    )
    parser_keys.add_argument(
        '--keycontent', nargs='?', type=str, default='',
        help='list only the keys matching the given content',
    )

    return parser.parse_args()


def make_request(args):
    if args.entity == 'keys':
        return {
            'command': 'public_keys',
            'version': args.version,
            'key_fp': args.keyfp,
            'key_type': args.keytype,
            'key_content': args.keycontent,
        }
    elif args.entity == 'consoles':
        if args.entityid is None:
            raise ValueError('entityid required and not found')
        return {
            'command': 'available_consoles',
            'version': args.version,
            'user_id': args.entityid,
        }
    else:
        raise ValueError('unknown entity: %s', args.entity)


def handle_response(res_string):
    if not res_string:
        return res_string

    res_obj = json.loads(res_string)
    # fixup types as ovirt-vmconsole-proxy-keys expects them
    res_obj['version'] = int(res_obj['version'])
    for con in res_obj.get('consoles', []):
        # fixup: servlet uses 'vmname' to reduce ambiguity;
        # ovirt-vmconsole-* however, expects 'vm'.
        con['vm'] = con['vmname']
        # fixup: to avoid name clashes between VMs
        # .sock suffix is for clarity
        con['console'] = '%s.sock' % con['vmid']

    return json.dumps(res_obj)


def main():
    service.setupLogger()

    logger = logging.getLogger(_LOGGER_NAME)

    try:
        args = parse_args()

        cfg_file = configfile.ConfigFile([config.ENGINE_VMPROXY_VARS])

        if cfg_file.getboolean('DEBUG') or args.debug:
            logger.setLevel(logging.DEBUG)

        base_url = (
            # debug, emergency override
            os.getenv('OVIRT_VMCONSOLE_ENGINE_BASE_URL') or
            cfg_file.get('ENGINE_BASE_URL')
        )

        logger.debug('using engine base url: %s', base_url)

        enc = make_ticket_encoder(cfg_file)
        data = enc.encode(json.dumps(make_request(args)))
        req = urllib2.Request(
            urlparse.urljoin(base_url, 'services/vmconsole-proxy'),
            data=data,
            headers={
                'Content-Type': 'text/plain',
                'Content-Length': len(data),
            },
        )

        with contextlib.closing(urllib2.urlopen(req)) as res:
            if res.getcode() != _HTTP_STATUS_CODE_SUCCESS:
                raise RuntimeError(
                    'Engine call failed: code=%d' % res.getcode()
                )
            print(handle_response(res.read()))

    except Exception as ex:
        logger.error('Error: %s', ex)
        logger.debug('Exception', exc_info=True)
        return 1
    else:
        return 0


if __name__ == "__main__":
    sys.exit(main())
