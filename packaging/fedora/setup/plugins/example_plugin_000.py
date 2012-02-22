"""
Example plugin
List the provided API
"""

import logging
from setup_controller import Controller
#import time
#import common_utils as utils
#import engine_validators as validate


# Controller holds ALL. and is a singleton. so you MUST invoke it.
# Unless you do not care for the params or configuration
controller = Controller()

logging.debug("plugin %s loaded" % __name__)


# initConfig & initSequences must be defined.
# initConfig is used to alter the configuration and asked question
# initSequences is used to alter the sequences and steps

"""
This Plugin will ask 2 questions, one in the middle of the ALL_PARAMS group and one in a new group in the end of it all
"""
def initConfig(controller): pass
#    logging.debug("adding new param named: question1")
#    group = controller.getGroupByName("ALL_PARAMS")
#    if group:
#        # We will insert the new param before host-fqdn question
#        newParamDict = {
#                "CMD_OPTION"      :"sample-question1",
#                "USAGE"           :"I'm sample question #1",
#                "PROMPT"          :"Should i sleep for 3 seconds before creating the database",
#                "OPTION_LIST"     :["yes","no"],
#                "VALIDATION_FUNC" :validate.validateOptions,
#                "DEFAULT_VALUE"   :"yes",
#                "MASK_INPUT"      : False,
#                "LOOSE_VALIDATION": False,
#                "CONF_NAME"       : "SAMPLE_QUESTION1",
#                "USE_DEFAULT"     : False,
#                "NEED_CONFIRM"    : False,
#                "CONDITION"       : False}
#        group.insertParamBeforeParam("HOST_FQDN", newParamDict)
#
#    logging.debug("Adding a new group named: groupTest1")
#    groupDict = {
#                      "GROUP_NAME"            : "GROUPTEST1",
#                      "DESCRIPTION"           : "GROUPTEST1",
#                      "PRE_CONDITION"         : False,
#                      "PRE_CONDITION_MATCH"   : True,
#                      "POST_CONDITION"        : False,
#                      "POST_CONDITION_MATCH"  : True}
#    newParamDict = {
#                "CMD_OPTION"      :"sample-question2",
#                "USAGE"           :"I'm sample question #2",
#                "PROMPT"          :"What is your opinion on question 2",
#                "OPTION_LIST"     :[],
#                "VALIDATION_FUNC" :validate.validateStringNotEmpty,
#                "DEFAULT_VALUE"   :"",
#                "MASK_INPUT"      : False,
#                "LOOSE_VALIDATION": False,
#                "CONF_NAME"       : "SAMPLE_QUESTION2",
#                "USE_DEFAULT"     : False,
#                "NEED_CONFIRM"    : False,
#                "CONDITION"       : False}
#    controller.addGroup(groupDict, [newParamDict])

def initSequences(controller): pass
#    logging.debug("Adding a new sequence to run after \'Initial Steps\' and before \'Create DB\'")
#    # This new sequece will only be executed if the user answered "YES" to question SAMPLE_QUESTION1 which we inserted
#    # Before
#    sleepStepDict = { 'title':"Sleeping 3 seconds",'functions':[__letmeSleep] }
#    controller.insertSequenceBeforeSequence("Create DB", "Let me sleep", [controller.CONF["SAMPLE_QUESTION1"]],["yes"],[sleepStepDict])
#
#    # This new sequence will alwayys run
#    logging.debug("Adding a new sequence in the end of execution")
#    controller.addSequence("You Said "+ controller.CONF["SAMPLE_QUESTION2"], [],[], [sleepStepDict])

#def __letmeSleep():
#    logging.debug("sleeping 3 seconds")
#    logging.debug("Sample Question 1 answered: %s", controller.CONF["SAMPLE_QUESTION1"])
#    logging.debug("Sample Question 2 answered: %s", controller.CONF["SAMPLE_QUESTION2"])
#    time.sleep(3)

"""
Available API from within controller:
addSequence(description, [condition, params], [condition_result, params], [steps])

append a new sequence to be executed after all other sequences are done
Parameters should be:
description:      a short description of the sequence
condition:        must be a list, if it is a function that needs additional params, add them to the list,
                  otherwise, just place the value needed as a single member list
condition_result: same as condition
steps:            list of steps, each step must be in the following format:
                  { 'title'     : 'step1's title',
                    'functions' : [ func1, func2, func3 ] }
-------------------
getAllSequences()
returns a list of all sequences
-------------------
runAllSequences()
iterate over all sequences and execute the method run() on each object
-------------------
getSequenceByDesc(description)
returns the sequence which match the given description
-------------------
insertSequenceBeforeSequence(sequenceName, description, [condition, params], [condition_result, params], [steps])
add a sequence before the given sequenceName. if sequenceName does not match any sequence, append to the end of the list.
the rest of the parameters are the same as the addSequence method.
-------------------
addGroup(groupDict, [params])
add a group to the controller
Parameters should be:
groupDict: a dictionary which describes the group, example:
           { "GROUP_NAME"            : "ALL_PARAMS",
             "DESCRIPTION"           : "MESSAGE",
             "PRE_CONDITION"         : False,
             "PRE_CONDITION_MATCH"   : True,
             "POST_CONDITION"        : False,
             "POST_CONDITION_MATCH"  : True}
params:    List of params, each param is a dictionary, example:
           {   "CMD_OPTION"      :"override-iptables",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_IPTABLES_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_IPTABLES_PROMPT,
                "OPTION_LIST"     :["yes","no"],
                "VALIDATION_FUNC" :validate.validateOptions,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "OVERRIDE_IPTABLES",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False}
-------------------
getGroupByName(name)
returns the group which matches the given name
-------------------
getAllGroups()
returns a list of all groups
-------------------
getParamByName(paramName):
iterate over all groups and returns the param which matches the given paramName
-------------------
getParamKeyValue(name, key):
returns the specific value for the given key from the param which matches the given name
-------------------
insertGroupBeforeGroup(groupName, groupDict, [params])
add a group before the given groupName. if sequenceName does not match any group, append to the end of the list.
the rest of the parameters are the same as the addGroup method.

"""
