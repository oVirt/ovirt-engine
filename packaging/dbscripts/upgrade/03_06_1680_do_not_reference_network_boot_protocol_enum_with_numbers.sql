UPDATE
  network_attachments
set boot_protocol =
  case boot_protocol
    when '0' then 'NONE'
    when '1' then 'DHCP'
    when '2' then 'STATIC_IP'
    when 'DHCP' then 'DHCP'
    when 'STATIC_IP' then 'STATIC_IP'
    else 'NONE'
  end;

ALTER TABLE
  network_attachments
ADD CONSTRAINT
  boot_protocol_enum_values
CHECK (boot_protocol = 'DHCP'
  or boot_protocol = 'STATIC_IP'
  or boot_protocol = 'NONE');
