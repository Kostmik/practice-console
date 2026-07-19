const { createApp, ref, reactive, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    const page = ref('home');
    const isLoading = ref(false);
    const error = ref('');
    const showPassportForm = ref(false);
    const showDetailedReport = ref(false);

    // === НАВИГАЦИЯ ПО РАЗДЕЛАМ ===
    const sections = [
      { key: 'materials', title: '5. Материалы' },
      { key: 'loads', title: '6. Нагрузки' },
      { key: 'slab', title: '7. Плита' },
      { key: 'beam', title: '7. Балка' }
      // В будущем сюда добавятся:
      // { key: 'slabFatigue', title: '7.3. Выносливость плиты' },
      // { key: 'beamFatigue', title: '7.3. Выносливость балки' },
      // { key: 'norms', title: '8. Сопоставление норм' },
      // и т.д.
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
        page.value = sections[currentSectionIndex.value - 1].key;
      }
    }

    function goToNextSection() {
      if (hasNextSection.value) {
        page.value = sections[currentSectionIndex.value + 1].key;
      }
    }

    function goToSection(key) {
      page.value = key;
    }

    // 🎯 ПОЛНЫЙ ПАСПОРТ ОБЪЕКТА (12 полей)
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
      showPassportForm.value = false;
      console.log('Паспорт сохранён');
    }

    function loadPassport() {
      try {
        const saved = localStorage.getItem('bridgePassport');
        if (saved) {
          const data = JSON.parse(saved);
          Object.assign(bridgeData, data);
          console.log('Паспорт загружен из localStorage');
        }
      } catch (e) {
        console.error('Ошибка загрузки паспорта:', e);
      }
    }

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

    // === РАЗДЕЛ 5: МАТЕРИАЛЫ ===
    const materialsResult = ref(null);

    async function calculateMaterials() {
      if (!isPassportFilled.value) {
        error.value = 'Сначала заполните паспорт объекта';
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
      if (data) materialsResult.value = data;
    }

    function toggleDetailedReport() {
      showDetailedReport.value = !showDetailedReport.value;
    }

    // Парсинг текстового отчёта в структурированный HTML
    function parseReport(text) {
      if (!text) return '';

      const lines = text.split('\n');
      let html = '';
      let inSection = false;

      for (const line of lines) {
        const trimmed = line.trim();

        if (trimmed.startsWith('====')) continue;

        if (trimmed === '') {
          if (inSection) {
            html += '</div>';
            inSection = false;
          }
          continue;
        }

        if (trimmed.match(/^\[\d+\./)) {
          if (inSection) html += '</div>';
          const cleanTitle = trimmed.replace(/^\[|\]$/g, '');
          html += `<div class="report-section">`;
          html += `<h4 class="report-section-title">${escapeHtml(cleanTitle)}</h4>`;
          inSection = true;
          continue;
        }

        const content = escapeHtml(trimmed);
        html += `<p class="report-line">${content}</p>`;
      }

      if (inSection) html += '</div>';
      return html;
    }

    function escapeHtml(text) {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
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

    // === РАЗДЕЛ 6.1-6.3: ПОСТОЯННЫЕ НАГРУЗКИ ===
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
      if (data) loadsPermResult.value = data;
    }

    // === РАЗДЕЛ 6.4: ДИНАМИЧЕСКИЙ КОЭФФИЦИЕНТ (разделён на балку и плиту) ===
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
      if (data) loadsDynBeamResult.value = data;
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
      if (data) loadsDynSlabResult.value = data;
    }

    // === РАЗДЕЛ 6.6-6.7: ДОЛИ ВРЕМЕННОЙ НАГРУЗКИ ===
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
      if (data) loadsShareResult.value = data;
    }

    // === ФУНКЦИЯ ФОРМАТИРОВАНИЯ ЧИСЕЛ ===
    function formatNum(val, digits = 2) {
      if (val === null || val === undefined || val === '') return '—';
      return Number(val).toFixed(digits);
    }

    onMounted(() => {
      console.log('Vue приложение загружено');
      loadPassport();
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
      materialsResult, calculateMaterials,
      toggleDetailedReport, parseReport,
      // Раздел 6.1-6.3
      loadsPermForm, loadsPermResult, showPermReport, calculatePermanentLoads,
      // Раздел 6.4 (балка и плита раздельно)
      loadsDynBeamForm, loadsDynBeamResult, showDynBeamReport, calculateDynamicCoeffBeam,
      loadsDynSlabForm, loadsDynSlabResult, showDynSlabReport, calculateDynamicCoeffSlab,
      // Раздел 6.6-6.7
      loadsShareForm, loadsShareResult, showShareReport, calculateShare,
      // Утилиты
      formatNum
    };
  }
});

app.mount('#app');