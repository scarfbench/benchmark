## New Application Contribution

Thank you for contributing a new benchmark application to SCARFBench!

### Application Details

- **Application Name**: 
- **Category**: (e.g., `business_domain`, `dependency_injection`, `infrastructure`, `integration`, `persistence`, `presentation`, `security`, `whole_applications`)
- **Frameworks Included**: 
  - [ ] Jakarta EE
  - [ ] Quarkus
  - [ ] Spring Boot
- **Key Features Being Tested**: 

### Description

Briefly describe what this application demonstrates and why it's a valuable addition to the benchmark suite:

## Testing Completed

### For Each Framework (Jakarta, Quarkus, Spring)

Verify that all implementations work by checking off each item:

- [ ] `just build` succeeds for Jakarta
- [ ] `just build` succeeds for Quarkus
- [ ] `just build` succeeds for Spring
- [ ] `just up` starts container and logs show app is ready (all frameworks)
- [ ] `just test` passes all smoke tests (all frameworks)
- [ ] `just logs` displays application logs (all frameworks)
- [ ] `just down` cleans up successfully (all frameworks)
- [ ] `just local` runs application locally where applicable

### Smoke Tests

- [ ] Smoke tests validate real application functionality (not just health checks)
- [ ] Tests exit with code `0` on success, non-zero on failure
- [ ] Tests work inside container via `docker exec`
- [ ] Tests have clear `[PASS]`/`[FAIL]` messages
- [ ] Tests handle network errors gracefully
- [ ] All three frameworks have equivalent test behavior

### Dockerfile Requirements

For each framework:
- [ ] Uses appropriate base image (Maven + JDK)
- [ ] Installs required runtime dependencies
- [ ] Does NOT include `EXPOSE` directive
- [ ] Makes scripts executable (chmod +x where needed)
- [ ] Copies all necessary files
- [ ] Sets up working directory correctly
- [ ] Uses correct Maven goal in CMD

### Justfile Requirements

For each framework:
- [ ] `APP_NAME` and `IMAGE_NAME` variables defined
- [ ] `build` target builds Docker image
- [ ] `rebuild` target rebuilds without cache
- [ ] `up` target starts container and waits for correct startup pattern
- [ ] `logs` target streams container logs
- [ ] `down` target cleans up container
- [ ] `test` target runs smoke tests successfully
- [ ] `local` target runs without Docker (where applicable)

### Framework-Specific Startup Patterns

- [ ] **Jakarta**: Waits for `CWWKF0011I` in logs
- [ ] **Quarkus**: Waits for `started in` pattern in logs
- [ ] **Spring Boot**: Waits for `Tomcat started on port` or `Started` pattern

## File Structure

Verify your submission includes all required files:

```
benchmark/<category>/<application-name>/
├── README.md
├── jakarta/
│   ├── Dockerfile
│   ├── justfile
│   ├── smoke.py (or smoke/ folder)
│   ├── pom.xml
│   ├── .mvn/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── src/
├── quarkus/
│   ├── Dockerfile
│   ├── justfile
│   ├── smoke.py (or smoke/ folder)
│   ├── pom.xml
│   ├── .mvn/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── src/
└── spring/
    ├── Dockerfile
    ├── justfile
    ├── smoke.py (or smoke/ folder)
    ├── pom.xml
    ├── .mvn/
    ├── mvnw
    ├── mvnw.cmd
    └── src/
```

## Documentation

- [ ] README.md in the application directory explains its purpose
- [ ] README.md documents key features and patterns demonstrated
- [ ] Smoke test behavior is documented
- [ ] Any special requirements or configuration are noted
- [ ] All three framework implementations have identical external behavior

## Smoke Test Implementation

**Technology Used**: (e.g., Python/pytest, Shell script, JavaScript, etc.)

Note: This benchmark supports any testing approach. See [CONTRIBUTING.md](../../CONTRIBUTING.md#alternative-smoke-test-implementations) for examples in different languages.

## Verification Commands

Confirm you've run these commands successfully for each framework:

```bash
cd benchmark/<category>/<app-name>/jakarta
just build && just up && just test && just down

cd ../quarkus
just build && just up && just test && just down

cd ../spring
just build && just up && just test && just down
```

- [ ] All commands completed successfully for all three frameworks

## Additional Notes

Any additional context, implementation decisions, or special considerations:

---

**Reviewers**: Please verify:
- [ ] All three frameworks (Jakarta, Quarkus, Spring) are present and working
- [ ] Smoke tests pass and validate real functionality
- [ ] Dockerfile and justfile follow established patterns
- [ ] No hardcoded paths or environment-specific configurations
- [ ] Application fits the specified category appropriately
- [ ] Documentation is clear and complete
