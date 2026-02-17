# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations.

**Questions you may have and considerations:**

### Key challenges
1. **Multi-level attribution complexity**  
   Costs must be allocated across Warehouse, Store, Product, and Warehouse-Product-Store relationships. A weak allocation model can distort unit economics and decision making.

2. **Shared resource allocation**  
   Warehouses serve multiple stores and products simultaneously, so labor, rent, utilities, and transport overhead must be split with transparent drivers (volume, picks, weight, distance, dwell time).

3. **Historical continuity during warehouse replacement**  
   Since a new warehouse can reuse Business Unit Code while the old one is archived, cost history must remain comparable across pre/post replacement periods.

4. **Timing mismatch (operational vs financial posting)**  
   If allocation is delayed to batch-only runs, fulfillment state changes can produce reconciliation gaps. A hybrid near-real-time + daily reconciliation approach is usually safer.

### Important considerations
- Define cost dimensions first: warehouse, store, product, route, period, and fulfillment association.
- Separate direct costs (transport, handling) and indirect costs (overhead pools).
- Make allocation rules configurable and versioned (effective-date based).
- Keep immutable audit trail for allocation inputs, formulas, and outputs.
- Support period close rules so prior months are locked after reconciliation.

### Questions to ask business stakeholders
- Which allocation drivers are accepted by Finance and Operations?
- What tolerance is acceptable for month-end variance (e.g., <=2%)?
- Which metrics drive decisions: cost per unit, cost per order, store margin, or SLA-adjusted cost?
- What is the expected close cycle (daily provisional vs monthly final)?

---

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

### Optimization strategies
1. **Network consolidation and rebalancing**  
   Reduce unnecessary multi-warehouse servicing patterns for the same store where service levels allow consolidation.

2. **Product-store affinity optimization**  
   Place high-velocity products closer to demand-heavy stores to reduce transport and handling costs.

3. **Utilization leveling**  
   Identify under-utilized and over-utilized warehouses; rebalance loads before adding capacity.

4. **Route and shipment batching improvements**  
   Optimize frequency, route density, and shipment grouping to lower per-unit logistics cost.

### Prioritization framework
- **Impact**: expected savings (absolute and per-unit).
- **Effort**: implementation complexity and dependency footprint.
- **Risk**: SLA impact, change-management overhead.
- **Time-to-value**: quick wins vs structural improvements.

### Practical roadmap
- **Phase 1 (0-30 days):** Quick wins from existing data (consolidation and obvious route inefficiencies).
- **Phase 2 (30-90 days):** Capacity rebalancing and rule tuning in allocation/routing.
- **Phase 3 (90+ days):** Structural changes (warehouse replacement plans, network redesign, automation investments).

### Expected outcomes
- Lower cost per fulfillment unit.
- Better warehouse utilization and fewer capacity hotspots.
- More stable service levels via fewer reactive reallocations.

---

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

### Why integration matters
- Creates a single source of truth between operational activity and financial reporting.
- Reduces reconciliation effort and month-end surprises.
- Improves audit readiness through traceable, explainable cost postings.

### Business benefits
- Faster close cycles and lower manual adjustment volume.
- Better forecast quality because operational trends flow into finance sooner.
- Higher confidence in profitability analysis by warehouse/store/product.

### Integration approach
1. **Event-driven operational feed** for key changes (warehouse lifecycle, fulfillment association changes, stock-related cost triggers).
2. **Idempotent posting strategy** in finance integration layer to avoid duplicate journal impact.
3. **Daily reconciliation job** comparing operationally allocated cost vs GL-posted values.
4. **Exception workflow** with ownership and SLA for discrepancy resolution.

### Controls and safeguards
- Contract versioning and schema validation for integration payloads.
- Correlation IDs for traceability across systems.
- Backfill/replay capability for recovery after outage.
- Clear master data ownership (cost centers, account mapping, dimensions).

---

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

### Why this is important
- Fulfillment costs are highly volume-sensitive; weak forecasts create either stockouts/service degradation or expensive overcapacity.
- Budgeting translates demand expectations into labor, transport, and capacity plans.

### Design considerations
1. **Granularity**: forecast at warehouse-store-product level where feasible, then aggregate upward.
2. **Driver-based model**: include seasonality, store growth, product mix shift, route cost inflation, labor rate changes.
3. **Constraint-aware planning**: account for location capacity and warehouse/store servicing limits.
4. **Scenario analysis**: base, growth, and stress cases.
5. **Rolling updates**: monthly/weekly re-forecasting instead of static annual plan only.

### Core metrics
- Cost per unit fulfilled.
- Warehouse utilization trend.
- Forecast accuracy (MAPE or equivalent).
- Service-level adherence at planned cost.

### Governance
- Define ownership for assumptions (Finance vs Operations vs Supply Chain).
- Version assumptions and keep variance commentary at each cycle.

---

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

### Cost-control priorities
1. **Baseline continuity**  
   Preserve old warehouse cost baseline to set realistic targets for the replacement warehouse.

2. **Variance transparency**  
   Track pre/post replacement variances by cost category (labor, transport, overhead, inventory handling).

3. **Ramp-up budgeting**  
   Allow temporary transition inefficiency (first months) but define target stabilization timeline.

4. **ROI validation**  
   Evaluate whether replacement actually reduces unit cost or increases throughput at acceptable incremental cost.

### Why history preservation is critical
- Enables apples-to-apples comparison across replacement boundary.
- Prevents loss of accountability for budget performance.
- Supports audit and compliance for asset transition and cost-center continuity.

### Practical budget-control model
- Establish target `new cost per unit <= old cost per unit` adjusted for justified changes.
- Apply monthly variance thresholds and escalation rules.
- Separate one-time transition costs from steady-state run-rate costs.

---

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
