"""
Controller class is a SINGLETON which handles all groups, params, sequences,
steps and replaces the CONF dictionary.
"""
from setup_params import Group
from setup_sequences import Sequence

class Controller(object):

    __GROUPS=[]
    __SEQUENCES=[]
    __PLUGINS=[]
    MESSAGES=[]
    CONF={}

    __single = None # the one, true Singleton

    def __new__(self, *args, **kwargs):
        """
        Singleton implementation.
        Will return __single if self is the same class as the class of __single
        which means that we will not invoke this singleton if someone tries to create a new
        instance from a class which inherit Controller.
        did not use isinstance because inheritence makes it behave erratically.
        """ 
        if self != type(self.__single):
            self.__single = object.__new__(self, *args, **kwargs)
        return self.__single

    def __init__(self): pass

    # PLugins
    def addPlugin(self, plugObj):
        self.__PLUGINS.append(plugObj)

    def getPluginByName(self, pluginName):
        for plugin in self.__PLUGINS:
            if plugin.__name__ == pluginName:
                return plugin
        return None

    def getAllPlugins(self):
        return self.__PLUGINS

    # Sequences and steps
    def addSequence(self, desc, cond, cond_match, steps):
        self.__SEQUENCES.append(Sequence(desc, cond, cond_match, steps))

    def getAllSequences(self):
        return self.__SEQUENCES

    def runAllSequences(self):
        for sequence in self.__SEQUENCES:
            sequence.run()

    def getSequenceByDesc(self, desc):
        for sequence in self.getAllSequences():
            if sequence.getDescription() == desc:
                return sequence
        return None

    def __getSequenceIndexByDesc(self, desc):
        for sequence in self.getAllSequences():
            if sequence.getDescription() == desc:
                return self.__SEQUENCES.index(sequence)
        return None

    def insertSequenceBeforeSequence(self, sequenceName, desc, cond, cond_match, steps):
        """
        Insert a sequence before a named sequence.
        i.e. if the specified sequence name is "update x", the new
        sequence will be inserted BEFORE "update x"
        """
        index = self.__getSequenceIndexByDesc(sequenceName)
        if index == None:
            index = len(self.getAllSequences())
        self.__SEQUENCES.insert(index, Sequence(desc, cond, cond_match, steps))

    # Groups and params
    def addGroup(self, group, params):
        self.__GROUPS.append(Group(group, params))

    def getGroupByName(self, groupName):
        for group in self.getAllGroups():
            if group.getKey("GROUP_NAME") == groupName:
                return group
        return None

    def getAllGroups(self):
        return self.__GROUPS

    def __getGroupIndexByDesc(self, name):
        for group in self.getAllGroups():
            if group.getKey("GROUP_NAME") == name:
                return self.__GROUPS.index(group)
        return None

    def insertGroupBeforeGroup(self, groupName, group, params):
        """
        Insert a group before a named group.
        i.e. if the specified group name is "update x", the new
        group will be inserted BEFORE "update x"
        """
        index = self.__getGroupIndexByDesc(groupName)
        if index == None:
            index = len(self.getAllGroups())
        self.__GROUPS.insert(index, Group(group, params))

    def getParamByName(self, paramName):
        for group in self.getAllGroups():
            param = group.getParamByName(paramName)
            if param:
                return param
        return None

    def getParamKeyValue(self, paramName, keyName):
        param = self.getParamByName(paramName)
        if param:
            return param.getKey(keyName)
        else:
            return None
