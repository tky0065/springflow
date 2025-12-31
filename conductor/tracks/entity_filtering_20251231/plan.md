# Plan: Entity Filtering Support

## Phase 1: Core Definitions [checkpoint: a6261e8]
- [x] Task: Define `FilterType` Enum 64306c3
- [x] Task: Create `@Filterable` Annotation 659e9cc
- [x] Task: Conductor - User Manual Verification 'Core Definitions' (Protocol in workflow.md) a6261e8

## Phase 2: Specification Logic [checkpoint: 92f3edf]
- [x] Task: Implement `SearchCriteria` class 9684dc7
    - [x] Subtask: Define Pojo to hold key, operation, and value
- [x] Task: Implement `GenericSpecification` 3c93b26
    - [x] Subtask: Create class implementing `Specification<T>`
    - [x] Subtask: Implement `toPredicate` logic for each `FilterType`
- [x] Task: Implement `SpecificationBuilder` 14b5d73
    - [x] Subtask: Logic to combine multiple specs
- [x] Task: Conductor - User Manual Verification 'Specification Logic' (Protocol in workflow.md) 92f3edf

## Phase 3: Integration
- [x] Task: Update `GenericCrudController` 0c205bb
    - [x] Subtask: Add argument resolver for filters
    - [x] Subtask: Pass specifications to service
- [x] Task: Update `GenericCrudService` 0c205bb
    - [x] Subtask: Change `findAll` to accept `Specification`
- [x] Task: Verify Integration Tests 0c205bb
    - [x] Subtask: Create tests for filtering scenarios
- [x] Task: Conductor - User Manual Verification 'Integration' (Protocol in workflow.md) 0c205bb
