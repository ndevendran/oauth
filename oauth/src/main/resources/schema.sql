create table if not exists Client (
	id identity,
	clientId varchar(36) not null,
	clientSecret varchar(36) not null,
	clientName varchar(25) not null,
	redirectUri varchar(2048) not null
);

create table if not exists "User" (
	id identity,
	username varchar(25) not null,
	password BINARY(60) not null
);

create table if not exists AuthCode (
	authorizationCode varchar(36) not null,
	clientId varchar(36) not null
);