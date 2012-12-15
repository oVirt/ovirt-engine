-- Add the vm_guest_agent_interfaces table.
CREATE TABLE vm_guest_agent_interfaces
(
   vm_id UUID NOT NULL,
   interface_name VARCHAR (50),
   mac_address VARCHAR(59),
   ipv4_addresses text,
   ipv6_addresses text,
   CONSTRAINT FK_vm_guest_agent_interfaces FOREIGN KEY(vm_id) REFERENCES vm_static(vm_guid) ON UPDATE NO ACTION ON DELETE CASCADE
) WITH OIDS;
