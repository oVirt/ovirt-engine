-- Avoid lower and upper case mixture, to match RESTful API enum backward compatibility
UPDATE cluster_policies SET name = 'evenly_distributed' WHERE name =  'Evenly_Distributed';
UPDATE cluster_policies SET name = 'power_saving' WHERE name =  'Power_Saving';
UPDATE cluster_policies SET name = 'none' WHERE name =  'None';
UPDATE cluster_policies SET name = 'vm_evenly_distributed' WHERE name =  'VM_Evenly_Distributed';
