# Security Policy

## Supported Version

This repository currently tracks development on the `main` branch.

## Reporting

Do not open a public issue for secrets, authentication bypasses, payment flaws, or data exposure reports.

Please report security concerns privately to the repository owner through GitHub contact channels. Include:

- Affected surface: `miniapp`, `admin-web`, `server`, or deployment.
- Steps to reproduce.
- Expected impact.
- Relevant logs, screenshots, or request samples with secrets redacted.

## Secret Handling

- Never commit real `.env` files, JWT secrets, database passwords, Redis passwords, WeChat credentials, certificates, or payment keys.
- `.env.example` is the only environment file intended for version control.
- Demo credentials in documentation are public demo values only and must not be reused in production.
