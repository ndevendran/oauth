create table if not exists Client (
	id identity,
	clientId varchar(36) not null,
	clientSecret varchar(36) not null,
	clientName varchar(25) not null,
	redirectUri varchar(2048) not null,
	scope varchar(60000)
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

create table if not exists AccessToken (
	id identity,
	token varchar(36) not null,
	clientId varchar(36) not null,
	expirationTime DATETIME not null
);

create table if not exists RefreshToken (
	id identity,
	refreshToken varchar(36) not null,
	clientId varchar(36) not null,
	expirationTime DATETIME not null
);

create table if not exists RefreshToken_AccessToken (
	refreshToken bigint not null,
	token bigint not null
);

alter table RefreshToken_AccessToken
	add foreign key(refreshToken) references RefreshToken(id);

alter table RefreshToken_AccessToken
	add foreign key(token) references AccessToken(id);