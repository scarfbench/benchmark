DROP TABLE IF EXISTS visits;

CREATE TABLE visits (
  id          INTEGER AUTO_INCREMENT PRIMARY KEY,
  pet_id      INTEGER NOT NULL,
  visit_date  DATE,
  description VARCHAR(8192)
);
