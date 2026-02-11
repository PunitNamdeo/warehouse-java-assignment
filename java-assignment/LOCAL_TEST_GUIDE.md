# Local Test Execution & Coverage Report Guide

## Quick Start

### Run Tests & Generate Coverage Report
```bash
cd java-assignment
mvn clean install -DskipTests=false
mvn jacoco:report
```

### View Coverage Report
**Report location:** `java-assignment/target/site/jacoco/index.html`

**Windows - Open in browser:**
```powershell
# Method 1: Use PowerShell
Invoke-Item "target\site\jacoco\index.html"

# Method 2: Manual - Open File Explorer and navigate to:
# c:\Users\c plus\Downloads\fcs-interview-code-assignment-main\java-assignment\target\site\jacoco\index.html
```

## Understanding the Report

When you open `target/site/jacoco/index.html` in a browser, you'll see:

1. **Overall Coverage** - Top-level percentage for entire project
2. **Package-level breakdown** - Coverage % for each package
3. **Class-level details** - Coverage for individual classes
4. **Line coverage** - Green = covered, Red = not covered, Yellow = partially covered

**Color Legend:**
- 🟢 **Green (80-100%)** - Fully covered by tests
- 🟡 **Yellow (50-79%)** - Partially covered
- 🔴 **Red (0-49%)** - Not covered by tests

## Troubleshooting

### Problem: "Warning: Classes in bundle do not match with execution data"

**Cause:** jacoco.exec file is stale (classes were recompiled but tests weren't re-run)

**Solution:**
```bash
# Delete the cache and rebuild from scratch
rm -r target/
mvn clean install -DskipTests=false
mvn jacoco:report
```

### Problem: "Cannot find report at target/site/jacoco/"

**Cause:** Build failed or tests were skipped

**Solution:**
1. Check build output for errors
2. Ensure PostgreSQL is running: `localhost:5432` with `admin/admin123`
3. Run: `mvn clean install -DskipTests=false -X` (verbose output)

### Problem: Memory errors during build ("insufficient memory")

**Cause:** JVM heap size is too small for builds

**Solution:**
```powershell
# Increase Maven memory before running build
$env:MAVEN_OPTS = "-Xmx2G"
mvn clean install -DskipTests=false
```

### Problem: Tests timeout

**Cause:** Database slow or network latency

**Solution:**
```bash
# Run a specific test class instead
mvn test -Dtest=CreateWarehouseUseCaseTest

# Or increase timeout
mvn test -DargLine="-Dtimeout=120000"
```

## Running Different Test Scenarios

### All Tests (Full Build)
```bash
mvn clean install -DskipTests=false
```

### Just Tests (No compilation)
```bash
mvn test
```

### Specific Test Class
```bash
mvn test -Dtest=WarehouseUseCaseTest
```

### Skip Coverage Check (Build faster)
```bash
mvn clean install -DskipTests=false -Dskip.jacoco.check=true
```

### Enable Coverage Check (Strict validation)
```bash
mvn clean install -DskipTests=false -Dskip.jacoco.check=false
```

## Coverage Thresholds

**Current Configuration:**
- **Minimum:** 50% line coverage (required to pass)
- **Target:** 75%+ (best practice)
- **Excluded:** Auto-generated code (`com.warehouse.api.beans`, `com.warehouse.api.delegates`)

To improve coverage:
1. Open `target/site/jacoco/index.html` in browser
2. Click on packages/classes with low coverage (red)
3. See exactly which lines are untested (red marks in code)
4. Write unit tests for those lines
5. Re-run tests to verify

## Performance Tips

```bash
# Skip entire test phase (for code-only changes)
mvn clean install -DskipTests=true

# Use parallel execution
mvn test -T 1C  # Uses 1 thread per CPU core

# Only compile, don't package
mvn compile

# Show only errors and warnings
mvn clean install -DskipTests=false -q
```

## CI/CD Integration

The GitHub Actions pipeline will:
1. Run: `mvn clean install -DskipTests=false`
2. Generate: `mvn jacoco:report`
3. Validate: Code coverage ≥ 50% (minimum threshold)
4. Fail build if coverage drops below threshold

To match CI behavior locally:
```bash
mvn clean install -DskipTests=false -Dskip.jacoco.check=false
```

---

**Key Point:** Coverage report file is always at: `target/site/jacoco/index.html` after a successful build with tests.
