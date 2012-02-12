"""
Container set for groups and parameters
"""
class Param(object):
    allowed_keys = ('CMD_OPTION','USAGE','PROMPT','OPTION_LIST',
                    'VALIDATION_FUNC','DEFAULT_VALUE','MASK_INPUT','LOOSE_VALIDATION',
                    'CONF_NAME','USE_DEFAULT','NEED_CONFIRM','CONDITION')

    def __init__(self, attributes={}):
        self.__ATTRIBUTES = {}
        if attributes:
            for key in self.allowed_keys:
                self.__ATTRIBUTES[key] = attributes[key]
        else:
            self.__ATTRIBUTES = {}.fromkeys(self.allowed_keys)

    def setKey(self, key, value):
        self.validateKey(key)
        self.__ATTRIBUTES[key] = value

    def getKey(self, key):
        self.validateKey(key)
        return self.__ATTRIBUTES[key]

    def validateKey(self, key):
        if not self.__ATTRIBUTES.has_key(key):
            raise KeyError("%s is not a valid key" % key)

class Group(Param):
    allowed_keys = ('GROUP_NAME', 'DESCRIPTION', 'PRE_CONDITION', 'PRE_CONDITION_MATCH', 'POST_CONDITION', 'POST_CONDITION_MATCH')

    def __init__(self, attributes={}, params=[]):
        self.__PARAMS = []
        Param.__init__(self, attributes)
        for param in params:
            self.addParam(param)

    def addParam(self,paramDict):
        p = Param(paramDict)
        self.__PARAMS.append(p)

    def getParamByName(self,paramName):
        for param in self.__PARAMS:
            if param.getKey("CONF_NAME") == paramName:
                return param
        return None

    def getAllParams(self):
        return self.__PARAMS

    def getParams(self,paramKey, paramValue):
        output = []
        for param in self.__PARAMS:
           if param.getKey(paramKey) == paramValue:
                output.append(param)
        return output

    def __getParamIndexByDesc(self, name):
        for param in self.getAllParams():
            if param.getKey("CONF_NAME") == name:
                return self.__PARAMS.index(param)
        return None
    
    def insertParamBeforeParam(self, paramName, param):
        """
        Insert a param before a named param.
        i.e. if the specified param name is "update x", the new 
        param will be inserted BEFORE "update x"
        """
        index = self.__getParamIndexByDesc(paramName)
        if index == None:
            index = len(self.getAllParams())
        self.__PARAMS.insert(index, Param(param))

    def removeParamByName(self, paramName):
        self.__removeParams("CONF_NAME", paramName)

    def __removeParams(self, paramKey, paramValue):
        list = self.getParams(paramKey, paramValue)
        for item in list:
            self.__PARAMS.remove(item)
