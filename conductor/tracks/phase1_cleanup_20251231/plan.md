# Plan: Phase 1 Finalization - Integration Tests & Minor Features

## Phase 1: Integration Testing Infrastructure
- [x] Task: Setup H2 Integration Test Environment for generated modules [322a035]
- [ ] Task: Implement Integration Tests for `RepositoryGenerator` (CRUD, Multi-entity)
- [ ] Task: Implement Integration Tests for `GenericCrudService`
- [ ] Task: Implement Integration Tests for `GenericCrudController` (MockMvc, Full Flow)
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Integration Testing Infrastructure' (Protocol in workflow.md)

## Phase 2: DtoMapper & Controller Polish
- [ ] Task: Refactor `GenericCrudController` to fully integrate `DtoMapper` for all endpoints
- [ ] Task: Implement Sort Parameter Validation in `GenericCrudController` / `GenericCrudService`
- [ ] Task: Add 400 Bad Request error handling for invalid sort fields
- [ ] Task: Conductor - User Manual Verification 'Phase 2: DtoMapper & Controller Polish' (Protocol in workflow.md)

## Phase 3: Documentation & HATEOAS
- [ ] Task: Enhance OpenAPI schemas with example values and Javadoc descriptions
- [ ] Task: Implement HATEOAS `_links` support in paged responses
- [ ] Task: Final verification of Phase 1 completeness across all modules
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Documentation & HATEOAS' (Protocol in workflow.md)

## Phase 4: Final Review and update docs
- [ ] Task: Update README
- [ ] Task: Update mkdocs
