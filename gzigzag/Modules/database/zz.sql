
CREATE TABLE members (
	id	INT4,
	fname	TEXT,
	lname	TEXT,
	address	TEXT,
	phone	TEXT,
	email	TEXT,
	PRIMARY KEY(id)
);

CREATE TABLE positions (
	person	INT4,
	sig	INT4
/*	type	TEXT */
);

CREATE TABLE sigs (
	id	INT4,
	name	TEXT,
	PRIMARY KEY(id)
);

INSERT INTO members VALUES 
	(1,'Henna','Koskinen','Kuusikkokuja 4','321-564356','henna43@hotmail.com');
INSERT INTO sigs VALUES 
	(1,'helicopters');
INSERT INTO positions VALUES 
	(1,1);
