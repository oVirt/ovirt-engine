-- upgrade the current clusters with the value emulated_machine in vdc_options, by version

UPDATE vds_groups SET emulated_machine = (
  SELECT option_value from vdc_options
   WHERE option_name = 'EmulatedMachine'
   AND version = vds_groups.compatibility_version);

