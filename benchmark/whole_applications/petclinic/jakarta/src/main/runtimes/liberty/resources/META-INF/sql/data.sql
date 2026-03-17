--
-- Seed data for Jakarta EE Petclinic
--

INSERT INTO owner (id, address, address_info, city, email, first_name, housenumber, lastname, phonenumber, searchindex, uuid, zipcode) VALUES (1, 'Glatzer Str.', NULL, 'Berlin', 'thomas.woehlke@gmail.com', 'Thomas', '5a', 'Wöhlke', '+493052007953', 'Thomas W hlke Glatzer Str 5a 10247 Berlin  493052007953 thomas woehlke gmail com ', '6ee91567-a8eb-4c53-91fe-c4bd88ca3f11', '10247');
INSERT INTO owner (id, address, address_info, city, email, first_name, housenumber, lastname, phonenumber, searchindex, uuid, zipcode) VALUES (2, 'Hill Drive', 'Auf dem Schrottplatz von Titus Jonas', 'Rocky Beach', 'thomas.woehlke@rub.de', 'Justus', '128', 'Jonas', '+4923452007953', 'Auf dem Schrottplatz von Titus Jonas Justus Jonas Hill Drive 128 99777 Rocky Beach  4923452007953 thomas woehlke rub de ', '0349af29-16f3-4ffc-b3ab-e23daea5a90b', '99777');

INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (1, 'Dog', 'Dog ', 'f0d464c6-2149-4c6c-b477-bec0766b8052');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (2, 'Cat', 'Cat ', 'b88b6449-b44b-4cc4-b30a-311a3d1c3361');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (3, 'Mouse', 'Mouse ', '052ccd24-43e4-4181-8b32-ad3244b93199');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (4, 'Hamster', 'Hamster ', 'ee0149cf-5efb-420c-8e81-93143b97dfea');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (5, 'Rabbit', 'Rabbit ', '3f593756-7350-4580-8300-1b1e943dc026');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (6, 'Fish', 'Fish ', '4fd9a619-d9a5-43c4-b725-ca17ab60159f');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (8, 'Guinea pig', 'Guinea pig ', '3389ac8c-a541-4565-9577-3e93bdfc3ad1');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (9, 'Pigeon', 'Pigeon ', '91cbd075-d8cc-4b92-a5a6-0db340ddf019');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (10, 'Snake', 'Snake ', 'a23a4023-0c57-44a4-bc16-7949460b980e');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (11, 'Spider', 'Spider ', '070aacf3-08ca-4820-a6ee-bd011dff4e21');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (7, 'Pony', 'Pony ', 'd76b2627-af8d-4f7f-88e7-0fcee289f3ce');
INSERT INTO owner_pet_pettype (id, name, searchindex, uuid) VALUES (12, 'Donkey', 'Donkey ', '3417d73c-c8a3-4034-b37b-c31ebcd4cb49');

INSERT INTO owner_pet (id, birth_date, name, searchindex, uuid, owner_id, owner_pet_pettype_id) VALUES (1, '2018-02-10', 'Roger', 'Roger ', '2fa7e4f8-4009-48eb-b175-54a3bd5d9e3a', 2, 5);
INSERT INTO owner_pet (id, birth_date, name, searchindex, uuid, owner_id, owner_pet_pettype_id) VALUES (2, '2021-08-15', 'Jerry', 'Jerry ', '7cf6da17-52f0-401e-a8f8-8d7f3d63ea16', 2, 3);
INSERT INTO owner_pet (id, birth_date, name, searchindex, uuid, owner_id, owner_pet_pettype_id) VALUES (3, '2020-06-10', 'Tom', 'Tom ', 'a553f6aa-ac28-440d-8f28-981cddafec86', 2, 2);
INSERT INTO owner_pet (id, birth_date, name, searchindex, uuid, owner_id, owner_pet_pettype_id) VALUES (4, '2020-06-14', 'Python', 'Python ', '0b8c7059-063f-4c55-bd22-0c8f9bc3b9c5', 1, 10);
INSERT INTO owner_pet (id, birth_date, name, searchindex, uuid, owner_id, owner_pet_pettype_id) VALUES (5, '2022-02-18', 'Tarantula', 'Tarantula ', 'faf15ccf-383a-4b90-9155-e41e6107d658', 1, 11);

INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (1, '2022-08-09', 'Routine', '2022 08 08T22 00 00Z Routine ', 'a9affdd1-7eeb-4baa-b52d-46c3711396c4', 1);
INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (2, '2022-02-06', 'Routine ASDQWER JOLO', '2022 02 05T23 00 00Z Routine ASDQWER JOLO ', '92dba22d-ac1a-4f53-8528-1bf211a2173e', 4);
INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (3, '2022-03-08', 'Routine KO', '2022 03 07T23 00 00Z Routine KO ', 'c7257865-f858-4760-a6c6-cf66efd35794', 4);
INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (4, '2022-03-10', 'Routine ZZ', '2022 03 09T23 00 00Z Routine ZZ ', '4f64804b-a1ae-412f-9c46-ecf897ef80d0', 5);
INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (5, '2022-08-10', 'Routine KO', '2022 08 09T22 00 00Z Routine KO ', '23f9c0a1-8d3c-4327-b8c4-0100207419fb', 5);
INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (6, '2022-08-18', 'Routine ASDQWER 11', '2022 08 17T22 00 00Z Routine ASDQWER 11 ', '0ab10181-9724-44e3-9fd7-252cc8b64dda', 2);
INSERT INTO owner_pet_visit (id, visit_date, description, searchindex, uuid, owner_pet_id) VALUES (7, '2022-10-10', 'Routine 333', '2022 10 09T22 00 00Z Routine 333 ', '19a5978b-bbbc-49f1-bd7a-d2e785533290', 3);

INSERT INTO specialty (id, name, searchindex, uuid) VALUES (1, 'Cardiologist', 'Cardiologist ', 'b1f1c1ec-c823-4ff2-b8de-9905ce802041');
INSERT INTO specialty (id, name, searchindex, uuid) VALUES (2, 'Radiologist', 'Radiologist ', 'dfe23ba1-9fc7-428a-aae0-99cd69887286');
INSERT INTO specialty (id, name, searchindex, uuid) VALUES (3, 'Rescue Doctor', 'Rescue Doctor ', '2ff652fb-df03-48af-aa77-f7b47e155604');
INSERT INTO specialty (id, name, searchindex, uuid) VALUES (4, 'Surgeon', 'Surgeon ', '4247ab00-93f8-4593-9c8e-bea2b5f56d2f');
INSERT INTO specialty (id, name, searchindex, uuid) VALUES (5, 'Neurosurgeon', 'Neurosurgeon ', 'e6643c99-7e95-410c-9184-a3b0c1edb597');
INSERT INTO specialty (id, name, searchindex, uuid) VALUES (6, 'Anesthetist', 'Anesthetist ', 'd021c584-feda-4f79-80e6-c2e22cfdc880');
INSERT INTO specialty (id, name, searchindex, uuid) VALUES (7, 'Shaman', 'Shaman ', 'd9814cff-01e7-4f3d-b1b1-46b6df125641');

INSERT INTO vet (id, first_name, lastname, searchindex, uuid) VALUES (1, 'Marie', 'Curie', 'Marie Curie Neurosurgeon Rescue Doctor Shaman Cardiologist Radiologist Anesthetist Surgeon ', '8f05d625-6bcb-4762-a500-46df2f8ebdb5');
INSERT INTO vet (id, first_name, lastname, searchindex, uuid) VALUES (2, 'Walther', 'von der Vogelweide', 'Walther von der Vogelweide Shaman ', '7c3620bd-8c04-41b3-818c-db32434f8fea');

INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 5);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 3);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 7);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 1);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 2);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 6);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (1, 4);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (2, 7);

ALTER SEQUENCE owner_seq RESTART WITH 100;
ALTER SEQUENCE owner_pet_seq RESTART WITH 100;
ALTER SEQUENCE owner_pet_pettype_seq RESTART WITH 100;
ALTER SEQUENCE owner_pet_visit_seq RESTART WITH 100;
ALTER SEQUENCE specialty_seq RESTART WITH 100;
ALTER SEQUENCE vet_seq RESTART WITH 100;
