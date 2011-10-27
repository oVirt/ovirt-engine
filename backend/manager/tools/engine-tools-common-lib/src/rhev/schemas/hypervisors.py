"""This module uses the api to get a collection of information about hypervisors"""
import logging
import urllib
import urllib2
import base64
import threading

import api

class ENGINETree(object):

    class DataCenter(object):

        def __init__(self, id, name):
            self.id = id
            self.name = name
            self.clusters = set()

        def add_cluster(self, cluster):
            self.clusters.add(cluster)

        def __str__(self):
            return self.name

    class Cluster(object):

        def __init__(self, id, name):
            self.id = id
            self.name = name
            self.hosts = set()

        def add_host(self, host):
            self.hosts.add(host)

        def __str__(self):
            return self.name

    class Host(object):

        def __init__(self, address, name=None):
            self.address = address
            self.name = name

        def __str__(self):
            return self.address

    def __init__(self):
        self.datacenters = set()
        self.clusters = set()
        self.hosts = set()

    def add_datacenter(self, datacenter):
        dc_obj = self.DataCenter(datacenter.id, datacenter.name)
        self.datacenters.add(dc_obj)

    def add_cluster(self, cluster):
        c_obj = self.Cluster(cluster.id, cluster.name)
        self.clusters.add(c_obj)
        if cluster.get_data_center() is not None:
            for dc in self.datacenters:
                if dc.id == cluster.get_data_center().id:
                    dc.add_cluster(c_obj)
        else:
            dummySeen = 0
            for dc in self.datacenters:
                if dc.id == "":
                    dc.add_cluster(c_obj)
                    dummySeen = 1
            if dummySeen == 0:
                dc = self.DataCenter("", "")
                dc.add_cluster(c_obj)
                self.datacenters.add(dc)

    def add_host(self, host):
        host_obj = self.Host(host.get_address(), host.name)
        self.hosts.add(host_obj)
        if host.get_cluster() is not None:
            for cluster in self.clusters:
                if cluster.id == host.get_cluster().id:
                    cluster.add_host(host_obj)
        else:
            dummySeen = 0
            for cluster in self.clusters:
                if cluster.id == "":
                    cluster.add_host(host_obj)
                    dummySeen = 1
            if dummySeen == 0:
                c_obj = self.Cluster("", "")
                c_obj.add_host(host_obj)
                self.clusters.add(c_obj)
                dc = self.DataCenter("", "")
                dc.add_cluster(c_obj)
                self.datacenters.add(dc)

    def __str__(self):
        return "\n".join(["%-20s | %-20s | %s" % (dc, cluster, host)
                            for dc in self.datacenters
                            for cluster in dc.clusters
                            for host in cluster.hosts])

    def get_sortable(self):
        return [(dc.name, cluster.name, host.address)
                    for dc in self.datacenters
                    for cluster in dc.clusters
                    for host in cluster.hosts]


def _fetch_from_api(endpoint, hostname, username, password, result_dict):

    url = "https://" + hostname + "/api/" + endpoint
    req = urllib2.Request(url)

    # Not using the AuthHandlers because they actually make two requests
    auth = "%s:%s" % (username, password)

    auth = base64.encodestring(auth).strip()
    req.add_header("Authorization", "Basic %s" % auth)

    try:
        fp = urllib2.urlopen(req)
        xml_doc = fp.read()
        logging.debug("_fetch_from_api:\n" + xml_doc)
        result_dict[endpoint] = api.parseString(xml_doc)
    except Exception, e:
        logging.error("_fetch_from_api: Failure while fetching %s: %s", url, e)


def get_all(hostname, username, password):

    results = {}

    threads = [
            threading.Thread(target=_fetch_from_api, args=('datacenters', hostname, username, password, results)),
            threading.Thread(target=_fetch_from_api, args=('clusters', hostname, username, password, results)),
            threading.Thread(target=_fetch_from_api, args=('hosts', hostname, username, password, results)),
            ]

    for thread in threads:
        thread.start()

    for thread in threads:
        thread.join()

    tree = ENGINETree()
    try:
        for dc in results['datacenters'].get_data_center():
            tree.add_datacenter(dc)
        for cluster in results['clusters'].get_cluster():
            tree.add_cluster(cluster)
        for host in results['hosts'].get_host():
            tree.add_host(host)
        return set(tree.get_sortable())
    except Exception, e:
        logging.error("get_all: Failed to get hosts from ENGINE. Did the API calls fail?")
        return set()

