# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would standardize the data access layer for consistency and maintainability. Currently, we have mixed 
approaches: Store and Product use Panache entities directly with PanacheEntity, while Warehouse uses a separate 
DbWarehouse entity with explicit mapping. The fulfillment module uses yet another pattern with DbWarehouseProductStore.

Refactoring approach:
1. Adopt the Warehouse/Fulfillment pattern across all entities - separate domain models from database entities. 
   This provides better separation of concerns and allows evolving the domain model independently of persistence.

2. Create a consistent repository interface for all entities (like WarehouseStore, WarehouseProductStoreStore).

3. Move common query logic to a base repository class or abstract methods to reduce duplication.

4. Keep business validation in domain use cases (current approach is good), not in repositories.

Benefits:
- Better testability: domain models can be tested without storage concerns
- Domain-driven design: the domain model becomes the single source of truth
- Flexibility: easier database migration or schema changes without affecting business logic
- Team consistency: everyone follows the same pattern, reducing cognitive load

For legacy constraints or time-sensitive projects, pragmatic decision: keep Store/Product as simple Panache 
entities since they're basic CRUD operations, but enforce the separate-entity pattern for complex domains 
like Warehouse and Fulfillment where we have intricate business rules and validations.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches have merits and drawbacks:

OPENAPI-FIRST APPROACH (Code Generation from YAML):
Pros:
- Single source of truth: API contract is defined in YAML, serves as documentation and specification
- Consumer-friendly: clients get generated stubs immediately
- Contract-first thinking: forces thinking about API design before implementation
- Backwards compatibility: easier to track API versions and maintain compatibility
- Automated testing: generated models can be validated against contract
- Less boilerplate: generators create request/response classes automatically

Cons:
- Code generation adds complexity: need to understand and manage the generation process
- Learning curve: team must understand OpenAPI/Swagger specifications
- Code ownership: generated code may feel like black box to developers
- Synchronization challenges: if specs drift from implementation, can be problematic
- Generator tool dependencies: locked into tooling constraints

CODE-FIRST APPROACH (Direct Implementation):
Pros:
- Simplicity: developers write code directly, no intermediate step
- Flexibility: can implement custom logic, annotations, and patterns easily
- Performance: no build step for code generation
- Debugging: easier to debug and understand the implementation
- Agility: faster for small, straightforward APIs like CRUD operations

Cons:
- No single source of truth: API contract is implicit in code
- Documentation drift: docs and implementation can easily diverge
- Consumer friction: clients must reverse-engineer the API from implementation
- Discipline required: team must manually maintain API consistency
- Versioning complexity: harder to track and communicate API changes

MY CHOICE FOR THIS PROJECT:
I would use a hybrid approach strategically:

1. Complex, well-defined APIs (like Warehouse) → OpenAPI-first
   - These have business rules and validations that justify documentation
   - Multiple consumers may use these endpoints
   - Stability and versioning matter

2. Simple CRUD endpoints (Store, Product) → Code-first
   - For straightforward operations, code-first is faster
   - Can always generate OpenAPI documentation from annotations using Swagger tools
   - Reduces overhead for simple operations

3. Add Swagger/SpringDoc annotations to code-first endpoints
   - Provides best of both worlds: simple implementation with API documentation
   - No code generation, but maintains documentation

For the Fulfillment module (BONUS), I'd use code-first with Swagger annotations since:
- It's relatively new/evolving
- Complex logic is in use case classes, not REST handlers
- Team needs flexibility during development

This balanced approach provides documentation, flexibility, and reduces unnecessary complexity.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I'd implement a risk-driven, pyramid-based testing strategy that maximizes ROI on testing effort:

TEST PYRAMID (Effort vs Impact):

TIER 1: Unit Tests (Base - 60-70% effort)
Focus on:
- Business logic validation: constraints, calculations, transformations
- Use Cases: CreateWarehouseUseCase, ArchiveWarehouseUseCase, AssociateWarehouseToProductStoreUseCase
- Constraint validation: all the max limits and validations
- Edge cases: null handling, boundary conditions

Why: Catches bugs early, fast to execute, cheap to maintain, provides confidence in core logic

Example: Test each constraint independently in AssociateWarehouseToProductStoreUseCase
- Test max 2 warehouses per product-store constraint
- Test max 3 warehouses per store constraint
- Test max 5 products per warehouse constraint
- Test duplicate prevention
- Test all combinations that should fail and succeed

TIER 2: Integration Tests (Middle - 20-25% effort)
Focus on:
- Repository layer: WarehouseRepository, WarehouseProductStoreRepository queries
- Database persistence: ensure entities are saved/retrieved correctly
- Transaction boundaries: flush operations, rollback scenarios
- REST endpoints: happy path and error paths for critical operations

Why: Validates that components work together, catches database/ORM issues

Examples:
- Create warehouse and verify it's retrievable by businessUnitCode
- Archive warehouse and verify it doesn't appear in getAll()
- Test cascade behavior: replacing warehouse archives old one
- Test legacy system integration: verify LegacyStoreManagerGateway is called after DB flush

TIER 3: End-to-End Tests (Top - 5-10% effort)
Focus on:
- Critical user journeys: entire warehouse lifecycle (create → replace → archive)
- Cross-module flows: warehouse creation + product association
- Error scenarios: constraint violations, invalid locations
- Performance: ensure operations complete in acceptable time

Why: Validates real-world scenarios, catches integration issues between modules

Examples:
- Create warehouse, associate products, verify constraints, replace warehouse
- Try creating duplicate business unit codes, verify 409 conflict
- Create store, associate multiple warehouses with products, verify limits

COVERAGE STRATEGY:

1. Code Coverage Targets (not 100% = pragmatic):
   - Use Cases: 85-90% (business logic must be thoroughly tested)
   - Repositories: 70-80% (focus on query paths and edge cases)
   - REST Resources: 60-70% (error handling and parameter validation)
   - Entities/Models: 40-50% (simple POJOs, test only complex logic)
   - Ignore: Getters/setters, ToString/Equals on simple entities

2. Critical Path Priority:
   - Warehouse operations (create/replace/archive): 90%+ coverage
   - Fulfillment constraints: 90%+ coverage (complex business rules)
   - Store operations: 60% coverage (simpler CRUD)
   - Product operations: 60% coverage (simpler CRUD)

MAINTAINING TEST COVERAGE OVER TIME:

1. Enforce via CI/CD:
   - Set minimum coverage threshold (e.g., 75% overall, 85% for use cases)
   - Fail builds if coverage drops
   - Generate coverage reports in PRs

2. Continuous improvement:
   - Review coverage gaps quarterly
   - Add tests when bugs are discovered
   - Refactor tests to reduce duplication

3. Code Review practices:
   - Require tests for any new business logic
   - Allow skipping tests for trivial getters/setters
   - Document why line is excluded from coverage (if needed)

4. Testing debt management:
   - Keep a "testing backlog" for low-priority areas
   - Prioritize based on: risk + change frequency

IMPLEMENTATION APPROACH FOR THIS PROJECT:

Phase 1 (Immediate - 60% complete):
- Unit tests for all use cases with constraint validation
- Integration tests for warehouse repository queries
- Endpoint tests for happy path scenarios

Phase 2 (Next sprint - 80% complete):
- Edge case tests and boundary conditions
- Error scenario tests (all 400/404/409 responses)
- Legacy system integration tests

Phase 3 (Maintenance - 90%+ maintain):
- Exploratory testing for unexpected scenarios
- Performance tests for critical paths
- Coverage gap analysis and closure

TOOLS & PRACTICES:
- JUnit 5: assertions, parameterized tests
- Mockito: mock repositories in use case tests
- Rest Assured: test REST endpoints easily
- JaCoCo: measure code coverage
- CI/CD integration: automatically run tests, enforce thresholds

This approach ensures maximum test ROI: catching real bugs quickly without maintaining 100% coverage overhead.
```