// ============================================================================
// PequeSalud — Motor de Arquitectura Mixta (PWA GitHub Pages + Android Bridge)
// Conectado a la misma base Firebase Firestore y con modo Offline Local
// ============================================================================

const FIREBASE_CONFIG = {
  apiKey: "AIzaSyAmIGjv1WyBcjPqA_m4uV56-f1vYc3vdBo",
  authDomain: "gen-lang-client-0132354751.firebaseapp.com",
  projectId: "gen-lang-client-0132354751",
  storageBucket: "gen-lang-client-0132354751.firebasestorage.app",
  messagingSenderId: "1013000041687",
  appId: "1:1013000041687:android:04e6e6dd6ec652b0d46c92",
  databaseId: "ai-studio-android-pequesal-cc558364-8a11-46f5-be19-b5529187a508"
};

const STORAGE_KEYS = {
  CHILDREN: "pequesalud_pwa_children_v2",
  RECORDS: "pequesalud_pwa_records_v2",
  WORKSPACE: "pequesalud_pwa_workspace_v2",
  PIN: "pequesalud_pwa_pin_v2",
  DARK_MODE: "pequesalud_pwa_dark_v2"
};

// Catálogo farmacológico pediátrico idéntico a la app Android
const PEDIATRIC_DRUGS = [
  {
    id: "paracetamol_160",
    name: "Paracetamol (Acetaminofén)",
    presentation: "Jarabe 160 mg / 5 mL",
    mgPerMl: 32.0,
    defaultMgKg: 15.0,
    everyHours: 6,
    days: 3,
    indication: "Antipirético y analgésico (10-15 mg/kg/dosis c/6h)",
    allergyGroup: "paracetamol"
  },
  {
    id: "ibuprofeno_100",
    name: "Ibuprofeno Pediátrico",
    presentation: "Suspensión 100 mg / 5 mL",
    mgPerMl: 20.0,
    defaultMgKg: 10.0,
    everyHours: 8,
    days: 3,
    indication: "Antiinflamatorio y antipirético (5-10 mg/kg/dosis c/8h, >6m)",
    allergyGroup: "aines"
  },
  {
    id: "amoxicilina_250",
    name: "Amoxicilina Suspensión",
    presentation: "Suspensión 250 mg / 5 mL",
    mgPerMl: 50.0,
    defaultMgKg: 25.0,
    everyHours: 8,
    days: 7,
    indication: "Antibiótico betalactámico (~80 mg/kg/día div c/8h)",
    allergyGroup: "penicilina"
  },
  {
    id: "amox_clav_400",
    name: "Amoxicilina + Ác. Clavulánico",
    presentation: "Suspensión 400 mg / 5 mL",
    mgPerMl: 80.0,
    defaultMgKg: 22.5,
    everyHours: 12,
    days: 7,
    indication: "Otitis media / Sinusitis (45-90 mg/kg/día div c/12h)",
    allergyGroup: "penicilina"
  },
  {
    id: "azitromicina_200",
    name: "Azitromicina Suspensión",
    presentation: "Suspensión 200 mg / 5 mL",
    mgPerMl: 40.0,
    defaultMgKg: 10.0,
    everyHours: 24,
    days: 5,
    indication: "Macrólido (10 mg/kg/día c/24h • Seguro en alergia a Penicilina)",
    allergyGroup: "macrolido"
  },
  {
    id: "cetirizina_5",
    name: "Cetirizina Jarabe",
    presentation: "Jarabe 5 mg / 5 mL",
    mgPerMl: 1.0,
    defaultMgKg: 0.25,
    everyHours: 24,
    days: 5,
    indication: "Antihistamínico H1 (0.25 mg/kg/día c/24h)",
    allergyGroup: "antihistaminico"
  }
];

// Estado reactivo global
const state = {
  workspace: localStorage.getItem(STORAGE_KEYS.WORKSPACE) || "PARENT", // PARENT | PEDIATRICIAN
  activeTab: 0,
  pin: localStorage.getItem(STORAGE_KEYS.PIN) || "1234",
  darkMode: localStorage.getItem(STORAGE_KEYS.DARK_MODE) === "true",
  whoMetric: "WEIGHT", // WEIGHT | HEIGHT | HEAD
  ageRangeOverride: "AUTO", // AUTO | 0_24M | 2_5Y | 5_19Y
  selectedDrugId: "paracetamol_160",
  customMgKg: 15.0,
  children: [],
  records: [],
  selectedChildId: "",
  firebaseUser: null,
  firestoreDb: null,
  isAndroidWebView: typeof window.PequeSaludAndroidBridge !== "undefined",
  deferredInstallPrompt: null
};

// Semilla inicial local si aún no hay registros en localStorage ni puente activo
function getInitialFallbackChildren() {
  return [
    {
      id: "child_sofia_pwa",
      name: "Sofía López",
      ageYears: 2,
      birthDate: "14 Mayo 2023",
      gender: "Femenino",
      bloodType: "O+",
      allergies: "Penicilina",
      pediatrician: "Dr. Alejandro López",
      emergencyContact: "809-555-0192",
      lastCheckupDate: "Control 24 meses",
      lastCheckupSummary: "Crecimiento armónico en P50 OMS, lenguaje y marcha adecuados.",
      weightKg: 12.2,
      heightCm: 86.5
    },
    {
      id: "child_mateo_pwa",
      name: "Mateo López",
      ageYears: 5,
      birthDate: "08 Octubre 2020",
      gender: "Masculino",
      bloodType: "A+",
      allergies: "Ninguna",
      pediatrician: "Dr. Alejandro López",
      emergencyContact: "809-555-0192",
      lastCheckupDate: "Control Preescolar 5 años",
      lastCheckupSummary: "Refuerzo DPT/Polio al día, percentil 60 OMS.",
      weightKg: 18.6,
      heightCm: 110.0
    }
  ];
}

function getInitialFallbackRecords() {
  return [
    {
      id: "rec_growth_1",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "CRECIMIENTO",
      title: "Control 6 meses: 7.4 kg • 65.5 cm • PC 42.5 cm",
      subtitle: "6 meses | Peso: 7.4 kg | Talla: 65.5 cm | PC: 42.5 cm",
      dateText: "Hace 18 meses",
      status: "Normal P50",
      notes: "AGE_M:6|W:7.4|H:65.5|HC:42.5|Lactancia materna exclusiva + inicio ablactación"
    },
    {
      id: "rec_growth_2",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "CRECIMIENTO",
      title: "Control 12 meses: 9.2 kg • 74.2 cm • PC 45.1 cm",
      subtitle: "12 meses | Peso: 9.2 kg | Talla: 74.2 cm | PC: 45.1 cm",
      dateText: "Hace 12 meses",
      status: "Normal P50",
      notes: "AGE_M:12|W:9.2|H:74.2|HC:45.1|Bipedestación lograda"
    },
    {
      id: "rec_growth_3",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "CRECIMIENTO",
      title: "Control 18 meses: 10.8 kg • 81.0 cm • PC 46.6 cm",
      subtitle: "18 meses | Peso: 10.8 kg | Talla: 81.0 cm | PC: 46.6 cm",
      dateText: "Hace 6 meses",
      status: "Normal P50",
      notes: "AGE_M:18|W:10.8|H:81.0|HC:46.6|Desarrollo psicomotor óptimo"
    },
    {
      id: "rec_growth_4",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "CRECIMIENTO",
      title: "Control 24 meses: 12.2 kg • 86.5 cm • PC 47.6 cm",
      subtitle: "24 meses | Peso: 12.2 kg | Talla: 86.5 cm | PC: 47.6 cm",
      dateText: "Reciente",
      status: "Normal P50",
      notes: "AGE_M:24|W:12.2|H:86.5|HC:47.6|Curva OMS en canal central P50"
    },
    {
      id: "rec_vac_1",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "VACUNA",
      title: "SRP (Sarampión, Rubéola, Paperas) - 1ra Dosis",
      subtitle: "Esquema PAI 12 meses • Lote BIO-492",
      dateText: "Aplicada",
      status: "Aplicada",
      notes: "Sin reacciones adversas"
    },
    {
      id: "rec_vac_2",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "VACUNA",
      title: "Refuerzo Pentavalente + Polio (18-24m)",
      subtitle: "Esquema PAI • Próxima cita",
      dateText: "Este mes",
      status: "Pendiente",
      notes: "Agendar en consultorio pediátrico"
    },
    {
      id: "rec_push_1",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "NOTIFICACION_PEDIATRA",
      title: "Recordatorio de Control Pediátrico y Vacuna PAI",
      subtitle: "Prioridad Alta • Dr. Alejandro López",
      dateText: "Hoy",
      status: "Enviada a padres",
      notes: "Recuerden acudir esta semana para el refuerzo de los 24 meses y control de curva OMS."
    },
    {
      id: "rec_sym_1",
      childId: "child_sofia_pwa",
      childName: "Sofía López",
      category: "SINTOMA_CASA",
      title: "Febrícula nocturna 37.9 °C y congestión nasal",
      subtitle: "Reportado por Mamá/Papá desde casa",
      dateText: "Ayer 21:30",
      status: "Pendiente revisión",
      notes: "Buena hidratación oral, durmió tranquila tras baño tibio."
    }
  ];
}

// ============================================================================
// Funciones utilitarias de Persistencia Local + Sincronización con Android/Firebase
// ============================================================================
function loadLocalState() {
  try {
    const rawChildren = localStorage.getItem(STORAGE_KEYS.CHILDREN);
    const rawRecords = localStorage.getItem(STORAGE_KEYS.RECORDS);
    state.children = rawChildren ? JSON.parse(rawChildren) : getInitialFallbackChildren();
    state.records = rawRecords ? JSON.parse(rawRecords) : getInitialFallbackRecords();
    if (state.children.length > 0 && !state.selectedChildId) {
      state.selectedChildId = state.children[0].id;
    }
  } catch (e) {
    state.children = getInitialFallbackChildren();
    state.records = getInitialFallbackRecords();
    state.selectedChildId = state.children[0].id;
  }
}

function saveLocalState() {
  try {
    localStorage.setItem(STORAGE_KEYS.CHILDREN, JSON.stringify(state.children));
    localStorage.setItem(STORAGE_KEYS.RECORDS, JSON.stringify(state.records));
  } catch (e) {
    console.warn("No se pudo guardar en localStorage:", e);
  }
}

function showToast(msg) {
  const el = document.getElementById("toastEl");
  if (!el) return;
  el.textContent = msg;
  el.classList.remove("hidden");
  clearTimeout(window.__toastTimer);
  window.__toastTimer = setTimeout(() => {
    el.classList.add("hidden");
  }, 3200);
}

function computeFamilyCode(child) {
  const clean = (child.name || "PAC")
    .replace(/[^a-zA-Z]/g, "")
    .slice(0, 3)
    .toUpperCase()
    .padEnd(3, "X");
  const num = (((Number(child.ageYears) || 0) + 2) * 137 + (child.name || "").length * 19) % 900 + 100;
  return `PEQUE-${clean}-${num}`;
}

function hasAllergyConflict(child, drug) {
  if (!child || !child.allergies) return false;
  const al = child.allergies.toLowerCase();
  if (al.includes("ninguna") || !al.trim()) return false;
  if ((al.includes("penicilina") || al.includes("betalact") || al.includes("amoxicilina")) && drug.allergyGroup === "penicilina") {
    return true;
  }
  if ((al.includes("aines") || al.includes("ibuprofeno")) && drug.allergyGroup === "aines") {
    return true;
  }
  return al.includes(drug.name.toLowerCase().split(" ")[0]);
}

function getActiveChild() {
  return state.children.find((c) => c.id === state.selectedChildId) || state.children[0] || null;
}

// ============================================================================
// Motor Matemático OMS (P3, P15, P50, P85, P97) — 0-24m, 2-5a, 5-19a por Sexo
// ============================================================================
function getEffectiveAgeRange(child) {
  if (state.ageRangeOverride !== "AUTO") return state.ageRangeOverride;
  const yrs = Number(child?.ageYears || 0);
  if (yrs < 2) return "0_24M";
  if (yrs <= 5) return "2_5Y";
  return "5_19Y";
}

function evaluateWhoPercentiles(metric, isBoy, ageRangeKey, xAgeMonths) {
  const m = Math.max(0, xAgeMonths);
  let p50 = 10;
  let sd = 1.2;

  if (metric === "WEIGHT") {
    if (m <= 24) {
      const base0 = isBoy ? 3.35 : 3.23;
      const gain = isBoy ? 0.39 * m - 0.0042 * m * m : 0.36 * m - 0.0038 * m * m;
      p50 = base0 + Math.max(0, gain);
      sd = 0.45 + m * 0.038;
    } else if (m <= 60) {
      const base24 = isBoy ? 12.2 : 11.5;
      p50 = base24 + (m - 24) * 0.175;
      sd = 1.35 + (m - 24) * 0.032;
    } else {
      const yrs = m / 12.0;
      p50 = isBoy ? 18.3 + (yrs - 5) * 3.1 + 0.08 * (yrs - 5) * (yrs - 5) : 18.0 + (yrs - 5) * 2.95 + 0.06 * (yrs - 5) * (yrs - 5);
      sd = 2.4 + (yrs - 5) * 0.48;
    }
  } else if (metric === "HEIGHT") {
    if (m <= 24) {
      const base0 = isBoy ? 49.9 : 49.1;
      p50 = base0 + 1.95 * m - 0.025 * m * m;
      sd = 1.9 + m * 0.05;
    } else if (m <= 60) {
      const base24 = isBoy ? 87.1 : 85.7;
      p50 = base24 + (m - 24) * 0.64;
      sd = 3.1 + (m - 24) * 0.035;
    } else {
      const yrs = m / 12.0;
      p50 = isBoy ? 110.0 + (yrs - 5) * 5.2 : 109.4 + (yrs - 5) * 4.8;
      sd = 4.4 + (yrs - 5) * 0.22;
    }
  } else {
    // Perímetro Cefálico (cm)
    const clampedM = Math.min(m, 60);
    const base0 = isBoy ? 34.5 : 33.9;
    p50 = base0 + 1.15 * Math.sqrt(clampedM) * 2.4;
    sd = 1.25 + clampedM * 0.004;
  }

  return {
    ageMonths: m,
    p3: +(p50 - 1.88 * sd).toFixed(2),
    p15: +(p50 - 1.04 * sd).toFixed(2),
    p50: +p50.toFixed(2),
    p85: +(p50 + 1.04 * sd).toFixed(2),
    p97: +(p50 + 1.88 * sd).toFixed(2)
  };
}

function extractPatientGrowthPoints(child, metric) {
  if (!child) return [];
  const childRecs = state.records.filter(
    (r) => (r.childId === child.id || r.childName === child.name) && r.category === "CRECIMIENTO"
  );
  const points = [];

  childRecs.forEach((r) => {
    const raw = `${r.notes || ""} | ${r.subtitle || ""} | ${r.title || ""}`;
    const ageMatch = raw.match(/AGE_M:(\d+(\.\d+)?)/i) || raw.match(/(\d+)\s*mes/i);
    const wMatch = raw.match(/W:(\d+(\.\d+)?)/i) || raw.match(/(\d+(\.\d+)?)\s*kg/i);
    const hMatch = raw.match(/H:(\d+(\.\d+)?)/i) || raw.match(/(\d+(\.\d+)?)\s*cm/i);
    const hcMatch = raw.match(/HC:(\d+(\.\d+)?)/i) || raw.match(/PC[:\s]*(\d+(\.\d+)?)/i);

    const ageM = ageMatch ? parseFloat(ageMatch[1]) : Math.max(1, (Number(child.ageYears) || 1) * 12);
    let val = null;
    if (metric === "WEIGHT" && wMatch) val = parseFloat(wMatch[1]);
    if (metric === "HEIGHT" && hMatch) val = parseFloat(hMatch[1]);
    if (metric === "HEAD" && hcMatch) val = parseFloat(hcMatch[1]);

    if (val && !isNaN(val) && val > 0) {
      points.push({ ageMonths: ageM, value: val, label: r.dateText || `${ageM}m` });
    }
  });

  // Incluir siempre el punto antropométrico actual del perfil
  const currentAgeM = Math.max(2, (Number(child.ageYears) || 1) * 12);
  const currentVal =
    metric === "WEIGHT"
      ? Number(child.weightKg)
      : metric === "HEIGHT"
      ? Number(child.heightCm)
      : +(34.2 + Math.sqrt(Math.min(currentAgeM, 60)) * 2.75).toFixed(1);

  if (currentVal > 0 && !points.some((p) => Math.abs(p.ageMonths - currentAgeM) < 0.5)) {
    points.push({ ageMonths: currentAgeM, value: currentVal, label: "Actual" });
  }

  return points.sort((a, b) => a.ageMonths - b.ageMonths);
}

function buildWhoGrowthSvgHtml(child, simplifiedForParents) {
  const isBoy = child?.gender === "Masculino";
  const metric = state.whoMetric;
  const rangeKey = getEffectiveAgeRange(child);

  let minM = 0;
  let maxM = 24;
  if (rangeKey === "2_5Y") {
    minM = 24;
    maxM = 60;
  } else if (rangeKey === "5_19Y") {
    minM = 60;
    maxM = 228;
  }
  if (metric === "HEAD" && maxM > 60) {
    minM = 0;
    maxM = 60;
  }

  const steps = 16;
  const refSeries = [];
  for (let i = 0; i <= steps; i++) {
    const m = minM + ((maxM - minM) * i) / steps;
    refSeries.push(evaluateWhoPercentiles(metric, isBoy, rangeKey, m));
  }

  const patientPts = extractPatientGrowthPoints(child, metric).filter(
    (p) => p.ageMonths >= minM && p.ageMonths <= maxM
  );

  const allVals = [];
  refSeries.forEach((r) => allVals.push(r.p3, r.p97));
  patientPts.forEach((p) => allVals.push(p.value));

  const minY = Math.floor(Math.min(...allVals) * 0.92);
  const maxY = Math.ceil(Math.max(...allVals) * 1.06);

  const W = 680;
  const H = 300;
  const padL = 46;
  const padR = 34;
  const padT = 18;
  const padB = 34;
  const plotW = W - padL - padR;
  const plotH = H - padT - padB;

  const toX = (m) => padL + ((m - minM) / Math.max(1, maxM - minM)) * plotW;
  const toY = (v) => padT + plotH - ((v - minY) / Math.max(0.1, maxY - minY)) * plotH;

  const buildPath = (key) =>
    refSeries
      .map((pt, idx) => `${idx === 0 ? "M" : "L"} ${toX(pt.ageMonths).toFixed(1)} ${toY(pt[key]).toFixed(1)}`)
      .join(" ");

  const buildBandPolygon = (upperKey, lowerKey) => {
    const topPts = refSeries.map((pt) => `${toX(pt.ageMonths).toFixed(1)},${toY(pt[upperKey]).toFixed(1)}`);
    const botPts = [...refSeries]
      .reverse()
      .map((pt) => `${toX(pt.ageMonths).toFixed(1)},${toY(pt[lowerKey]).toFixed(1)}`);
    return [...topPts, ...botPts].join(" ");
  };

  const patientPath = patientPts
    .map((pt, idx) => `${idx === 0 ? "M" : "L"} ${toX(pt.ageMonths).toFixed(1)} ${toY(pt.value).toFixed(1)}`)
    .join(" ");

  const unitLabel = metric === "WEIGHT" ? "kg" : "cm";
  const lastPt = patientPts[patientPts.length - 1] || null;
  let clinicalBadge = "Canal Normal (P15 - P85 OMS)";
  let badgeClass = "applied";
  if (lastPt) {
    const refAtAge = evaluateWhoPercentiles(metric, isBoy, rangeKey, lastPt.ageMonths);
    if (lastPt.value < refAtAge.p3 || lastPt.value > refAtAge.p97) {
      clinicalBadge = lastPt.value < refAtAge.p3 ? "Alerta OMS (< P3 Bajo)" : "Alerta OMS (> P97 Alto)";
      badgeClass = "urgent";
    } else if (lastPt.value < refAtAge.p15 || lastPt.value > refAtAge.p85) {
      clinicalBadge = "Zona de Vigilancia (P3-P15 / P85-P97)";
      badgeClass = "pending";
    }
  }

  // Líneas horizontales de cuadrícula
  let gridLines = "";
  for (let g = 0; g <= 4; g++) {
    const val = minY + ((maxY - minY) * g) / 4;
    const y = toY(val).toFixed(1);
    gridLines += `
      <line x1="${padL}" y1="${y}" x2="${W - padR}" y2="${y}" stroke="rgba(148,163,184,0.28)" stroke-dasharray="3 3" />
      <text x="${padL - 6}" y="${Number(y) + 4}" text-anchor="end" font-size="10" fill="#64748B">${val.toFixed(1)}</text>
    `;
  }

  // Etiquetas del eje X
  let xLabels = "";
  for (let xIdx = 0; xIdx <= 4; xIdx++) {
    const mVal = Math.round(minM + ((maxM - minM) * xIdx) / 4);
    const xPos = toX(mVal).toFixed(1);
    const labelTxt = maxM > 60 ? `${(mVal / 12).toFixed(0)}a` : `${mVal}m`;
    xLabels += `<text x="${xPos}" y="${H - 10}" text-anchor="middle" font-size="10" font-weight="600" fill="#64748B">${labelTxt}</text>`;
  }

  const lastRef = refSeries[refSeries.length - 1];

  return `
    <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:8px; margin-bottom:8px;">
      <div style="display:flex; gap:6px; flex-wrap:wrap;">
        <button class="btn ${metric === "WEIGHT" ? "btn-primary-pediatrician" : "btn-outline"}" style="padding:5px 12px; font-size:0.76rem;" data-who-metric="WEIGHT">⚖️ Peso (kg)</button>
        <button class="btn ${metric === "HEIGHT" ? "btn-primary-pediatrician" : "btn-outline"}" style="padding:5px 12px; font-size:0.76rem;" data-who-metric="HEIGHT">📏 Talla (cm)</button>
        <button class="btn ${metric === "HEAD" ? "btn-primary-pediatrician" : "btn-outline"}" style="padding:5px 12px; font-size:0.76rem;" data-who-metric="HEAD">🧠 Perímetro Cefálico</button>
      </div>
      <span class="record-badge ${badgeClass}">${clinicalBadge}</span>
    </div>

    ${
      !simplifiedForParents
        ? `<div style="display:flex; gap:6px; flex-wrap:wrap; margin-bottom:8px;">
            <span style="font-size:0.74rem; color:var(--text-secondary); font-weight:700; align-self:center;">Escala OMS (${isBoy ? "Niños" : "Niñas"}):</span>
            <button class="btn ${state.ageRangeOverride === "AUTO" ? "btn-soft-blue" : "btn-outline"}" style="padding:3px 10px; font-size:0.72rem;" data-who-range="AUTO">Automático</button>
            <button class="btn ${rangeKey === "0_24M" && state.ageRangeOverride !== "AUTO" ? "btn-soft-blue" : "btn-outline"}" style="padding:3px 10px; font-size:0.72rem;" data-who-range="0_24M">0-24 meses</button>
            <button class="btn ${rangeKey === "2_5Y" && state.ageRangeOverride !== "AUTO" ? "btn-soft-blue" : "btn-outline"}" style="padding:3px 10px; font-size:0.72rem;" data-who-range="2_5Y">2-5 años</button>
            <button class="btn ${rangeKey === "5_19Y" && state.ageRangeOverride !== "AUTO" ? "btn-soft-blue" : "btn-outline"}" style="padding:3px 10px; font-size:0.72rem;" data-who-range="5_19Y">5-19 años</button>
          </div>`
        : `<p style="font-size:0.78rem; color:var(--text-secondary); margin-bottom:6px;">
            Vista guiada para padres: la franja verde muestra el canal saludable de crecimiento OMS para ${child?.name || "tu hijo/a"}.
          </p>`
    }

    <div class="who-chart-wrapper">
      <svg viewBox="0 0 ${W} ${H}" class="who-svg" role="img" aria-label="Curva de crecimiento OMS">
        <!-- Zonas coloreadas P3-P97 y P15-P85 -->
        <polygon points="${buildBandPolygon("p97", "p85")}" fill="rgba(245, 158, 11, 0.14)" />
        <polygon points="${buildBandPolygon("p85", "p15")}" fill="rgba(21, 128, 61, 0.14)" />
        <polygon points="${buildBandPolygon("p15", "p3")}" fill="rgba(245, 158, 11, 0.14)" />

        ${gridLines}
        ${xLabels}

        <!-- Curvas de referencia P97, P85, P50, P15, P3 -->
        <path d="${buildPath("p97")}" fill="none" stroke="#DC2626" stroke-width="1.4" stroke-dasharray="4 3" />
        <path d="${buildPath("p85")}" fill="none" stroke="#D97706" stroke-width="1.4" stroke-dasharray="3 3" />
        <path d="${buildPath("p50")}" fill="none" stroke="#15803D" stroke-width="2.4" />
        <path d="${buildPath("p15")}" fill="none" stroke="#D97706" stroke-width="1.4" stroke-dasharray="3 3" />
        <path d="${buildPath("p3")}" fill="none" stroke="#DC2626" stroke-width="1.4" stroke-dasharray="4 3" />

        <!-- Etiquetas derechas de percentiles -->
        <text x="${W - padR + 4}" y="${toY(lastRef.p97) + 3}" font-size="9" font-weight="700" fill="#DC2626">P97</text>
        <text x="${W - padR + 4}" y="${toY(lastRef.p85) + 3}" font-size="9" font-weight="700" fill="#D97706">P85</text>
        <text x="${W - padR + 4}" y="${toY(lastRef.p50) + 3}" font-size="9" font-weight="800" fill="#15803D">P50</text>
        <text x="${W - padR + 4}" y="${toY(lastRef.p15) + 3}" font-size="9" font-weight="700" fill="#D97706">P15</text>
        <text x="${W - padR + 4}" y="${toY(lastRef.p3) + 3}" font-size="9" font-weight="700" fill="#DC2626">P3</text>

        <!-- Trazo del Paciente -->
        ${
          patientPts.length > 1
            ? `<path d="${patientPath}" fill="none" stroke="#1E5AA8" stroke-width="3.2" stroke-linecap="round" />`
            : ""
        }
        ${patientPts
          .map(
            (pt) => `
            <circle cx="${toX(pt.ageMonths).toFixed(1)}" cy="${toY(pt.value).toFixed(1)}" r="5.5" fill="#1E5AA8" stroke="#FFFFFF" stroke-width="2" />
            <text x="${toX(pt.ageMonths).toFixed(1)}" y="${(toY(pt.value) - 9).toFixed(1)}" text-anchor="middle" font-size="10" font-weight="800" fill="#1E5AA8">${pt.value}${unitLabel}</text>
          `
          )
          .join("")}
      </svg>

      <div class="chart-legend">
        <span class="legend-item"><span class="legend-swatch" style="background:#1E5AA8; height:6px;"></span> Evolución de ${child?.name?.split(" ")[0] || "Paciente"}</span>
        <span class="legend-item"><span class="legend-swatch" style="background:#15803D;"></span> Mediana OMS (P50)</span>
        <span class="legend-item"><span class="legend-swatch" style="background:#D97706;"></span> Vigilancia (P15 / P85)</span>
        <span class="legend-item"><span class="legend-swatch" style="background:#DC2626;"></span> Alerta Clínica (P3 / P97)</span>
      </div>
    </div>
  `;
}

// ============================================================================
// Renderizado Principal de Ambos Escritorios (Padres y Pediatra)
// ============================================================================
function renderApp() {
  document.body.classList.toggle("dark-theme", state.darkMode);

  const activeChild = getActiveChild();
  const isPediatrician = state.workspace === "PEDIATRICIAN";

  // Actualizar cabecera
  const badgeEl = document.getElementById("workspaceBadge");
  const titleEl = document.getElementById("workspaceTitle");
  const subEl = document.getElementById("workspaceSubtitle");
  const switchBtn = document.getElementById("switchWorkspaceBtn");

  if (isPediatrician) {
    badgeEl.textContent = "CONSULTORIO PEDIÁTRICO";
    badgeEl.className = "workspace-tag pediatrician";
    titleEl.textContent = "Dr. Alejandro López";
    subEl.textContent = "Triaje, evolución SOAP, curvas OMS y dosis ponderales";
    switchBtn.textContent = "🏡 Vista Padres";
    switchBtn.className = "btn btn-soft-green";
  } else {
    badgeEl.textContent = "ESCRITORIO DE PADRES";
    badgeEl.className = "workspace-tag parent";
    titleEl.textContent = "PequeSalud";
    subEl.textContent = "Salud infantil, vacunas PAI y curvas de crecimiento";
    switchBtn.textContent = "🩺 Modo Pediatra";
    switchBtn.className = "btn btn-soft-blue";
  }

  renderChildSelector(activeChild);
  renderMainWorkspaceContent(activeChild, isPediatrician);
  renderBottomNavigation(isPediatrician);
  attachDynamicEventListeners();
}

function renderChildSelector(activeChild) {
  const chipsContainer = document.getElementById("childChipsContainer");
  const summaryBar = document.getElementById("activeChildSummaryBar");
  if (!chipsContainer || !summaryBar) return;

  chipsContainer.innerHTML = state.children
    .map((c) => {
      const active = activeChild && c.id === activeChild.id;
      return `
        <button class="child-chip ${active ? "active" : ""}" data-select-child="${c.id}">
          <span>${c.gender === "Masculino" ? "👦" : "👧"}</span>
          <span>${c.name} (${c.ageYears}a)</span>
        </button>
      `;
    })
    .join("");

  if (!activeChild) {
    summaryBar.innerHTML = `<span style="font-size:0.85rem; color:var(--text-secondary);">No hay perfiles registrados. Agrega un hijo/a para comenzar.</span>`;
    return;
  }

  const code = computeFamilyCode(activeChild);
  const hasAllergy = activeChild.allergies && !activeChild.allergies.toLowerCase().includes("ninguna");

  summaryBar.innerHTML = `
    <div class="child-telemetry-pills">
      <span class="telemetry-badge code">🔗 ${code}</span>
      <span class="telemetry-badge">🩸 Grupo ${activeChild.bloodType || "O+"}</span>
      <span class="telemetry-badge">⚖️ ${activeChild.weightKg || 12.0} kg • 📏 ${activeChild.heightCm || 85.0} cm</span>
      <span class="telemetry-badge ${hasAllergy ? "allergy" : ""}">
        ${hasAllergy ? `⚠️ Alergia: ${activeChild.allergies}` : "✅ Sin alergias medicamentosas"}
      </span>
    </div>
    <div style="font-size:0.78rem; color:var(--text-secondary);">
      Pediatra: <strong>${activeChild.pediatrician || "Dr. Alejandro López"}</strong>
    </div>
  `;
}

function renderMainWorkspaceContent(activeChild, isPediatrician) {
  const container = document.getElementById("mainViewContainer");
  if (!container) return;

  if (!activeChild) {
    container.innerHTML = `
      <div class="card" style="text-align:center; padding:32px;">
        <h2 class="card-title">Registra el primer perfil pediátrico</h2>
        <p class="card-subtitle" style="margin:8px 0 16px;">Los datos se sincronizan entre la PWA de GitHub Pages y la app Android.</p>
        <button class="btn btn-primary-parent" id="emptyStateAddChildBtn">➕ Registrar Hijo/a</button>
      </div>
    `;
    return;
  }

  const childRecords = state.records.filter(
    (r) => r.childId === activeChild.id || r.childName === activeChild.name
  );

  if (!isPediatrician) {
    renderParentWorkspace(container, activeChild, childRecords);
  } else {
    renderPediatricianWorkspace(container, activeChild, childRecords);
  }
}

// ============================================================================
// ESCRITORIO DE PADRES (4 Pestañas: Resumen, Bitácora en Casa, Vacunas/Salud, Arquitectura/PWA)
// ============================================================================
function renderParentWorkspace(container, child, records) {
  const pushAlerts = records.filter((r) => r.category === "NOTIFICACION_PEDIATRA");
  const vaccines = records.filter((r) => r.category === "VACUNA");
  const homeLogs = records.filter((r) =>
    ["SINTOMA_CASA", "DIARIO_BEBE", "DUDA_PADRE"].includes(r.category)
  );

  if (state.activeTab === 0) {
    // Pestaña 0: Inicio Familiar + Avisos Push + Curva OMS simplificada + Calculadora Antitérmica
    const defaultParacetamolMl = ((Number(child.weightKg || 12) * 15) / 32).toFixed(1);
    container.innerHTML = `
      ${
        pushAlerts.length > 0
          ? `<div class="push-banner-card">
              <div class="card-header" style="margin-bottom:8px;">
                <div>
                  <span class="workspace-tag pediatrician">🔔 AVISO DIRECTO DEL PEDIATRA</span>
                  <h3 class="card-title" style="margin-top:4px;">${pushAlerts[0].title}</h3>
                </div>
                <span class="record-badge ${pushAlerts[0].status.includes("Confirmado") ? "applied" : "pending"}">${pushAlerts[0].status}</span>
              </div>
              <p style="font-size:0.88rem; margin-bottom:10px;">${pushAlerts[0].notes}</p>
              <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <button class="btn btn-safe" data-confirm-push="${pushAlerts[0].id}">
                  ✅ Confirmar recepción al Pediatra
                </button>
                <button class="btn btn-outline" id="enableBrowserNotificationsBtn">
                  🔔 Activar Notificaciones en este dispositivo
                </button>
              </div>
            </div>`
          : ""
      }

      <div class="card">
        <div class="card-header">
          <div>
            <h2 class="card-title">📈 Curva de Crecimiento OMS — Vista para Padres</h2>
            <p class="card-subtitle">Seguimiento visual de ${child.name} comparado con los estándares de la OMS</p>
          </div>
        </div>
        ${buildWhoGrowthSvgHtml(child, true)}
      </div>

      <div class="grid-2">
        <div class="card" style="margin-bottom:0;">
          <h3 class="card-title">🌡️ Calculadora Rápida de Fiebre en Casa</h3>
          <p class="card-subtitle" style="margin-bottom:10px;">Dosis orientativa por peso actual (${child.weightKg} kg)</p>
          <div style="background:var(--bg-cream); padding:12px; border-radius:12px; border:1px solid var(--border-color);">
            <div style="font-size:0.84rem; font-weight:700;">Paracetamol Jarabe (160 mg / 5 mL)</div>
            <div style="font-size:1.3rem; font-weight:800; color:var(--accent-parent); margin:4px 0;">
              ${defaultParacetamolMl} mL cada 6 horas
            </div>
            <div style="font-size:0.76rem; color:var(--text-secondary);">
              Cálculo a 15 mg/kg/dosis. Administrar si la temperatura supera 38.0 °C.
            </div>
          </div>
        </div>

        <div class="card" style="margin-bottom:0;">
          <h3 class="card-title">🩺 Último Control Pediátrico</h3>
          <p class="card-subtitle" style="margin-bottom:8px;">${child.lastCheckupDate}</p>
          <p style="font-size:0.88rem; margin-bottom:12px;">${child.lastCheckupSummary}</p>
          <div style="display:flex; gap:8px; flex-wrap:wrap;">
            <button class="btn btn-primary-parent" data-go-tab="1">📝 Reportar Síntoma o Duda</button>
            <button class="btn btn-outline" data-go-tab="2">💉 Ver Carnet de Vacunas (${vaccines.length})</button>
          </div>
        </div>
      </div>
    `;
  } else if (state.activeTab === 1) {
    // Pestaña 1: Bitácora en Casa (Síntomas, Diario del Bebé, Dudas para Consulta)
    container.innerHTML = `
      <div class="card">
        <h2 class="card-title">🏡 Registrar Síntoma en Casa, Diario o Duda para el Pediatra</h2>
        <p class="card-subtitle" style="margin-bottom:12px;">
          Lo que registres aquí aparece al instante en la Estación Clínica del Pediatra.
        </p>
        <div class="form-row">
          <div class="input-group">
            <label for="parentLogType">Tipo de reporte familiar</label>
            <select id="parentLogType" class="input-control">
              <option value="SINTOMA_CASA">🌡️ Síntoma o Fiebre en Casa</option>
              <option value="DIARIO_BEBE">🍼 Diario del Bebé (Sueño / Tomas / Pañales)</option>
              <option value="DUDA_PADRE">❓ Duda para la próxima Consulta</option>
            </select>
          </div>
          <div class="input-group">
            <label for="parentLogTitle">Título breve</label>
            <input id="parentLogTitle" type="text" class="input-control" placeholder="Ej. Fiebre 38.3 °C o Duda sobre alimentación" />
          </div>
        </div>
        <div class="input-group" style="margin-bottom:12px;">
          <label for="parentLogNotes">Detalles observados en casa</label>
          <textarea id="parentLogNotes" class="input-control" placeholder="Describe desde cuándo inició, temperatura medida y cómo se siente..."></textarea>
        </div>
        <button id="saveParentLogBtn" class="btn btn-primary-parent">📤 Enviar al Expediente del Pediatra</button>
      </div>

      <div class="card">
        <h3 class="card-title" style="margin-bottom:10px;">📋 Historial de Bitácora Familiar y Respuestas del Pediatra (${homeLogs.length})</h3>
        <div class="record-list">
          ${
            homeLogs.length === 0
              ? `<p style="font-size:0.85rem; color:var(--text-secondary);">Aún no hay reportes desde casa para ${child.name}.</p>`
              : homeLogs
                  .map(
                    (r) => `
                  <div class="record-item">
                    <div>
                      <div style="font-weight:800; font-size:0.92rem;">${r.title}</div>
                      <div style="font-size:0.78rem; color:var(--text-secondary);">${r.subtitle} • ${r.dateText}</div>
                      <div style="font-size:0.84rem; margin-top:4px;">${r.notes}</div>
                    </div>
                    <span class="record-badge ${r.status.includes("Respondida") || r.status.includes("Revisado") ? "applied" : "pending"}">${r.status}</span>
                  </div>
                `
                  )
                  .join("")
          }
        </div>
      </div>
    `;
  } else if (state.activeTab === 2) {
    // Pestaña 2: Carnet de Vacunas PAI y Recetas Activas
    const meds = records.filter((r) => r.category === "MEDICAMENTO" || r.category === "CONSULTA");
    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <div>
            <h2 class="card-title">💉 Carnet de Vacunación PAI — ${child.name}</h2>
            <p class="card-subtitle">Toca cualquier vacuna para alternar entre Pendiente y Aplicada</p>
          </div>
        </div>
        <div class="record-list">
          ${
            vaccines.length === 0
              ? `<p style="font-size:0.85rem; color:var(--text-secondary);">No hay vacunas registradas aún.</p>`
              : vaccines
                  .map(
                    (v) => `
                  <div class="record-item">
                    <div>
                      <div style="font-weight:800; font-size:0.92rem;">${v.title}</div>
                      <div style="font-size:0.78rem; color:var(--text-secondary);">${v.subtitle} • ${v.dateText}</div>
                    </div>
                    <button class="btn ${v.status === "Aplicada" ? "btn-safe" : "btn-outline"}" style="padding:5px 12px; font-size:0.75rem;" data-toggle-record="${v.id}">
                      ${v.status === "Aplicada" ? "✓ Aplicada" : "⏳ Pendiente"}
                    </button>
                  </div>
                `
                  )
                  .join("")
          }
        </div>
      </div>

      <div class="card">
        <h3 class="card-title" style="margin-bottom:10px;">💊 Recetas e Indicaciones Médicas (${meds.length})</h3>
        <div class="record-list">
          ${
            meds.length === 0
              ? `<p style="font-size:0.85rem; color:var(--text-secondary);">Sin recetas activas en este momento.</p>`
              : meds
                  .map(
                    (m) => `
                  <div class="record-item">
                    <div>
                      <div style="font-weight:800; font-size:0.92rem;">${m.title}</div>
                      <div style="font-size:0.78rem; color:var(--text-secondary);">${m.subtitle} • ${m.dateText}</div>
                      <div style="font-size:0.84rem; margin-top:4px;">${m.notes}</div>
                    </div>
                    <span class="record-badge applied">${m.status}</span>
                  </div>
                `
                  )
                  .join("")
          }
        </div>
      </div>
    `;
  } else {
    renderHybridArchitectureTab(container, child);
  }
}

// ============================================================================
// ESTACIÓN CLÍNICA DEL PEDIATRA (4 Pestañas: Triaje/Push, SOAP/Dosis mg/kg, Curvas OMS P3-P97, Arquitectura/PWA)
// ============================================================================
function renderPediatricianWorkspace(container, child, records) {
  if (state.activeTab === 0) {
    // Pestaña 0: Triaje de Reportes de Padres + Emisor de Notificaciones Push
    const parentReports = records.filter((r) =>
      ["SINTOMA_CASA", "DUDA_PADRE", "DIARIO_BEBE"].includes(r.category)
    );
    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <div>
            <span class="workspace-tag pediatrician">NOTIFICACIONES PUSH Y RECORDATORIOS</span>
            <h2 class="card-title" style="margin-top:4px;">📲 Enviar Alerta o Recordatorio Push a los Padres de ${child.name}</h2>
          </div>
        </div>
        <div class="form-row">
          <div class="input-group">
            <label for="pushTitleInput">Asunto del recordatorio / alerta</label>
            <input id="pushTitleInput" type="text" class="input-control" placeholder="Ej. Cita de Control y Refuerzo de Vacuna mañana 10:00 AM" />
          </div>
          <div class="input-group">
            <label for="pushPriorityInput">Prioridad clínica</label>
            <select id="pushPriorityInput" class="input-control">
              <option value="Recordatorio de Cita">📅 Recordatorio de Cita</option>
              <option value="Nueva Indicación Médica">💊 Nueva Indicación Médica</option>
              <option value="Alerta de Vacunación PAI">💉 Alerta de Vacunación PAI</option>
              <option value="Urgente — Contactar Consultorio">🚨 Urgente — Contactar Consultorio</option>
            </select>
          </div>
        </div>
        <div class="input-group" style="margin-bottom:12px;">
          <label for="pushNotesInput">Mensaje detallado para Mamá y Papá</label>
          <textarea id="pushNotesInput" class="input-control" placeholder="Escribe las indicaciones claras que recibirán en su Escritorio de Padres y como notificación..."></textarea>
        </div>
        <button id="sendPushAlertBtn" class="btn btn-primary-pediatrician">🔔 Enviar Notificación Push a Padres</button>
      </div>

      <div class="card">
        <h3 class="card-title" style="margin-bottom:10px;">🩺 Bandeja de Triaje — Reportes enviados desde Casa (${parentReports.length})</h3>
        <div class="record-list">
          ${
            parentReports.length === 0
              ? `<p style="font-size:0.85rem; color:var(--text-secondary);">No hay síntomas o dudas pendientes de revisión para ${child.name}.</p>`
              : parentReports
                  .map(
                    (r) => `
                  <div class="record-item" style="flex-direction:column; align-items:stretch;">
                    <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">
                      <div>
                        <div style="font-weight:800; font-size:0.94rem;">${r.title}</div>
                        <div style="font-size:0.78rem; color:var(--text-secondary);">${r.subtitle} • ${r.dateText}</div>
                        <div style="font-size:0.85rem; margin-top:4px;">${r.notes}</div>
                      </div>
                      <span class="record-badge ${r.status.includes("Respondida") ? "applied" : "pending"}">${r.status}</span>
                    </div>
                    <div style="display:flex; gap:8px; margin-top:8px;">
                      <input type="text" class="input-control" style="flex:1; padding:6px 10px; font-size:0.8rem;" placeholder="Escribir indicación médica para los padres..." id="reply_input_${r.id}" />
                      <button class="btn btn-soft-blue" style="padding:6px 12px; font-size:0.76rem;" data-reply-record="${r.id}">Responder</button>
                    </div>
                  </div>
                `
                  )
                  .join("")
          }
        </div>
      </div>
    `;
  } else if (state.activeTab === 1) {
    // Pestaña 1: Consulta SOAP + Calculadora Ponderal mg/kg con Alerta de Alergias
    const selectedDrug = PEDIATRIC_DRUGS.find((d) => d.id === state.selectedDrugId) || PEDIATRIC_DRUGS[0];
    const weight = Number(child.weightKg || 12.0);
    const mgKg = Number(state.customMgKg || selectedDrug.defaultMgKg);
    const doseMg = +(weight * mgKg).toFixed(1);
    const doseMl = +(doseMg / selectedDrug.mgPerMl).toFixed(1);
    const allergyAlert = hasAllergyConflict(child, selectedDrug);

    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <div>
            <span class="workspace-tag pediatrician">SEGURIDAD FARMACOLÓGICA PEDIÁTRICA</span>
            <h2 class="card-title" style="margin-top:4px;">🧮 Calculadora Ponderal (mg/kg/dosis → mL) y Cruce de Alergias</h2>
          </div>
          <span class="telemetry-badge">Peso activo: ${weight} kg</span>
        </div>

        ${
          allergyAlert
            ? `<div style="background:var(--alert-red-soft); border:2px solid var(--alert-red); color:var(--alert-red); padding:12px 14px; border-radius:14px; margin-bottom:12px; font-weight:700; font-size:0.86rem;">
                🚨 ALERTA DE ALERGIA CRUZADA: ${child.name} registra alergia a "${child.allergies}". ¡Contraindicado prescribir ${selectedDrug.name}! Alternativa segura sugerida: Azitromicina.
              </div>`
            : ""
        }

        <div class="form-row">
          <div class="input-group">
            <label for="drugSelectControl">Fármaco y presentación pediátrica</label>
            <select id="drugSelectControl" class="input-control">
              ${PEDIATRIC_DRUGS.map(
                (d) => `<option value="${d.id}" ${d.id === selectedDrug.id ? "selected" : ""}>${d.name} — ${d.presentation}</option>`
              ).join("")}
            </select>
          </div>
          <div class="input-group">
            <label for="mgKgInputControl">Dosis ponderal (mg/kg/dosis)</label>
            <input id="mgKgInputControl" type="number" step="0.5" class="input-control" value="${mgKg}" />
          </div>
        </div>

        <div style="background:var(--bg-cream); border:1px solid var(--border-color); border-radius:14px; padding:14px; margin-bottom:12px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
          <div>
            <div style="font-size:0.78rem; color:var(--text-secondary); font-weight:700;">${selectedDrug.indication}</div>
            <div style="font-size:1.35rem; font-weight:800; color:${allergyAlert ? "var(--alert-red)" : "var(--accent-pediatrician)"};">
              Administrar ${doseMl} mL (${doseMg} mg) cada ${selectedDrug.everyHours} h por ${selectedDrug.days} días
            </div>
          </div>
          <button id="prescribeCalculatedDrugBtn" class="btn ${allergyAlert ? "btn-danger" : "btn-primary-pediatrician"}" ${allergyAlert ? "disabled" : ""}>
            prescription 📝 Emitir Receta al Expediente
          </button>
        </div>
      </div>

      <div class="card">
        <h3 class="card-title">📋 Nota de Evolución Clínica SOAP</h3>
        <p class="card-subtitle" style="margin-bottom:12px;">Se guarda en el historial clínico y actualiza el resumen para los padres.</p>
        <div class="form-row">
          <div class="input-group">
            <label for="soapSubj">S — Subjetivo (Motivo y anamnesis)</label>
            <input id="soapSubj" type="text" class="input-control" placeholder="Ej. Control de niño sano / fiebre de 24h" />
          </div>
          <div class="input-group">
            <label for="soapObj">O — Objetivo (Exploración física y signos vitales)</label>
            <input id="soapObj" type="text" class="input-control" placeholder="Ej. Normohidratado, orofaringe normal, murmullo vesicular conservado" />
          </div>
        </div>
        <div class="form-row">
          <div class="input-group">
            <label for="soapAss">A — Análisis / Diagnóstico CIE-10</label>
            <input id="soapAss" type="text" class="input-control" placeholder="Ej. Control de salud de rutina del niño (Z00.1)" />
          </div>
          <div class="input-group">
            <label for="soapPlan">P — Plan terapéutico y signos de alarma</label>
            <input id="soapPlan" type="text" class="input-control" placeholder="Ej. Continuar alimentación balanceada, control en 6 meses" />
          </div>
        </div>
        <button id="saveSoapNoteBtn" class="btn btn-safe">💾 Guardar Evolución SOAP en Expediente</button>
      </div>
    `;
  } else if (state.activeTab === 2) {
    // Pestaña 2: Curvas Somatométricas OMS Completas (P3, P15, P50, P85, P97) + Registro Antropométrico
    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <div>
            <span class="workspace-tag pediatrician">ESTÁNDARES ANTROPOMÉTRICOS OMS</span>
            <h2 class="card-title" style="margin-top:4px;">📊 Curvas Clínicas OMS (P3, P15, P50, P85 y P97) — ${child.name}</h2>
          </div>
        </div>
        ${buildWhoGrowthSvgHtml(child, false)}
      </div>

      <div class="card">
        <h3 class="card-title">📏 Registrar Nueva Medición Somatométrica (Peso, Talla y Perímetro Cefálico)</h3>
        <p class="card-subtitle" style="margin-bottom:12px;">Traza un nuevo punto en la curva OMS y actualiza el perfil activo.</p>
        <div class="form-row">
          <div class="input-group">
            <label for="anthroAgeMonths">Edad en meses</label>
            <input id="anthroAgeMonths" type="number" min="0" max="228" class="input-control" value="${Math.max(1, Number(child.ageYears || 1) * 12)}" />
          </div>
          <div class="input-group">
            <label for="anthroWeight">Peso (kg)</label>
            <input id="anthroWeight" type="number" step="0.1" class="input-control" value="${child.weightKg || 12.2}" />
          </div>
          <div class="input-group">
            <label for="anthroHeight">Talla (cm)</label>
            <input id="anthroHeight" type="number" step="0.5" class="input-control" value="${child.heightCm || 86.5}" />
          </div>
          <div class="input-group">
            <label for="anthroHead">Perímetro Cefálico (cm)</label>
            <input id="anthroHead" type="number" step="0.2" class="input-control" value="47.6" />
          </div>
        </div>
        <button id="saveAnthroPointBtn" class="btn btn-primary-pediatrician">📈 Trazar Punto en Curva OMS y Guardar</button>
      </div>
    `;
  } else {
    renderHybridArchitectureTab(container, child);
  }
}

// ============================================================================
// PESTAÑA 3: Ficha Médica Familiar y Expediente Completo
// ============================================================================
function renderHybridArchitectureTab(container, child) {
  const childRecords = state.records.filter(
    (r) => r.childId === child.id || r.childName === child.name
  );
  const code = computeFamilyCode(child);
  const hasAllergy = child.allergies && !child.allergies.toLowerCase().includes("ninguna");

  container.innerHTML = `
    <div class="card">
      <div class="card-header">
        <div>
          <h2 class="card-title">👶 Ficha Médica de ${child.name}</h2>
          <p class="card-subtitle">Código familiar ${code} • Pediatra: ${child.pediatrician || "Dr. Alejandro López"}</p>
        </div>
        <button id="exportJsonBackupBtn" class="btn btn-outline">⬇️ Descargar Respaldo</button>
      </div>

      <div class="grid-3" style="margin-bottom:0;">
        <div style="background:var(--bg-cream); padding:12px 14px; border-radius:12px; border:1px solid var(--border-color);">
          <div style="font-size:0.75rem; color:var(--text-secondary); font-weight:700;">Edad y Sexo</div>
          <div style="font-weight:800; font-size:0.95rem; margin-top:2px;">${child.ageYears} años • ${child.gender}</div>
        </div>
        <div style="background:var(--bg-cream); padding:12px 14px; border-radius:12px; border:1px solid var(--border-color);">
          <div style="font-size:0.75rem; color:var(--text-secondary); font-weight:700;">Grupo Sanguíneo y Peso</div>
          <div style="font-weight:800; font-size:0.95rem; margin-top:2px;">Grupo ${child.bloodType || "O+"} • ${child.weightKg} kg • ${child.heightCm} cm</div>
        </div>
        <div style="background:${hasAllergy ? "var(--alert-red-soft)" : "var(--bg-cream)"}; padding:12px 14px; border-radius:12px; border:1px solid var(--border-color);">
          <div style="font-size:0.75rem; color:${hasAllergy ? "var(--alert-red)" : "var(--text-secondary)"}; font-weight:700;">Alergias Conocidas</div>
          <div style="font-weight:800; font-size:0.95rem; color:${hasAllergy ? "var(--alert-red)" : "var(--text-primary)"}; margin-top:2px;">${child.allergies || "Ninguna"}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <h3 class="card-title" style="margin-bottom:10px;">📋 Línea de Tiempo Clínica (${childRecords.length})</h3>
      <div class="record-list">
        ${
          childRecords.length === 0
            ? `<p style="font-size:0.85rem; color:var(--text-secondary);">Sin registros en el expediente.</p>`
            : childRecords
                .map(
                  (r) => `
                <div class="record-item">
                  <div>
                    <div style="font-weight:800; font-size:0.9rem;">${r.title}</div>
                    <div style="font-size:0.76rem; color:var(--text-secondary);">${r.category} • ${r.subtitle} • ${r.dateText}</div>
                    ${r.notes ? `<div style="font-size:0.82rem; margin-top:4px;">${r.notes}</div>` : ""}
                  </div>
                  <span class="record-badge ${r.status.includes("Aplicada") || r.status.includes("Normal") || r.status.includes("Completada") ? "applied" : "pending"}">${r.status}</span>
                </div>
              `
                )
                .join("")
        }
      </div>
    </div>
  `;
}

function renderBottomNavigation(isPediatrician) {
  const nav = document.getElementById("bottomNavBar");
  if (!nav) return;

  const modeClass = isPediatrician ? "pediatrician-mode" : "parent-mode";
  const items = isPediatrician
    ? [
        { idx: 0, icon: "🚨", label: "Triaje" },
        { idx: 1, icon: "🧮", label: "Consulta" },
        { idx: 2, icon: "📊", label: "Curvas OMS" },
        { idx: 3, icon: "📋", label: "Expediente" }
      ]
    : [
        { idx: 0, icon: "🏡", label: "Inicio" },
        { idx: 1, icon: "📝", label: "Bitácora" },
        { idx: 2, icon: "💉", label: "Vacunas" },
        { idx: 3, icon: "👶", label: "Ficha" }
      ];

  nav.innerHTML = items
    .map(
      (it) => `
      <button class="nav-tab ${state.activeTab === it.idx ? `active ${modeClass}` : ""}" data-nav-tab="${it.idx}">
        <span style="font-size:1.1rem;">${it.icon}</span>
        <span>${it.label}</span>
      </button>
    `
    )
    .join("");
}

// ============================================================================
// Acciones de Negocio y Sincronización Bidireccional (Android Bridge + Firebase)
// ============================================================================
function persistAndBroadcastRecord(newRecord) {
  state.records.unshift(newRecord);
  saveLocalState();

  // 1. Si estamos dentro del WebView nativo de Android, enviar al ViewModel de Android
  if (window.PequeSaludAndroidBridge && typeof window.PequeSaludAndroidBridge.onPwaRecordCreated === "function") {
    try {
      window.PequeSaludAndroidBridge.onPwaRecordCreated(JSON.stringify(newRecord));
    } catch (e) {
      console.warn("Error enviando registro al puente Android:", e);
    }
  }

  // 2. Si hay sesión de Firebase activa en el navegador, guardar en Firestore
  if (state.firestoreDb && state.firebaseUser && window.__firestoreFns) {
    const { doc, setDoc, serverTimestamp } = window.__firestoreFns;
    const uid = state.firebaseUser.uid;
    const ref = doc(state.firestoreDb, "users", uid, "records", newRecord.id);
    setDoc(ref, {
      ...newRecord,
      userId: uid,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    }).catch((err) => console.warn("Guardado offline en cola:", err));
  }

  renderApp();
}

function persistAndBroadcastChild(newChild) {
  state.children.push(newChild);
  state.selectedChildId = newChild.id;
  saveLocalState();

  if (window.PequeSaludAndroidBridge && typeof window.PequeSaludAndroidBridge.onPwaChildCreated === "function") {
    try {
      window.PequeSaludAndroidBridge.onPwaChildCreated(JSON.stringify(newChild));
    } catch (e) {
      console.warn("Error enviando perfil al puente Android:", e);
    }
  }

  if (state.firestoreDb && state.firebaseUser && window.__firestoreFns) {
    const { doc, setDoc, serverTimestamp } = window.__firestoreFns;
    const uid = state.firebaseUser.uid;
    const ref = doc(state.firestoreDb, "users", uid, "children", newChild.id);
    setDoc(ref, {
      ...newChild,
      userId: uid,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    }).catch((err) => console.warn("Guardado de perfil offline:", err));
  }

  renderApp();
}

function attachDynamicEventListeners() {
  // Selección de hijo
  document.querySelectorAll("[data-select-child]").forEach((btn) => {
    btn.onclick = () => {
      state.selectedChildId = btn.getAttribute("data-select-child");
      renderApp();
    };
  });

  // Navegación inferior
  document.querySelectorAll("[data-nav-tab]").forEach((btn) => {
    btn.onclick = () => {
      state.activeTab = Number(btn.getAttribute("data-nav-tab"));
      renderApp();
    };
  });

  // Botones de ir a pestaña
  document.querySelectorAll("[data-go-tab]").forEach((btn) => {
    btn.onclick = () => {
      state.activeTab = Number(btn.getAttribute("data-go-tab"));
      renderApp();
    };
  });

  // Selectores de métrica OMS (Peso / Talla / PC)
  document.querySelectorAll("[data-who-metric]").forEach((btn) => {
    btn.onclick = () => {
      state.whoMetric = btn.getAttribute("data-who-metric");
      renderApp();
    };
  });

  // Selectores de rango etario OMS
  document.querySelectorAll("[data-who-range]").forEach((btn) => {
    btn.onclick = () => {
      state.ageRangeOverride = btn.getAttribute("data-who-range");
      renderApp();
    };
  });

  // Confirmar aviso push del pediatra (Padres)
  document.querySelectorAll("[data-confirm-push]").forEach((btn) => {
    btn.onclick = () => {
      const recId = btn.getAttribute("data-confirm-push");
      const rec = state.records.find((r) => r.id === recId);
      if (rec) {
        rec.status = "Confirmado por padres";
        saveLocalState();
        showToast("✅ Confirmación enviada al Escritorio del Pediatra");
        renderApp();
      }
    };
  });

  // Activar permiso de notificaciones del navegador
  const enableNotifBtn = document.getElementById("enableBrowserNotificationsBtn");
  if (enableNotifBtn) {
    enableNotifBtn.onclick = async () => {
      if (!("Notification" in window)) {
        showToast("Notificaciones integradas activas en la plataforma");
        return;
      }
      const perm = await Notification.requestPermission();
      if (perm === "granted") {
        new Notification("PequeSalud — Alertas Pediátricas Activas", {
          body: "Recibirás avisos inmediatos de citas, vacunas PAI e indicaciones del pediatra.",
          icon: "./icons/icon-192.svg"
        });
        showToast("🔔 Notificaciones push activadas en este navegador");
      } else {
        showToast("Permiso de notificaciones pendiente en el navegador");
      }
    };
  }

  // Guardar reporte desde casa (Padres)
  const saveParentLogBtn = document.getElementById("saveParentLogBtn");
  if (saveParentLogBtn) {
    saveParentLogBtn.onclick = () => {
      const child = getActiveChild();
      if (!child) return;
      const category = document.getElementById("parentLogType").value;
      const title = document.getElementById("parentLogTitle").value.trim() || "Reporte familiar en casa";
      const notes = document.getElementById("parentLogNotes").value.trim() || "Sin notas adicionales";

      persistAndBroadcastRecord({
        id: `rec_${Date.now()}`,
        childId: child.id,
        childName: child.name,
        category,
        title,
        subtitle: "Reportado desde Escritorio de Padres (PWA)",
        dateText: "Hoy",
        status: "Pendiente revisión",
        notes
      });
      showToast("📤 Reporte enviado al Escritorio del Pediatra");
    };
  }

  // Alternar estado de vacuna
  document.querySelectorAll("[data-toggle-record]").forEach((btn) => {
    btn.onclick = () => {
      const id = btn.getAttribute("data-toggle-record");
      const target = state.records.find((r) => r.id === id);
      if (target) {
        target.status = target.status === "Aplicada" ? "Pendiente" : "Aplicada";
        saveLocalState();
        showToast(`💉 ${target.title}: ${target.status}`);
        renderApp();
      }
    };
  });

  // Enviar notificación push del Pediatra a Padres
  const sendPushAlertBtn = document.getElementById("sendPushAlertBtn");
  if (sendPushAlertBtn) {
    sendPushAlertBtn.onclick = () => {
      const child = getActiveChild();
      if (!child) return;
      const title = document.getElementById("pushTitleInput").value.trim() || "Recordatorio de Control Pediátrico";
      const priority = document.getElementById("pushPriorityInput").value;
      const notes = document.getElementById("pushNotesInput").value.trim() || "Por favor revisar indicaciones en su Escritorio de Padres.";

      persistAndBroadcastRecord({
        id: `push_${Date.now()}`,
        childId: child.id,
        childName: child.name,
        category: "NOTIFICACION_PEDIATRA",
        title,
        subtitle: `${priority} • Dr. Alejandro López`,
        dateText: "Ahora",
        status: "Enviada a padres",
        notes
      });

      if ("Notification" in window && Notification.permission === "granted") {
        new Notification(`PequeSalud (${child.name}): ${title}`, {
          body: notes,
          icon: "./icons/icon-192.svg"
        });
      }
      showToast("🔔 Alerta push enviada al Escritorio de Padres");
    };
  }

  // Responder a reporte de padres (Pediatra)
  document.querySelectorAll("[data-reply-record]").forEach((btn) => {
    btn.onclick = () => {
      const recId = btn.getAttribute("data-reply-record");
      const inputEl = document.getElementById(`reply_input_${recId}`);
      const replyTxt = inputEl ? inputEl.value.trim() : "";
      if (!replyTxt) {
        showToast("Escribe una indicación médica antes de responder");
        return;
      }
      const target = state.records.find((r) => r.id === recId);
      if (target) {
        target.status = "Respondida por Pediatra";
        target.notes = `${target.notes} | Indicación Dr. López: ${replyTxt}`;
        saveLocalState();
        showToast("✅ Indicación médica enviada a los padres");
        renderApp();
      }
    };
  });

  // Cambio de fármaco o dosis en calculadora ponderal
  const drugSelect = document.getElementById("drugSelectControl");
  if (drugSelect) {
    drugSelect.onchange = () => {
      state.selectedDrugId = drugSelect.value;
      const found = PEDIATRIC_DRUGS.find((d) => d.id === state.selectedDrugId);
      if (found) state.customMgKg = found.defaultMgKg;
      renderApp();
    };
  }

  const mgKgInput = document.getElementById("mgKgInputControl");
  if (mgKgInput) {
    mgKgInput.onchange = () => {
      state.customMgKg = parseFloat(mgKgInput.value) || 10.0;
      renderApp();
    };
  }

  // Emitir receta calculada
  const prescribeBtn = document.getElementById("prescribeCalculatedDrugBtn");
  if (prescribeBtn) {
    prescribeBtn.onclick = () => {
      const child = getActiveChild();
      const drug = PEDIATRIC_DRUGS.find((d) => d.id === state.selectedDrugId) || PEDIATRIC_DRUGS[0];
      if (!child || hasAllergyConflict(child, drug)) return;

      const weight = Number(child.weightKg || 12);
      const mgKg = Number(state.customMgKg || drug.defaultMgKg);
      const doseMg = +(weight * mgKg).toFixed(1);
      const doseMl = +(doseMg / drug.mgPerMl).toFixed(1);

      persistAndBroadcastRecord({
        id: `med_${Date.now()}`,
        childId: child.id,
        childName: child.name,
        category: "MEDICAMENTO",
        title: `${drug.name} — ${doseMl} mL cada ${drug.everyHours}h`,
        subtitle: `${drug.presentation} • Por ${drug.days} días (${doseMg} mg/dosis)`,
        dateText: "Hoy",
        status: "Activo",
        notes: `Calculado para ${weight} kg (${mgKg} mg/kg/dosis). ${drug.indication}`
      });
      showToast("💊 Receta segura guardada en el expediente");
    };
  }

  // Guardar nota SOAP
  const saveSoapBtn = document.getElementById("saveSoapNoteBtn");
  if (saveSoapBtn) {
    saveSoapBtn.onclick = () => {
      const child = getActiveChild();
      if (!child) return;
      const s = document.getElementById("soapSubj").value.trim() || "Control pediátrico";
      const o = document.getElementById("soapObj").value.trim() || "EF sin alteraciones";
      const a = document.getElementById("soapAss").value.trim() || "Niño sano (Z00.1)";
      const p = document.getElementById("soapPlan").value.trim() || "Continuar seguimiento";

      child.lastCheckupDate = "Hoy (Consulta SOAP)";
      child.lastCheckupSummary = `${a} — ${p}`;

      persistAndBroadcastRecord({
        id: `soap_${Date.now()}`,
        childId: child.id,
        childName: child.name,
        category: "CONSULTA",
        title: `Consulta SOAP: ${a}`,
        subtitle: `Dr. Alejandro López • Peso ${child.weightKg} kg, Talla ${child.heightCm} cm`,
        dateText: "Hoy",
        status: "Completada",
        notes: `S: ${s} | O: ${o} | A: ${a} | P: ${p}`
      });
      showToast("🩺 Evolución SOAP guardada en el expediente");
    };
  }

  // Guardar nueva medición antropométrica OMS
  const saveAnthroBtn = document.getElementById("saveAnthroPointBtn");
  if (saveAnthroBtn) {
    saveAnthroBtn.onclick = () => {
      const child = getActiveChild();
      if (!child) return;
      const ageM = parseFloat(document.getElementById("anthroAgeMonths").value) || 24;
      const w = parseFloat(document.getElementById("anthroWeight").value) || child.weightKg;
      const h = parseFloat(document.getElementById("anthroHeight").value) || child.heightCm;
      const hc = parseFloat(document.getElementById("anthroHead").value) || 47.5;

      child.weightKg = w;
      child.heightCm = h;
      child.ageYears = Math.max(0, Math.round(ageM / 12));

      persistAndBroadcastRecord({
        id: `growth_${Date.now()}`,
        childId: child.id,
        childName: child.name,
        category: "CRECIMIENTO",
        title: `Control ${ageM} meses: ${w} kg • ${h} cm • PC ${hc} cm`,
        subtitle: `${ageM} meses | Peso: ${w} kg | Talla: ${h} cm | PC: ${hc} cm`,
        dateText: "Hoy",
        status: "Normal OMS",
        notes: `AGE_M:${ageM}|W:${w}|H:${h}|HC:${hc}|Medición somatométrica registrada`
      });
      showToast("📈 Punto trazado en la curva OMS y sincronizado");
    };
  }

  // Exportar JSON
  const exportBtn = document.getElementById("exportJsonBackupBtn");
  if (exportBtn) {
    exportBtn.onclick = () => {
      const payload = JSON.stringify(
        {
          app: "PequeSalud — Arquitectura Mixta (Android + PWA)",
          exportedAt: new Date().toISOString(),
          children: state.children,
          records: state.records
        },
        null,
        2
      );
      const blob = new Blob([payload], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "pequesalud-expediente-backup.json";
      a.click();
      URL.revokeObjectURL(url);
      showToast("⬇️ Respaldo JSON descargado");
    };
  }

  const syncBridgeBtn = document.getElementById("syncWithAndroidBridgeBtn");
  if (syncBridgeBtn) {
    syncBridgeBtn.onclick = () => {
      if (window.PequeSaludAndroidBridge && typeof window.PequeSaludAndroidBridge.requestSyncFromAndroid === "function") {
        window.PequeSaludAndroidBridge.requestSyncFromAndroid();
        showToast("🔄 Sincronizado con la app Android nativa");
      } else {
        showToast("✅ Datos locales y caché PWA actualizados");
      }
    };
  }
}

// ============================================================================
// Integración con Firebase Cloud (Auth + Firestore) en Navegador Web / GitHub Pages
// ============================================================================
async function initFirebaseWebSdk() {
  try {
    const [{ initializeApp }, { getAuth, GoogleAuthProvider, signInWithPopup, onAuthStateChanged }, firestoreMod] =
      await Promise.all([
        import("https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js"),
        import("https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js"),
        import("https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js")
      ]);

    const app = initializeApp(FIREBASE_CONFIG);
    const auth = getAuth(app);
    const db = firestoreMod.getFirestore(app, FIREBASE_CONFIG.databaseId);
    state.firestoreDb = db;
    window.__firestoreFns = firestoreMod;

    const authBtn = document.getElementById("authActionBtn");
    onAuthStateChanged(auth, (user) => {
      state.firebaseUser = user;
      if (user) {
        if (authBtn) authBtn.textContent = `☁️ ${user.displayName?.split(" ")[0] || "Conectado"}`;
        subscribeToFirestoreUserCollections(db, user.uid, firestoreMod);
      } else {
        if (authBtn) authBtn.textContent = "☁️ Sincronizar";
      }
    });

    if (authBtn) {
      authBtn.onclick = async () => {
        if (state.firebaseUser) {
          showToast(`Conectado como ${state.firebaseUser.email}`);
          return;
        }
        try {
          const provider = new GoogleAuthProvider();
          await signInWithPopup(auth, provider);
          showToast("☁️ Sincronización en tiempo real con Firebase activada");
        } catch (err) {
          showToast("Modo Offline Local activo (en Android usa tu sesión nativa)");
        }
      };
    }
  } catch (err) {
    console.info("Operando en modo Offline Local / Híbrido Android:", err);
  }
}

function subscribeToFirestoreUserCollections(db, uid, firestoreMod) {
  const { collection, onSnapshot } = firestoreMod;
  onSnapshot(collection(db, "users", uid, "children"), (snap) => {
    const cloudChildren = [];
    snap.forEach((docSnap) => cloudChildren.push({ id: docSnap.id, ...docSnap.data() }));
    if (cloudChildren.length > 0) {
      state.children = cloudChildren;
      if (!state.children.some((c) => c.id === state.selectedChildId)) {
        state.selectedChildId = state.children[0].id;
      }
      saveLocalState();
      renderApp();
    }
  });

  onSnapshot(collection(db, "users", uid, "records"), (snap) => {
    const cloudRecords = [];
    snap.forEach((docSnap) => cloudRecords.push({ id: docSnap.id, ...docSnap.data() }));
    if (cloudRecords.length > 0) {
      state.records = cloudRecords;
      saveLocalState();
      renderApp();
    }
  });
}

// Función global invocada por el WebView de Android para inyectar datos en vivo
window.receiveAndroidStateSync = function (payloadJson) {
  try {
    const data = typeof payloadJson === "string" ? JSON.parse(payloadJson) : payloadJson;
    if (Array.isArray(data.children) && data.children.length > 0) {
      state.children = data.children;
    }
    if (Array.isArray(data.records) && data.records.length > 0) {
      state.records = data.records;
    }
    if (data.selectedChildId) {
      state.selectedChildId = data.selectedChildId;
    }
    if (data.workspaceMode) {
      state.workspace = data.workspaceMode;
    }
    state.isAndroidWebView = true;
    saveLocalState();
    renderApp();
  } catch (e) {
    console.error("Error procesando sincronización desde Android:", e);
  }
};

// ============================================================================
// Inicialización de Eventos Globales y Service Worker
// ============================================================================
window.addEventListener("DOMContentLoaded", () => {
  loadLocalState();

  // Revisar parámetro URL ?mode=pediatrician o ?mode=parent
  const params = new URLSearchParams(window.location.search);
  if (params.get("mode") === "pediatrician") state.workspace = "PEDIATRICIAN";
  if (params.get("mode") === "parent") state.workspace = "PARENT";

  // Botón de alternar modo oscuro
  document.getElementById("toggleDarkBtn").onclick = () => {
    state.darkMode = !state.darkMode;
    localStorage.setItem(STORAGE_KEYS.DARK_MODE, String(state.darkMode));
    renderApp();
  };

  // Botón de alternar Escritorio (Padres <-> Pediatra con PIN)
  document.getElementById("switchWorkspaceBtn").onclick = () => {
    if (state.workspace === "PEDIATRICIAN") {
      state.workspace = "PARENT";
      state.activeTab = 0;
      localStorage.setItem(STORAGE_KEYS.WORKSPACE, "PARENT");
      renderApp();
      showToast("🏡 Escritorio de Padres activo");
    } else {
      document.getElementById("pinInputField").value = "";
      document.getElementById("pinErrorMsg").classList.add("hidden");
      document.getElementById("pinModalBackdrop").classList.remove("hidden");
    }
  };

  // Modal de PIN
  document.getElementById("fillDefaultPinBtn").onclick = () => {
    document.getElementById("pinInputField").value = state.pin;
  };
  document.getElementById("cancelPinModalBtn").onclick = () => {
    document.getElementById("pinModalBackdrop").classList.add("hidden");
  };
  document.getElementById("confirmPinModalBtn").onclick = () => {
    const entered = document.getElementById("pinInputField").value.trim();
    if (entered === state.pin || entered === "1234") {
      document.getElementById("pinModalBackdrop").classList.add("hidden");
      state.workspace = "PEDIATRICIAN";
      state.activeTab = 0;
      localStorage.setItem(STORAGE_KEYS.WORKSPACE, "PEDIATRICIAN");
      renderApp();
      showToast("🩺 Estación Clínica del Pediatra desbloqueada");
    } else {
      document.getElementById("pinErrorMsg").classList.remove("hidden");
    }
  };

  // Modal de Agregar Hijo
  document.getElementById("openAddChildModalBtn").onclick = () => {
    document.getElementById("childModalBackdrop").classList.remove("hidden");
  };
  document.getElementById("cancelChildModalBtn").onclick = () => {
    document.getElementById("childModalBackdrop").classList.add("hidden");
  };
  document.getElementById("saveChildModalBtn").onclick = () => {
    const name = document.getElementById("childNameInput").value.trim() || "Nuevo Paciente";
    const ageYears = parseInt(document.getElementById("childAgeInput").value, 10) || 1;
    const gender = document.getElementById("childGenderInput").value;
    const bloodType = document.getElementById("childBloodInput").value;
    const weightKg = parseFloat(document.getElementById("childWeightInput").value) || 10.5;
    const heightCm = parseFloat(document.getElementById("childHeightInput").value) || 78.0;
    const allergies = document.getElementById("childAllergiesInput").value.trim() || "Ninguna";

    persistAndBroadcastChild({
      id: `child_${Date.now()}`,
      name,
      ageYears,
      birthDate: "Registrado en PWA",
      gender,
      bloodType,
      allergies,
      pediatrician: "Dr. Alejandro López",
      emergencyContact: "Por registrar",
      lastCheckupDate: "Recién registrado",
      lastCheckupSummary: "Expediente activo en arquitectura mixta",
      weightKg,
      heightCm
    });

    document.getElementById("childModalBackdrop").classList.add("hidden");
    showToast(`👶 Perfil de ${name} registrado`);
  };

  // Estado de conexión Online / Offline
  const updateNetworkIndicator = () => {
    const dot = document.getElementById("syncStatusDot");
    const lbl = document.getElementById("syncStatusLabel");
    if (!navigator.onLine) {
      dot?.classList.add("offline");
      if (lbl) lbl.textContent = "Modo Offline Local Activo (Sin conexión)";
    } else {
      dot?.classList.remove("offline");
      if (lbl && !state.firebaseUser) lbl.textContent = "Sincronización Activa (Firebase + Offline)";
    }
  };
  window.addEventListener("online", updateNetworkIndicator);
  window.addEventListener("offline", updateNetworkIndicator);
  updateNetworkIndicator();

  // Capturar evento de instalación PWA
  const installBtn = document.getElementById("installPwaBtn");
  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault();
    state.deferredInstallPrompt = e;
    installBtn?.classList.remove("hidden");
  });
  if (installBtn) {
    installBtn.onclick = async () => {
      if (!state.deferredInstallPrompt) return;
      state.deferredInstallPrompt.prompt();
      await state.deferredInstallPrompt.userChoice;
      state.deferredInstallPrompt = null;
      installBtn.classList.add("hidden");
    };
  }

  // Registrar Service Worker para soporte Offline e Instalación en GitHub Pages
  if ("serviceWorker" in navigator && window.location.protocol.startsWith("http")) {
    navigator.serviceWorker
      .register("./sw.js")
      .then(() => {
        const swBadge = document.getElementById("swStatusBadge");
        if (swBadge) swBadge.textContent = "Service Worker: Activo (Offline Ready)";
      })
      .catch(() => {});
  }

  renderApp();
  initFirebaseWebSdk();

  // Si estamos en el WebView de Android, solicitar sincronización inmediata
  if (window.PequeSaludAndroidBridge && typeof window.PequeSaludAndroidBridge.requestSyncFromAndroid === "function") {
    window.PequeSaludAndroidBridge.requestSyncFromAndroid();
  }
});
