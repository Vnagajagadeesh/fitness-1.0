# Security Policy — FITNESS 1.0

## 🔒 Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

---

## 🛡️ Security Architecture & Principles

- **Zero-Secret Codebase**: This repository contains no hardcoded API keys, bearer tokens, passwords, private certificates, or production database credentials.
- **Client-Side Encryption & Public Anon Key**: Cloud synchronization via Supabase requires only the project's public anonymous key (`anon`) with Row-Level Security (RLS) configured on tables. The sensitive `service_role` key must never be bundled into the application.
- **Health Data Isolation**: Step data accessed via Android Health Connect requires explicit user consent and adheres to Google Health Connect permission guidelines. No health data is transmitted to third parties without user configuration.
- **Local-First Storage**: User habits, weight entries, fasting cycles, and challenge records are persisted in a sandboxed local SQLite database using Android Room.

---

## 🚨 Reporting a Vulnerability

If you discover a security vulnerability within **FITNESS 1.0**:
1. Do **NOT** open a public issue.
2. Please report the finding privately to the project maintainer.
3. Include detailed reproduction steps and affected components.
4. Reports will be investigated promptly and patched in an expedited update.
