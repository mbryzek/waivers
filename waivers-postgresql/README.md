# Waivers Database

This directory contains the PostgreSQL database setup and migration scripts for the waivers application.

## Prerequisites

- PostgreSQL server running locally
- [schema-evolution-manager](https://github.com/mbryzek/schema-evolution-manager) installed

## Setup

### Initial Setup

1. Run the installation script to create the database:
   ```bash
   ./install.sh
   ```

   This will:
   - Create the `waiverdb` database
   - Create the `api` user role
   - Apply all migration scripts

### Development Updates

When new migration scripts are added, run:

```bash
./dev.rb
```

This applies any new migration scripts to your local database.

## Database Schema

The database includes the following main tables:

- `projects` - Waiver projects/organizations
- `users` - Users who sign waivers
- `waivers` - Waiver document versions
- `signatures` - User signatures on waivers
- `signature_templates` - Templates for external signature providers
- `signature_requests` - Requests to external signature providers

## Migration Scripts

Migration scripts are managed using schema-evolution-manager:

- Add new scripts: `sem-add ./new-script.sql`
- Apply scripts: `sem-apply --url postgresql://api@localhost/waiverdb`

## Test Data

The database includes sample test data for development purposes. See the latest migration script for examples.

## Database Connection

- **Database**: `waiverdb`
- **User**: `api`
- **Host**: `localhost`
- **Port**: `5432` (default)
- **Connection URL**: `postgresql://api@localhost/waiverdb`