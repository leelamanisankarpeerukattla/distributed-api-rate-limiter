# Contributing

Thank you for your interest in contributing to this project!
Contributions of all kinds—bug reports, feature requests, documentation improvements, and code changes—are welcome.

---

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Create a new branch from `main`:
   ```bash
   git checkout -b feature/your-change
   ```
4. Make your changes with clear, descriptive commits
5. Push your branch and open a Pull Request

---

## Development Setup

### Prerequisites
- Java 17 or later
- Maven 3.8+
- Docker Desktop

### Run locally
```bash
docker compose up --build
```

The service will be available at:
```
http://localhost:8085
```

---

## Testing

Run the test suite:
```bash
mvn test
```

Ensure all tests pass before opening a Pull Request.

---

## Code Guidelines

- Follow existing code structure and naming conventions
- Keep changes focused and easy to review
- Write clear commit messages
- Add or update tests where applicable
- Avoid unrelated formatting changes

---

## Documentation

If your change affects behavior, configuration, or APIs:
- Update the README or relevant documentation
- Include examples where appropriate

---

## Reporting Issues

If you find a bug or want to request a feature:
1. Check existing issues to avoid duplicates
2. Open a new issue with:
    - Clear description
    - Steps to reproduce (if applicable)
    - Expected vs actual behavior

---

## Pull Request Checklist

Before submitting a PR, please ensure:
- Code builds successfully
- Tests pass locally
- Changes are documented where necessary
- PR description clearly explains the change

---

## Code of Conduct

This project follows the Contributor Covenant Code of Conduct.
By participating, you are expected to uphold these standards.

---

Thank you for helping improve this project!
