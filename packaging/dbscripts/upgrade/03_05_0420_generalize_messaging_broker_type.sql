-- The following update statements modifies an existing representation
-- of provider's agent properties
-- qpidConfiguration ---> messagingConfiguration
-- added brokerType with a specific type (QPID)

-- Input:
--
-- {                                       +
--   "qpidConfiguration" : {               +
--     "address" : "192.168.10.33",        +
--     "port" : 5678,                      +
--     "username" : "noone",               +
--     "password" : "nopass"               +
--   },                                    +
--   "networkMappings" : "xxx:eth600"      +
-- }

-- Output:
-- {                                       +
--   "messagingConfiguration" : {          +
--     "brokerType" : "QPID",              +
--     "address" : "192.168.10.33",        +
--     "port" : 5678,                      +
--     "username" : "noone",               +
--     "password" : "nopass"               +
--   },                                    +
--   "networkMappings" : "xxx:eth600"      +
-- }

update providers
set agent_configuration = replace (agent_configuration, '"qpidConfiguration" : {', E'"messagingConfiguration" : {\n    "brokerType" : "QPID",');
