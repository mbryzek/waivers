# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Structure

This is a waivers application with the following components:

- **backend/** - Scala Play Framework backend API (port 9300)
- **waivers-ui/** - Elm frontend with Tailwind CSS (port 8080/8081)  
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

### Internal Models Pattern
All internal case classes in `models.internal.Models` follow this established pattern:

#### For Database-Backed Models:
```scala
case class ModelName(
  // fields matching the API model structure
  id: String,
  // ... other fields
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

object ModelName {
  def apply(generated: db.generated.ModelName): ModelName = {
    ModelName(
      id = generated.id,
      // ... field mappings with enum conversions where needed
      enumField = EnumType.fromString(generated.enumField).getOrElse(EnumType.DefaultValue),
      createdAt = generated.createdAt,
      updatedAt = generated.updatedAt,
      updatedByUserId = generated.updatedByUserId
    )
  }

  implicit val format: Format[ModelName] = Json.format[ModelName]
}
```

#### Key Requirements:
- **Wrap db.generated models**: Use companion object `apply` method to convert from database models
- **Enum conversions**: Database stores enums as strings, internal models use proper Scala sealed traits
- **JSON serialization**: Use Play's `Json.format` for automatic JSON conversion
- **API Builder imports**: Use `io.bryzek.waivers.api.v0.models.json.*` for generated JSON converters when needed

#### Established Internal Models:
- **Project**: Wraps `db.generated.Project`
- **User**: Wraps `db.generated.User` 
- **Waiver**: Wraps `db.generated.Waiver`
- **SignatureTemplate**: Wraps `db.generated.SignatureTemplate` with `SignatureProvider` enum conversion
- **SignatureRequest**: Wraps `db.generated.SignatureRequest` with `SignatureProvider` and `SignatureRequestStatus` enum conversions
- **Signature**: Wraps `db.generated.Signature` with `SignatureStatus` enum conversion

### Scala 3 Style Guidelines
This project follows the [Scala Style Guide](https://docs.scala-lang.org/style/) with these key conventions:

#### Import Conventions:
- **Wildcard imports**: Use `*` (not `_`) for Scala 3 compatibility
  ```scala
  import io.bryzek.waivers.api.v0.models.json.*
  import play.api.libs.json.*
  import models.internal.*
  ```
- **Qualified imports**: Use aliases to avoid conflicts
  ```scala
  import io.bryzek.waivers.api.v0.{models => apiModels}
  ```
- **Specific imports**: Group related imports with curly braces
  ```scala
  import helpers.{DatabaseHelpers, DefaultServerSpec, MockApiClient}
  ```

#### Naming Conventions:
- **Classes/Traits**: PascalCase (`ProjectService`, `DatabaseHelpers`)
- **Objects**: PascalCase (`SignatureProvider`, `SignatureStatus`)
- **Methods/Variables**: camelCase (`createProject`, `uniqueSlug`)
- **Constants**: PascalCase (`HelloSign`, `DocuSign`)
- **Type Parameters**: Single uppercase letters (`T`, `A`, `B`)

#### Code Organization:
- **Package structure**: Follow domain-driven organization (`models.internal`, `services`, `controllers`)
- **Method ordering**: Group related functionality together
- **Companion objects**: Place directly after case class definition
- **Import ordering**: Standard library → Third-party → Project imports

#### JSON and API Patterns:
- **Use API Builder generated converters** instead of manual JSON field extraction
- **Prefer type-safe model parsing**: `contentAsJson(response).as[apiModels.Project]` over `(json \ "field").as[String]`
- **Transform internal models to API models** in controllers before serialization

### API and Database Design
- Prefer enums over boolean types in API and database schemas
  - Instead of using `is_active: boolean`, use `status: enum` with values like `active` or `inactive`
  - This provides more explicit and descriptive state representation

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

## Frontend Development (waivers-ui/)

### Elm Application Architecture
The frontend is built with Elm 0.19.1 following patterns established in acumen-ui:

**Project Structure:**
```
waivers-ui/
├── src/
│   ├── Main.elm                    # Application entry point with routing
│   ├── Route.elm                   # URL routing system  
│   ├── Ports.elm                   # JavaScript interop
│   ├── ValidationError.elm         # Validation error handling
│   ├── Generated/                  # API Builder generated clients
│   │   ├── ApiRequest.elm         # HTTP request handling
│   │   ├── IoBryzekWaiversApi.elm # Main API client
│   │   ├── IoBryzekWaiversAdmin.elm # Admin API client
│   │   └── IoBryzekWaiversError.elm # Error models
│   ├── Page/                       # Page modules with TEA pattern
│   │   ├── Index.elm              # Landing page
│   │   ├── Waiver.elm             # Waiver signing form
│   │   └── Admin.elm              # Admin dashboard
│   ├── Templates/                  # Reusable UI components
│   │   ├── Shell.elm              # Page layout and navigation
│   │   └── Forms.elm              # Form input components
│   └── Util/                       # Utility modules
├── elm.json                        # Elm dependencies
├── package.json                    # Node/npm dependencies  
├── tailwind.config.js              # Tailwind CSS configuration
├── run.sh                          # Development server (elm-live)
├── run-tailwind.sh                # CSS compilation
└── review.sh                       # Code quality (elm-review)
```

### Development Workflow

#### Frontend Development Commands:
```bash
# Start development server (port 8080/8081)
./run.sh

# Compile Tailwind CSS (separate terminal)
./run-tailwind.sh  

# Run code quality checks
./review.sh

# Generate API clients from backend specs
apibuilder update
```

#### Backend Integration:
- **API Host**: `http://localhost:9300` (configurable in Page.Waiver)
- **CORS**: Backend configured to allow requests from frontend ports
- **API Builder**: Type-safe API clients generated from JSON specifications

### Key Implementation Patterns

#### API Integration:
```elm
-- Custom API request for correct URL structure
createSignatureRequest : String -> WaiverForm -> Api.HttpRequestParams -> Cmd Msg
createSignatureRequest slug waiverForm params =
    Http.request
        { method = "POST"
        , url = params.apiHost ++ "/projects/" ++ slug ++ "/signatures"
        , expect = Generated.ApiRequest.expectJson SignatureResponse Api.signatureDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.jsonBody (Api.waiverFormEncoder waiverForm)
        }
```

#### External URL Redirection:
```elm
-- Port for HelloSign redirection
port redirectToExternalUrl : String -> Cmd msg

-- Usage in JavaScript
app.ports.redirectToExternalUrl.subscribe(function(url) {
  window.location.href = url;
});
```

#### Form Validation:
```elm
-- Client-side validation with real-time feedback
let
    isValid = not (String.isEmpty model.firstName || String.isEmpty model.lastName || String.isEmpty model.email)
    buttonText = 
        if model.isSubmitting then 
            "Processing..." 
        else if not isValid then
            "Please fill required fields"
        else 
            "Sign Waiver"
```

#### Error Handling:
```elm
-- Type-safe API error handling
apiErrorToString : ApiError -> String
apiErrorToString error =
    case error of
        Generated.ApiRequest.ApiErrorSystem msg ->
            "System error: " ++ msg
        Generated.ApiRequest.ApiErrorValidation errors ->
            "Validation errors: " ++ String.join ", " (List.map .message errors)
        -- ... other error cases
```

### JSON Field Mapping Issues

**Critical Learning**: API Builder generates snake_case JSON but Elm expects camelCase.

#### Backend Controller Fix:
```scala
// Use fully qualified API model types to get proper JSON readers
form <- request.body.validate[apiModels.WaiverForm].asEither.leftMap(errors => 
  List(GenericError("validation_failed", "Invalid form data: " + JsError.toJson(errors)))
)

// Convert API model to internal model
private def toInternalWaiverForm(apiForm: apiModels.WaiverForm): WaiverForm = {
  WaiverForm(
    firstName = apiForm.firstName,
    lastName = apiForm.lastName, 
    email = apiForm.email,
    phone = apiForm.phone
  )
}
```

#### Generated JSON Mapping:
API Builder automatically handles snake_case ↔ camelCase conversion:
- **Frontend sends**: `{"first_name": "John", "last_name": "Doe"}`
- **Backend receives**: `WaiverForm(firstName = "John", lastName = "Doe")`
- **Key**: Import `io.bryzek.waivers.api.v0.models.json.*` for proper readers

### CORS Configuration

#### Backend CORS Setup:
```conf
# application.conf
play.filters.cors {
  allowedOrigins = ["http://localhost:3000", "http://localhost:8080", "http://localhost:8081"]
  allowedHttpMethods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
  allowedHttpHeaders = ["Accept", "Content-Type", "Origin", "X-Requested-With"]
}
```

#### Frontend Development Ports:
- **8080**: Standard development port (run.sh default)
- **8081**: Alternative port for testing/conflicts
- **IMPORTANT**: Backend CORS must include all frontend ports

### Responsive Design

#### Mobile-First Approach:
```elm
-- Tailwind CSS classes for responsive forms
div [ class "w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500" ]

-- Responsive grid layouts  
div [ class "grid md:grid-cols-2 gap-8" ]
```

#### Form Components:
```elm
-- Reusable form input with proper styling
textInput : { value : String, placeholder : String, onInput : String -> msg } -> Html msg
textInput { value, placeholder, onInput } =
    input
        [ type_ "text"
        , Attr.value value
        , Attr.placeholder placeholder
        , Html.Events.onInput onInput
        , class "w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        ]
        []
```

### Testing and Quality

#### Code Quality Tools:
- **elm-review**: Configured with comprehensive rule set
- **Tailwind CSS**: Consistent styling with utility classes
- **elm-live**: Hot reloading during development

#### API Builder Integration:
- **Type Safety**: Generated Elm types from backend specifications
- **Automatic Updates**: `apibuilder update` regenerates clients
- **Error Models**: Shared error handling between frontend/backend

### User Experience Flow

#### Waiver Signing Process:
1. **User visits**: `/waiver/{project-slug}`
2. **Form validation**: Real-time validation with disabled submit
3. **API request**: POST to `/projects/{slug}/signatures`
4. **Backend processing**: Creates user, signature, HelloSign request
5. **URL extraction**: Gets `signnowUrl` from response
6. **External redirect**: Browser navigates to HelloSign
7. **Completion**: User signs and receives confirmation

#### Error States:
- **Network errors**: User-friendly error messages
- **Validation errors**: Field-level feedback
- **API errors**: Formatted error display
- **Loading states**: Button text changes and disabled state

### Dependencies and Tools

#### Required for Development:
- **Elm 0.19.1**: Core language and compiler
- **elm-live**: Development server with hot reloading
- **Node.js/npm**: For Tailwind CSS and elm-review
- **Tailwind CSS**: Utility-first CSS framework
- **API Builder CLI**: For client code generation

#### Build Process:
1. **Elm compilation**: `elm-live src/Main.elm --output=elm.js`
2. **CSS processing**: `tailwindcss -i ./input.css -o ./style.css`
3. **API generation**: `apibuilder update` (when specs change)
4. **Code review**: `elm-review --fix` for quality checks

This frontend provides a complete, production-ready waiver signing experience with proper error handling, responsive design, and integration with external signature providers.