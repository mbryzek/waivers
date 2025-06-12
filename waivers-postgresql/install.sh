#!/bin/sh

psql -U postgres -c 'create database waiverdb' postgres
psql -U postgres -c 'create role api login PASSWORD NULL' postgres > /dev/null
psql -U postgres -c 'GRANT ALL ON DATABASE waiverdb TO api' postgres
psql -U postgres -c 'ALTER DATABASE waiverdb OWNER TO api' postgres

sem-apply --url postgresql://api@localhost/waiverdb