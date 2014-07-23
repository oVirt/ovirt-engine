grammar Osinfo;


parse
    :
    ((osRecord | compatibilityRecord | comment) (EOL|EOF))*
    ;


osRecord
    :
    'os' DOT OS_UNIQUE_NAME '.' attribute
    ;

compatibilityRecord
    :
    'backwardCompatibility' '.' OS_UNIQUE_NAME WS* EQUALS WS* INT
    ;

comment
    :
    LineComment
    ;

attribute
    :
    ID intValue
    | NAME stringValue
    | DESCRIPTION stringValue
    | 'derivedFrom' stringValue
    | 'family' stringValue
    | 'cpuArchitecture' archValue
    | 'cpu.unsupported' stringValue
    | 'bus' busValue
    | 'sysprepPath' stringValue
    | 'sysprepFileName' stringValue
    | 'productKey' stringValue
    | 'isTimezoneTypeInteger' booleanValue
    | resources
    | devices
    ;

resources
    :
    'resources'
    ( DOT 'minimum' DOT ('ram'|'disksize'|'numberOfCpus') intValue
    | DOT 'maximum' DOT ('ram'|'disksize'|'numberOfCpus') intValue
    )+
    ;

devices
    :
    'devices'
    ( DOT DISPLAY_PROTOCOLS displayValue
    | DOT 'watchdog.models' watchdogValue
    | DOT 'network' networkValue
    | DOT 'network.hotplugSupport' booleanValue
    | DOT 'disk.hotpluggableInterfaces' hardwareInterfacesValue
    | DOT 'balloon.enabled' booleanValue
    | DOT 'audio' audioValue
    | DOT 'cdInterface' cdInterfaceValue
    | DOT 'diskInterfaces' hardwareInterfacesValue
    | DOT 'maxPciDevices' intValue
    | DOT 'hyperv.enabled' booleanValue
    )
    ;

intValue
    :
    valueSpecifier (INT | bus_width )
    ;

stringValue
    :
    valueSpecifier rest_of_line
    ;

rest_of_line
    :
    ~EOL*
    ;

booleanValue
    :
    valueSpecifier ('true' | 'false')
    ;

archValue
    :
    valueSpecifier ('x86_64' | 'ppc64')
    ;

busValue
    :
    valueSpecifier bus_width
    ;

displayValue
    :
    valueSpecifier DISPLAY_PROTOCOL_TYPE(',' WS* DISPLAY_PROTOCOL_TYPE )*
    ;


watchdogValue
    :
    valueSpecifier ('i6300esb')
    ;

networkValue
    :
    valueSpecifier NETWORK_DEVICE_TYPE (',' WS* NETWORK_DEVICE_TYPE)*
    ;


audioValue
    :
    valueSpecifier ('ich6' | 'ac97')
    ;

cdInterfaceValue
    :
    valueSpecifier ('ide' | 'scsi')
    ;


hardwareInterfacesValue
    :
    valueSpecifier HARDWARE_INTERFACE_TYPE* (',' WS* HARDWARE_INTERFACE_TYPE)*
    ;

valueSpecifier
    :
     VALUE (DOT VERSION)* WS* EQUALS WS*
    ;


//keywords
OS : 'os' ;
BACKWARDCOMPATIBILITY : 'backwardCompatibility' ;
ID : 'id' ;
NAME : 'name' ;
DESCRIPTION : 'description' ;
DERIVEDFROM : 'derivedFrom' ;
FAMILY : 'family' ;
CPUARCHITECTURE : 'cpuArchitecture' ;
BUS : 'bus' ;
RESOURCES : 'resources' ;
MINIMUM : 'minimum' ;
RAM : 'ram' ;
DISKSIZE : 'disksize' ;
NUMBEROFCPUS : 'numberOfCpus' ;
MAXIMUM : 'maximum' ;
DEVICES : 'devices' ;
DISPLAY_PROTOCOLS : 'display.protocols' ;
WATCHDOG_MODELS : 'watchdog.models' ;
NETWORK : 'network' ;
NETWORK_HOTPLUGSUPPORT : 'network.hotplugSupport' ;
DISK_HOTPLUGGABLEINTERFACES : 'disk.hotpluggableInterfaces' ;
BALLOON_ENABLED : 'balloon.enabled' ;
AUDIO : 'audio' ;
CDINTERFACE : 'cdInterface' ;
DISKINTERFACES : 'diskInterfaces' ;
MAXPCIDEVICES : 'maxPciDevices' ;
TRUE : 'true' ;
FALSE : 'false' ;
X86_64 : 'x86_64' ;
PPC64 : 'ppc64' ;
COMMA : ',' ;
I6300ESB : 'i6300esb' ;
ICH6 : 'ich6' ;
AC97 : 'ac97' ;
IDE : 'ide' ;
SCSI : 'scsi' ;

DISPLAY_PROTOCOL_TYPE
    :
    GRAPHICS_TYPE'/'DISPLAY_TYPE
    ;

GRAPHICS_TYPE
    :
    'spice' | 'vnc'
    ;

DISPLAY_TYPE
    :
    'qxl' | 'cirrus' | 'vga'
    ;

NETWORK_DEVICE_TYPE
    :
    'rtl8139_pv' | 'rtl8139' | 'e1000' | 'pv' | 'spaprVlan'
    ;

HARDWARE_INTERFACE_TYPE
    :
    'IDE' | 'VirtIO' | 'VirtIO_SCSI' | 'SPAPR_VSCSI'
    ;


INT : DIGIT+ ;
bus_width: '32' | '64' ;

fragment DIGIT : [0-9] ;
fragment CHAR : [a-zA-Z0-9_] ;
DOT: '.' ;
VALUE: '.value' ;
VERSION: '3' '.' '0'..'5' ;
OS_UNIQUE_NAME : CHAR+ ;
EQUALS : '=' ;
WS : [ \t] ;
TEXT: [a-zA-Z0-9\\\/${}]+ ;
LineComment
    :
    '#' ~[\r\n]*
    ;

EOL : [\r\n]+ ;
