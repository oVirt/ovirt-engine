#!/usr/bin/python

# RHEV Manager post upgrade scripts
# Imported dynamically from rhevm-upgrade

#TODO: Implement all

import common_utils as utils
import logging
import traceback

MSG_ERR_UPDATE_PRODUCT_VERSION="Error updating product version"

def updateProductVersion():
    """
    Update product version in vdc options
    table from rpm version
    """
    try:
        # Update new version in vdc_option
        utils.updateVDCOption("ProductRPMVersion", utils.getEngineVersion())

    except:
        logging.error(traceback.format_exc())
        logging.error(MSG_ERR_UPDATE_PRODUCT_VERSION)

def run():
    # Update product version in db from rpm version
    updateProductVersion()
