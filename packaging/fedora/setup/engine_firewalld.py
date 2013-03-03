from firewall.client import FirewallClient
from firewall.errors import *

def getActiveZones():
    fw = FirewallClient()
    zones = fw.getActiveZones()
    return zones

def addServiceToZone(service, zone):
    fw = FirewallClient()
    fw_zone = fw.config().getZoneByName(zone)
    fw_settings = fw_zone.getSettings()
    fw_settings.addService(service)
    fw_zone.update(fw_settings)


