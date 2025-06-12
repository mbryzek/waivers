# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Structure

This is a waivers application with the following components:

- **backend/** - Scala Play Framework backend API
- **waivers-ui/** - Frontend UI (TBD)
- **waivers-postgresql/** - Database schema and migration scripts
- **dao/** - API Builder DAO specifications and generated files

## Development Guidelines

### Database Setup and Schema Management

**Important**: This project uses API Builder DAOs for database schema management. Follow this workflow:

1. **Define schema in API Builder JSON**: Edit `dao/spec/waivers-public.json` to define database models
2. **Generate DDL files**: Run `apibuilder_daos` from the `dao/` directory to generate PostgreSQL DDL files
3. **Apply to database**: Copy generated files from `dao/psql/` to `waivers-postgresql/` and use `sem-add` to add them
4. **Apply migrations**: Run `./dev.rb` from `waivers-postgresql/` to apply new schema changes

#### Database Convention Requirements:
- Use `sem-add ./filename.sql` to add new migration scripts
- All tables automatically get `created_at`, `updated_at`, `updated_by_user_id`, and `hash_code` columns
- Updated_at triggers are automatically created: `select schema_evolution_manager.create_updated_at_trigger('public', 'table_name');`
- Primary keys are always `id` field with format like `prj-{uuid}`, `usr-{uuid}`, etc.
- Use proper validation functions from `util` schema (e.g., `util.non_empty_trimmed_string(id)`)

#### Database Connection:
- **Database name**: `waiverdb` (convention: `<app name>db`)
- **User**: `api`
- **URL**: `postgresql://api@localhost/waiverdb`

### Scala Compilation Workflow
After making changes to the scala code, run the "sbt Test/compile" tool to verify that the code compiles. If not, review the errors and iterate on the implementation until all compilation errors are fixed.

### Testing
- Use ScalaTest with PlaySpec for testing
- Service tests extend `DefaultAppSpec` with helper traits like `DatabaseHelpers`
- Controller tests extend `DefaultServerSpec` with `MockApiClient`
- Test configuration uses the same database (`waiverdb`) but with evolutions disabled
- Test warnings are not treated as fatal errors (unlike main compilation)

### API Builder Integration
This project uses API Builder for:
- **API specifications** in `spec/` directory (waivers-api.json, waivers-admin.json, waivers-error.json)
- **DAO generation** from `dao/spec/` specifications
- **Client generation** with mock clients for testing
- **PostgreSQL DDL generation** with proper constraints and triggers

### Key Patterns Learned
1. **Database schema MUST be defined via API Builder DAOs**, not manual SQL
2. **Use schema-evolution-manager** (`sem-add`, `sem-apply`) for all database changes
3. **Database conventions** are enforced through API Builder DAO generation
4. **Test infrastructure** follows specific patterns with helpers and mock clients
5. **Compilation settings** differ between main code (strict) and tests (warnings allowed)

### File Generation
The following directories contain generated files and should not be edited manually:
- `backend/generated/` - Generated from API Builder specs
- `dao/psql/` - Generated DDL files from API Builder DAO specs
- `waivers-postgresql/scripts/` - Migration scripts managed by schema-evolution-manager

### Dependencies and Tools Required
- PostgreSQL with `api` user role
- [schema-evolution-manager](https://github.com/mbryzek/schema-evolution-manager) (`sem-add`, `sem-apply`)
- `apibuilder_daos` tool for DAO generation
- API Builder organization: `bryzek`

## Project-Specific Notes

This waivers application manages:
- **Projects**: Waiver projects/organizations (e.g., sports teams, events)
- **Users**: People who sign waivers
- **Waivers**: Document templates with versions
- **Signatures**: User signatures on waivers with optional external provider integration
- **Signature Templates/Requests**: Integration with external signature providers (HelloSign, DocuSign, etc.)

The application supports both simple form-based signatures and integration with external e-signature providers for more formal document signing workflows.