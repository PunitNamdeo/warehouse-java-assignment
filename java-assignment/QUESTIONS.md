# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**

Looking at the codebase, I notice a pragmatic mix of patterns for database access, and I believe the current approach is actually well-justified rather than problematic. Let me explain my reasoning.

The codebase uses three distinct patterns depending on complexity:

**Simple entities** like `Product` and `Store` leverage Quarkus Panache directly—the entity classes extend `PanacheEntity` and get basic CRUD operations for free. This is the right choice here because these entities have straightforward requirements: basic create, read, update, and delete operations with minimal business logic. Panache eliminates boilerplate code beautifully for these cases.

**Complex entities** like `Warehouse` and `WarehouseProductStore` separate the domain model from the database representation. They use explicit `DbEntity` classes paired with repositories. This pattern is necessary because these entities have substantial business rules—warehouse replacements need to archive old records while creating new ones, fulfillment associations enforce three separate constraints, and stock management requires careful validation. The explicit separation makes it clear to new developers that "this entity has complex behavior, treat it carefully."

**Reference data** like `Location` uses a pure domain gateway with no persistence layer. Locations are predefined, immutable business rules, not database records. This avoids unnecessary ORM overhead and makes the code's intent transparent.

Rather than refactor, I would actually preserve this mixed approach because **it lets the implementation complexity match the domain complexity**. Every pattern serves a purpose.

If I were to make improvements, they would be about consistency and documentation rather than wholesale refactoring:

First, I'd add a style guide documenting when to apply each pattern, so future developers understand the decision-making framework. Second, I might add a few more Panache annotations to `Product` and `Store`—specifically `@Schema` for OpenAPI documentation—to improve API clarity without changing the database pattern. Third, I'd ensure all repositories have consistent query naming conventions.

The bottom line is that this codebase demonstrates practical engineering: using simple solutions for simple problems and investing complexity only where it's genuinely needed. That's professional software design.
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**

This is a great example of two legitimate API development philosophies, each with real tradeoffs. Let me walk through the decision.

The **OpenAPI-first approach** used for Warehouse creates a specification file that serves as the single source of truth. This has genuine advantages: the API contract is explicit and versioned, which helps external teams integrating with your API, and code generation ensures consistency between documentation and implementation. For something as complex as warehouse operations—with multiple business rules, error states, and state transitions—this approach provides clarity that's genuinely valuable.

However, there's a cost. You need tooling expertise, the specification file adds maintenance burden, and there's a synchronization risk between the spec and the actual implementation. It's not the right choice for every API.

The **code-first approach** used for Product and Store is much more agile. You write the endpoint, add some annotations, and Quarkus automatically generates Swagger documentation. There's zero boilerplate, no specification file to maintain, and you can prototype quickly. For simple CRUD operations, this is perfect—the overhead of a specification file would genuinely slow you down.

The tradeoff is that documentation can drift from implementation, and external clients don't have an explicit contract to build against.

**For this project, my recommendation would be:** Keep the current hybrid approach, but with one addition. I'd add OpenAPI annotations (`@OpenAPIDefinition`, `@Schema`) to the Product and Store endpoints to improve their Swagger documentation without maintaining a separate specification file. This gives you the best of both worlds: simple code-first development with good API documentation.

For anything more complex in the future, I'd use the OpenAPI-first pattern. The rule of thumb: if an API has more than a few endpoints or complex business rules, invest in a specification. For simple CRUD, code-first with annotations is faster and cleaner.

The warehouse module made the right choice because warehouse operations genuinely benefit from an explicit contract. Product and Store made the right choice because they're straightforward. Different tools for different problems.
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**

I'd prioritize tests by business risk and execution speed, using a practical test pyramid.

**1) Unit/business-logic tests first (highest ROI):**
Focus on use cases where constraints and state transitions live: `CreateWarehouseUseCase`, `ReplaceWarehouseUseCase`, `ArchiveWarehouseUseCase`, and `AssociateWarehouseToProductStoreUseCase`. These tests should validate each rule independently (invalid location, duplicate business unit code, stock/capacity constraints, fulfillment association limits, etc.).

**2) Targeted integration tests second:**
Cover repository/query behavior that can silently break business flows (archived warehouse filtering, replacement persistence behavior, uniqueness/conflict behavior). Keep these focused because they are slower and more expensive to maintain.

**3) API/resource tests as contract and error-mapping checks:**
Use resource tests to confirm HTTP semantics and payload/validation behavior (201/200/204 success paths, 400/404/409 failure paths). Keep them representative, not exhaustive.

For this codebase, the current mix is appropriate: endpoint coverage tests, focused use-case tests, and Quarkus coverage tests for branch-heavy paths. To keep coverage effective over time, I would enforce it in CI with realistic guardrails (for example: overall threshold plus stricter gates for domain/use-case packages), publish trend visibility in pull requests, and require every bug fix to add or update at least one regression test.

The goal is not maximizing a single percentage; it is protecting critical business behavior while keeping feedback fast enough for day-to-day development.