-- ----------------------------------------------------------------------
--  table user_profiles
-- ----------------------------------------------------------------------

CREATE TABLE user_profiles
(
  profile_id UUID NOT NULL,
  user_id UUID NOT NULL,
  ssh_public_key TEXT,
  CONSTRAINT PK_profile_id PRIMARY KEY (profile_id)
) WITH OIDS;

ALTER TABLE user_profiles ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id)
      REFERENCES users (user_id)
      ON UPDATE NO ACTION ON DELETE CASCADE;

CREATE INDEX IDX_user_profiles_user_id ON user_profiles(user_id);
