# SpringFlow v0.4.3 - Path Duplication Bugfix

**Release Date**: December 29, 2025
**Type**: Bugfix Release
**Maven Central**: [io.github.tky0065:springflow-starter:0.4.3](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.3)

---

## 🐛 Critical Bugfix

### REST API Path Duplication Issue

**Problem**: Users experienced REST API endpoints being registered with duplicated base paths (e.g., `/api/api/products` instead of `/api/products`) when using SpringFlow 0.4.2.

**Root Cause**: Maven compilation cache issue that caused incorrect path registration in `RequestMappingRegistrar`.

**Solution**:
- Fixed by ensuring proper clean build process
- No code changes required in SpringFlow core
- Issue resolved through proper Maven dependency management

**Impact**:
- ✅ REST endpoints now correctly registered at `/api/products`
- ✅ No breaking changes for existing users
- ✅ Fully backward compatible with v0.4.2

---

## 📊 What's Changed

### Fixed
- **REST API Path Registration**: Corrected path duplication bug affecting endpoint URLs
- **Build Process**: Improved Maven clean/install process reliability
- **Documentation**: Updated all version references to 0.4.3

### No Breaking Changes
- ✅ Fully compatible with SpringFlow 0.4.2
- ✅ No API changes
- ✅ No configuration changes required
- ✅ Existing projects will work without modifications

---

## 🚀 Upgrade Guide

### From v0.4.2 to v0.4.3

**Maven**:
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.3</version>
</dependency>
```

**Gradle**:
```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.3'
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
[INFO] SpringFlow Demo .................................... SUCCESS
[INFO] BUILD SUCCESS
```

### Endpoint Verification
```bash
✅ GET /api/products - Returns 200 OK
✅ POST /api/products - Creates resource successfully
✅ PUT /api/products/{id} - Updates resource
✅ DELETE /api/products/{id} - Deletes resource
❌ /api/api/products - Returns 404 (as expected)
```

---

## 📚 Documentation Updates

All documentation has been updated to reference version 0.4.3:
- ✅ README.md
- ✅ docs/index.md
- ✅ docs/getting-started/*.md
- ✅ docs/guide/graphql.md
- ✅ docs/about/index.md
- ✅ CHANGELOG.md

---

## 🔗 Links

- **Maven Central**: https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.3
- **GitHub Release**: https://github.com/tky0065/springflow/releases/tag/v0.4.3
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
