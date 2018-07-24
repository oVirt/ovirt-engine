UPDATE vm_static
SET vm_type = 1
/* These are the GUIDs for the default templates
   set vm_type to 1 to change from Desktop to Server
*/
WHERE vm_type = 0
  AND vm_guid IN (
    '00000003-0003-0003-0003-0000000000be',
    '00000005-0005-0005-0005-0000000002e6',
    '00000009-0009-0009-0009-0000000000f1',
    '00000007-0007-0007-0007-00000000010a',
    '0000000b-000b-000b-000b-00000000021f',
    '00000000-0000-0000-0000-000000000000'
  )
