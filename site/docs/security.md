---
hide:
  - toc
---
# Security

Authentication and authorization patterns featuring Jakarta Security identity stores, form-based and basic authentication, EJB security, role-based access control, and password hashing.

## Examples

- **built-in-db-identity-store** - Jakarta Security `@DatabaseIdentityStoreDefinition` with Pbkdf2PasswordHash
- **custom-identity-store** - Custom IdentityStore implementation with `@ApplicationScoped` CDI bean
- **security-cart-secure** - EJB security with `@DeclareRoles` and `@RolesAllowed` annotations
- **security-converter-secure** - Servlet-based security with declarative role-based access control
- **security-hello1-formauth** - Form-based authentication with `@FormAuthenticationMechanismDefinition`
- **security-hello2-basicauth** - HTTP Basic authentication with `@BasicAuthenticationMechanismDefinition`
