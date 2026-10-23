package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PequeSaludRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveChild_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = PequeSaludRepository(firestore)
        val childId = "child_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createChild(
                ChildProfile(
                    name = "Sofía López",
                    ageYears = 8L,
                    birthDate = "12/01/2018",
                    gender = "Femenino",
                    bloodType = "O+",
                    allergies = "Penicilina"
                ),
                customId = childId
            )
        }
        assertTrue(createResult.isSuccess)

        val children = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeChildren(uid).first { list -> list.any { it.id == childId } }
        }
        assertTrue(children.any { it.id == childId && it.name == "Sofía López" })
    }

    @Test
    fun createAndUpdateRecord_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = PequeSaludRepository(firestore)
        val recId = "rec_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createRecord(
                HealthRecord(
                    childId = "sofia_lopez",
                    childName = "Sofía López",
                    category = "VACUNA",
                    title = "BCG",
                    subtitle = "Dosis única",
                    dateText = "12/01/2018",
                    status = "Pendiente",
                    notes = "Nota inicial"
                ),
                customId = recId
            )
        }
        assertTrue(createResult.isSuccess)

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateRecordStatus(recId, "Aplicada", "12/01/2018")
        }
        assertTrue(updateResult.isSuccess)

        val records = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeRecords(uid).first { list -> list.any { it.id == recId && it.status == "Aplicada" } }
        }
        assertTrue(records.any { it.id == recId })
    }

    @Test
    fun getChildById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = PequeSaludRepository(firestore)
        val childId = "child_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createChild(ChildProfile(name = "Sofía"), customId = childId).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = PequeSaludRepository(firestore)
        val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getChildById(aliceUid, childId)
        }
        assertTrue(crossResult.isFailure)
        val fsError = crossResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun observeChildren_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = PequeSaludRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeChildren(aliceUid).first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        assertNotNull(caught)
        val fsException = caught?.unwrapFirestoreException()
        assertNotNull(fsException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@pequesalud.test"
        const val BOB_EMAIL = "bob@pequesalud.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
