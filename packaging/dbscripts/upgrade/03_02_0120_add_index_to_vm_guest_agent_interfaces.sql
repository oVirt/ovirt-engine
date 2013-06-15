-- Create partial index for fetching guest agent nics for a VM
CREATE INDEX IDX_vm_guest_agent_interfaces_vm_id ON vm_guest_agent_interfaces(vm_id);
