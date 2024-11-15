create table if not exists Client (
	clientId varchar(25) not null,
	clientSecret varchar(36) not null,
	redirectUri varchar(25) not null
);