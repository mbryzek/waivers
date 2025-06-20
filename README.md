# Waivers Application

A modern, secure digital waiver signing system built with Scala Play Framework and Elm.

## Overview

The Waivers application allows organizations to create digital waiver projects and collect legally binding signatures from participants. It supports multiple projects, HelloSign integration for digital signatures, and provides admin tools for managing and exporting signed waivers.

## Features

- **Multi-Project Support**: Create and manage multiple waiver projects
- **Digital Signatures**: Integration with HelloSign for legally binding signatures
- **Mobile-Friendly**: Responsive design optimized for all devices
- **Admin Dashboard**: View, search, and export signed waivers
- **Email Notifications**: Automatic delivery of signed documents
- **Secure**: Modern security practices and audit trails

## Architecture

### Backend (Scala + Play Framework)
- **API-First Design**: REST APIs defined with API Builder specifications
- **Database**: PostgreSQL with Schema Evolution Manager for migrations
- **Authentication**: Placeholder for future authentication system
- **Digital Signatures**: HelloSign API integration
- **Email**: Play Mailer for sending signed documents

### Frontend (Elm + Tailwind CSS)
- **Single Page Application**: Elm 0.19.1 with client-side routing
- **Responsive Design**: Tailwind CSS with mobile-first approach
- **Type Safety**: Generated API clients from API Builder specs
- **Modern UI**: Clean, accessible interface

### Database Schema
- **projects**: Waiver projects with URL-friendly slugs
- **users**: Participant information
- **waivers**: Waiver template versions
- **signatures**: Signed waiver records with status tracking

## Getting Started

### Prerequisites
- PostgreSQL 12+
- Scala 3.6+ and SBT
- Node.js 18+ and npm
- Schema Evolution Manager
- API Builder CLI (optional, for code generation)

### Database Setup
```bash
cd waivers-postgresql
./install.sh
./dev.rb
```

### Backend Setup
```bash
cd waivers
sbt run
```
Backend will be available at http://localhost:9300

### Frontend Setup
```bash
cd waivers-ui
npm install
./run.sh
```
Frontend will be available at http://localhost:8080

In a separate terminal, run Tailwind CSS compilation:
```bash
cd waivers-ui
./run-tailwind.sh
```

## Development Workflow

### API Builder Integration
1. Update API specifications in `spec/` directories
2. Run `apibuilder update` to generate client code
3. Implement backend services using generated models
4. Build frontend using generated Elm clients

### Database Migrations
1. Create new SQL files in `waivers-postgresql/scripts/`
2. Run `./dev.rb` to apply migrations

### Code Generation
- **Backend**: Controllers, models, and routes generated from API Builder specs
- **Frontend**: Elm models and HTTP clients generated from API Builder specs
- **Database**: Table definitions generated from DAO specifications

## Environment Configuration

### Backend Environment Variables
```bash
# Database
DB_URL=jdbc:postgresql://localhost/waivers_development
DB_USER=postgres
DB_PASSWORD=

# HelloSign Integration
HELLOSIGN_API_KEY=your_api_key
HELLOSIGN_CLIENT_ID=your_client_id

# Email
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USER=your_smtp_user
SMTP_PASSWORD=your_smtp_password

# Application
APP_BASE_URL=http://localhost:9300
```

## API Endpoints

### Public API
- `GET /projects/:slug` - Get project information
- `GET /projects/:slug/waiver` - Get current waiver for project
- `POST /projects/:slug/signatures` - Create new signature
- `GET /signatures/:id` - Get signature status

### Admin API
- `GET /admin/projects` - List projects
- `POST /admin/projects` - Create project
- `GET /admin/projects/:id` - Get project details
- `PUT /admin/projects/:id` - Update project
- `GET /admin/signatures` - List signatures with filtering
- `POST /admin/exports` - Export signatures

### Webhooks
- `POST /webhooks/hellosign` - HelloSign webhook handler

## Usage

### Creating a Waiver Project
1. Use the admin API to create a new project with a unique slug
2. The project will be accessible at `/waiver/{slug}`
3. Share the waiver URL with participants

### Signing a Waiver
1. Participant visits `/waiver/{project-slug}`
2. Fills out their information (name, email, optional phone)
3. Reviews waiver content and clicks "Sign Waiver"
4. Redirected to HelloSign for digital signature
5. Receives confirmation and email copy after signing

### Admin Management
1. View all projects and their statistics
2. Search and filter signed waivers
3. Export waiver data as CSV with PDF attachments
4. Manage project status (active/inactive)

## Security Considerations

- All form inputs are validated on both client and server
- Database queries use parameterized statements to prevent SQL injection
- HTTPS enforcement in production
- HelloSign provides legally binding digital signatures
- Audit trails for all database changes

## Deployment

The application is designed to be deployed using the existing infrastructure patterns:
- Backend: Standard Play Framework deployment
- Frontend: Static files served from CDN
- Database: PostgreSQL with connection pooling
- Configuration: Environment variables for secrets

## Contributing

1. Follow existing code patterns and conventions
2. Update API Builder specifications before changing interfaces
3. Run tests before submitting changes
4. Use the established Git workflow

## License

MIT License - see LICENSE file for details

## Support

For questions or issues, contact the development team or create an issue in the repository.