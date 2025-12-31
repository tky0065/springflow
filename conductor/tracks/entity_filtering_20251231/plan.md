# Plan: Entity Filtering Support

## Phase 1: Core Definitions
- [ ] Task: Define `FilterType` Enum
    - [ ] Subtask: Create enum with values (EQUALS, LIKE, IN, GT, LT, etc.)
- [ ] Task: Create `@Filterable` Annotation
    - [ ] Subtask: Define annotation with `FilterType[]` attribute
- [ ] Task: Conductor - User Manual Verification 'Core Definitions' (Protocol in workflow.md)

## Phase 2: Specification Logic
- [ ] Task: Implement `SearchCriteria` class
    - [ ] Subtask: Define Pojo to hold key, operation, and value
- [ ] Task: Implement `GenericSpecification`
    - [ ] Subtask: Create class implementing `Specification<T>`
    - [ ] Subtask: Implement `toPredicate` logic for each `FilterType`
- [ ] Task: Implement `SpecificationBuilder`
    - [ ] Subtask: Logic to combine multiple specs
- [ ] Task: Conductor - User Manual Verification 'Specification Logic' (Protocol in workflow.md)

## Phase 3: Integration
- [ ] Task: Update `GenericCrudController`
    - [ ] Subtask: Add argument resolver for filters
    - [ ] Subtask: Pass specifications to service
- [ ] Task: Update `GenericCrudService`
    - [ ] Subtask: Change `findAll` to accept `Specification`
- [ ] Task: Verify Integration Tests
    - [ ] Subtask: Create tests for filtering scenarios
- [ ] Task: Conductor - User Manual Verification 'Integration' (Protocol in workflow.md)
