# Security Specification (`security_spec.md`) — PequeSalud

## 1. Data Invariants
1. **Strict Tenant Isolation**: Every document in `/users/{userId}/children/{childId}` and `/users/{userId}/records/{recordId}` belongs exclusively to `userId` (`request.auth.uid == userId` and `data.userId == request.auth.uid`). No cross-user read, list, create, update, or delete is permitted.
2. **PII & Medical Data Protection**: Child medical profiles (`bloodType`, `allergies`, `emergencyContact`) and clinical records (`VACUNA`, `HISTORIAL`, `MEDICAMENTO`, etc.) are strictly restricted to the authenticated owner (`request.auth.uid == userId`).
3. **Immutable Ownership & Creation Metadata**: On `update`, `userId` and `createdAt` must remain unchanged (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Strict Schema & Bounds Validation**: Every `create` and `update` operation validates exact allowed keys (`hasOnly`), required keys (`hasAll`), string length bounds, numeric ranges, enum allowlists, and `timestamp <= request.time`.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read/List**: `auth == null` attempting to list `/users/alice_123/children`.
2. **Cross-User Read (PII Leak)**: `bob_456` attempting to read `/users/alice_123/children/sofia`.
3. **Identity Spoofing on Create**: `alice_123` writing to `/users/alice_123/children/sofia` with `userId: "bob_456"`.
4. **Shadow/Ghost Field Injection on Create**: Adding `"isAdmin": true` to a `ChildProfile` payload.
5. **Shadow/Ghost Field Injection on Update**: Updating a `HealthRecord` with an undeclared field `"verifiedByDoctor": true`.
6. **Immutable `createdAt` Tampering on Update**: Modifying `createdAt` during an update to `/users/alice_123/records/rec_1`.
7. **Immutable `userId` Transfer on Update**: Changing `userId` from `alice_123` to `bob_456` on update.
8. **Invalid Category Enum on `HealthRecord`**: Supplying `category: "HACKED_CATEGORY"` outside the 10 allowed clinical module types.
9. **Invalid Gender Enum on `ChildProfile`**: Supplying `gender: "Unknown"` outside `["Femenino", "Masculino"]`.
10. **String Overflow (Denial of Wallet)**: Supplying a 5,000-character string in `title` or `notes`.
11. **Future Timestamp Spoofing**: Setting `createdAt` or `updatedAt` in the future (`> request.time`).
12. **Negative Age / Invalid Anthropometrics**: Creating a `ChildProfile` with `ageYears: -3` or `weightKg: -10`.
