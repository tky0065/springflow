# Plan: Phase 2 - Advanced Security & JPA Relationships

## Phase 1: Declarative API Security [checkpoint: c5141bd]
- [x] Task: Define `@SecuredApi` annotation in `springflow-annotations` 22b6a55
- [x] Task: Implement role-based security enforcement in `GenericCrudController` 08aa4ce
- [x] Task: Integration tests for secured entities with different user roles 4e4bec8

## Phase 2: Enhanced Relationship Mapping
- [ ] Task: Update `DtoMapper` to handle JPA relationship annotations
- [ ] Task: Implement circular reference detection and recursion depth control
- [ ] Task: Add support for relationship projection (e.g., summary DTOs for relations)
- [ ] Task: Integration tests for complex entity graphs (Circular, Many-to-Many)

## Phase 3: Custom Controller Integration
- [ ] Task: Refactor `RequestMappingRegistrar` for better custom method detection
- [ ] Task: Enable easy overriding of generated CRUD methods
- [ ] Task: Documentation update for custom extensions and security

## Phase 4: Finalization
- [ ] Task: Full regression testing across all features
- [ ] Task: Conductor - User Manual Verification
