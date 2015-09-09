update vm_static set default_display_type =
 case default_display_type
  when 0 then 2 -- vga changed from 0 to 2
  when 1 then 0 -- cirrus(vnc) restored from 1 to 0
  when 2 then 1 -- qxl restored from 2 to 1
 end;
