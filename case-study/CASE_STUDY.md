# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

**Key Challenges:**

1. **Multi-level Cost Attribution** - Costs must flow bidirectionally: from Warehouses to Stores (fulfillment costs) and from Stores back to Warehouses (inventory holding costs). The system architecture shows Warehouses fulfill Products to Stores through associations, requiring proportional cost allocation per fulfilled unit.

2. **Shared Infrastructure Costs** - Warehouses serve multiple Stores and Products simultaneously. A single warehouse's operational costs must be distributed across all fulfilled associations using a fair allocation model (e.g., by volume, frequency, or SKU count).

3. **Time-Series Cost Tracking** - The Warehouse replacement feature (reusing Business Unit Code) creates historical cost tracking challenges. When a warehouse is replaced, cost history must be preserved and segregated from the new warehouse operation to enable accurate trend analysis and performance comparison.

4. **Real-time vs. Batch Processing** - The system's fulfillment associations are created/deleted dynamically. Cost impacts must be recorded transactionally (at association creation time) rather than batch-processed, to prevent cost mismatches during peak operational periods.

**Important Considerations:**

- **Cost Dimension Attributes**: Track costs at Warehouse, Store, Product, and association levels - create cost dimension mapping in the database
- **Cost Allocation Rules**: Implement configurable cost pools (labor, transportation, inventory holding) with allocation drivers specific to each
- **Audit Trail**: Every cost transaction must be immutable and linked to source operations (warehouse creation, product fulfillment, store stock replenishment)
- **Period Closing**: Support monthly/quarterly reconciliation where allocated costs are locked and compared against actual spend from financial system
- **Forecasting Inputs**: Historical cost ratios per warehouse/store pair should feed into budgeting models avoiding peak-season distortions

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

**Optimization Strategies:**

1. **Consolidation Analysis** - Analyze which stores are being served by multiple warehouses simultaneously. Identify geographic clusters where store demand could be consolidated into fewer, more efficient warehouses. The system tracks all warehouse-product-store associations, enabling data-driven consolidation decisions that could reduce operational redundancy by 15-25%.

2. **Product-Store Affinity Optimization** - Track which products move most frequently between specific warehouse-store pairs. Redesign the fulfillment network to pre-position fast-moving products closer to stores that demand them, reducing transportation costs by moving inventory closer to demand points.

3. **Warehouse Utilization Leveling** - Monitor warehouse capacity utilization patterns. Identified underutilized warehouses (below 60% capacity) are candidates for decommissioning or reallocation, while overutilized warehouses (above 85%) warrant expansion or load balancing to other locations.

**Business Value of Integration:**

1. **Single Source of Truth** - The system maintains operational entities (Warehouse, Store, Product, Associations) which directly drive financial transactions. Integrating with ERP/Financial systems ensures cost allocations are based on actual fulfillment activities, not estimations, reducing variance in month-end reconciliation from 8-12% to <2%.

2. **Real-time Visibility** - When warehouses are created/replaced or fulfillment associations change, financial impacts are immediately visible to finance teams. This enables proactive cash flow management and quarter-end forecasting accuracy, critical when a warehouse replacement involves capital investments.

3. **Audit & Compliance** - Every fulfillment operation has a complete audit trail (creation timestamp, associations, warehouse history via Business Unit Code tracking). This creates an immutable ledger for financial audits and regulatory compliance (SOX, IFRS 16 for lease accounting if warehouses are rented).

**Integration Architecture:**

- **Event-Driven Sync**: Publish operational events (warehouse.created, association.deleted) to financial system asynchronously - prevents operational system from blocking on finance processing
- **Reconciliation Process**: Daily batch reconciliation comparing operational cost allocation vs. GL-recorded costs - flag discrepancies <24hrs for investigation
- **Master Data Governance**: Warehouse Business Unit Code acts as GL account code prefix - cost center assignment happens at warehouse creation and follows through to all associated fulfillment costs
- **Historical Preservation**: Archive Warehouse cost history before replacement - creates before/after comparison for cost control during transitions

**Technical Safeguards:**
- Transactional idempotency: Financial transactions keyed by (warehouse|store|product, timestamp) to prevent duplicate posting
- Change Data Capture (CDC): Monitor database changelog, feed to financial system queue for eventual consistency
- Reconciliation Dashboard: Real-time comparison of operational units vs. GL transactions with drill-down capability
4. **Supplier Network Rationalization** - When a warehouse is replaced using Business Unit Code reuse, evaluate if the new warehouse configuration can serve more stores with the same capacity investment, improving the fulfillment network topology.

**Implementation Roadmap:**

- **Phase 1 (Quick Wins)**: Eliminate single-store-single-warehouse fulfillments - consolidate into multi-store shipments (30 days, cost impact: -8%)
- **Phase 2 (Network Rebalancing)**: Implement location-based fulfillment optimization - ensure stores within a location prefer nearby warehouses first (60 days, cost impact: -12%)
- **Phase 3 (Warehouse Replacement)**: As warehouses come due for replacement, redesign capacity to match peak-season demand not just average usage (90+ days, cost impact: -15%)

**Measurement & Outcomes:**

**Importance:**

1. **Seasonal Demand Planning** - Different stores experience different seasonal peaks (e.g., TONSTAD store handles 30% higher inventory in Q4). Forecast model must aOld Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

**Cost Control Aspects of Warehouse Replacement:**

1. **Baseline Establishment** - The old warehouse's cost history (stored via Business Unit Code archive) provides the baseline for the new warehouse's budget. If the old MWH.001 in ZWOLLE-001 operated at $45k/month cost, the new MWH.001 should be budgeted at same or lower. Without this history, budget targets are made blind - new management might grossly over/under-budget the replacement.

2. **Performance Accountability** - By preserving old warehouse cost per unit (e.g., old MWH.001 = $2.50/unit fulfillment cost), the new warehouse is measured against the same metric. This creates accountability: if new MWH.001 costs $3.20/unit, operations must investigate and justify the variance (newer equipment should improve, not worsen, efficiency).

3. **Change Isolation** - When a warehouse is replaced, often multiple changes happen simultaneously: new equipment, new location layout, different staffing model. Archiving old warehouse data allows cost variance analysis to isolate what caused changes:
   - Equipment efficiency gains: (old cost - new cost) × volume
   - Labor cost changes: new staffing level vs. headcount-adjusted old model
   - Location efficiency: if moved elsewhere, compare loading time costs
   
   Without this breakdown, you cannot identify which investments returned value.

**Why Preserve History:**

1. **Trend Analysis**: Monitor if warehouse replacement delivered expected ROI:
   - Old: capacity 500 units, cost $45k/month = $90/unit
   - New: capacity 700 units, cost $55k/month = $79/unit
   - Result: 25% capacity increase, 12% cost increase = 12% cost improvement per unit

2. **Knowledge Transfer**: Operations manual for old warehouse informs new warehouse ramp-up - avoid repeating efficiency mistakes, accelerate time-to-steady-state operation

3. **Comparative Analysis**: If the company replaces multiple warehouses (e.g., MWH.001 in 2024, MWH.012 in 2025), comparing old→new transitions across locations identifies which replacement strategies work best

4. **Regulatory/Audit Trail**: Tax code might consider old warehouse depreciation & disposal a capitalized event. GL requires tracking old asset retirement date vs. new asset capitalization date.

**Budget Control Strategy:**

- **Template Budget**: New warehouse budget = old warehouse average monthly cost + expected operational improvements (e.g., -10% for automation)
- **Ramp-up Period**: First 3 months budgeted at 70% of full run-rate (staffing ramp, system integration delays are normal)
- **Monthly Variance Reporting**: Track new warehouse actual vs. budget with drill-down to old warehouse comparison - flag >5% variances for investigation
- **Performance Thresholds**: If new warehouse exceeds old warehouse cost per unit by >15% after 6 months, escalate for corrective action (staffing adjustment, process review)

**Practical Example:**
- Old MWH.012 (AMSTERDAM-001): $85k/month, serves 8,000 fulfillment units/month = $10.63/unit cost
- New MWH.012 (AMSTERDAM-001 replacement): Budget $78k/month for 8,500 units = $9.18/unit cost target
- Months 1-3 (ramp): Budget $55k/month for 5,000 units while systems stabilize
- Month 4+ (full operation): Track against $9.18/unit target with monthly reporting vs. archived old warehouse baseline
**System Design Considerations:**

1. **Dimensionality**: Build forecasts at the lowest detail level (warehouse-product-store association), not just warehouse-level:
   - Store growth trends (some stores' sales grow 15% YoY while others plateau)
   - Product lifecycle patterns (new products ramp up, legacy products decline)
   - Seasonality factors for each store (tourism impact, local shopping patterns)
   - Warehouse efficiency curves (new warehouses operate below design capacity initially)

2. **Constraint-Based Forecasting**:
   - Location capacity constraints: ZWOLLE-001 max 40 units, can only support N warehouses - if forecast predicts demand exceeding this, initiate warehouse replacement
   - Warehouse-product limits: Each warehouse can store max 5 product types - forecast product mix changes may require rebalancing fulfillment network
   - Store fulfillment limits: Each store fulfilled by max 3 warehouses - forecast demand changes affect which warehouses serve which stores

3. **Scenario Planning**:
   - Base case: trend continuation with known seasonality
   - Growth case: new store openings, product launches - model impact on warehouse fulfillment network
   - Stress case: supply disruption - which fulfillment associations are critical, which are redundant

4. **Feedback Loop**: Historical cost-per-fulfillment ratio by warehouse feeds into budget variance analysis - if warehouse fulfillment costs exceed forecast, investigate root causes (excess labor, transportation rate inflation, etc.)

**Key Metrics to Track:**
- Units per warehouse-store pair (fulfillment throughput)
- Fulfillment cost per unit by warehouse
- Warehouse capacity utilization trend
- Store demand growth rate by product type
- Forecast accuracy % (actual vs. predicted fulfillment volume)it ($ per warehouse-product-store association)
- Days inventory outstanding by warehouse-product pair
- Cost as percentage of revenue by store
- Warehouse replacement ROI (increased throughput vs. investment)

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
[ fill here your answer ]

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
[ fill here your answer ]

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
[ fill here your answer ]

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
