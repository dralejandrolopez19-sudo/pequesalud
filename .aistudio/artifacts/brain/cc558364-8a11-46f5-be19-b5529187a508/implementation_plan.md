# PequeSalud — Arquitectura Mixta (Android Nativa + PWA Instalable para GitHub Pages)

Este plan define la evolución de **PequeSalud** hacia una **arquitectura mixta multiplataforma**: conservando toda la potencia de la aplicación Android nativa e incorporando una **Progressive Web App (PWA) completa, instalable y lista para desplegarse directamente en GitHub Pages**, además de un centro de control y previsualización híbrida integrado dentro de la propia aplicación Android. Ambas plataformas comparten la misma base de datos en tiempo real de Firebase Firestore y cuentan con persistencia local offline.

---

## Revisión del Usuario y Decisiones Críticas

- **Estructura de Arquitectura Mixta Confirmada**: Se mantiene la **aplicación Android nativa** como núcleo móvil y se incorpora un paquete **PWA estático completo listo para GitHub Pages**, junto con un **Centro PWA & GitHub Pages** integrado en la app Android (con vista previa híbrida interactiva, estado de sincronización multiplataforma, guía de publicación en 1 clic y generador de enlace/código de acceso web).
- **Alcance Funcional Completo en la PWA**: La versión web instalable incluye **ambos escritorios completos** (**Escritorio de Padres** y **Estación Clínica del Pediatra**), replicando fielmente las curvas de crecimiento OMS (Peso, Talla y Perímetro Cefálico con percentiles P3, P15, P50, P85 y P97), el esquema de vacunación, recetas con alerta de alergias, diario del bebé, triaje de urgencias, evolución SOAP y el sistema de notificaciones/recordatorios entre pediatra y familia.
- **Sincronización Híbrida y Modo Offline**: Ambas aplicaciones (Android y PWA en GitHub Pages) se conectan al **mismo proyecto y colecciones de Firebase Firestore en tiempo real** bajo autenticación de Google, respaldadas por **almacenamiento offline local** (caché de Firestore + persistencia local y Service Worker en la PWA) para operar sin conexión y sincronizar cambios al recuperar red.

---

## 1. Visión General y Concepto Central

**PequeSalud** operará bajo un modelo **Dual Cloud-First & Offline-Ready**:

1. **Ecosistema Unificado Padres–Pediatra en Cualquier Dispositivo**:
   - Los padres o el pediatra pueden utilizar la app nativa en Android o abrir la PWA desde cualquier navegador (iOS, Android, PC o Mac alojada en GitHub Pages) e instalarla en su pantalla de inicio o escritorio como aplicación independiente (`standalone`).
2. **Paridad Clínica y Minimalismo Cálido**:
   - La PWA conserva el diseño editorial pediátrico de alta precisión y baja carga cognitiva: selector superior instantáneo entre **Modo Padres** y **Modo Pediatra**, selector de paciente activo con código de vinculación familiar (`PEQUE-XXX-000`), gráficas vectoriales interactivas de percentiles OMS y calculadora posológica por peso.
3. **Puente Nativo–Web Integrado en Android**:
   - Dentro de la app Android se añade una sección dedicada **Arquitectura Mixta & PWA GitHub Pages** que permite:
     - Ejecutar y probar la PWA en vivo dentro de un contenedor híbrido optimizado.
     - Verificar el estado del manifiesto web (`Web App Manifest`), el `Service Worker` offline y la conexión en tiempo real con Firebase.
     - Copiar la configuración y seguir los pasos exactos para activar GitHub Pages desde el repositorio exportado.

---

## 2. Experiencia de Usuario y Diseño Visual

### Estética y Sistema de Diseño Compartido
- **Paleta Clínica Minimalista**:
  - Superficies cálidas en crema marfil (`#FAF7F2`) y tarjetas blancas puras con bordes sutiles para reducir la fatiga visual en consulta y en casa.
  - Acentos diferenciados por rol: **Coral Cálido (`#E05A47`)** para el Escritorio de Padres y **Azul Clínico Profundo (`#1E5AA8`)** para la Estación del Pediatra.
  - Semáforo clínico OMS: Verde Esmeralda (`#2E7D32`) para canal óptimo (P15–P85), Ámbar (`#EF6C00`) para zona de vigilancia (P3–P15 / P85–P97) y Rojo Clínico (`#C62828`) para alerta (<P3 o >P97) y alergias medicamentosas.
- **Tipografía y Jerarquía**:
  - Encabezados claros, métricas antropométricas en tipografía monoespaciada/tabular para rápida lectura clínica (peso en `kg`, talla en `cm`, perímetro cefálico en `cm`, IMC y Z-score/percentil estimado).

### Flujo de Navegación en la PWA y App Nativa
1. **Cabecera Adaptativa Multiplataforma**:
   - Conmutador instantáneo **Escritorio de Padres / Estación del Pediatra**, selector de hijo/paciente activo, indicador de estado **Online (Firebase Realtime) / Offline (Local Cache)** y botón **"Instalar App (PWA)"** cuando se ejecuta en navegador.
2. **Escritorio de Padres (Web + Android)**:
   - Resumen de salud del niño, tarjeta de urgencias y alergias, gráfica simplificada de crecimiento OMS con explicación amigable, bandeja de notificaciones y recordatorios enviados por el pediatra, bitácora de síntomas en casa con semáforo de triaje, diario de lactancia/sueño, dudas para la próxima consulta y carnet de vacunas interactivo.
3. **Estación Clínica del Pediatra (Web + Android)**:
   - Ficha clínica integral, trazado vectorial interactivo de **Curvas OMS (Peso, Talla y Perímetro Cefálico)** con ajuste automático por sexo y rango etario (`0–24 meses`, `2–5 años`, `5–19 años`), evolución clínica estructurada SOAP, prescripción con calculadora mg/kg/dosis y detector automático de alergias cruzadas, hito de neurodesarrollo, certificados pediátricos imprimibles y emisor de alertas/recordatorios push para los padres.
4. **Hub PWA & GitHub Pages (en Android)**:
   - Panel interactivo accesible desde el menú principal y cabecera que muestra la arquitectura híbrida activa, permite abrir el **Visor PWA Integrado** y detalla la publicación directa en GitHub Pages.

---

## 3. Decisiones Clave de Producto y Compensaciones

- **PWA Sin Paso de Compilación Complejo para GitHub Pages**:
  - *Decisión*: Construir el motor de la PWA utilizando estándares web modernos nativos (ES Modules, SDK modular de Firebase vía CDN oficial, renderizado reactivo ligero y gráficos SVG/D3 para las curvas OMS) junto con `.nojekyll`, `manifest.webmanifest` y `sw.js`.
  - *Ventaja*: Al subir o sincronizar el proyecto con GitHub, GitHub Pages sirve la PWA de forma inmediata sin depender de pipelines externos que puedan fallar, y permite además empaquetar exactamente el mismo motor dentro de la app Android para su ejecución híbrida local.
- **Sincronización Dual (Firebase + Respaldo Local Instantáneo)**:
  - *Decisión*: Utilizar las mismas rutas de Firestore (`/users/{userId}/children/{childId}` y `/users/{userId}/records/{recordId}`) con persistencia offline habilitada tanto en el SDK nativo de Android como en el cliente web de la PWA, complementado con almacenamiento local (`IndexedDB` / `localStorage`) para modo demostración o uso sin red.
  - *Ventaja*: Cualquier vacuna, medición antropométrica, nota SOAP o recordatorio médico creado en la PWA aparece en tiempo real en la app Android y viceversa, sin perder datos si se corta la conexión.
- **Rutas Relativas en el Manifiesto y Service Worker**:
  - *Decisión*: Configurar todas las rutas del `manifest.webmanifest` y del `Service Worker` de forma relativa (`./`).
  - *Ventaja*: Garantiza que la PWA funcione e instale correctamente tanto en un dominio raíz como en subrutas de GitHub Pages (`https://<usuario>.github.io/<repositorio>/`).

---

## 4. Arquitectura Técnica y Estrategia de Datos

### Diagrama de Arquitectura Mixta (Android + PWA GitHub Pages + Firebase)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                   ECOSISTEMA MIXTO PEQUESALUD                           │
├───────────────────────────────────┬─────────────────────────────────────┤
│       APP ANDROID NATIVA          │     PWA INSTALABLE (GITHUB PAGES)   │
│  ┌─────────────────────────────┐  │  ┌───────────────────────────────┐  │
│  │ Jetpack Compose UI (M3)     │  │  │ Interfaz Web Adaptativa (PWA) │  │
│  │ • Escritorio de Padres      │  │  │ • Escritorio de Padres        │  │
│  │ • Estación del Pediatra     │  │  │ • Estación del Pediatra       │  │
│  │ • Gráficas OMS + Alertas    │  │  │ • Gráficas OMS SVG/D3 + Push  │  │
│  └──────────────┬──────────────┘  │  └───────────────┬───────────────┘  │
│                 │                 │                  │                  │
│  ┌──────────────▼──────────────┐  │  ┌───────────────▼───────────────┐  │
│  │ Hub Híbrido PWA Integrado   │◄─┼─►│ Service Worker (sw.js)        │  │
│  │ • Visor WebView PWA Local   │  │  │ • Caché Offline App Shell     │  │
│  │ • Diagnóstico GitHub Pages  │  │  │ • Web App Manifest Instalable │  │
│  └──────────────┬──────────────┘  │  └───────────────┬───────────────┘  │
└─────────────────┼─────────────────┴──────────────────┼──────────────────┘
                  │                                    │
                  ▼                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│            CAPA DE SINCRONIZACIÓN Y PERSISTENCIA COMPARTIDA             │
│  • Autenticación Google (Credential Manager en Android / Auth en Web)   │
│  • Firebase Firestore en Tiempo Real (/users/{uid}/children y /records) │
│  • Caché Offline Multiplataforma (Firestore Offline + Local Storage)    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Modelo de Datos y Estructura de Estado Compartida

Ambas plataformas leen y escriben exactamente las mismas entidades validadas por las reglas de seguridad de Firestore:

- **Perfil Pediátrico (`ChildProfile`)**:
  - Identificación: `id`, `userId`, `name`, `ageYears`, `birthDate`, `gender` (`Femenino` | `Masculino`), `avatarColorHex`.
  - Datos clínicos críticos: `bloodType`, `allergies`, `pediatrician`, `emergencyContact`, `weightKg`, `heightCm`, `lastCheckupDate`, `lastCheckupSummary`.
  - Código de enlace familiar determinista: `PEQUE-<PREFIJO>-<CODIGO>` para vinculación rápida entre consulta y hogar.
- **Registro Clínico y Familiar (`HealthRecord`)**:
  - Identificación y vínculo: `id`, `userId`, `childId`, `childName`.
  - Categorías soportadas en ambos escritorios:
    - Clínicas y compartidas: `VACUNA`, `CRECIMIENTO`, `MEDICAMENTO`, `CONSULTA`, `LABORATORIO`, `NUTRICION`, `HISTORIAL`, `CALENDARIO`, `DOCUMENTO`, `HOSPITALIZACION`.
    - Exclusivas o iniciadas en Escritorio de Padres: `SINTOMA_CASA`, `DIARIO_BEBE`, `DUDA_PADRE`, `ESTUDIO_PADRE`.
    - Exclusivas o iniciadas en Estación del Pediatra: `PERINATAL`, `NEURODESARROLLO`, `CERTIFICADO`, más recordatorios/alertas con prefijo de notificación médica para disparar avisos locales y banners en el Escritorio de Padres.

### Mapeo de Estado de Componentes Interactivos

| Componente / Módulo | Estado Gestionado | Comportamiento en Android y PWA |
| :--- | :--- | :--- |
| **Conmutador de Escritorio** | `workspaceMode` (`PARENT` / `PEDIATRICIAN`) | Cambia instantáneamente la paleta, las pestañas de navegación y las herramientas clínicas o familiares sin perder el paciente seleccionado. |
| **Motor de Curvas OMS** | `selectedMetric` (`WEIGHT`, `HEIGHT`, `HEAD`), `ageRange` automático, `childGender` | Calcula y dibuja las 5 curvas estándar de la OMS (P3, P15, P50, P85, P97) con bandas de alerta coloreadas y superpone la evolución histórica del paciente; muestra vista analítica completa para el pediatra y vista guiada para padres. |
| **Sistema de Alertas y Recordatorios** | `pendingAlerts`, `notificationPermission`, `unreadReminders` | Permite al pediatra emitir recordatorios de citas, vacunas o indicaciones urgentes; dispara notificaciones del sistema (Android NotificationManager / Web Notifications API) y alertas destacadas en el inicio de Padres. |
| **Prescripción Segura y Posología** | `drugName`, `doseMgKg`, `weightKg`, `allergyMatch` | Calcula automáticamente los miligramos y mililitros por toma según el peso actual del niño y bloquea/alerta visualmente si detecta alergia registrada (ej. Penicilina/Betalactámicos o AINEs). |
| **Gestor Offline & PWA Installer** | `networkStatus`, `installPromptEvent`, `syncQueue` | Intercepta el evento `beforeinstallprompt` en navegadores para instalar la PWA en 1 clic, mantiene la interfaz 100% operativa sin internet mediante el `Service Worker` y sincroniza los registros con Firestore al reconectar. |
| **Hub Híbrido PWA (Android)** | `pwaPreviewActive`, `deploymentChecklist` | Permite ejecutar la versión PWA dentro de la app Android, probar la sincronización cruzada y consultar la configuración lista para publicar en GitHub Pages. |
