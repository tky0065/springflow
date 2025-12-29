# SpringFlow v0.4.4 - Swagger UI OpenAPI Server URL Fix

**Release Date**: December 29, 2025
**Type**: Bugfix Release
**Maven Central**: [io.github.tky0065:springflow-starter:0.4.4](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.4)

---

## 🐛 Critical Bugfix

### Swagger UI Double Path Concatenation Issue

**Problem**: When using Swagger UI to test REST API endpoints, clicking "Try it out" → "Execute" resulted in requests being sent to incorrect URLs with duplicated base paths (e.g., `/api/api/products` instead of `/api/products`).

**Symptoms**:
- Swagger UI displayed correct paths in the documentation (`/api/products`) ✓
- But "Execute" button sent requests to wrong URLs (`/api/api/products`) ❌
- Error: `NoResourceFoundException: No static resource api/api/products`

**Root Cause**: OpenAPI server base URL was configured with `springflow.base-path` value (`/api`). Swagger UI concatenates server URL with operation paths, which already included the full path (`/api/products`), resulting in duplicate paths: `/api` + `/api/products` = `/api/api/products`.

**Solution**:
- Changed OpenAPI server URL from `basePath` to `/` in `OpenApiConfiguration.java`
- Operation paths already include the full path, so server URL should be root
- No changes required to user configuration or entity annotations

**Impact**:
- ✅ Swagger UI "Execute" now correctly calls `/api/products`
- ✅ All REST endpoints accessible via Swagger UI
- ✅ No breaking changes for existing users
- ✅ Fully backward compatible with v0.4.3

---

## 📊 What's Changed

### Fixed
- **OpenAPI server URL configuration** - Changed from `springflow.base-path` to `/` to prevent double path concatenation
- **Swagger UI request URLs** - Execute button now sends requests to correct endpoints
- **NoResourceFoundException errors** - Resolved 404 errors when testing via Swagger UI

### Technical Details

**File Modified:**
`springflow-starter/src/main/java/io/springflow/starter/config/OpenApiConfiguration.java`

**Change:**
```java
// Before (v0.4.3)
server.setUrl(basePath);  // basePath = "/api"

// After (v0.4.4)
server.setUrl("/");  // Fixed: Always use root since paths include base
```

**Why This Matters:**
- **SpringFlowOpenApiCustomizer** already constructs operation paths with `springflow.base-path` included
- Setting server URL to the base path caused Swagger UI to concatenate both
- Server URL should always be `/` for correct URL construction

### No Breaking Changes
- ✅ Fully compatible with SpringFlow 0.4.3
- ✅ No API changes
- ✅ No configuration changes required
- ✅ Existing projects work without modifications

---

## 🚀 Upgrade Guide

### From v0.4.3 to v0.4.4

**Maven**:
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.4</version>
</dependency>
```

**Gradle**:
```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.4'
```

**Steps**:
1. Update version in `pom.xml` or `build.gradle`
2. Run `mvn clean install` or `gradle clean build`
3. Restart your application

**No configuration changes needed!**

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

### Swagger UI Verification
After the fix, Swagger UI correctly:
```bash
✅ Displays paths as /api/products
✅ Executes requests to /api/products (not /api/api/products)
✅ Returns 200 OK for valid requests
✅ OpenAPI JSON shows server URL as "/"
```

### Direct cURL Testing
```bash
✅ curl http://localhost:8080/api/products - Returns 200 OK
✅ POST /api/products - Creates resource successfully
✅ PUT /api/products/{id} - Updates resource
✅ DELETE /api/products/{id} - Deletes resource
❌ /api/api/products - Returns 404 (as expected)
```

---

## 📚 Documentation Updates

All documentation references remain at v0.4.3 (no doc changes required for this bugfix):
- README.md
- docs/
- CHANGELOG.md (v0.4.4 entry added)

---

## 🔗 Links

- **Maven Central**: https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.4
- **GitHub Release**: https://github.com/tky0065/springflow/releases/tag/v0.4.4
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
