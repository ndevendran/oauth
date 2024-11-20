create table if not exists Client (
	clientId varchar(25) not null,
	clientSecret varchar(36) not null,
	redirectUri varchar(25) not null
);

create table if not exists AuthUser (
	id identity,
	username varchar(25) not null,
	password BINARY(60) not null
);