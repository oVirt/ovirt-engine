-- Rename architecture family name: x86_64 -> x86, ppc64 -> ppc
UPDATE vdc_options
  SET
      option_value = replace(option_value, 'x86_64', 'x86')
  WHERE option_name in(
      'HotPlugCpuSupported',
      'HotUnplugCpuSupported',
      'HotPlugMemorySupported',
      'HotUnplugMemorySupported',
      'IsMigrationSupported',
      'IsMemorySnapshotSupported',
      'IsSuspendSupported');

UPDATE vdc_options
  SET
      option_value = replace(option_value, 'ppc64', 'ppc')
  WHERE option_name in(
      'HotPlugCpuSupported',
      'HotUnplugCpuSupported',
      'HotPlugMemorySupported',
      'HotUnplugMemorySupported',
      'IsMigrationSupported',
      'IsMemorySnapshotSupported',
      'IsSuspendSupported');

