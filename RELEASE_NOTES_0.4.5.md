# SpringFlow v0.4.5 - BigDecimal & BigInteger Type Conversion Fix

**Release Date**: December 30, 2025
**Type**: Bugfix Release
**Maven Central**: [io.github.tky0065:springflow-starter:0.4.5](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.5)

---

## 🐛 Critical Bugfix

### BigDecimal and BigInteger Type Conversion Issue

**Problem**: When creating or updating entities with `BigDecimal` or `BigInteger` fields via REST API, the request failed with an error.

**Symptoms**:
- REST API POST/PUT requests with BigDecimal fields failed ❌
- Error: `IllegalArgumentException: Can not set java.math.BigDecimal field to java.lang.Double`
- Entities with price, amount, or other decimal fields could not be created

**Example Failure**:
```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;
    private String name;
    private BigDecimal price;  // ❌ Failed when receiving JSON: {"price": 99.99}
}
```

**Request**:
```bash
POST /api/products
Content-Type: application/json

{
  "name": "Test Product",
  "price": 99.99
}
```

**Error**:
```
HTTP 400 Bad Request
{
  "status": 400,
  "error": "Bad Request",
  "message": "Failed to convert DTO to entity: Can not set java.math.BigDecimal field com.example.Product.price to java.lang.Double"
}
```

**Root Cause**: When Jackson parses JSON, numeric values like `99.99` become Java `Double` objects. SpringFlow's `EntityDtoMapper.convertValue()` method did not handle conversion from `Number` types (Double, Integer, Long) to `BigDecimal` or `BigInteger`.

**Solution**:
- Enhanced `EntityDtoMapper.java` with comprehensive numeric type conversions
- Added support for String → BigDecimal/BigInteger
- Added support for Number → BigDecimal with proper precision handling
- Added support for Number → BigInteger

**Impact**:
- ✅ Entities with BigDecimal fields now work correctly via REST API
- ✅ All numeric JSON values properly converted (Double, Integer, Long, String)
- ✅ Precision preserved for floating-point to BigDecimal conversion
- ✅ No breaking changes for existing users
- ✅ Fully backward compatible with v0.4.4

---

## 📊 What's Changed

### Fixed
- **BigDecimal type conversion** - Handles JSON numeric values (Double, Integer, Long) to BigDecimal
- **BigInteger type conversion** - Handles JSON numeric values to BigInteger
- **Precision handling** - Floating-point to BigDecimal conversion preserves precision via string conversion
- **String conversions** - Supports String to BigDecimal/BigInteger for flexibility

### Technical Details

**File Modified:**
`springflow-core/src/main/java/io/springflow/core/mapper/EntityDtoMapper.java`

**Changes:**
1. **Added imports** (lines 12-13):
```java
import java.math.BigDecimal;
import java.math.BigInteger;
```

2. **Enhanced `convertValue()` method** (lines 275-314):
```java
private Object convertValue(Object value, Class<?> targetType) {
    if (targetType.isInstance(value)) return value;

    // String to target type conversions
    if (value instanceof String strValue) {
        if (targetType == Integer.class || targetType == int.class) return Integer.valueOf(strValue);
        if (targetType == Long.class || targetType == long.class) return Long.valueOf(strValue);
        if (targetType == Double.class || targetType == double.class) return Double.valueOf(strValue);
        if (targetType == Float.class || targetType == float.class) return Float.valueOf(strValue);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.valueOf(strValue);
        if (targetType == BigDecimal.class) return new BigDecimal(strValue);  // ✅ NEW
        if (targetType == BigInteger.class) return new BigInteger(strValue);  // ✅ NEW
    }

    // Number to target type conversions
    if (value instanceof Number numValue) {
        if (targetType == Integer.class || targetType == int.class) return numValue.intValue();
        if (targetType == Long.class || targetType == long.class) return numValue.longValue();
        if (targetType == Double.class || targetType == double.class) return numValue.doubleValue();
        if (targetType == Float.class || targetType == float.class) return numValue.floatValue();
        if (targetType == BigDecimal.class) {  // ✅ NEW
            // Handle BigDecimal conversion from various number types
            if (numValue instanceof BigDecimal) return numValue;
            if (numValue instanceof BigInteger) return new BigDecimal((BigInteger) numValue);
            if (numValue instanceof Double || numValue instanceof Float) {
                // Use string conversion to avoid precision loss
                return new BigDecimal(numValue.toString());
            }
            // For Integer, Long, etc.
            return BigDecimal.valueOf(numValue.longValue());
        }
        if (targetType == BigInteger.class) {  // ✅ NEW
            if (numValue instanceof BigInteger) return numValue;
            if (numValue instanceof BigDecimal) return ((BigDecimal) numValue).toBigInteger();
            return BigInteger.valueOf(numValue.longValue());
        }
    }

    return value;
}
```

**Why This Matters:**
- JSON parsers (Jackson) convert numeric values to Java types (Double, Integer, Long)
- SpringFlow needs to map these to entity field types (BigDecimal, BigInteger)
- Previous implementation only handled primitives and their wrapper classes
- New implementation handles all numeric conversions with proper precision

### Tests Added

**File Modified:**
`springflow-core/src/test/java/io/springflow/core/mapper/EntityDtoMapperTest.java`

**8 New Test Cases**:
1. `toEntity_shouldConvertDoubleStringToBigDecimal`
   ```java
   inputDto.put("price", "99.99");  // String → BigDecimal
   assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
   ```

2. `toEntity_shouldConvertDoubleNumberToBigDecimal`
   ```java
   inputDto.put("price", 99.99);  // Double → BigDecimal (from JSON)
   assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
   ```

3. `toEntity_shouldConvertIntegerToBigDecimal`
   ```java
   inputDto.put("price", 100);  // Integer → BigDecimal
   assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("100"));
   ```

4. `toEntity_shouldConvertLongToBigDecimal`
   ```java
   inputDto.put("price", 999999L);  // Long → BigDecimal
   assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("999999"));
   ```

5. `toEntity_shouldHandleBigDecimalDirectly`
   ```java
   inputDto.put("price", new BigDecimal("123.45"));  // BigDecimal → BigDecimal
   assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("123.45"));
   ```

6. `toEntity_shouldConvertStringToBigInteger`
   ```java
   inputDto.put("quantity", "1000");  // String → BigInteger
   assertThat(entity.getQuantity()).isEqualTo(new BigInteger("1000"));
   ```

7. `toEntity_shouldConvertIntegerToBigInteger`
   ```java
   inputDto.put("quantity", 500);  // Integer → BigInteger
   assertThat(entity.getQuantity()).isEqualTo(new BigInteger("500"));
   ```

8. `updateEntity_shouldUpdateBigDecimalField`
   ```java
   Map<String, Object> inputDto = new HashMap<>();
   inputDto.put("price", 99.99);  // Update with Double
   productMapper.updateEntity(entity, inputDto);
   assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
   ```

**Test Statistics**:
- **Total tests**: 173 (was 165 in v0.4.4)
- **New tests**: 8
- **Passed**: 172
- **Skipped**: 1 (performance test)
- **Failed**: 0 ✅

### No Breaking Changes
- ✅ Fully compatible with SpringFlow 0.4.4
- ✅ No API changes
- ✅ No configuration changes required
- ✅ Existing projects work without modifications

---

## 🚀 Upgrade Guide

### From v0.4.4 to v0.4.5

**Maven**:
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.5</version>
</dependency>
```

**Gradle**:
```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.5'
```

**Steps**:
1. Update version in `pom.xml` or `build.gradle`
2. Run `mvn clean install` or `gradle clean build`
3. Restart your application

**No configuration changes needed!**

**Benefits**:
- Entities with BigDecimal/BigInteger fields that previously failed will now work automatically
- No code changes required - just upgrade the version

---

## ✅ Testing Results

### Build & Tests
```
[INFO] SpringFlow Annotations ............................. SUCCESS
[INFO] SpringFlow Core .................................... SUCCESS
[INFO] SpringFlow GraphQL ................................. SUCCESS
[INFO] SpringFlow Starter ................................. SUCCESS
[INFO] BUILD SUCCESS
```

### Unit Tests
```
Tests run: 173, Failures: 0, Errors: 0, Skipped: 1
✅ All core tests passing
✅ All BigDecimal conversion tests passing
✅ All BigInteger conversion tests passing
```

### Manual Verification
After the fix, creating entities with BigDecimal fields works correctly:
```bash
✅ POST /api/products with {"name":"Test","price":99.99} - Returns 201 Created
✅ PUT /api/products/1 with {"price":149.99} - Returns 200 OK
✅ GET /api/products/1 - Returns product with correct BigDecimal price
✅ All CRUD operations work correctly
```

---

## 📚 Documentation Updates

All documentation references remain at v0.4.4 (no doc changes required for this bugfix):
- README.md
- docs/
- CHANGELOG.md (v0.4.5 entry added)

---

## 🔗 Links

- **Maven Central**: https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.5
- **GitHub Release**: https://github.com/tky0065/springflow/releases/tag/v0.4.5
- **Documentation**: https://tky0065.github.io/springflow/
- **Issues**: https://github.com/tky0065/springflow/issues

---

## 📝 Full Changelog

See [CHANGELOG.md](CHANGELOG.md) for complete version history.

---

## 💬 Feedback

If you encounter any issues with this release, please:
- Open an issue: https://github.com/tky0065/springflow/issues
- Start a discussion: https://github.com/tky0065/springflow/discussions

---

**Thank you for using SpringFlow!** 🚀

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
