# SpringFlow Documentation

This directory contains the source files for the SpringFlow documentation site built with MkDocs Material.

## Building Locally

### Prerequisites

Install MkDocs and dependencies:

```bash
pip install mkdocs-material
pip install mkdocs-git-revision-date-localized-plugin
```

### Commands

```bash
# Serve documentation locally (with live reload)
mkdocs serve

# Build static site
mkdocs build

# Deploy to GitHub Pages (requires push access)
mkdocs gh-deploy
```

The local server will be available at: http://127.0.0.1:8000

## Documentation Structure

```
docs/
├── index.md                    # Homepage
├── getting-started/
│   ├── quickstart.md          # Quick start guide
│   ├── installation.md        # Installation instructions
│   └── first-project.md       # First project tutorial
├── guide/
│   ├── annotations.md         # Annotations reference
│   ├── configuration.md       # Configuration guide
│   ├── dto-mapping.md         # DTO mapping
│   ├── filtering.md           # Filtering guide
│   ├── pagination.md          # Pagination & sorting
│   ├── validation.md          # Validation
│   ├── security.md            # Security integration
│   ├── soft-delete.md         # Soft delete
│   ├── auditing.md            # Audit trail
│   └── kotlin.md              # Kotlin support
├── advanced/
│   ├── architecture.md        # Architecture overview
│   ├── custom-endpoints.md    # Custom endpoints
│   ├── performance.md         # Performance tuning
│   └── best-practices.md      # Best practices
├── api/
│   ├── annotations.md         # Annotations API
│   ├── configuration.md       # Configuration properties
│   └── endpoints.md           # Generated endpoints
├── development/
│   └── contributing.md        # Contributing guide
└── about/
    ├── roadmap.md            # Roadmap
    ├── changelog.md          # Changelog
    └── license.md            # License
```

## GitHub Pages Deployment

The documentation is automatically deployed to GitHub Pages via GitHub Actions when:
- Changes are pushed to the `main` branch affecting docs/** or **.md files
- The workflow is manually triggered

**Live site:** https://tky0065.github.io/springflow/

## Configuration

Documentation configuration is in `mkdocs.yml` at the project root.

Theme: Material for MkDocs
Features:
- Dark/light mode toggle
- Code highlighting
- Search
- Navigation tabs
- Git revision dates
