
-- make blank template public

insert into permissions (id,role_id,ad_element_id,object_id,object_type_id)
 select uuid_generate_v1(),
'DEF00009-0000-0000-0000-DEF000000009', -- TemplateUser
 getGlobalIds('everyone'), 
 '00000000-0000-0000-0000-000000000000',    -- blank template id --
 4                                          -- template object type id --
 where not exists (
  select * from permissions where
  role_id = 'DEF00009-0000-0000-0000-DEF000000009'
  and
  ad_element_id = getGlobalIds('everyone')
  and
  object_id = '00000000-0000-0000-0000-000000000000' 
  and 
  object_type_id = 4);
