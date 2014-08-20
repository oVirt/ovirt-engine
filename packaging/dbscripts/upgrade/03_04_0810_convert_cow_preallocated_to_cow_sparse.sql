UPDATE images SET volume_type = 2 WHERE volume_type = 1 AND volume_format = 4; -- Update volume type to sparse where the volume type is preallocated and the volume format is COW
