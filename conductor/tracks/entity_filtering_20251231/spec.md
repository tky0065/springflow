# Specification: Entity Filtering Support

## 1. Overview
This feature introduces dynamic filtering capabilities to the generated REST APIs. It allows clients to query resources using various filter criteria (equality, string matching, ranges, etc.) via query parameters, leveraging Spring Data JPA Specifications.

## 2. Goals
*   Enable clients to filter resources dynamically without custom code.
*   Support common filter operations: EQUALS, LIKE, IN, GREATER_THAN, LESS_THAN, BETWEEN.
*   Allow configuration of filterable fields via annotations.

## 3. Key Components

### 3.1. Annotations
*   `@Filterable`: An annotation to mark entity fields that can be used for filtering.
    *   `types`: Array of `FilterType` (e.g., EQUALS, LIKE) allowed for this field.

### 3.2. Core Logic
*   `FilterType`: Enum defining supported operations.
*   `SpecificationBuilder`: A component that constructs JPA `Specification` objects based on HTTP query parameters and entity metadata.
*   `FilterResolver`: Parses incoming request parameters into a criteria list.

### 3.3. Integration
*   Update `GenericCrudController` to accept filter parameters.
*   Update `GenericCrudService` and Repositories to support `JpaSpecificationExecutor`.

## 4. API Usage Example
`GET /api/products?name[like]=Lap&price[gt]=1000`

## 5. Constraints
*   Must support standard JPA entity types.
*   Must validate that the requested filter type is allowed for the field.
