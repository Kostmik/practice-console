const { createApp, ref, reactive, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    const page = ref('home');
    const isLoading = ref(false);
    const error = ref('');
    const showPassportForm = ref(false);
    const showDetailedReport = ref(false);

    // 🎯 ПОЛНЫЙ ПАСПОРТ ОБЪЕКТА
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

        // Пропускаем разделители ===
        if (trimmed.startsWith('====')) continue;

        // Пустые строки
        if (trimmed === '') {
          if (inSection) {
            html += '</div>';
            inSection = false;
          }
          continue;
        }

        // Заголовки секций [1. ...], [2. ...] и т.д.
        if (trimmed.match(/^\[\d+\./)) {
          if (inSection) html += '</div>';
          // Убираем квадратные скобки
          const cleanTitle = trimmed.replace(/^\[|\]$/g, '');
          html += `<div class="report-section">`;
          html += `<h4 class="report-section-title">${escapeHtml(cleanTitle)}</h4>`;
          inSection = true;
          continue;
        }

        // Обычный текст (формулы, пояснения)
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

    // Функция для формирования полного commonData для запросов
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

    onMounted(() => {
      console.log('Vue приложение загружено');
      loadPassport();
    });

    return {
      page,
      isLoading,
      error,
      showPassportForm,
      showDetailedReport,
      bridgeData,
      isPassportFilled,
      trackTypeName,
      sleeperTypeName,
      rebarTypeName,
      savePassport,
      materialsResult,
      calculateMaterials,
      toggleDetailedReport,
      parseReport,
      getCommonData
    };
  }
});

app.mount('#app');