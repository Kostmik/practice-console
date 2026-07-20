const { createApp, ref, reactive, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    const page = ref('home');
    const isLoading = ref(false);
    const error = ref('');
    const showPassportForm = ref(false);
    const showDetailedReport = ref(false);

    // ==========================================================
    // 1. НАВИГАЦИЯ ПО РАЗДЕЛАМ
    // ==========================================================
    const sections = [
      { key: 'materials', title: '5. Материалы' },
      { key: 'loads', title: '6. Нагрузки' },
      { key: 'slab', title: '7. Плита' },
      { key: 'beam', title: '7. Балка' },
      { key: 'strengthening', title: '14. Усиление' },
      { key: 'inspection', title: '15. Обследование' }
    ];

    const currentSectionIndex = computed(() => {
      return sections.findIndex(s => s.key === page.value);
    });

    const currentSectionTitle = computed(() => {
      if (page.value === 'home') return 'Главная';
      const section = sections.find(s => s.key === page.value);
      return section ? section.title : '';
    });

    const hasPrevSection = computed(() => currentSectionIndex.value > 0);
    const hasNextSection = computed(() => currentSectionIndex.value < sections.length - 1);

    function goToPrevSection() {
      if (hasPrevSection.value) {
          error.value = '';
          isLoading.value = false;
          page.value = sections[currentSectionIndex.value - 1].key;
      }
    }

    function goToNextSection() {
      if (hasNextSection.value) {
        error.value = '';
        isLoading.value = false;
        page.value = sections[currentSectionIndex.value + 1].key;
      }
    }

    function goToSection(key) {
      error.value = '';
      isLoading.value = false;
      page.value = key;
    }

    // ==========================================================
    // 2. ПАСПОРТ ОБЪЕКТА (12 полей)
    // ==========================================================
    const bridgeData = reactive({
      spanLength: 10.8,
      ballastThickness: 0.25,
      trackType: 1,
      sleeperType: 1,
      concreteStrengthR: 23.0,
      distanceBetweenBeams: 1.8,
      trackOffsetLeft: 0.2,
      trackOffsetRight: 0.2,
      mBeams: 2,
      rebarType: 1,
      designYear: 1931,
      loadType: 'Н7'
    });

    const isPassportFilled = computed(() => {
      return bridgeData.spanLength > 0 &&
             bridgeData.ballastThickness > 0 &&
             bridgeData.concreteStrengthR > 0 &&
             bridgeData.distanceBetweenBeams > 0 &&
             bridgeData.mBeams > 0;
    });

    const trackTypeName = computed(() => bridgeData.trackType === 1 ? 'Звеньевой' : 'Бесстыковой');
    const sleeperTypeName = computed(() => bridgeData.sleeperType === 1 ? 'Ж/б' : 'Деревянные');
    const rebarTypeName = computed(() => bridgeData.rebarType === 1 ? 'Гладкая (А240)' : 'Периодич. (А400)');

    function savePassport() {
      if (!isPassportFilled.value) {
        alert('Заполните все обязательные поля паспорта!');
        return;
      }
      localStorage.setItem('bridgePassport', JSON.stringify(bridgeData));

      // Очищаем устаревшие результаты при изменении паспорта
      localStorage.removeItem('materialsResult');
      localStorage.removeItem('loadsPermResult');
      localStorage.removeItem('loadsDynBeamResult');
      localStorage.removeItem('loadsDynSlabResult');
      localStorage.removeItem('loadsShareResult');
      localStorage.removeItem('slabStrengthResult');
      localStorage.removeItem('slabShearResult');
      localStorage.removeItem('slabFatigueResult');
      localStorage.removeItem('strengtheningResult');
      localStorage.removeItem('inspectionResult');

      materialsResult.value = null;
      loadsPermResult.value = null;
      loadsDynBeamResult.value = null;
      loadsDynSlabResult.value = null;
      loadsShareResult.value = null;
      slabStrengthResult.value = null;
      slabShearResult.value = null;
      slabFatigueResult.value = null;
      strengtheningResult.value = null;
      inspectionResult.value = null;

      showPassportForm.value = false;
      alert('✅ Паспорт обновлён!\n\nВнимание: предыдущие результаты расчётов автоматически сброшены.');
    }

    function loadPassport() {
      try {
        const saved = localStorage.getItem('bridgePassport');
        if (saved) {
          const data = JSON.parse(saved);
          Object.assign(bridgeData, data);
        }
      } catch (e) {
        console.error('Ошибка загрузки паспорта:', e);
      }
    }

    // ==========================================================
    // 3. API ЗАПРОСЫ
    // ==========================================================
    async function api(path, opts = {}) {
      isLoading.value = true;
      error.value = '';
      try {
        const res = await fetch(path, {
          headers: { 'Content-Type': 'application/json', ...opts.headers },
          ...opts
        });
        if (!res.ok) {
          const text = await res.text();
          throw new Error(text || 'Ошибка запроса');
        }
        const text = await res.text();
        return text ? JSON.parse(text) : null;
      } catch (e) {
        error.value = e.message;
        console.error('API ошибка:', e);
        return null;
      } finally {
        isLoading.value = false;
      }
    }

    function getCommonData() {
      return {
        spanLength: bridgeData.spanLength,
        ballastThickness: bridgeData.ballastThickness,
        trackType: bridgeData.trackType,
        sleeperType: bridgeData.sleeperType,
        concreteStrengthR: bridgeData.concreteStrengthR,
        distanceBetweenBeams: bridgeData.distanceBetweenBeams,
        trackOffsetLeft: bridgeData.trackOffsetLeft,
        trackOffsetRight: bridgeData.trackOffsetRight,
        mBeams: bridgeData.mBeams,
        rebarType: bridgeData.rebarType,
        designYear: bridgeData.designYear,
        loadType: bridgeData.loadType
      };
    }

    // ==========================================================
    // 4. РАЗДЕЛ 5: МАТЕРИАЛЫ
    // ==========================================================
    const materialsResult = ref(null);

    async function calculateMaterials() {
      if (!isPassportFilled.value) { error.value = 'Сначала заполните паспорт'; return; }
      materialsResult.value = null;
      showDetailedReport.value = false;
      const data = await api('/api/v1/materials/calculate', {
        method: 'POST',
        body: JSON.stringify({
          concreteStrengthR: bridgeData.concreteStrengthR,
          rebarType: bridgeData.rebarType
        })
      });
      if (data) {
        materialsResult.value = data;
        localStorage.setItem('materialsResult', JSON.stringify(data));
      }
    }

    function toggleDetailedReport() {
      showDetailedReport.value = !showDetailedReport.value;
    }

    // ==========================================================
    // 5. РАЗДЕЛ 6.1-6.3: ПОСТОЯННЫЕ НАГРУЗКИ
    // ==========================================================
    const loadsPermForm = reactive({ hSlab: 0.26, vConcrete: 30.6, pDevices: 0, sBallast: 2.06, mBeams: 2 });
    const loadsPermResult = ref(null);
    const showPermReport = ref(false);

    async function calculatePermanentLoads() {
      if (!isPassportFilled.value) { error.value = 'Сначала заполните паспорт'; return; }
      loadsPermResult.value = null; showPermReport.value = false;
      const data = await api('/api/v1/loads/permanent', {
        method: 'POST',
        body: JSON.stringify({ commonData: getCommonData(), ...loadsPermForm })
      });
      if (data) {
        loadsPermResult.value = data;
        localStorage.setItem('loadsPermResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 6. РАЗДЕЛ 6.4: ДИНАМИЧЕСКИЙ КОЭФФИЦИЕНТ
    // ==========================================================
    const loadsDynBeamForm = reactive({ lambda: 10.8 });
    const loadsDynSlabForm = reactive({ useMaxCoefficient: true, lambda: 2.0 });

    const loadsDynBeamResult = ref(null);
    const loadsDynSlabResult = ref(null);
    const showDynBeamReport = ref(false);
    const showDynSlabReport = ref(false);

    async function calculateDynamicCoeffBeam() {
      if (!isPassportFilled.value) { error.value = 'Сначала заполните паспорт'; return; }
      loadsDynBeamResult.value = null; showDynBeamReport.value = false;
      const data = await api('/api/v1/loads/dynamic-coeff', {
        method: 'POST',
        body: JSON.stringify({
          commonData: getCommonData(),
          elementName: 'ГЛАВНАЯ БАЛКА',
          useMaxCoefficient: false,
          lambda: loadsDynBeamForm.lambda
        })
      });
      if (data) {
        loadsDynBeamResult.value = data;
        localStorage.setItem('loadsDynBeamResult', JSON.stringify(data));
      }
    }

    async function calculateDynamicCoeffSlab() {
      if (!isPassportFilled.value) { error.value = 'Сначала заполните паспорт'; return; }
      loadsDynSlabResult.value = null; showDynSlabReport.value = false;
      const data = await api('/api/v1/loads/dynamic-coeff', {
        method: 'POST',
        body: JSON.stringify({
          commonData: getCommonData(),
          elementName: 'ПЛИТА БАЛЛАСТНОГО КОРЫТА',
          useMaxCoefficient: loadsDynSlabForm.useMaxCoefficient,
          lambda: loadsDynSlabForm.lambda
        })
      });
      if (data) {
        loadsDynSlabResult.value = data;
        localStorage.setItem('loadsDynSlabResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 7. РАЗДЕЛ 6.6-6.7: ДОЛИ ВРЕМЕННОЙ НАГРУЗКИ
    // ==========================================================
    const loadsShareForm = reactive({ isMonolithic: true, xRatio: 0.5 });
    const loadsShareResult = ref(null);
    const showShareReport = ref(false);

    async function calculateShare() {
      if (!isPassportFilled.value) { error.value = 'Сначала заполните паспорт'; return; }
      loadsShareResult.value = null; showShareReport.value = false;
      const data = await api('/api/v1/loads/share', {
        method: 'POST',
        body: JSON.stringify({ commonData: getCommonData(), ...loadsShareForm })
      });
      if (data) {
        loadsShareResult.value = data;
        localStorage.setItem('loadsShareResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 8. РАЗДЕЛ 7: ПЛИТА НА ПРОЧНОСТЬ
    // ==========================================================
    const slabStrengthForm = reactive({
      slabHeight: 0.26,
      asTensile: 0.026,
      asCompressive: 0.026,
      asTensileArea: 0.000905,
      asCompressiveArea: 0.000452,
      lp: 1.2,
      B: 2.4,
      ls: 2.7,
      lbPrime: 1.05,
      lbDoubleprime: 1.05,
      hbPrime: 0.35,
      hbDoubleprime: 0.35,
      mpMonolithic: 0,
      mpExternalCantilever: 12.5
    });

    const slabStrengthResult = ref(null);
    const showSlabStrengthReport = ref(false);

    const slabReadiness = computed(() => {
      const missing = [];
      if (!materialsResult.value) missing.push('Материалы');
      if (!loadsPermResult.value) missing.push('Пост. нагрузки');
      if (!loadsDynSlabResult.value) missing.push('Динамика');
      return { missing, ready: missing.length === 0 };
    });

    async function calculateSlabStrength() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт объекта';
        return;
      }

      // Проверка зависимостей
      const missing = [];
      if (!materialsResult.value) missing.push('• Раздел 5: Характеристики материалов');
      if (!loadsPermResult.value) missing.push('• Раздел 6.1-6.3: Постоянные нагрузки');
      if (!loadsDynSlabResult.value) missing.push('• Раздел 6.4: Динамический коэффициент для плиты');

      if (missing.length > 0) {
        alert(
          '⚠️ Для расчёта плиты необходимо сначала выполнить:\n\n' +
          missing.join('\n') +
          '\n\nПожалуйста, перейдите в соответствующие разделы и выполните расчёты.'
        );
        return;
      }

      slabStrengthResult.value = null;
      showSlabStrengthReport.value = false;

      const loadsForSlab = {
        gammaReinforcedConcrete: loadsPermResult.value.gammaReinforcedConcrete,
        gammaBallastWithTrack: loadsPermResult.value.gammaBallastWithTrack,
        ppSlab: loadsPermResult.value.ppSlab,
        pbSlab: loadsPermResult.value.pbSlab,
        np: loadsPermResult.value.np,
        npPrime: loadsPermResult.value.npPrime,
        nk: loadsPermResult.value.nk,
        dynamicCoeffSlab: loadsDynSlabResult.value.dynamicCoeff
      };

      const data = await api('/api/v1/slab/strength', {
        method: 'POST',
        body: JSON.stringify({
          commonData: getCommonData(),
          materials: materialsResult.value,
          loads: loadsForSlab,
          ...slabStrengthForm
        })
      });

      if (data) {
        slabStrengthResult.value = data;
        localStorage.setItem('slabStrengthResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 9. РАЗДЕЛ 7.2.4: ПЛИТА ПО ПОПЕРЕЧНОЙ СИЛЕ
    // ==========================================================
    const slabShearForm = reactive({ P0: 0.65, pt: 0.25, lt: 2.7, lk: 1.05 });
    const slabShearResult = ref(null);
    const showSlabShearReport = ref(false);

    const slabShearReadiness = computed(() => {
      const missing = [];
      if (!materialsResult.value) missing.push('Материалы');
      if (!loadsPermResult.value) missing.push('Пост. нагрузки');
      if (!loadsDynSlabResult.value) missing.push('Динамика');
      return { missing, ready: missing.length === 0 };
    });

    async function calculateSlabShear() {
      if (!slabShearReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчеты: ' + slabShearReadiness.value.missing.join(', '));
        return;
      }
      slabShearResult.value = null; showSlabShearReport.value = false;

      const loadsForSlab = {
        gammaReinforcedConcrete: loadsPermResult.value.gammaReinforcedConcrete,
        gammaBallastWithTrack: loadsPermResult.value.gammaBallastWithTrack,
        ppSlab: loadsPermResult.value.ppSlab,
        pbSlab: loadsPermResult.value.pbSlab,
        np: loadsPermResult.value.np,
        npPrime: loadsPermResult.value.npPrime,
        nk: loadsPermResult.value.nk,
        dynamicCoeffSlab: loadsDynSlabResult.value.dynamicCoeff
      };

      const data = await api('/api/v1/slab/shear', {
        method: 'POST',
        body: JSON.stringify({
          commonData: getCommonData(),
          materials: materialsResult.value,
          loads: loadsForSlab,
          ...slabStrengthForm,   // ПЕРЕДАЁМ ГЕОМЕТРИЮ!
          ...slabShearForm       // ПЕРЕДАЁМ СПЕЦИФИКУ ПОПЕРЕЧКИ
        })
      });
      if (data) {
        slabShearResult.value = data;
        localStorage.setItem('slabShearResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 10. РАЗДЕЛ 7.3.1: ПЛИТА НА ВЫНОСЛИВОСТЬ
    // ==========================================================
    const slabFatigueResult = ref(null);
    const showSlabFatigueReport = ref(false);

    const slabFatigueReadiness = computed(() => {
      const missing = [];
      if (!slabStrengthResult.value) missing.push('Прочность плиты (7.2)');
      return { missing, ready: missing.length === 0 };
    });

    async function calculateSlabFatigue() {
      if (!slabFatigueReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчет плиты на прочность (7.2)');
        return;
      }
      slabFatigueResult.value = null; showSlabFatigueReport.value = false;

      const loadsForSlab = {
        gammaReinforcedConcrete: loadsPermResult.value.gammaReinforcedConcrete,
        gammaBallastWithTrack: loadsPermResult.value.gammaBallastWithTrack,
        ppSlab: loadsPermResult.value.ppSlab,
        pbSlab: loadsPermResult.value.pbSlab,
        np: loadsPermResult.value.np,
        npPrime: loadsPermResult.value.npPrime,
        nk: loadsPermResult.value.nk,
        dynamicCoeffSlab: loadsDynSlabResult.value.dynamicCoeff
      };

      const data = await api('/api/v1/slab/fatigue', {
        method: 'POST',
        body: JSON.stringify({
          commonData: getCommonData(),
          materials: materialsResult.value,
          loads: loadsForSlab,
          ...slabStrengthForm    // ПЕРЕДАЁМ ГЕОМЕТРИЮ!
        })
      });
      if (data) {
        slabFatigueResult.value = data;
        localStorage.setItem('slabFatigueResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 14. РАЗДЕЛ 14: УСИЛЕНИЕ УГЛЕВОЛОКНОМ (ТАБЛИЦА)
    // ==========================================================
    const strengtheningSchemes = ref([]);

    async function loadStrengtheningSchemes() {
      try {
        const data = await api('/api/v1/carbonRecs/schemes');
        if (data) {
          strengtheningSchemes.value = data;
        }
      } catch (e) {
        error.value = e.message;
      }
    }

    // ==========================================================
    // 15. РАЗДЕЛ 15: ОБСЛЕДОВАНИЕ И ИСПЫТАНИЕ
    // ==========================================================
    const inspectionForm = reactive({
      aPrime: null,
      bPrime: null,
      b0Prime: null,
      concreteStrengthInput: '',
      deflectionsInput: '',
      momentsInput: '',
      targetBeamIndex: 0
    });
    const inspectionResult = ref(null);
    const showInspectionReport = ref(false);

    async function calculateInspection() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт объекта';
        return;
      }

      // Парсинг массивов из строк
      const parseArray = (str) => {
        return str.split(',').map(s => parseFloat(s.trim())).filter(n => !isNaN(n));
      };

      const concreteStrengths = parseArray(inspectionForm.concreteStrengthInput);
      const deflections = parseArray(inspectionForm.deflectionsInput);
      const inertias = parseArray(inspectionForm.momentsInput);

      // Валидация
      if (inspectionForm.aPrime === null || inspectionForm.bPrime === null || inspectionForm.b0Prime === null) {
        error.value = 'Заполните все поля для формулы 15.3';
        return;
      }
      if (concreteStrengths.length === 0) {
        error.value = 'Введите хотя бы одно измерение прочности бетона';
        return;
      }

      // ВАЖНО: количество балок берётся из паспорта
      const mBeams = bridgeData.mBeams;

      if (deflections.length !== mBeams) {
        error.value = `Количество прогибов должно быть равно числу балок (${mBeams}). Введите ${mBeams} значений через запятую.`;
        return;
      }
      if (inertias.length !== mBeams) {
        error.value = `Количество моментов инерции должно быть равно числу балок (${mBeams}). Введите ${mBeams} значений через запятую.`;
        return;
      }
      if (inspectionForm.targetBeamIndex < 0 || inspectionForm.targetBeamIndex >= mBeams) {
        error.value = `Индекс целевой балки должен быть от 0 до ${mBeams - 1}`;
        return;
      }

      isLoading.value = true;
      error.value = '';
      inspectionResult.value = null;
      showInspectionReport.value = false;

      try {
        const data = await api('/api/v1/inspection/calculate', {
          method: 'POST',
          body: JSON.stringify({
            aPrime: inspectionForm.aPrime,
            bPrime: inspectionForm.bPrime,
            b0Prime: inspectionForm.b0Prime,
            concreteStrengths: concreteStrengths,
            deflections: deflections,
            inertias: inertias,
            targetBeamIndex: inspectionForm.targetBeamIndex
          })
        });

        if (data) {
          inspectionResult.value = data;
          localStorage.setItem('inspectionResult', JSON.stringify(data));
        }
      } catch (e) {
        error.value = e.message;
      } finally {
        isLoading.value = false;
      }
    }

    // ==========================================================
    // 11. УТИЛИТЫ
    // ==========================================================
    function formatNum(val, digits = 2) {
      if (val === null || val === undefined || val === '') return '—';
      return Number(val).toFixed(digits);
    }

    function parseReport(text) {
      if (!text) return '';
      const lines = text.split('\n');
      let html = '';
      let inSection = false;

      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed.startsWith('====')) continue;

        if (trimmed === '') {
          if (inSection) { html += '</div>'; inSection = false; }
          continue;
        }

        if (trimmed.match(/^\[\d+\./)) {
          if (inSection) html += '</div>';
          const cleanTitle = trimmed.replace(/^\[|\]$/g, '');
          html += `<div class="report-section"><h4 class="report-section-title">${escapeHtml(cleanTitle)}</h4>`;
          inSection = true;
          continue;
        }

        html += `<p class="report-line">${escapeHtml(trimmed)}</p>`;
      }
      if (inSection) html += '</div>';
      return html;
    }

    function escapeHtml(text) {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    }

    // ==========================================================
    // 12. ИНИЦИАЛИЗАЦИЯ И ВОЗВРАТ
    // ==========================================================
    onMounted(() => {
      showPassportForm.value = false;
      showDetailedReport.value = false;

      loadPassport();

      if (isPassportFilled.value) {
          loadStrengtheningSchemes();
      }

      const safeParse = (key) => {
        try {
          const saved = localStorage.getItem(key);
          return saved ? JSON.parse(saved) : null;
        } catch (e) {
          console.error(`Ошибка загрузки ${key}:`, e);
          return null;
        }
      };

      materialsResult.value = safeParse('materialsResult');
      loadsPermResult.value = safeParse('loadsPermResult');
      loadsDynBeamResult.value = safeParse('loadsDynBeamResult');
      loadsDynSlabResult.value = safeParse('loadsDynSlabResult');
      loadsShareResult.value = safeParse('loadsShareResult');
      slabStrengthResult.value = safeParse('slabStrengthResult');
      slabShearResult.value = safeParse('slabShearResult');
      slabFatigueResult.value = safeParse('slabFatigueResult');
      strengtheningResult.value = safeParse('strengtheningResult');
      inspectionResult.value = safeParse('inspectionResult');
    });

    return {
      // Навигация
      page, sections, currentSectionIndex, currentSectionTitle,
      hasPrevSection, hasNextSection,
      goToPrevSection, goToNextSection, goToSection,
      // Общие
      isLoading, error, showPassportForm, showDetailedReport,
      bridgeData, isPassportFilled,
      trackTypeName, sleeperTypeName, rebarTypeName,
      savePassport, getCommonData,
      // Раздел 5
      materialsResult, calculateMaterials, toggleDetailedReport, parseReport,
      // Раздел 6.1-6.3
      loadsPermForm, loadsPermResult, showPermReport, calculatePermanentLoads,
      // Раздел 6.4
      loadsDynBeamForm, loadsDynBeamResult, showDynBeamReport, calculateDynamicCoeffBeam,
      loadsDynSlabForm, loadsDynSlabResult, showDynSlabReport, calculateDynamicCoeffSlab,
      // Раздел 6.6-6.7
      loadsShareForm, loadsShareResult, showShareReport, calculateShare,
      // Раздел 7: Прочность
      slabStrengthForm, slabStrengthResult, showSlabStrengthReport, calculateSlabStrength,
      slabReadiness,
      // Раздел 7: Поперечная сила и выносливость
      slabShearForm, slabShearResult, showSlabShearReport, calculateSlabShear, slabShearReadiness,
      slabFatigueResult, showSlabFatigueReport, calculateSlabFatigue, slabFatigueReadiness,
      // Раздел 14
      strengtheningSchemes, loadStrengtheningSchemes,
      // Раздел 15
      inspectionForm, inspectionResult, showInspectionReport, calculateInspection,
      // Утилиты
      formatNum
    };
  }
});

app.mount('#app');