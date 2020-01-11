drop table if exists users;

create table users(
  `id` varchar(64) not null,
  `name` varchar (255) not null,
  `age` int not null,
  primary key(id)
);
