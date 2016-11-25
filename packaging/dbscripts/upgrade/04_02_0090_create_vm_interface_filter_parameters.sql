CREATE TABLE vm_interface_filter_parameters (
    id UUID NOT NULL CONSTRAINT pk_vm_interface_filter_parameters PRIMARY KEY,
    name CHARACTER VARYING(255),
    value CHARACTER VARYING(255),
    vm_interface_id UUID NOT NULL,
    FOREIGN KEY (vm_interface_id) REFERENCES vm_interface(id) ON DELETE CASCADE
);

CREATE INDEX idx_vm_interface_filter_parameters_vm_interface_id ON vm_interface_filter_parameters(vm_interface_id);

