-- set blank version name to '' for UI sorting issues.

UPDATE vm_static
SET template_version_name=''
WHERE vm_guid='00000000-0000-0000-0000-000000000000';
