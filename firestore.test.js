const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validChildPayload(userId = ALICE_UID) {
  const now = new Date();
  return {
    userId,
    name: "Sofía López",
    ageYears: 8,
    birthDate: "12/01/2018",
    gender: "Femenino",
    bloodType: "O+",
    allergies: "Penicilina",
    pediatrician: "Dra. María González",
    emergencyContact: "809-555-1234",
    lastCheckupDate: "15 ago 2026",
    lastCheckupSummary: "Consulta general - Todo en orden",
    avatarColorHex: "#FDE2E4",
    weightKg: 26.5,
    heightCm: 128.0,
    createdAt: now,
    updatedAt: now,
  };
}

function validRecordPayload(userId = ALICE_UID) {
  const now = new Date();
  return {
    userId,
    childId: "sofia_1",
    childName: "Sofía López",
    category: "VACUNA",
    title: "BCG",
    subtitle: "Dosis única",
    dateText: "12/01/2018",
    status: "Aplicada",
    notes: "Aplicada al nacer sin reacciones adversas.",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or list children or records", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("children").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("records").get()
  );
});

test("Authenticated owner can create, read, list, update, and delete child profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const childRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("children")
    .doc("sofia_1");

  await assertSucceeds(childRef.set(validChildPayload(ALICE_UID)));
  await assertSucceeds(childRef.get());
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("children")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    childRef.update({
      ageYears: 9,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(childRef.delete());
});

test("Cross-user access to children and records is rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("children")
      .doc("sofia_1")
      .set(validChildPayload(ALICE_UID));
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("records")
      .doc("rec_1")
      .set(validRecordPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("children")
      .doc("sofia_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("records")
      .doc("rec_1")
      .get()
  );
});

test("Shadow fields and invalid enums are rejected on HealthRecord", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const recRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("records")
    .doc("rec_bad");

  await assertFails(
    recRef.set({
      ...validRecordPayload(ALICE_UID),
      isAdmin: true,
    })
  );

  await assertFails(
    recRef.set({
      ...validRecordPayload(ALICE_UID),
      category: "INVALID_MODULE",
    })
  );
});
