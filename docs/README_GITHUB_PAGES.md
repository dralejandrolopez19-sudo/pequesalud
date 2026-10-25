# PequeSalud — Arquitectura Mixta (App Android Nativa + PWA para GitHub Pages)

Este repositorio implementa una **Arquitectura Mixta** que permite usar **PequeSalud** simultáneamente como:
1. **Aplicación Android Nativa (`app/`)**: Construida con Kotlin, Jetpack Compose, Material 3, Firebase Firestore, Google Sign-In y Visor Híbrido PWA integrado.
2. **Progressive Web App Instalable (`docs/` y `public/`)**: Lista para desplegar gratuitamente en **GitHub Pages** sin requerir pasos complejos de compilación Node.js, conectada a la **misma base de datos Firebase Firestore en tiempo real** y con **Modo Offline Local (`Service Worker` + `localStorage`)**.

---

## 🚀 Cómo publicar la PWA en GitHub Pages (En 30 segundos)

### Opción A: Despliegue directo desde la carpeta `/docs` (Recomendado, sin configuración extra)
1. Sube este proyecto a tu repositorio de GitHub (desde el menú **Export to GitHub** en AI Studio o mediante `git push`).
2. En tu repositorio de GitHub, entra en **Settings** ⚙️ → **Pages** (en la barra lateral izquierda).
3. En **Build and deployment** → **Source**, selecciona **Deploy from a branch**.
4. Elige la rama **`main`** (o `master`) y la carpeta **`/docs`** y haz clic en **Save**.
5. ¡Listo! En menos de 1 minuto tu PWA estará publicada en:
   `https://<tu-usuario>.github.io/<nombre-del-repositorio>/`

### Opción B: Despliegue automático con GitHub Actions
- El repositorio ya incluye el flujo `.github/workflows/deploy-pwa-pages.yml`.
- Si en **Settings → Pages → Source** eliges **GitHub Actions**, cada cambio en la rama principal publicará automáticamente la carpeta `/docs` en GitHub Pages.

---

## ✨ Módulos incluidos en la PWA de GitHub Pages
- **Doble Escritorio Completo**:
  - **Escritorio de Padres**: Resumen familiar, confirmación de alertas push del pediatra, curva de crecimiento OMS guiada, calculadora rápida de fiebre en casa, carnet de vacunación PAI interactivo y bitácora en casa (síntomas, diario del bebé y dudas para consulta).
  - **Estación Clínica del Pediatra (Protegido con PIN `1234`)**: Emisor de notificaciones push y recordatorios a padres, bandeja de triaje con respuesta médica directa, calculadora ponderal de recetas (`mg/kg/dosis → mL`) con bloqueo automático por alergias cruzadas, notas clínicas SOAP y curvas somatométricas OMS completas.
- **Curvas de Crecimiento OMS Interactivas (`P3`, `P15`, `P50`, `P85`, `P97`)**:
  - Peso (`kg`), Talla (`cm`) y Perímetro Cefálico (`cm`) con zonas de alerta coloreadas y ajuste automático por edad (`0-24m`, `2-5a`, `5-19a`) y sexo biológico.
- **Sincronización en Tiempo Real + Modo Offline**:
  - Conectada al proyecto Firebase (`gen-lang-client-0132354751`) y a la base Firestore de PequeSalud, más persistencia local inmediata y caché offline mediante `sw.js`.
  - Cuando decides iniciar sesión con Google en GitHub Pages, recuerda autorizar tu dominio `<tu-usuario>.github.io` en **Firebase Console → Authentication → Settings → Authorized domains**.
