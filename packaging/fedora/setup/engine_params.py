"""
Container set for groups and parameters
"""
class Param(object):
    allowed_keys = ('CMD_OPTION','USAGE','PROMPT','OPTION_LIST',
                    'VALIDATION_FUNC','DEFAULT_VALUE','MASK_INPUT','LOOSE_VALIDATION',
                    'CONF_NAME','USE_DEFAULT','NEED_CONFIRM','CONDITION')

    def __init__(self, attributes={}):
        self._ATTRIBUTES = {}
        if attributes:
            for key in self.allowed_keys:
                self._ATTRIBUTES[key] = attributes[key]
        else:
            self._ATTRIBUTES = {}.fromkeys(self.allowed_keys)

    def setKey(self, key, value):
        self.validateKey(key)
        self._ATTRIBUTES[key] = value

    def getKey(self, key):
        self.validateKey(key)
        return self._ATTRIBUTES[key]

    def validateKey(self, key):
        if not self._ATTRIBUTES.has_key(key):
            raise KeyError("%s is not a valid key" % key)

class Group(Param):
    allowed_keys = ('GROUP_NAME', 'DESCRIPTION', 'PRE_CONDITION', 'PRE_CONDITION_MATCH', 'POST_CONDITION', 'POST_CONDITION_MATCH')

    def __init__(self, attributes={}, params=[]):
        self._PARAMS = []
        Param.__init__(self, attributes)
        for param in params:
            self.addParam(param)

    def addParam(self,paramDict):
        p = Param(paramDict)
        self._PARAMS.append(p)

    def getParamByName(self,paramName):
        for param in self._PARAMS:
            if param.getKey("CONF_NAME") == paramName:
                return param
        return None

    def getAllParams(self):
        return self._PARAMS

    def getParams(self,paramKey, paramValue):
        output = []
        for param in self._PARAMS:
           if param.getKey(paramKey) == paramValue:
                output.append(param)
        return output

    def removeParamByName(self, paramName):
        self._removeParams("CONF_NAME", paramName)

    def _removeParams(self, paramKey, paramValue):
        list = self.getParams(paramKey, paramValue)
        for item in list:
            self._PARAMS.remove(item)
