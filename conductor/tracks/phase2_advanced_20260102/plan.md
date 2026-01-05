# Plan: Phase 2 - Advanced Security & JPA Relationships

## Phase 1: Declarative API Security [checkpoint: c5141bd]
- [x] Task: Define `@SecuredApi` annotation in `springflow-annotations` 22b6a55
- [x] Task: Implement role-based security enforcement in `GenericCrudController` 08aa4ce
- [x] Task: Integration tests for secured entities with different user roles 4e4bec8

## Phase 2: Enhanced Relationship Mapping [checkpoint: 6f95e9a]
- [x] Task: Update `DtoMapper` to handle JPA relationship annotations 1d1aa17
- [x] Task: Implement circular reference detection and recursion depth control adc9ba4
- [x] Task: Add support for relationship projection (e.g., summary DTOs for relations) 2e12a0f
- [x] Task: Integration tests for complex entity graphs (Circular, Many-to-Many) d1984f1

## Phase 3: Custom Controller Integration [checkpoint: d54c82b]
- [x] Task: Refactor `RequestMappingRegistrar` for better custom method detection d54c82b
- [x] Task: Enable easy overriding of generated CRUD methods d54c82b
- [x] Task: Documentation update for custom extensions and security d54c82b

## Phase 4: Finalization [checkpoint: 5214424]
- [x] Task: Full regression testing across all features 5214424
- [x] Task: Conductor - User Manual Verification 5214424