#! /usr/bin/env python

# Copyright 2017 Red Hat, Inc.
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
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
#
# Refer to the README and COPYING files for full details of the license
#

from __future__ import absolute_import

import socket
import time

import ovirtsdk4 as sdk
import ovirtsdk4.types as types


def main():
    with get_connection() as connection:
        system_service = connection.system_service()

        dc_service = system_service.data_centers_service()
        datacenters = dc_service.list()
        if len(datacenters) > 1:
            heDc = select_dc(datacenters)
        else:
            heDc = datacenters.pop()

        host_services = system_service.hosts_service()
        hosts = host_services.list(search='datacenter=%s and spm_id=1'
                                   % heDc.name)
        for host in hosts:
            host_service = host_services.host_service(host.id)
            if host.status == types.HostStatus.UP:
                print("Putting host %s to maintenance" % host.name)
                host_service.deactivate()
                while True:
                    updated_host = host_service.get()
                    if updated_host.status == types.HostStatus.MAINTENANCE:
                        print("Host is in Maintenance state")
                        return
                    print("Waiting for host to switch into Maintenance state")
                    time.sleep(1)


def get_connection():
    URL_DEFAULT = 'https://%s/ovirt-engine/api' % socket.getfqdn()
    USERNAME_DEFAULT = 'admin@internal'
    CA_DEFAULT = '/etc/pki/ovirt-engine/ca.pem'

    url = raw_input("Engine REST API url[%s]:" % URL_DEFAULT)
    if not url:
        url = URL_DEFAULT
    username = raw_input("Engine REST API username[%s]:" % USERNAME_DEFAULT)
    if not username:
        username = USERNAME_DEFAULT
    password = raw_input("Engine REST API password:")
    ca_file = raw_input("Engine CA certificate file[%s]:" % CA_DEFAULT)
    if not ca_file:
        ca_file = CA_DEFAULT

    return sdk.Connection(
        url=url,
        username=username,
        password=password,
        ca_file=ca_file,
    )


def select_dc(datacenters):
    while True:
        for i, dc in enumerate(datacenters):
            print("\t %d) %s" % (i, dc.name))
        dc_answer = raw_input(
            "Select which Data Center will run Hosted Engine:")
        try:
            return datacenters[int(dc_answer)]
        except ValueError:
            print("Invalid input - not a number")
        except IndexError:
            print("Invalid input - out of range")


if __name__ == '__main__':
    main()
