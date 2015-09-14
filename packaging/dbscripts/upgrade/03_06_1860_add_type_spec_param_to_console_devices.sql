update vm_device set spec_params =
'{
  "consoleType": "serial",
  "enableSocket" : "true"
}'
where device='console' and spec_params=
'{
  "enableSocket" : "true"
}';
