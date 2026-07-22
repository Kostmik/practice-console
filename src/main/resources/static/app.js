const { createApp, ref, reactive, computed, watch, onMounted } = Vue;

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

      const keysToClear = [
        'materialsResult', 'loadsPermResult', 'loadsDynBeamResult', 'loadsDynSlabResult',
        'loadsShareResult', 'slabStrengthResult', 'slabShearResult', 'slabFatigueResult',
        'beamMomentResult', 'beamShearResult', 'beamFatigueResult',
        'inspection153Result', 'inspection154Result', 'inspection155Result'
      ];
      keysToClear.forEach(key => localStorage.removeItem(key));

      materialsResult.value = null;
      loadsPermResult.value = null;
      loadsDynBeamResult.value = null;
      loadsDynSlabResult.value = null;
      loadsShareResult.value = null;
      slabStrengthResult.value = null;
      slabShearResult.value = null;
      slabFatigueResult.value = null;
      beamMomentResult.value = null;
      beamShearResult.value = null;
      beamFatigueResult.value = null;
      inspection153Result.value = null;
      inspection154Result.value = null;
      inspection155Result.value = null;

      showPassportForm.value = false;
      alert('✅ Паспорт обновлён!\n\nВнимание: предыдущие результаты расчётов автоматически сброшены.');
    }

    function loadPassport() {
      try {
        const saved = localStorage.getItem('bridgePassport');
        if (saved) {
          Object.assign(bridgeData, {
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
            loadType: 'Н7',
            ...JSON.parse(saved)
          });
        }
      } catch (e) {
        console.error('Ошибка загрузки паспорта:', e);
        localStorage.removeItem('bridgePassport');
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
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
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
    // 5. РАЗДЕЛ 6: НАГРУЗКИ
    // ==========================================================
    const loadsPermForm = reactive({ hSlab: 0.26, vConcrete: 30.6, pDevices: 0, sBallast: 2.06, mBeams: 2 });
    const loadsPermResult = ref(null);
    const showPermReport = ref(false);

    async function calculatePermanentLoads() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      loadsPermResult.value = null;
      showPermReport.value = false;
      const data = await api('/api/v1/loads/permanent', {
        method: 'POST',
        body: JSON.stringify({ commonData: getCommonData(), ...loadsPermForm })
      });
      if (data) {
        loadsPermResult.value = data;
        localStorage.setItem('loadsPermResult', JSON.stringify(data));
      }
    }

    const loadsDynBeamForm = reactive({ lambda: 10.8 });
    const loadsDynSlabForm = reactive({ useMaxCoefficient: true, lambda: 2.0 });
    const loadsDynBeamResult = ref(null);
    const loadsDynSlabResult = ref(null);
    const showDynBeamReport = ref(false);
    const showDynSlabReport = ref(false);

    async function calculateDynamicCoeffBeam() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      loadsDynBeamResult.value = null;
      showDynBeamReport.value = false;
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
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      loadsDynSlabResult.value = null;
      showDynSlabReport.value = false;
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

    const loadsShareForm = reactive({ isMonolithic: true, xRatio: 0.5 });
    const loadsShareResult = ref(null);
    const showShareReport = ref(false);

    async function calculateShare() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      loadsShareResult.value = null;
      showShareReport.value = false;
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
    // 6. РАЗДЕЛ 7: ПЛИТА (ОБЪЕДИНЁННАЯ ФОРМА, КАК У БАЛКИ)
    // ==========================================================
    const slabForm = reactive({
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
      mpExternalCantilever: 12.5,
      P0: 0.65,
      pt: 0.25,
      lt: 2.7,
      lk: 1.05
    });

    const slabStrengthResult = ref(null);
    const showSlabStrengthReport = ref(false);
    const slabShearResult = ref(null);
    const showSlabShearReport = ref(false);
    const slabFatigueResult = ref(null);
    const showSlabFatigueReport = ref(false);

    const slabReadiness = computed(() => {
      const missing = [];
      if (!materialsResult.value) missing.push('Материалы');
      if (!loadsPermResult.value) missing.push('Пост. нагрузки');
      if (!loadsDynSlabResult.value) missing.push('Динамика');
      return { missing, ready: missing.length === 0 };
    });

    const slabFatigueReadiness = computed(() => {
      const missing = [];
      if (!slabStrengthResult.value) missing.push('Прочность плиты');
      return { missing, ready: missing.length === 0 };
    });

    async function calculateSlabStrength() {
      if (!slabReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчеты: ' + slabReadiness.value.missing.join(', '));
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
          ...slabForm
        })
      });
      if (data) {
        slabStrengthResult.value = data;
        localStorage.setItem('slabStrengthResult', JSON.stringify(data));
      }
    }

    async function calculateSlabShear() {
      if (!slabReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчеты: ' + slabReadiness.value.missing.join(', '));
        return;
      }
      slabShearResult.value = null;
      showSlabShearReport.value = false;
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
          ...slabForm
        })
      });
      if (data) {
        slabShearResult.value = data;
        localStorage.setItem('slabShearResult', JSON.stringify(data));
      }
    }

    async function calculateSlabFatigue() {
      if (!slabFatigueReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчет плиты на прочность');
        return;
      }
      slabFatigueResult.value = null;
      showSlabFatigueReport.value = false;
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
          ...slabForm
        })
      });
      if (data) {
        slabFatigueResult.value = data;
        localStorage.setItem('slabFatigueResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 7. РАЗДЕЛ 7: ГЛАВНАЯ БАЛКА
    // ==========================================================
    const beamForm = reactive({
      beamHeight: 5,
      beamWidth: 0.35,
      bf: 2.4,
      hf: 0.26,
      asBeamTensile: 0.07,
      asBeamCompressive: 0.05,
      asBeamTensileArea: 0.003079,
      asBeamCompressiveArea: 0.000942,
      asw: 0.000201,
      sStirrups: 0.15,
      sumAsi: 0.001257,
      alphaBent: 45,
      omegaP: 14.58,
      omegaK: 7.29
    });

    const beamMomentResult = ref(null);
    const beamShearResult = ref(null);
    const beamFatigueResult = ref(null);
    const showBeamMomentReport = ref(false);
    const showBeamShearReport = ref(false);
    const showBeamFatigueReport = ref(false);

    const beamReadiness = computed(() => {
      const missing = [];
      if (!materialsResult.value) missing.push('Материалы');
      if (!loadsPermResult.value) missing.push('Пост. нагрузки');
      if (!loadsDynBeamResult.value) missing.push('Динамика балки');
      if (!loadsShareResult.value) missing.push('Доли нагрузки');
      return { missing, ready: missing.length === 0 };
    });

    const beamFatigueReadiness = computed(() => {
      const missing = [];
      if (!beamMomentResult.value) missing.push('Момент балки');
      return { missing, ready: missing.length === 0 };
    });

    function getBeamRequestData() {
      return {
        commonData: getCommonData(),
        materials: materialsResult.value,
        ppBeam: loadsPermResult.value ? loadsPermResult.value.ppBeam : null,
        pbBeam: loadsPermResult.value ? loadsPermResult.value.pbBeam : null,
        np: loadsPermResult.value ? loadsPermResult.value.np : null,
        npPrime: loadsPermResult.value ? loadsPermResult.value.npPrime : null,
        nk: loadsPermResult.value ? loadsPermResult.value.nk : null,
        epsilonM: loadsShareResult.value ? loadsShareResult.value.epsilonM_Beam1 : null,
        epsilonQ: loadsShareResult.value ? loadsShareResult.value.epsilonQ_Beam1 : null,
        dynamicCoeffBeam: loadsDynBeamResult.value ? loadsDynBeamResult.value.dynamicCoeff : null,
        ...beamForm
      };
    }

    async function calculateBeamMoment() {
      if (!beamReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчеты: ' + beamReadiness.value.missing.join(', '));
        return;
      }
      beamMomentResult.value = null;
      showBeamMomentReport.value = false;
      const data = await api('/api/v1/beam/moment', {
        method: 'POST',
        body: JSON.stringify(getBeamRequestData())
      });
      if (data) {
        beamMomentResult.value = data;
        localStorage.setItem('beamMomentResult', JSON.stringify(data));
      }
    }

    async function calculateBeamShear() {
      if (!beamReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчеты: ' + beamReadiness.value.missing.join(', '));
        return;
      }
      beamShearResult.value = null;
      showBeamShearReport.value = false;
      const data = await api('/api/v1/beam/shear', {
        method: 'POST',
        body: JSON.stringify(getBeamRequestData())
      });
      if (data) {
        beamShearResult.value = data;
        localStorage.setItem('beamShearResult', JSON.stringify(data));
      }
    }

    async function calculateBeamFatigue() {
      if (!beamFatigueReadiness.value.ready) {
        alert('⚠️ Сначала выполните расчет балки по моменту');
        return;
      }
      beamFatigueResult.value = null;
      showBeamFatigueReport.value = false;
      const data = await api('/api/v1/beam/fatigue', {
        method: 'POST',
        body: JSON.stringify(getBeamRequestData())
      });
      if (data) {
        beamFatigueResult.value = data;
        localStorage.setItem('beamFatigueResult', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 8. РАЗДЕЛ 14: УСИЛЕНИЕ
    // ==========================================================
    const strengtheningSchemes = ref([
      { number: 1, description: 'Холст на нижней грани', increasePercent: 3 },
      { number: 2, description: 'То же, но с устройством пологих обойм', increasePercent: 5 },
      { number: 3, description: 'То же, но с устройством вертикальных обойм', increasePercent: 7 },
      { number: 4, description: 'Холст на нижней грани "до опор"', increasePercent: 7 },
      { number: 5, description: 'U-образная обойма', increasePercent: 26 },
      { number: 6, description: 'Ламель на нижней грани с устройством вертикальных обойм', increasePercent: 11 },
      { number: 7, description: 'То же, но с устройством дополнительных обойм', increasePercent: 22 }
    ]);

    // ==========================================================
    // 9. РАЗДЕЛ 15: ОБСЛЕДОВАНИЕ
    // ==========================================================
    const inspection153Form = reactive({
      aPrime: 0.150,
      bPrime: 0.100,
      b0Prime: 1.520
    });
    const inspection153Result = ref(null);
    const showInspection153Report = ref(false);

    async function calculateInspection153() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      error.value = '';
      inspection153Result.value = null;
      showInspection153Report.value = false;
      const data = await api('/api/v1/inspection/153', {
        method: 'POST',
        body: JSON.stringify({
          aPrime: inspection153Form.aPrime,
          bPrime: inspection153Form.bPrime,
          b0Prime: inspection153Form.b0Prime
        })
      });
      if (data) {
        inspection153Result.value = data;
        localStorage.setItem('inspection153Result', JSON.stringify(data));
      }
    }

    const inspection154Form = reactive({
      strengths: [23.5]
    });
    const inspection154Result = ref(null);
    const showInspection154Report = ref(false);

    function addStrength() {
      inspection154Form.strengths.push(23.5);
    }

    function removeStrength(index) {
      if (inspection154Form.strengths.length > 1) {
        inspection154Form.strengths.splice(index, 1);
      }
    }

    async function calculateInspection154() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      error.value = '';
      const validStrengths = inspection154Form.strengths
        .map(s => parseFloat(s))
        .filter(n => !isNaN(n) && n > 0);

      if (validStrengths.length === 0) {
        error.value = 'Введите хотя бы одно корректное значение';
        return;
      }

      inspection154Result.value = null;
      showInspection154Report.value = false;
      const data = await api('/api/v1/inspection/154', {
        method: 'POST',
        body: JSON.stringify({ concreteStrengths: validStrengths })
      });
      if (data) {
        inspection154Result.value = data;
        localStorage.setItem('inspection154Result', JSON.stringify(data));
      }
    }

    const inspection155Form = reactive({
      deflections: [],
      inertias: [],
      targetBeamIndex: 0
    });
    const inspection155Result = ref(null);
    const showInspection155Report = ref(false);

    watch(() => bridgeData.mBeams, (newCount) => {
      while (inspection155Form.deflections.length < newCount) {
        inspection155Form.deflections.push(5.0);
        inspection155Form.inertias.push(0.125);
      }
      inspection155Form.deflections.length = newCount;
      inspection155Form.inertias.length = newCount;
      if (inspection155Form.targetBeamIndex >= newCount) {
        inspection155Form.targetBeamIndex = 0;
      }
    }, { immediate: true });

    async function calculateInspection155() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт';
        return;
      }
      error.value = '';
      const validDeflections = inspection155Form.deflections.map(d => parseFloat(d)).filter(n => !isNaN(n));
      const validInertias = inspection155Form.inertias.map(i => parseFloat(i)).filter(n => !isNaN(n));

      if (validDeflections.length !== bridgeData.mBeams || validInertias.length !== bridgeData.mBeams) {
        error.value = `Заполните все ${bridgeData.mBeams} полей`;
        return;
      }

      inspection155Result.value = null;
      showInspection155Report.value = false;
      const data = await api('/api/v1/inspection/155', {
        method: 'POST',
        body: JSON.stringify({
          deflections: validDeflections,
          inertias: validInertias,
          targetBeamIndex: inspection155Form.targetBeamIndex
        })
      });
      if (data) {
        inspection155Result.value = data;
        localStorage.setItem('inspection155Result', JSON.stringify(data));
      }
    }

    // ==========================================================
    // 10. УТИЛИТЫ
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
    // 11. ИНИЦИАЛИЗАЦИЯ И ВОЗВРАТ
    // ==========================================================
    onMounted(() => {
      showPassportForm.value = false;
      showDetailedReport.value = false;
      loadPassport();

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
      beamMomentResult.value = safeParse('beamMomentResult');
      beamShearResult.value = safeParse('beamShearResult');
      beamFatigueResult.value = safeParse('beamFatigueResult');
      inspection153Result.value = safeParse('inspection153Result');
      inspection154Result.value = safeParse('inspection154Result');
      inspection155Result.value = safeParse('inspection155Result');
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
      // Раздел 6
      loadsPermForm, loadsPermResult, showPermReport, calculatePermanentLoads,
      loadsDynBeamForm, loadsDynBeamResult, showDynBeamReport, calculateDynamicCoeffBeam,
      loadsDynSlabForm, loadsDynSlabResult, showDynSlabReport, calculateDynamicCoeffSlab,
      loadsShareForm, loadsShareResult, showShareReport, calculateShare,
      // Раздел 7: Плита (объединённая)
      slabForm, slabReadiness, slabFatigueReadiness,
      slabStrengthResult, showSlabStrengthReport, calculateSlabStrength,
      slabShearResult, showSlabShearReport, calculateSlabShear,
      slabFatigueResult, showSlabFatigueReport, calculateSlabFatigue,
      // Раздел 7: Балка
      beamForm, beamReadiness, beamFatigueReadiness,
      beamMomentResult, showBeamMomentReport, calculateBeamMoment,
      beamShearResult, showBeamShearReport, calculateBeamShear,
      beamFatigueResult, showBeamFatigueReport, calculateBeamFatigue,
      // Раздел 14
      strengtheningSchemes,
      // Раздел 15
      inspection153Form, inspection153Result, showInspection153Report, calculateInspection153,
      inspection154Form, inspection154Result, showInspection154Report, calculateInspection154,
      addStrength, removeStrength,
      inspection155Form, inspection155Result, showInspection155Report, calculateInspection155,
      // Утилиты
      formatNum
    };
  }
});

app.mount('#app');