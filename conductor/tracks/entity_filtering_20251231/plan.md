# Plan: Entity Filtering Support

## Phase 1: Core Definitions [checkpoint: a6261e8]
- [x] Task: Define `FilterType` Enum 64306c3
- [x] Task: Create `@Filterable` Annotation 659e9cc
- [x] Task: Conductor - User Manual Verification 'Core Definitions' (Protocol in workflow.md) a6261e8

## Phase 2: Specification Logic
- [x] Task: Implement `SearchCriteria` class 9684dc7
    - [ ] Subtask: Define Pojo to hold key, operation, and value
- [x] Task: Implement `GenericSpecification` 3c93b26
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
