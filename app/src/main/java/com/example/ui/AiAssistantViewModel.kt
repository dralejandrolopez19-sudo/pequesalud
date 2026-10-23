package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiAssistantVM"

data class StructuredConsultationSummary(
    val resumenGeneral: String,
    val alertasImportantes: List<String>,
    val preguntasSugeridasPediatra: List<String>
)

sealed interface AiAssistantUiState {
    data object Idle : AiAssistantUiState
    data object Loading : AiAssistantUiState
    data class Streaming(val partialText: String) : AiAssistantUiState
    data class Success(
        val responseText: String,
        val structuredSummary: StructuredConsultationSummary? = null
    ) : AiAssistantUiState
    data class Error(val message: String) : AiAssistantUiState
}

class AiAssistantViewModel : ViewModel() {

    private val chatModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val summarySchema = Schema.obj(
        properties = mapOf(
            "resumenGeneral" to Schema.string(description = "Resumen conciso del niño basado únicamente en los datos registrados"),
            "alertasImportantes" to Schema.array(
                items = Schema.string(),
                description = "Lista de alergias, fiebres en casa, vacunas pendientes o puntos clave a vigilar"
            ),
            "preguntasSugeridasPediatra" to Schema.array(
                items = Schema.string(),
                description = "Preguntas o conductas recomendadas para la consulta pediátrica"
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = summarySchema
            }
        )
    }

    private val _uiState = MutableStateFlow<AiAssistantUiState>(AiAssistantUiState.Idle)
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private fun buildContextPrompt(
        child: ChildProfile?,
        records: List<HealthRecord>,
        isPediatricianMode: Boolean
    ): String {
        if (child == null) return "No hay perfil de niño seleccionado."
        val childRecords = records.filter { it.childId == child.id || it.childName == child.name }
        val recordsSummary = childRecords.joinToString("\n") { rec ->
            "- [${rec.category}] ${rec.title} (${rec.subtitle}) | Fecha: ${rec.dateText} | Estado: ${rec.status} | Notas: ${rec.notes}"
        }
        val roleInstructions = if (isPediatricianMode) {
            """
            MODO ACTIVO: ESTACIÓN CLÍNICA DEL PEDIATRA (Uso exclusivo para el Médico Especialista).
            Usa terminología médica pediátrica rigurosa (SOAP, CIE-10, farmacología ponderal en mg/kg/día y mg/kg/dosis, percentiles OMS, signos de alarma y correlación clínica con los síntomas reportados en casa por los padres). Verifica siempre las alergias del paciente antes de sugerir fármacos.
            """.trimIndent()
        } else {
            """
            MODO ACTIVO: ESCRITORIO DE PADRES (Orientación para Mamá y Papá en casa).
            Usa un lenguaje cálido, empático, tranquilizador y 100% libre de tecnicismos médicos complejos. Explica de forma sencilla qué significan los registros de su hijo/a, qué cuidados generales en casa pueden realizar y cuáles son los signos de alarma para acudir al pediatra. Nunca prescribas medicamentos nuevos por cuenta propia; remite a las indicaciones que dejó su pediatra.
            """.trimIndent()
        }
        return """
            Eres el sistema de soporte clínico y orientación pediátrica de PequeSalud. Responde SIEMPRE en español basándote en los datos registrados del niño/a y nunca menciones que eres una inteligencia artificial.
            $roleInstructions
            
            DATOS DEL PACIENTE / HIJO(A):
            - Nombre: ${child.name} (Código familiar: ${child.familyLinkCode()})
            - Edad: ${child.ageYears} años (Nacimiento: ${child.birthDate})
            - Sexo: ${child.gender} | Grupo sanguíneo: ${child.bloodType}
            - ALERGIAS CONOCIDAS: ${child.allergies}
            - Pediatra de cabecera: ${child.pediatrician}
            - Peso actual: ${child.weightKg} kg | Talla actual: ${child.heightCm} cm
            - Último control: ${child.lastCheckupDate} (${child.lastCheckupSummary})
            
            REGISTROS EN EXPEDIENTE Y CASA (${childRecords.size}):
            $recordsSummary
        """.trimIndent()
    }

    fun askQuestionStream(
        question: String,
        child: ChildProfile?,
        records: List<HealthRecord>,
        isPediatricianMode: Boolean = false
    ) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.value = AiAssistantUiState.Loading
            val accumulated = StringBuilder()
            val prompt = """
                ${buildContextPrompt(child, records, isPediatricianMode)}
                
                CONSULTA (${if (isPediatricianMode) "MÉDICO PEDIATRA" else "PADRES EN CASA"}):
                $question
            """.trimIndent()

            try {
                withTimeout(60_000L) {
                    chatModel.generateContentStream(prompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        accumulated.append(text)
                        _uiState.value = AiAssistantUiState.Streaming(accumulated.toString())
                    }
                }
                if (accumulated.isNotBlank()) {
                    _uiState.value = AiAssistantUiState.Success(accumulated.toString())
                } else {
                    _uiState.value = AiAssistantUiState.Error("No se obtuvo respuesta para esta consulta.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Clinical guidance streaming timed out", e)
                _uiState.value = AiAssistantUiState.Error("La consulta tardó más de 60 segundos. Intenta nuevamente.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Clinical guidance streaming failed", e)
                _uiState.value = AiAssistantUiState.Error(
                    e.localizedMessage ?: "Error al procesar la consulta clínica."
                )
            }
        }
    }

    fun generateConsultationSummary(
        child: ChildProfile?,
        records: List<HealthRecord>,
        isPediatricianMode: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = AiAssistantUiState.Loading
            val goalText = if (isPediatricianMode) {
                "Genera una síntesis clínica pre-consulta para el pediatra integrando antecedentes, alergias, somatometría, síntomas reportados en casa por los padres y dudas pendientes."
            } else {
                "Genera un resumen sencillo y claro para que los padres lleven a la próxima consulta con su pediatra."
            }
            val prompt = """
                ${buildContextPrompt(child, records, isPediatricianMode)}
                
                $goalText
            """.trimIndent()

            try {
                val response = withTimeout(60_000L) {
                    structuredModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("Respuesta vacía del servicio clínico.")
                val json = JSONObject(jsonString)
                val resumen = json.optString("resumenGeneral", "")
                val alertasArr = json.optJSONArray("alertasImportantes")
                val alertas = if (alertasArr != null) {
                    (0 until alertasArr.length()).map { alertasArr.getString(it) }
                } else emptyList()
                val preguntasArr = json.optJSONArray("preguntasSugeridasPediatra")
                val preguntas = if (preguntasArr != null) {
                    (0 until preguntasArr.length()).map { preguntasArr.getString(it) }
                } else emptyList()

                val structured = StructuredConsultationSummary(
                    resumenGeneral = resumen,
                    alertasImportantes = alertas,
                    preguntasSugeridasPediatra = preguntas
                )
                _uiState.value = AiAssistantUiState.Success(
                    responseText = resumen,
                    structuredSummary = structured
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Structured summary timed out", e)
                _uiState.value = AiAssistantUiState.Error("El resumen tardó más de 60 segundos. Intenta nuevamente.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Structured summary failed", e)
                _uiState.value = AiAssistantUiState.Error(
                    e.localizedMessage ?: "No se pudo generar el resumen para consulta."
                )
            }
        }
    }

    fun generateSoapEvolutionDraft(
        child: ChildProfile?,
        vitalsText: String,
        examFindings: String,
        diagnosisCie10: String,
        onDraftReady: (String) -> Unit
    ) {
        viewModelScope.launch {
            val prompt = """
                Redacta en español una nota breve de evolución y plan terapéutico pediátrico (formato SOAP conciso, máximo 3 líneas, sin mencionar inteligencia artificial) para:
                - Paciente: ${child?.name ?: "Paciente"} (${child?.ageYears ?: 5} años, Peso: ${child?.weightKg ?: 20.0} kg, Alergias: ${child?.allergies ?: "Ninguna"})
                - Signos vitales: $vitalsText
                - Exploración física: $examFindings
                - Diagnóstico CIE-10: $diagnosisCie10
            """.trimIndent()
            try {
                val response = withTimeout(45_000L) {
                    chatModel.generateContent(prompt)
                }
                val text = response.text?.trim()
                if (!text.isNullOrBlank()) {
                    onDraftReady(text)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "SOAP draft generation failed", e)
            }
        }
    }
}
