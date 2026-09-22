# i-Wish

A Java desktop client-server application where users add friends, build a wish list, browse
their friends' wish lists, and contribute money toward buying items on those lists.

Built for the JETS **Orange Belt — Building Java Desktop Applications** track.

## Features

- Register / Sign-in
- Add / remove friends, accept or decline friend requests
- Create, update, delete items on your own wish list
- View your friends list and browse a friend's wish list
- Contribute a specific amount of money toward an item on a friend's wish list
- Notifications: buyers are notified when an item's funding completes; receivers are notified
  when a friend contributes to one of their items
- Multi-client TCP server backed by MySQL

## Tech stack / 3rd-party libraries

- Java 11+, Swing
- Maven (see `pom.xml`)
- `mysql-connector-j` 8.0.33 — JDBC driver
- `flatlaf` 3.4 and `flatlaf-intellij-themes` 3.4 — modern UI theme

## Getting started

1. Run `src/main/resources/db/iwish_schema.sql` against your MySQL server to create the
   `iwish_db` database and starter catalog data.
2. Edit `src/main/resources/config.properties` with your MySQL credentials.
3. Build with `mvn clean package`, then run the server (`ServerMain`) and one or more clients
   (`ClientMain`).

A recorded demo of the app is included as `demo.mp4`.

## Team

| Name | GitHub | Role |
|---|---|---|
| Mohamed Wael | [@MohamedWael05](https://github.com/MohamedWael05) | |
| Doha Alaraby | [@dohaalaraby-lang](https://github.com/dohaalaraby-lang) | |
| Eyad Hatem | [@eyaddhatem](https://github.com/eyaddhatem) | |
| Mohamed | [@Mo7amed107](https://github.com/Mo7amed107) | |
| Shaimaa Kotit | [@shaimaakotit-29](https://github.com/shaimaakotit-29) | |
| Rohima Ahmed | [@rohimaahmed101-sys](https://github.com/rohimaahmed101-sys) | |
