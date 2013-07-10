-- os 0 is Unassigned and 6 is OTHER.
-- code-wise the defacto default still remains 0 and it will point to Other instead to Unassigned
UPDATE vm_static set os = 0 where os = 6;
