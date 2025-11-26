document.addEventListener('DOMContentLoaded', () => {
    const timeInput = document.getElementById('timeInput');
    if (!timeInput) return;

    // 기본값: 전체
    timeInput.innerHTML = '<option value="">전체</option>';

    for (let h = 0; h < 24; h++) {
        const start = String(h).padStart(2, '0') + ':00';
        const end = String((h + 1) % 24).padStart(2, '0') + ':00';
        const label = `${start}~${end}`;
        timeInput.add(new Option(label, `${start}-${end}`));
    }

    ["dateInput", "timeInput"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('change', populateCategoryOptions);
    });

    populateCategoryOptions();
});

function requireDateOrAlert(date) {
    if (!date) {
        alert("날짜를 먼저 선택하세요.");
        return false;
    }
    return true;
}

// 백엔드에서 CSV 파일 목록 가져오기
async function fetchCsvFilenames() {
    const res = await fetch("/api/drive/csv-list");
    if (!res.ok) throw new Error("CSV 목록 불러오기 실패");
    return await res.json();
}

// 시간대별 파일명 변형 생성
function buildTimeKeys(time) {
    const [s, e] = time.split('-');
    const sH = s.slice(0, 2);
    const eH = e.slice(0, 2);
    const sHHmm = s.replace(':', '');
    const eHHmm = e.replace(':', '');
    return [
        `${s}-${e}`,
        `${sHHmm}-${eHHmm}`,
        `${sH}~${eH}`,
        `${sH}-${eH}`,
        `${sH}_${eH}`,
        `${sHHmm}~${eHHmm}`,
    ];
}

// 파일 찾기
function findFilenamesByCondition(files, prefix, date, time) {
    if (!time) {
        const startsWith = `${prefix}_${date}_`;
        return files.filter(name => name.startsWith(startsWith) && name.endsWith('.csv'));
    }
    const candidates = buildTimeKeys(time).map(key => `${prefix}_${date}_${key}.csv`);
    const hit = files.find(name => candidates.includes(name));
    return hit ? [hit] : [];
}

function normalizeName(name) {
    return String(name)
        .trim()                 // 앞뒤 공백 제거
        .replace(/\u200B/g, "") // zero-width space 제거
        .replace(/\s+/g, " ");  // 중간 여러 공백 → 1개 공백
}

// 제품별 시선수 합산
function aggregateGazeByProduct(rows) {
    const map = new Map();

    for (const row of rows) {
        const key = normalizeName(row['상품명']);
        const gazeCount = Number(row['시선추적수']) || 0;

        if (!map.has(key)) {
            map.set(key, {
                상품명: key,
                카테고리: row['카테고리'],
                상품가격: row['상품가격'],
                시선추적수: gazeCount
            });
        } else {
            map.get(key).시선추적수 += gazeCount;
        }
    }

    return Array.from(map.values());
}

// 구매 전환율 테이블 로딩
async function loadConversionTable() {
    const date = document.getElementById("dateInput").value;
    if (!requireDateOrAlert(date)) return;
    const time = document.getElementById("timeInput").value;
    const category = document.getElementById("categoryInput").value;

    const allFiles = await fetchCsvFilenames();

    const gazeFiles = findFilenamesByCondition(allFiles, "gaze-tracking", date, time);

    // ⭐ 시간대에 따라 sales 파일 선택
    const salesFiles = time
        ? ["real-sale_latest.csv"]               // 특정 시간대
        : ["real-sale_by_day_latest.csv"];       // 전체 시간

    if (!gazeFiles.length || !salesFiles.length) {
        alert("조건에 맞는 파일을 찾을 수 없습니다.");
        return;
    }

    const gazeData = await fetchManyCSVs(gazeFiles);
    const salesData = await fetchManyCSVs(salesFiles);

    let filteredGaze = category ? gazeData.filter(row => row['카테고리'] === category) : gazeData;
    const filteredSales = category ? salesData.filter(row => row['카테고리'] === category) : salesData;

    if (!time) {
        filteredGaze = aggregateGazeByProduct(filteredGaze);
    }

    const merged = filteredGaze.map(gaze => {
        const sale = filteredSales.find(s => s['상품명'] === gaze['상품명']);
        const saleCount = sale ? Number(sale['실판매수량']) : 0;
        const gazeCount = Number(gaze['시선추적수']);
        const rate = gazeCount ? ((saleCount / gazeCount) * 100).toFixed(1) + "%" : "0%";
        const saleRank = sale ? (sale['index'] && sale['index'] !== '-' ? sale['index'] : "0") : "-";

        return {
            제품명: gaze['상품명'],
            카테고리: gaze['카테고리'],
            가격: gaze['상품가격'],
            관심수: gazeCount,
            판매수: saleCount,
            구매전환율: rate,
            판매순위: saleRank
        };
    });

    renderConversionTable(merged);
}

async function fetchAndParseCSV(url) {
    const response = await fetch(url);
    const text = await response.text();

    const isGaze = url.includes("gaze-tracking");
    const isSale = url.includes("real-sale");

    if (isGaze) {
        const parsed = Papa.parse(text, { header: false, skipEmptyLines: true });
        return parsed.data.map(cols => ({
            index: cols[0],
            상품명: cols[1],
            카테고리: cols[2],
            상품가격: cols[3],
            시선추적수: cols[4] ?? "0"
        }));
    }

    if (isSale) {
        const parsed = Papa.parse(text, { header: true, skipEmptyLines: true });
        return parsed.data;
    }

    const parsed = Papa.parse(text, { header: true, skipEmptyLines: true });
    return parsed.data;
}

async function fetchManyCSVs(fileNames) {
    const urls = fileNames.map(n => `/api/drive/file?name=${encodeURIComponent(n)}`);
    const lists = await Promise.all(urls.map(fetchAndParseCSV));
    return lists.flat();
}

function renderConversionTable(data) {
    const tbody = document.getElementById("conversionTableBody");
    tbody.innerHTML = '';

    data.forEach((row, idx) => {
        tbody.innerHTML += `
            <tr>
                <td>${idx + 1}</td>
                <td>${row.제품명}</td>
                <td>${row.카테고리}</td>
                <td>${row.가격}</td>
                <td>${row.관심수}</td>
                <td>${row.판매수}</td>
                <td>${row.구매전환율}</td>
                <td>${row.판매순위}</td>
            </tr>
        `;
    });
}

function showSection(section) {
    const compareSection = document.getElementById("compareSection");
    const conversionSection = document.getElementById("conversionSection");

    if (section === "compare") {
        compareSection.classList.remove("hidden");
        conversionSection.classList.add("hidden");
        loadCompareTable();
    } else if (section === "conversion") {
        compareSection.classList.add("hidden");
        conversionSection.classList.remove("hidden");
        loadConversionTable();
    }
}

// ⭐ 시선추적 결과 비교 테이블
async function loadCompareTable() {
    const date = document.getElementById("dateInput").value;
    if (!requireDateOrAlert(date)) return;
    const time = document.getElementById("timeInput").value;
    const category = document.getElementById("categoryInput").value;

    const allFiles = await fetchCsvFilenames();

    const gazeFiles = findFilenamesByCondition(allFiles, "gaze-tracking", date, time);

    // ⭐ 시간대별 sales 파일 분기
    const salesFiles = time
        ? ["real-sale_latest.csv"]
        : ["real-sale_by_day_latest.csv"];

    if (!gazeFiles.length || !salesFiles.length) return;

    const gazeData = await fetchManyCSVs(gazeFiles);
    const salesData = await fetchManyCSVs(salesFiles);

    let filteredGaze = category ? gazeData.filter(r => r['카테고리'] === category) : gazeData;
    let filteredSales = category ? salesData.filter(r => r['카테고리'] === category) : salesData;

    if (!time) filteredGaze = aggregateGazeByProduct(filteredGaze);

    renderCompareTable(filteredGaze, filteredSales);
}

function renderCompareTable(gazeRows, salesRows) {
    const gazeTbody = document.getElementById("gazeTableBody");
    const salesTbody = document.getElementById("salesTableBody");
    gazeTbody.innerHTML = '';
    salesTbody.innerHTML = '';

    gazeRows.forEach(row => {
        gazeTbody.innerHTML += `<tr><td>${row['상품명']}</td><td>${row['카테고리']}</td><td>${row['상품가격']}</td></tr>`;
    });

    salesRows.forEach(row => {
        salesTbody.innerHTML += `<tr><td>${row['상품명']}</td><td>${row['카테고리']}</td><td>${row['상품가격']}</td></tr>`;
    });
}

// 카테고리 옵션 채우기
function normCat(v) {
    if (v == null) return null;
    const s = String(v).trim();
    return s.length ? s : null;
}

function extractCategories(...datasets) {
    const set = new Set();
    for (const data of datasets) {
        for (const row of data) {
            const raw = row['카테고리'] ?? row['category'] ?? row['Category'];
            const c = normCat(raw);
            if (c) set.add(c);
        }
    }
    return Array.from(set);
}

async function populateCategoryOptions() {
    const date = document.getElementById("dateInput").value;
    const time = document.getElementById("timeInput").value;
    const sel = document.getElementById("categoryInput");

    const prev = sel.value;
    sel.innerHTML = '<option value="">전체</option>';

    if (!date) return;

    try {
        const files = await fetchCsvFilenames();
        const gazeFiles = findFilenamesByCondition(files, "gaze-tracking", date, time);

        // ⭐ 시간대에 따라 sales 파일 선택
        const salesFiles = time
            ? ["real-sale_latest.csv"]
            : ["real-sale_by_day_latest.csv"];

        if (!gazeFiles.length && !salesFiles.length) return;

        const [gazeData, salesData] = await Promise.all([
            gazeFiles.length ? fetchManyCSVs(gazeFiles) : Promise.resolve([]),
            salesFiles.length ? fetchManyCSVs(salesFiles) : Promise.resolve([]),
        ]);

        const cats = extractCategories(gazeData, salesData)
            .sort((a, b) => a.localeCompare(b, 'ko'));

        for (const c of cats) sel.add(new Option(c, c));

        if (prev && cats.includes(prev)) sel.value = prev;

    } catch (e) {
        console.error("카테고리 옵션 로딩 실패:", e);
    }
}
