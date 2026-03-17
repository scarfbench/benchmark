INSERT INTO vets(id, first_name, last_name) VALUES (1001, 'James', 'Carter');
INSERT INTO vets(id, first_name, last_name) VALUES (1002, 'Helen', 'Leary');
INSERT INTO vets(id, first_name, last_name) VALUES (1003, 'Linda', 'Douglas');
INSERT INTO vets(id, first_name, last_name) VALUES (1004, 'Rafael', 'Ortega');
INSERT INTO vets(id, first_name, last_name) VALUES (1005, 'Henry', 'Stevens');
INSERT INTO vets(id, first_name, last_name) VALUES (1006, 'Sharon', 'Jenkins');

INSERT INTO specialties VALUES (1001, 'radiology');
INSERT INTO specialties VALUES (1002, 'surgery');
INSERT INTO specialties VALUES (1003, 'dentistry');

INSERT INTO vet_specialties(vet_id, specialty_id) VALUES (1002, 1001);
INSERT INTO vet_specialties(vet_id, specialty_id) VALUES (1003, 1002);
INSERT INTO vet_specialties(vet_id, specialty_id) VALUES (1003, 1003);
INSERT INTO vet_specialties(vet_id, specialty_id) VALUES (1004, 1002);
INSERT INTO vet_specialties(vet_id, specialty_id) VALUES (1005, 1001);

INSERT INTO types VALUES (1001, 'cat');
INSERT INTO types VALUES (1002, 'dog');
INSERT INTO types VALUES (1003, 'lizard');
INSERT INTO types VALUES (1004, 'snake');
INSERT INTO types VALUES (1005, 'bird');
INSERT INTO types VALUES (1006, 'hamster');

INSERT INTO owners VALUES (1001, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO owners VALUES (1002, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO owners VALUES (1003, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO owners VALUES (1004, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198');
INSERT INTO owners VALUES (1005, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765');
INSERT INTO owners VALUES (1006, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO owners VALUES (1007, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387');
INSERT INTO owners VALUES (1008, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683');
INSERT INTO owners VALUES (1009, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435');
INSERT INTO owners VALUES (1010, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1001, 'Leo', '2010-09-07', 1001, 1001);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1002, 'Basil', '2012-08-06', 1006, 1002);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1003, 'Rosy', '2011-04-17', 1002, 1003);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1004, 'Jewel', '2010-03-07', 1002, 1003);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1005, 'Iggy', '2010-11-30', 1003, 1004);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1006, 'George', '2010-01-20', 1004, 1005);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1007, 'Samantha', '2012-09-04', 1001, 1006);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1008, 'Max', '2012-09-04', 1001, 1006);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1009, 'Lucky', '2011-08-06', 1005, 1007);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1010, 'Mulligan', '2007-02-24', 1002, 1008);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1011, 'Freddy', '2010-03-09', 1005, 1009);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1012, 'Lucky', '2010-06-24', 1002, 1010);
INSERT INTO pets(id, name, birth_date, type_id, owner_id) VALUES (1013, 'Sly', '2012-06-08', 1001, 1010);

INSERT INTO visits(id, pet_id, visit_date, description) VALUES (1001, 1007, '2013-01-01', 'rabies shot');
INSERT INTO visits(id, pet_id, visit_date, description) VALUES (1002, 1008, '2013-01-02', 'rabies shot');
INSERT INTO visits(id, pet_id, visit_date, description) VALUES (1003, 1008, '2013-01-03', 'neutered');
INSERT INTO visits(id, pet_id, visit_date, description) VALUES (1004, 1007, '2013-01-04', 'spayed');
