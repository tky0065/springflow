# Plan: JpaSpecificationExecutor Support

## Phase 1: Foundation (Annotation & Metadata) [checkpoint: 03efefd]
- [x] Task: TDD - Add `supportSpecification` to `@AutoApi` annotation 4945c07
- [x] Task: TDD - Update metadata processing to extract specification support flag 67b442a
- [x] Task: Conductor - User Manual Verification 'Phase 1: Foundation' (Protocol in workflow.md)

## Phase 2: Repository Generation
- [x] Task: TDD - Update Dynamic Repository Generator to extend `JpaSpecificationExecutor` conditionally da721b9
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Repository Generation' (Protocol in workflow.md)

## Phase 3: DTOs & Search Logic
- [ ] Task: TDD - Implement `FilterDTO` generation logic for entities
- [ ] Task: TDD - Implement `SpecificationBuilder` utility to convert `FilterDTO` to `Specification`
- [ ] Task: TDD - Update Service generator to include search methods using Specifications
- [ ] Task: Conductor - User Manual Verification 'Phase 3: DTOs & Search Logic' (Protocol in workflow.md)

## Phase 4: Controller & API
- [ ] Task: TDD - Update Controller generator to add `POST /search` endpoint
- [ ] Task: TDD - Ensure search endpoint is documented in OpenAPI/Swagger
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Controller & API' (Protocol in workflow.md)

## Phase 5: Verification & Integration
- [ ] Task: Update `springflow-demo` to demonstrate the new search functionality
- [ ] Task: Final integration test pass for all generated layers
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Verification & Integration' (Protocol in workflow.md)