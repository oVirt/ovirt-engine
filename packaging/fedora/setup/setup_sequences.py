"""
Base class for steps & sequences
"""
import logging
import sys
import re
import string
import traceback
import basedefs
import output_messages
import common_utils as utils

class Step(object):
    def __init__(self, title=None, functions=[]):
        self.__TITLE = None
        self.__FUNCTIONS = []
        if title:
            if not isinstance(title, str):
                raise TypeError("step's title should be of string type instead of %s" % type(title))
        if not isinstance(functions, list):
            raise TypeError("step's function should be of list type instead of %s" % type(functions))
        for function in functions:
            if not callable(function):
                raise TypeError("All parameters which pass as functions should be callable. %s is not callable" % function)

        self.setTitle(title)
        for function in functions:
            self.addFunction(function)

    def setTitle(self, title):
        self.__TITLE = title

    def getTitle(self):
        return self.__TITLE

    def addFunction(self, function):
        self.__FUNCTIONS.append(function)

    def removeFunction(self, function):
        self.__FUNCTIONS.remove(function)

    def getFunctions(self):
        return self.__FUNCTIONS

    def run(self):
        # keep relative space
        # allow newline chars in title. This is useful for plugins
        alignedTitle = self.getTitle()
        if re.search('\n', alignedTitle):
            alignedTitle = self.getTitle().split('\n')[-1]
        for color in basedefs.COLORS:
            if color in alignedTitle:
                alignedTitle = string.replace(alignedTitle, color, '')
        spaceLen = basedefs.SPACE_LEN - len(alignedTitle)
        sys.stdout.write("%s..." % self.getTitle())
        sys.stdout.flush()
        for function in self.getFunctions():
            try:
                logging.debug("running %s"%(function.func_name))
                function()
            except:
                logging.debug(traceback.format_exc())
                print ("[ " + utils.getColoredText(output_messages.INFO_ERROR, basedefs.RED) + " ]").rjust(spaceLen)
                raise
        print ("[ " + utils.getColoredText(output_messages.INFO_DONE, basedefs.GREEN) + " ]").rjust(spaceLen)

class Sequence(object):
    """
    Gets 4 parameters:
    description, condition's name/function, condition's expected result and steps
    steps should be a list of dictionaries, example:
    [ { 'title'     : 'step1's title',
        'functions' : [ func1, func2, func3 ] },
      { 'title'     : 'step2's tittle',
        'functions' : [ func4, func6 ] } ]
    """
    def __init__(self, desc=None, cond=[], cond_match=[], steps=[]):
        self.__DESCRIPTION = None
        self.__CONDITION = None
        self.__COND_MATCH = None
        self.__STEPS = []

        self.setDescription(desc)
        self.setCondition(cond, cond_match)
        for step in steps:
            if not isinstance(step, dict):
                raise TypeError("step should be of dictionary type instead of %s" % type(step))
            self.addStep(step['title'], step['functions'])

    def addStep(self, title, functions):
        self.__STEPS.append(Step(title, functions))

    def setDescription(self, desc):
        self.__DESCRIPTION = desc

    def getDescription(self):
        return self.__DESCRIPTION

    def getSteps(self):
        return self.__STEPS

    def getStepByTitle(self, stepTitle):
        for step in self.__STEPS:
            if step.getTitle == stepTitle:
                return step
        return None

    def setCondition(self, cond, cond_match):
        for item in [cond, cond_match]:
            if not isinstance(item, list):
                raise TypeError("supplied parameter should be of list type instead of %s" % type(item))

        self.__CONDITION = cond
        self.__COND_MATCH = cond_match

    def __validateCondition(self):
        """
        Both _CONDITION & _COND_MATCH are lists.
        if any of them is a function that needs to be run, the first member
        of the list is the function and the rest of the members in that list
        are the params for the said function
        i.e. self._CONDITION = [function, arg1, arg2]
        will be executed as function(arg1, arg2)
        if the first member of the list is not a function. we handle it
        as anything else (i.e. string/bool etc)
        """
        if len(self.__CONDITION) < 1 and len(self.__COND_MATCH) < 1:
            return True

        condResult = None
        condMatchResult = None

        if callable(self.__CONDITION[0]):
            condResult = self.__CONDITION[0](*self.__CONDITION[1:])
        else:
            condResult = self.__CONDITION[0]

        if callable(self.__COND_MATCH[0]):
            condMatchResult = self.__COND_MATCH[0](*self.__COND_MATCH[1:])
        else:
            condMatchResult = self.__COND_MATCH[0]

        if condResult == condMatchResult:
            return True

        return False

    def removeStepByTitle(self, stepTitle):
        self.__STEPS.remove(stepTitle)

    def run(self):
        if self.__validateCondition():
            for step in self.__STEPS:
                step.run()

    def runStepByTitle(self, stepTitle):
        step = self.getStepByTitle(stepTitle)
        step.run()

    def listStepsByTitle(self):
        output = []
        for step in self.__STEPS:
            output.append(step.getTitle())
        return output
