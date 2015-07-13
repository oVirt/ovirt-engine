update command_entities set
    return_value =
        replace(return_value,'VdcFault','EngineFault'),
    return_value_class =
        replace(return_value_class,'VdcFault','EngineFault');

