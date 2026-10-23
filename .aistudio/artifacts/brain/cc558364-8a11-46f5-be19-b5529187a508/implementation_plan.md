# PequeSalud: Arquitectura de Doble Escritorio (Modo Padres y Estación Clínica del Pediatra)

Rediseño integral de **PequeSalud** que divide la aplicación en dos entornos especializados pero conectados en tiempo real: un **Escritorio de Padres** cálido, guiado y libre de tecnicismos para el cuidado en casa, y una **Estación Clínica del Pediatra** protegida por **PIN médico** que incorpora las herramientas reales de la práctica pediátrica (calculadora de dosis ponderal `mg/kg → mL`, signos vitales por edad, historia perinatal/APGAR, neurodesarrollo, exploración física cefalocaudal, certificados/interconsultas y triaje de reportes de casa).

---

## Revisión del Usuario y Decisiones Confirmadas

> [!IMPORTANT]
> Se ha ampliado significativamente el **Escritorio del Pediatra** para que no sea solo un visor de registros, sino una **estación clínica pediátrica completa** con herramientas de cálculo, evaluación neonatal/psicomotora y documentación médica formal.

- **Decisión 1 — Acceso y Seguridad por PIN Médico**:
  - La app inicia en el **Escritorio de Padres** y permite cambiar al **Escritorio del Pediatra** mediante un **PIN médico de 4 dígitos** (configurable por el especialista, predeterminado `1234`) más el perfil profesional del médico (**Nombre del Pediatra, Cédula / Exequátur, Subespecialidad y Teléfono de Consultorio**) que firma automáticamente recetas y certificados.
- **Decisión 2 — Nuevos Módulos Clínicos Agregados al Escritorio del Pediatra**:
  1. **Calculadora Farmacológica Ponderal (`mg/kg` a `mL` / `gotas`) Integrada al Recetario**:
     - Toma automáticamente el **peso actual en kg** del paciente.
     - Incluye catálogo de fármacos pediátricos frecuentes (Paracetamol, Ibuprofeno, Amoxicilina, Amoxicilina/Ácido Clavulánico, Azitromicina, Cetirizina, Prednisolona, Salbutamol, Suero Oral) con sus presentaciones comerciales (`100 mg/5 mL`, `120 mg/5 mL`, `250 mg/5 mL`, `400 mg/5 mL`, gotas `100 mg/mL`).
     - Calcula al instante los **mg por dosis**, la **conversión exacta en mL o gotas** cada 6, 8, 12 o 24 horas y permite insertarla con un toque en la **Receta Digital** que ven los padres en lenguaje claro.
  2. **Signos Vitales Pediátricos con Semáforo por Grupo Etario**:
     - Registro de **Frecuencia Cardíaca (FC lpm)**, **Frecuencia Respiratoria (FR rpm)**, **Temperatura (°C)**, **Saturación de O₂ (SpO₂ %)** y **Tensión Arterial (TA mmHg)** con validación automática según la edad del niño (alertando taquicardia, taquipnea, fiebre o desaturación).
  3. **Historia Perinatal, Neonatal y Tamizajes Ampliados**:
     - Ficha neonatal dedicada: **Semanas de Gestación (SDG)**, vía de nacimiento (Parto eutócico / Cesárea), **Peso, Talla y Perímetro Cefálico al nacer**, **APGAR (1 min / 5 min)**, **Silverman-Anderson**, requerimiento de UCIN/fototerapia y **5 Tamizajes** (Metabólico ampliado, Auditivo, Cardiológico crítico, Visual/Reflejo rojo y Cadera/Ortolani-Barlow).
  4. **Evaluación del Neurodesarrollo Psicomotor (Hitos por Edad y Banderas Rojas)**:
     - Evaluación estructurada en 4 áreas (**Motor Grueso, Motor Fino, Lenguaje y Social/Adaptativo**) con lista de hitos esperados para la edad y detección de signos de alarma neurológicos.
  5. **Exploración Física Pediátrica por Aparatos y Sistemas + Nota SOAP**:
     - Plantilla clínica rápida por sistemas (*Aspecto general/hidratación, Fontanelas y Cráneo, Otoscopia/Orofaringe, Cardiopulmonar, Abdomen, Genitourinario/Tanner, Piel y Neurológico*), diagnóstico **CIE-10** sugerido y plan de manejo.
  6. **Generador de Certificados Médicos, Constancias e Interconsultas**:
     - Emisión de **Certificado de Salud Escolar / Deportivo**, **Constancia de Vacunación al Día** y **Hoja de Referencia a Subespecialista Pediátrico** (Cardiología, Neurología, Neumología, Gastropediatría, Ortopedia, Alergología), listos para compartir o imprimir y visibles para los padres en su sección de Documentos.
  7. **Triaje de Pre-Consulta (Alertas Domiciliarias de los Padres)**:
     - Panel prioritario donde el pediatra ve en tiempo real qué pacientes reportaron fiebre alta o signos de alarma en casa, las gráficas de temperatura de las últimas horas, el diario de tomas/sueño y las **dudas escritas por los padres**, pudiendo responderlas directamente desde su escritorio para que los padres reciban la respuesta del doctor en su pantalla.

---

## 1. Visión General y Concepto Central

- **Qué Hace**: Conecta el hogar con el consultorio pediátrico a través de dos interfaces diseñadas a la medida de cada rol:
  - **Escritorio de Padres**: Cero tecnicismos. Enfocado en *"¿Qué debo darle hoy a mi hijo?"*, *"¿Qué síntomas ha tenido en casa?"*, *"¿Cómo va su diario de comida y sueño?"* y *"¿Qué quiero preguntarle al doctor?"*.
  - **Escritorio del Pediatra**: Estación médica completa con navegación clínica propia (**Pacientes y Triaje**, **Consulta SOAP + Dosis mg/kg**, **Somatometría + Vacunas**, **Perinatal + Neurodesarrollo**, **Certificados + IA Clínica**).

---

## 2. Experiencia de Usuario y Diseño Visual

### Comparativa de Ambos Escritorios

| Dimensión | Escritorio de Padres (Familiar) | Escritorio del Pediatra (Especialista) |
| :--- | :--- | :--- |
| **Navegación Inferior** | `Inicio Casa` · `Síntomas/Diario` · `Mis Recetas y Vacunas` · `Dudas y Estudios` · `Más` | `Pacientes y Triaje` · `Consulta y Dosis` · `Crecimiento y Vacunas` · `Clínica y Perinatal` · `Más / IA` |
| **Lenguaje e Interfaz** | Sencillo, visual, empático, botones grandes y semáforos ("Al día", "Creciendo sano") | Clínico, preciso, estructurado por sistemas (SOAP, CIE-10, Percentiles OMS, mg/kg/dosis) |
| **Permisos de Escritura** | Solo registra eventos en casa: fiebre/síntomas, sueño/comida, dudas y fotos de estudios | Control total del expediente: diagnósticos, recetas, vacunas con lote, percentiles, alergias y certificados |
| **Asistente IA Gemini** | Orientación para padres (cuidados en casa y cuándo acudir a urgencias, sin recetar) | Soporte de decisión clínica (dosis ponderales, diagnósticos diferenciales pediátricos y guías clínicas) |

### Flujo Completo del Pediatra en su Escritorio

1. **Barra Superior Clínica y Buscador de Pacientes**:
   - Muestra los datos del especialista (ej. *Dr. Alejandro López · Pediatra*) y permite buscar pacientes por nombre o **Código de Vinculación Familiar** (`PEQUE-SOF-842`), o dar de alta un nuevo paciente pediátrico.
   - Banda de alerta clínica permanente del paciente activo: **Alergias en rojo**, **Grupo sanguíneo**, **Edad cronológica exacta**, **Peso/Talla actual** y **Percentil OMS**.
2. **Pestaña 1 — Pacientes y Bandeja de Pre-Consulta (Triaje de Casa)**:
   - Muestra los **Síntomas y Fiebres registrados por los padres** (con alerta si la temperatura ≥ 38.0 °C), el resumen del **Diario del Bebé** (alimentación/sueño), los **Estudios subidos por la familia** y las **Dudas de los Padres** con un botón **"Responder a los padres"** para enviarles una indicación directa antes o durante la consulta.
3. **Pestaña 2 — Consulta SOAP, Exploración Física y Calculadora Ponderal (`mg/kg`)**:
   - **Signos Vitales Pediátricos**: FC, FR, Temp, SpO₂ y TA con evaluación automática según edad.
   - **Exploración Física por Sistemas**: Checklists rápidos (Otoscopia, Orofaringe, Cardiopulmonar, Abdomen, Fontanelas, Piel) + Nota Subjetiva/Objetiva y Diagnóstico CIE-10.
   - **Calculadora de Dosis Pediátrica y Recetario**: Selecciona el medicamento, ajusta `mg/kg/día` o `mg/kg/dosis`, elige la concentración del jarabe/suspensión (`mg/5 mL`) y genera automáticamente la indicación exacta en `mL` cada `X` horas por `Y` días, enviándola directo al recetario de los padres.
4. **Pestaña 3 — Somatometría OMS (Percentiles) y Vacunación Oficial**:
   - **Calculadora Antropométrica**: Peso (kg), Talla (cm), Perímetro Cefálico (cm), cálculo automático de **IMC** y **Percentil OMS estimado** (`P3`, `P15`, `P50`, `P85`, `P97`) con curva evolutiva.
   - **Esquema Oficial de Vacunación**: Registro exclusivo del médico con nombre del biológico, dosis (1ª, 2ª, Refuerzo), **número de lote**, centro/consultorio de aplicación y programación de la próxima dosis.
5. **Pestaña 4 — Historia Perinatal, Neurodesarrollo, Laboratorios y Certificados**:
   - **Ficha Perinatal y Tamizajes**: SDG, APGAR, peso/talla al nacer y estado de los 5 tamizajes neonatales.
   - **Hitos del Neurodesarrollo**: Evaluación psicomotora por edad (Motor grueso/fino, lenguaje, social).
   - **Órdenes de Laboratorio, Certificados e Interconsultas**: Solicitud de biometría/uroanálisis/perfil viral, emisión de **Certificado Médico Escolar** o **Referencia a Subespecialista** firmada por el pediatra.

---

## 3. Decisiones de Producto y Balance Técnico

- **Decisión 1: Calculadora de Dosis Ponderal Integrada al Expediente**
  - *Enfoque Elegido*: La calculadora lee automáticamente el campo `weightKg` del niño seleccionado y convierte la dosis ponderal en mililitros exactos listos para guardarse como prescripción activa.
  - *Por qué*: En pediatría no existen dosis estándar fijas; calcular `mg/kg` y transformarlo a `mL` para que los padres lean *"Dar 4.5 mL cada 8 horas por 5 días"* ahorra tiempo al médico y evita errores de dosificación en casa.
- **Decisión 2: Respuesta Directa del Pediatra a las Dudas y Síntomas de Casa**
  - *Enfoque Elegido*: Cada duda o síntoma reportado por los padres puede ser marcado como *"Revisado en consulta"* o recibir una *"Nota/Respuesta del Pediatra"* que se refleja en la pantalla de los padres.
  - *Por qué*: Cierra el ciclo de comunicación clínica entre el hogar y el consultorio.

---

## 4. Arquitectura Técnica y Estrategia de Datos

### Diagrama de Arquitectura de Doble Escritorio

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                         MainActivity & MyApplicationTheme                        │
│              (Modo Claro / Oscuro + Auth Gate Google + PIN Médico)               │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                           ┌─────────────▼─────────────┐
                           │   Conmutador de Entorno   │
                           │ (Padres <──PIN──> Médico) │
                           └──────┬─────────────┬──────┘
                                  │             │
     ┌────────────────────────────▼──┐       ┌──▼──────────────────────────────────┐
     │     ESCRITORIO DE PADRES      │       │  ESTACIÓN CLÍNICA DEL PEDIATRA      │
     │   (Hogar · Lenguaje Simple)   │       │  (Consultorio · Uso Exclusivo)      │
     ├───────────────────────────────┤       ├─────────────────────────────────────┤
     │ 1. Inicio Familiar:           │       │ 1. Buscador de Pacientes, Código    │
     │    • Indicaciones activas     │       │    Familiar y Bandeja de Triaje     │
     │      del pediatra y citas     │       │    (Fiebres, Dudas y Estudios Casa) │
     │ 2. Registro en Casa:          │       │ 2. Consulta SOAP y Signos Vitales:  │
     │    • Termómetro y Síntomas    │       │    • FC, FR, Temp, SpO2 por edad    │
     │    • Diario Sueño y Comida    │       │    • Exploración física por sistemas│
     │ 3. Dudas y Estudios:          │       │    • Calculadora Ponderal mg/kg→mL  │
     │    • Preguntas para consulta  │       │      y Emisión de Receta Digital    │
     │      y respuestas del doctor  │       │ 3. Somatometría OMS y Vacunación:   │
     │    • Subir foto / estudio     │       │    • Peso, Talla, PC, IMC, Percentil│
     │ 4. Mis Recetas, Vacunas y     │       │    • Aplicar Vacuna Oficial + Lote  │
     │    Certificados (Solo Lectura)│       │ 4. Perinatal, Neurodesarrollo,      │
     │ 5. Orientación IA para Padres │       │    Laboratorios, Certificados e IA  │
     └───────────────┬───────────────┘       └──────────────────┬──────────────────┘
                     │                                          │
                     └────────────────────┬─────────────────────┘
                                          │
                            ┌─────────────▼─────────────┐
                            │   PequeSaludRepository    │
                            │   (Firestore + Gemini AI) │
                            └───────────────────────────┘
```

### Modelo de Datos y Sincronización Bidireccional

1. **Perfil Ampliado del Paciente (`ChildProfile`)**:
   - Incluye código de vinculación familiar derivado, datos antropométricos actuales (`weightKg`, `heightCm`) y resumen del último control pediátrico.
2. **Categorías de Registros en `HealthRecord` (diferenciadas por rol)**:
   - **Origen Padres**:
     - `SINTOMA_CASA`: Fiebre (°C), síntomas en casa y evolución.
     - `DIARIO_BEBE`: Tomas de leche/comida, horas de sueño, pañales y estado general.
     - `DUDA_PADRE`: Pregunta de los padres + respuesta del pediatra en el campo de notas/estado.
     - `ESTUDIO_PADRE`: Estudio o fotografía clínica subida por los padres.
   - **Origen Exclusivo Pediatra**:
     - `CONSULTA`: Signos vitales (FC, FR, Temp, SpO₂), exploración física por sistemas, diagnóstico CIE-10 y plan.
     - `MEDICAMENTO`: Prescripción calculada por peso (`mg/kg` → `mL`, frecuencia y días) visible en formato amigable para los padres.
     - `VACUNA`: Biológico oficial, número de lote, centro de aplicación y estado (*Aplicada* / *Pendiente*).
     - `CRECIMIENTO`: Peso, talla, perímetro cefálico, IMC y percentil OMS.
     - `PERINATAL`: SDG, APGAR, peso/talla al nacer y los 5 tamizajes neonatales.
     - `NEURODESARROLLO`: Evaluación de hitos motores, de lenguaje y sociales por edad.
     - `CERTIFICADO`: Certificados médicos escolares, constancias de vacunación e interconsultas firmadas con la cédula/exequátur del pediatra.
     - `HISTORIAL`, `LABORATORIO`, `HOSPITALIZACION`, `NUTRICION`, `CALENDARIO`: Antecedentes patológicos, órdenes de laboratorio, urgencias, plan nutricional y programación de citas.
