package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PequeSaludRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión antes de acceder a Firestore.")
    }

    fun observeChildren(userId: String = requireUserId()): Flow<List<ChildProfile>> = flow {
        val path = "users/$userId/children"
        emitAll(
            db.collection("users").document(userId).collection("children")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChildProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.ageYears }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeRecords(userId: String = requireUserId()): Flow<List<HealthRecord>> = flow {
        val path = "users/$userId/records"
        emitAll(
            db.collection("users").document(userId).collection("records")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        HealthRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<HealthRecord> {
                            it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L
                        }.thenByDescending {
                            it.updatedAt?.nanoseconds ?: it.createdAt?.nanoseconds ?: 0
                        }
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getChildrenOnce(userId: String = requireUserId()): Result<List<ChildProfile>> {
        val path = "users/$userId/children"
        return try {
            val snapshot = db.collection("users").document(userId).collection("children")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            Result.success(
                snapshot.toObjects(
                    ChildProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getChildById(targetUserId: String, childId: String): Result<ChildProfile?> {
        val path = "users/$targetUserId/children/$childId"
        return try {
            val doc = db.collection("users").document(targetUserId)
                .collection("children").document(childId)
                .get()
                .await()
            Result.success(
                doc.toObject(
                    ChildProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    suspend fun createChild(profile: ChildProfile, customId: String? = null): Result<String> {
        val uid = requireUserId()
        val docId = customId ?: "child_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("children").document(docId)
        return try {
            docRef.set(profile.toCreateMap(uid)).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateChild(profile: ChildProfile): Result<Unit> {
        val uid = requireUserId()
        require(profile.id.isNotBlank()) { "El ID del perfil no puede estar vacío" }
        val docRef = db.collection("users").document(uid).collection("children").document(profile.id)
        return try {
            val updates = mapOf(
                "name" to profile.name.trim().ifEmpty { "Paciente" },
                "ageYears" to profile.ageYears,
                "birthDate" to profile.birthDate.trim().ifEmpty { "Por registrar" },
                "gender" to if (profile.gender == "Masculino") "Masculino" else "Femenino",
                "bloodType" to profile.bloodType.trim().ifEmpty { "O+" },
                "allergies" to profile.allergies.trim().ifEmpty { "Ninguna" },
                "pediatrician" to profile.pediatrician.trim().ifEmpty { "Dr. Alejandro López" },
                "emergencyContact" to profile.emergencyContact.trim().ifEmpty { "Por registrar" },
                "lastCheckupDate" to profile.lastCheckupDate.trim().ifEmpty { "Recién registrado" },
                "lastCheckupSummary" to profile.lastCheckupSummary.trim().ifEmpty { "Sin indicaciones registradas aún" },
                "avatarColorHex" to profile.avatarColorHex.trim().ifEmpty { "#E2ECFA" },
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteChild(childId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("children").document(childId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createRecord(record: HealthRecord, customId: String? = null): Result<String> {
        val uid = requireUserId()
        val docId = customId ?: "rec_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("records").document(docId)
        return try {
            docRef.set(record.toCreateMap(uid)).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateRecordStatus(recordId: String, newStatus: String, newDateText: String? = null): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (newDateText != null) {
                updates["dateText"] = newDateText
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateRecordNotesAndStatus(
        recordId: String,
        newStatus: String,
        newNotes: String,
        newDateText: String? = null
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus.trim().take(40),
                "notes" to newNotes.trim().take(1450),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (newDateText != null) {
                updates["dateText"] = newDateText.trim().take(120)
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(recordId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Limpia automáticamente los perfiles y registros de demostración pre-sembrados
     * ("sofia_lopez", "mateo_lopez", "valentina_lopez", "seed_rec_*", "seed_dual_ws_*")
     * para dejar la aplicación limpia y lista para ingresar datos reales.
     */
    suspend fun purgePreseededDemoData() {
        val uid = auth.currentUser?.uid ?: return
        val childrenCol = db.collection("users").document(uid).collection("children")
        val recordsCol = db.collection("users").document(uid).collection("records")

        val demoChildIds = listOf("sofia_lopez", "mateo_lopez", "valentina_lopez")
        val demoRecordIds = (1..25).map { "seed_rec_$it" } + (1..15).map { "seed_dual_ws_$it" }

        val batch = db.batch()
        demoChildIds.forEach { id ->
            batch.delete(childrenCol.document(id))
        }
        demoRecordIds.forEach { id ->
            batch.delete(recordsCol.document(id))
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Ignorar si ya no existen
        }
    }

    /**
     * Vacía todos los perfiles y registros del usuario activo para iniciar el expediente desde cero.
     */
    suspend fun clearAllUserData(): Result<Unit> {
        val uid = requireUserId()
        return try {
            val childrenSnap = db.collection("users").document(uid).collection("children")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val recordsSnap = db.collection("users").document(uid).collection("records")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            val allRefs = childrenSnap.documents.map { it.reference } + recordsSnap.documents.map { it.reference }
            allRefs.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { ref -> batch.delete(ref) }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedDualWorkspaceDemoRecordsIfMissing() {
        val uid = auth.currentUser?.uid ?: return
        val checkDoc = db.collection("users").document(uid)
            .collection("records").document("seed_dual_ws_1")
            .get()
            .await()
        if (checkDoc.exists()) return

        val dualWorkspaceSeeds = listOf(
            // Registros hechos por los Padres en Casa (SINTOMA_CASA, DIARIO_BEBE, DUDA_PADRE, ESTUDIO_PADRE)
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "SINTOMA_CASA",
                title = "Fiebre moderada (38.4 °C) + Congestión nasal",
                subtitle = "Registrado por Mamá/Papá en casa • Hace 3 horas",
                dateText = "Hoy • 06:30 AM",
                status = "Alerta en casa",
                notes = "Despertó calientita con 38.4 °C axilar y mocos transparentes. Se le ofreció agua y paños tibios. Buen ánimo general."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "SINTOMA_CASA",
                title = "Tos seca ocasional por la noche (36.7 °C)",
                subtitle = "Registrado por Mamá/Papá en casa",
                dateText = "Ayer • 09:15 PM",
                status = "Revisado por Pediatra",
                notes = "Tos leve al acostarse sin dificultad para respirar. | Respuesta Dr. López: Mantener hidratación tibia y elevar cabecera; vigilar si aparece fiebre."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "DIARIO_BEBE",
                title = "Sueño: 10.5 h nocturnas + 1 siesta • Apetito: Bueno",
                subtitle = "Diario en casa • 4 tomas de leche + papilla de frutas/verduras",
                dateText = "Hoy",
                status = "Normal",
                notes = "5 pañales mojados en el día. Durmió de corrido de 8:30 PM a 7:00 AM. Ánimo: Feliz y activa."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "DIARIO_BEBE",
                title = "Sueño: 9 h • 3 comidas completas + merienda escolar",
                subtitle = "Diario en casa • Hidratación: 6 vasos de agua",
                dateText = "Hoy",
                status = "Normal",
                notes = "Comió bien todo el almuerzo y realizó 45 min de actividad física en el colegio."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "DUDA_PADRE",
                title = "¿Podemos iniciar leche entera o seguimos con fórmula de crecimiento?",
                subtitle = "Duda de los padres para la próxima consulta",
                dateText = "Hoy",
                status = "Pendiente respuesta",
                notes = "Notamos que la tolerancia a derivados lácteos horneados ha mejorado mucho este mes."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "DUDA_PADRE",
                title = "¿Qué antibiótico puede tomar en caso de infección si es alérgica a Penicilina?",
                subtitle = "Duda de los padres • Respondida por el Pediatra",
                dateText = "18 oct 2026",
                status = "Respondida",
                notes = "Queremos saber qué informar en la enfermería del colegio. | Respuesta Dr. Alejandro López: En caso de requerir antibiótico bacteriano usamos Macrólidos (Azitromicina 10 mg/kg/día). Nunca administrar Amoxicilina ni Ampicilina."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "ESTUDIO_PADRE",
                title = "Foto de Biometría Hemática y Coprológico Escolar",
                subtitle = "Subido por los padres • Solicitud pre-consulta",
                dateText = "20 oct 2026",
                status = "Adjunto por padres",
                notes = "Estudio tomado en Laboratorio Referencia. Hb 13.1 g/dL, sin parásitos en examen seriado."
            ),
            // Registros Clínicos Exclusivos del Pediatra (PERINATAL, NEURODESARROLLO, CERTIFICADO)
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "PERINATAL",
                title = "39 SDG • Parto Eutócico • APGAR 9/10",
                subtitle = "Peso al nacer: 3,280 g • Talla: 50 cm • PC: 34.5 cm",
                dateText = "Nacimiento: 12/01/2018",
                status = "Normal",
                notes = "Tamiz Metabólico: Normal | Tamiz Auditivo (OEA): Pasa bilateral | Tamiz Cardiológico: Normal (SpO2 99%) | Tamiz Visual: Reflejo rojo presente | Cadera (Ortolani/Barlow): Negativo."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "PERINATAL",
                title = "38.4 SDG • Cesárea Electiva • APGAR 9/9",
                subtitle = "Peso al nacer: 3,050 g • Talla: 49 cm • PC: 34.0 cm",
                dateText = "Nacimiento: 19/08/2024",
                status = "Normal",
                notes = "Tamiz Metabólico Ampliado: Normal | Auditivo: Normal | Cardiológico: Normal | Visual: Normal | Cadera: Estable sin displasia. Lactancia materna exclusiva primeros 6 meses."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "NEURODESARROLLO",
                title = "Evaluación Psicomotora 24 Meses (2 años)",
                subtitle = "Motor Grueso, Motor Fino, Lenguaje y Social-Adaptativo",
                dateText = "10 oct 2026",
                status = "Acorde a edad",
                notes = "Motor Grueso: Corre sin caerse, sube escaleras alternando apoyo. | Motor Fino: Torre de 6 cubos, garabatea líneas. | Lenguaje: Frases de 2-3 palabras (>50 palabras). | Social: Juego simbólico e imitación. Sin signos de alarma neurológica."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "NEURODESARROLLO",
                title = "Evaluación Neurodesarrollo Escolar (8 años)",
                subtitle = "Coordinación, lectoescritura, atención y socialización",
                dateText = "15 ago 2026",
                status = "Acorde a edad",
                notes = "Excelente desempeño académico, motricidad fina madura, sueño reparador y adecuada integración con pares. Sin banderas rojas."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "CERTIFICADO",
                title = "Certificado de Buena Salud Escolar y Aptitud Física",
                subtitle = "Firmado: Dr. Alejandro López • Exequátur 482-19",
                dateText = "15 ago 2026",
                status = "Emitido",
                notes = "Se certifica que Sofía López (8 años) se encuentra clínicamente sana, con esquema de vacunación PAI al día para su edad y apta para realizar actividades físicas y deportivas escolares."
            )
        )

        dualWorkspaceSeeds.forEachIndexed { index, record ->
            createRecord(record, "seed_dual_ws_${index + 1}")
        }
    }

    suspend fun seedInitialFamilyIfEmpty() {
        val uid = auth.currentUser?.uid ?: return
        val existing = getChildrenOnce(uid).getOrNull() ?: return
        if (existing.isNotEmpty()) {
            seedDualWorkspaceDemoRecordsIfMissing()
            return
        }

        // 1. Seed the 3 children from the Familia López mockup
        val sofia = ChildProfile(
            name = "Sofía López",
            ageYears = 8L,
            birthDate = "12/01/2018",
            gender = "Femenino",
            bloodType = "O+",
            allergies = "Penicilina",
            pediatrician = "Dra. María González",
            emergencyContact = "809-555-1234",
            lastCheckupDate = "15 ago 2026",
            lastCheckupSummary = "Consulta general - Todo en orden",
            avatarColorHex = "#FDE2E4",
            weightKg = 26.4,
            heightCm = 128.0
        )
        val mateo = ChildProfile(
            name = "Mateo López",
            ageYears = 5L,
            birthDate = "04/05/2021",
            gender = "Masculino",
            bloodType = "A+",
            allergies = "Ninguna conocida",
            pediatrician = "Dra. María González",
            emergencyContact = "809-555-1234",
            lastCheckupDate = "02 sep 2026",
            lastCheckupSummary = "Control de los 5 años - Desarrollo óptimo",
            avatarColorHex = "#D8F3DC",
            weightKg = 18.8,
            heightCm = 110.0
        )
        val valentina = ChildProfile(
            name = "Valentina López",
            ageYears = 2L,
            birthDate = "19/08/2024",
            gender = "Femenino",
            bloodType = "O+",
            allergies = "Proteína de leche (leve)",
            pediatrician = "Dra. María González",
            emergencyContact = "809-555-1234",
            lastCheckupDate = "10 oct 2026",
            lastCheckupSummary = "Control de crecimiento y refuerzo vacunal",
            avatarColorHex = "#E2ECFA",
            weightKg = 12.4,
            heightCm = 87.5
        )

        createChild(sofia, "sofia_lopez")
        createChild(mateo, "mateo_lopez")
        createChild(valentina, "valentina_lopez")

        // 2. Seed Vaccines from the mockup for Sofía + Mateo + Valentina
        val initialRecords = listOf(
            // Sofía Vaccines (exact match with Screen 5 of mockup)
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "BCG",
                subtitle = "Dosis única",
                dateText = "12/01/2018",
                status = "Aplicada",
                notes = "Lote #BCG-2018A • Centro Pediátrico Santo Domingo. Sin reacción adversa."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "Hepatitis B",
                subtitle = "3 dosis",
                dateText = "12/01/2018 - 10/02/2018 - 15/06/2018",
                status = "Aplicada",
                notes = "Esquema completo neonatal y lactante."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "Pentavalente",
                subtitle = "4 dosis",
                dateText = "12/01/2018 - 15/03/2018 - 10/05/2018 - 12/07/2019",
                status = "Aplicada",
                notes = "DTP + HB + Hib completadas con refuerzo."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "Polio",
                subtitle = "4 dosis",
                dateText = "12/01/2018 - 15/03/2018 - 10/05/2018 - 12/07/2019",
                status = "Aplicada",
                notes = "Esquema IPV/OPV completo."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "Neumococo",
                subtitle = "3 dosis",
                dateText = "12/01/2018 - 15/03/2018 - 10/05/2018",
                status = "Aplicada",
                notes = "Prevenar 13 • Esquema completo."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "Influenza Estacional 2026",
                subtitle = "Refuerzo anual",
                dateText = "15/11/2026",
                status = "Pendiente",
                notes = "Programada para campaña de otoño con Dra. María González."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "VACUNA",
                title = "VPH (1ra Dosis)",
                subtitle = "Esquema escolar (9 años)",
                dateText = "12/01/2027",
                status = "Pendiente",
                notes = "Indicada al cumplir 9 años."
            ),
            // Mateo & Valentina Vaccines
            HealthRecord(
                childId = "mateo_lopez",
                childName = "Mateo López",
                category = "VACUNA",
                title = "DPT + Polio (Refuerzo 5 años)",
                subtitle = "Refuerzo preescolar",
                dateText = "04/05/2026",
                status = "Aplicada",
                notes = "Refuerzo de ingreso escolar aplicado sin fiebre."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "VACUNA",
                title = "SRP (Triple Viral - 2da dosis)",
                subtitle = "Refuerzo 2 años",
                dateText = "28/10/2026",
                status = "Pendiente",
                notes = "Cita agendada en vacunatorio pediátrico."
            ),
            // Historial Clínico
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "HISTORIAL",
                title = "Control Pediátrico General",
                subtitle = "Dra. María González • Pediatría",
                dateText = "15 ago 2026",
                status = "Completado",
                notes = "Consulta general - Todo en orden. Auscultación cardiopulmonar normal, agudeza visual 20/20."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "HISTORIAL",
                title = "Faringoamigdalitis Aguda",
                subtitle = "Diagnóstico y tratamiento ambulatorio",
                dateText = "10 mar 2026",
                status = "Recuperada",
                notes = "Tratada con Azitromicina suspensión oral por 5 días (alergia a Penicilina documentada)."
            ),
            // Crecimiento y Desarrollo
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "CRECIMIENTO",
                title = "Peso: 26.4 kg • Talla: 128 cm",
                subtitle = "IMC: 16.1 kg/m² • Percentil 62 OMS",
                dateText = "15 ago 2026",
                status = "Percentil Normal",
                notes = "Crecimiento armónico. Desarrollo psicomotor, lectoescritura y coordinación motriz fina excelentes."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "CRECIMIENTO",
                title = "Peso: 24.8 kg • Talla: 124 cm",
                subtitle = "IMC: 16.1 kg/m² • Percentil 60 OMS",
                dateText = "12 feb 2026",
                status = "Percentil Normal",
                notes = "Ganancia ponderal de +1.6 kg y +4 cm en 6 meses."
            ),
            HealthRecord(
                childId = "mateo_lopez",
                childName = "Mateo López",
                category = "CRECIMIENTO",
                title = "Peso: 18.8 kg • Talla: 110 cm",
                subtitle = "IMC: 15.5 kg/m² • Percentil 55 OMS",
                dateText = "02 sep 2026",
                status = "Percentil Normal",
                notes = "Lenguaje fluido, motricidad gruesa y equilibrio acordes a 5 años."
            ),
            // Medicamentos
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "MEDICAMENTO",
                title = "Vitamina D3 + Zinc Pediátrico",
                subtitle = "10 gotas cada mañana con el desayuno",
                dateText = "01 oct 2026 - 30 nov 2026",
                status = "Activo",
                notes = "Suplemento inmunológico estacional. ALERTA: Evitar derivados de Penicilina / Amoxicilina."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "MEDICAMENTO",
                title = "Sulfato Ferroso Gotas",
                subtitle = "15 gotas al día lejos de lácteos",
                dateText = "10 oct 2026 - 10 dic 2026",
                status = "Activo",
                notes = "Profilaxis de anemia ferropénica del lactante."
            ),
            // Laboratorios
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "LABORATORIO",
                title = "Hemograma Completo + Ferritina",
                subtitle = "Laboratorio Clínico Referencia",
                dateText = "10 ago 2026",
                status = "Normal",
                notes = "Hemoglobina: 13.1 g/dL, Hematocrito: 39.2%, Leucocitos: 6,800/mm³, Ferritina: 42 ng/mL.Valores dentro de rango."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "LABORATORIO",
                title = "Perfil Lipídico y Glucemia Basal",
                subtitle = "Chequeo escolar preventivo",
                dateText = "10 ago 2026",
                status = "Normal",
                notes = "Glucosa: 84 mg/dL, Colesterol total: 148 mg/dL. Sin hallazgos patológicos."
            ),
            // Consultas
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "CONSULTA",
                title = "Odontopediatría - Limpieza y Flúor",
                subtitle = "Dr. Carlos Méndez • Odontopediatra",
                dateText = "05 nov 2026 • 4:00 PM",
                status = "Programada",
                notes = "Revisión semestral de dentición mixta y sellantes."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "CONSULTA",
                title = "Control Pediátrico Anual",
                subtitle = "Dra. María González • Pediatra",
                dateText = "15 ago 2026 • 10:00 AM",
                status = "Completada",
                notes = "Se emitió certificado médico escolar 2026-2027."
            ),
            // Nutrición
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "NUTRICION",
                title = "Plan Alimentario Escolar Balanceado",
                subtitle = "1,650 kcal/día • Rico en calcio y hierro",
                dateText = "Actualizado ago 2026",
                status = "Activo",
                notes = "Desayuno: Avena con frutas y huevo. Merienda escolar: Frutos secos, yogur y manzana. Hidratación: 1.5 L de agua/día."
            ),
            // Documentos
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "DOCUMENTO",
                title = "Certificado Médico Escolar 2026-2027.pdf",
                subtitle = "Informe firmado por Dra. María González",
                dateText = "15 ago 2026",
                status = "Verificado",
                notes = "Apta para educación física y actividades deportivas escolares."
            ),
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "DOCUMENTO",
                title = "Tarjeta Oficial de Vacunación PAI.pdf",
                subtitle = "Carnet digitalizado de vacunas",
                dateText = "15 ago 2026",
                status = "Verificado",
                notes = "Incluye lotes y sellos de BCG, Hepatitis B, Pentavalente, Polio y Neumococo."
            ),
            // Calendario
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "CALENDARIO",
                title = "Vacuna Influenza Estacional",
                subtitle = "Recordatorio de vacunación",
                dateText = "15 nov 2026 • 9:30 AM",
                status = "Programada",
                notes = "Clínica Pediátrica • Consultorio 302."
            ),
            HealthRecord(
                childId = "valentina_lopez",
                childName = "Valentina López",
                category = "CALENDARIO",
                title = "Refuerzo SRP (Triple Viral)",
                subtitle = "Cita de vacunación",
                dateText = "28 oct 2026 • 11:00 AM",
                status = "Programada",
                notes = "Llevar carnet físico de vacunación."
            ),
            // Hospitalizaciones y Emergencias
            HealthRecord(
                childId = "sofia_lopez",
                childName = "Sofía López",
                category = "HOSPITALIZACION",
                title = "Observación por Bronquiolitis Leve",
                subtitle = "Urgencias Pediátricas • Nebulización",
                dateText = "18 nov 2020",
                status = "Alta médica",
                notes = "Estadía en observación de 6 horas a los 2 años de edad. Sin secuelas respiratorias."
            )
        )

        initialRecords.forEachIndexed { index, record ->
            createRecord(record, "seed_rec_${index + 1}")
        }
        seedDualWorkspaceDemoRecordsIfMissing()
    }
}
