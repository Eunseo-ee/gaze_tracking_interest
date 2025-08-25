document.addEventListener('DOMContentLoaded', () => {
    const timeInput = document.getElementById('timeInput');
    if (!timeInput) return;

    // 기본값: 전체
    timeInput.innerHTML = '<option value="">전체</option>';

    for (let h = 0; h < 24; h++) {
        const start = String(h).padStart(2, '0') + ':00';
        const end = String((h + 1) % 24).padStart(2, '0') + ':00';
        const label = `${start}~${end}`;
        // value는 "HH:mm-HH:mm" (파싱 쉽게)
        timeInput.add(new Option(label, `${start}-${end}`));
    }

    // 날짜/시간 변경 시 카테고리 갱신
    ["dateInput", "timeInput"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('change', populateCategoryOptions);
    });

    // 초기 1회 (필요하면)
    populateCategoryOptions();
});

function requireDateOrAlert(date) {
    if (!date) {
        alert("날짜를 먼저 선택하세요.");
        return false;
    }
    return true;
}

// 백엔드에 있는 드라이브 폴더 내 모든 csv 파일명 가져오기
async function fetchCsvFilenames() {
    const res = await fetch("/api/drive/csv-list");
    if (!res.ok) {
        const errorText = await res.text();
        console.error("CSV 목록 불러오기 실패:", res.status, errorText);
        throw new Error("CSV 목록 불러오기 실패");
    }
    return await res.json(); // 예: ["gaze-tracking_2025-08-26_1800-1900.csv", ...]
}

// 주어진 time value("HH:mm-HH:mm")에서 가능한 모든 키 변형을 만들어줌
function buildTimeKeys(time) {
    // time 예: "18:00-19:00"
    const [s, e] = time.split('-'); // "18:00", "19:00"
    const sH = s.slice(0, 2);
    const eH = e.slice(0, 2);
    const sHHmm = s.replace(':', '');
    const eHHmm = e.replace(':', '');
    return [
        `${s}-${e}`,           // "18:00-19:00"
        `${sHHmm}-${eHHmm}`,   // "1800-1900"
        `${sH}~${eH}`,         // "18~19"
        `${sH}-${eH}`,         // "18-19"
        `${sH}_${eH}`,         // "18_19" (혹시 모를 포맷)
    ];
}

// ✅ 배열을 반환: 특정 시간대면 길이 1, "전체"면 해당 날짜의 모든 시간대 파일 목록
function findFilenamesByCondition(files, prefix, date, time) {
    // 전체(빈 문자열)면 하루치 전부
    if (!time) {
        const startsWith = `${prefix}_${date}_`;
        return files.filter(name => name.startsWith(startsWith) && name.endsWith('.csv'));
    }

    // 특정 시간대면 여러 포맷 후보 중 일치하는 것 1개 선택
    const candidates = buildTimeKeys(time).map(key => `${prefix}_${date}_${key}.csv`);
    const hit = files.find(name => candidates.includes(name));
    return hit ? [hit] : [];
}

async function loadConversionTable() {
    const date = document.getElementById("dateInput").value;
    if (!requireDateOrAlert(date)) return;
    const time = document.getElementById("timeInput").value; // ""이면 전체
    const category = document.getElementById("categoryInput").value;

    const allFiles = await fetchCsvFilenames();

    // ⬇️ 단수 → 복수(배열 반환)로 변경
    const gazeFiles = findFilenamesByCondition(allFiles, "gaze-tracking", date, time);
    const salesFiles = findFilenamesByCondition(allFiles, "real-sale", date, time);

    if (!gazeFiles.length || !salesFiles.length) {
        alert("조건에 맞는 파일을 찾을 수 없습니다.");
        return;
    }

    // ⬇️ 여러 CSV 합치기
    const gazeData = await fetchManyCSVs(gazeFiles);
    const salesData = await fetchManyCSVs(salesFiles);

    const filteredGaze = category ? gazeData.filter(row => row['카테고리'] === category) : gazeData;
    const filteredSales = category ? salesData.filter(row => row['카테고리'] === category) : salesData;

    // 바코드 기준 매칭
    const merged = filteredGaze.map(gaze => {
        const sale = filteredSales.find(s => s['상품바코드'] === gaze['상품바코드']);
        const saleCount = sale ? Number(sale['실판매수량']) : 0;
        const gazeCount = Number(gaze['시선추적수']);
        const rate = gazeCount ? ((saleCount / gazeCount) * 100).toFixed(1) + "%" : "0%";
        const saleRank = sale ? sale['index'] : "-";

        return {
            순위: gaze['index'],
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
    const parsed = Papa.parse(text, { header: true, skipEmptyLines: true });
    return parsed.data;
}

async function fetchManyCSVs(fileNames) {
    const urls = fileNames.map(n => `/api/drive/file?name=${encodeURIComponent(n)}`);
    const lists = await Promise.all(urls.map(fetchAndParseCSV));
    // 2차원 배열 -> 1차원으로 합치기
    return lists.flat();
}


function renderConversionTable(data) {
    const tbody = document.getElementById("conversionTableBody");
    tbody.innerHTML = '';

    data.forEach(row => {
        tbody.innerHTML += `
            <tr>
                <td>${row.순위}</td>
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
    console.log("🟢 showSection 실행됨:", section);

    const compareSection = document.getElementById("compareSection");
    const conversionSection = document.getElementById("conversionSection");

    if (section === "compare") {
        compareSection.classList.remove("hidden");
        conversionSection.classList.add("hidden");

        console.log("🟢 순위 비교 테이블 로딩 시작");
        loadCompareTable();

    } else if (section === "conversion") {
        compareSection.classList.add("hidden");
        conversionSection.classList.remove("hidden");

        console.log("🟢 구매 전환율 테이블 로딩 시작");
        loadConversionTable();
    }
}

async function loadCompareTable() {
    const date = document.getElementById("dateInput").value;
    if (!requireDateOrAlert(date)) return;
    const time = document.getElementById("timeInput").value; // ""이면 전체
    const category = document.getElementById("categoryInput").value;

    const allFiles = await fetchCsvFilenames();

    const gazeFiles = findFilenamesByCondition(allFiles, "gaze-tracking", date, time);
    const salesFiles = findFilenamesByCondition(allFiles, "real-sale", date, time);

    if (!gazeFiles.length || !salesFiles.length) {
        alert("조건에 맞는 파일을 찾을 수 없습니다.");
        return;
    }

    const gazeData = await fetchManyCSVs(gazeFiles);
    const salesData = await fetchManyCSVs(salesFiles);

    const filteredGaze = category ? gazeData.filter(r => r['카테고리'] === category) : gazeData;
    const filteredSales = category ? salesData.filter(r => r['카테고리'] === category) : salesData;

    console.log("선택된 날짜:", date);
    console.log("선택된 시간:", time || '전체');
    console.log("선택된 카테고리:", category || '전체');
    console.log("찾은 gaze 파일:", gazeFiles);
    console.log("찾은 sales 파일:", salesFiles);

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

// 공백/케이스/널 처리 유틸
function normCat(v) {
    if (v == null) return null;
    const s = String(v).trim();
    return s.length ? s : null;
}

// CSV에서 카테고리 집합 추출
function extractCategories(...datasets) {
    const set = new Set();
    for (const data of datasets) {
        for (const row of data) {
            // 한/영 컬럼명 둘 다 시도
            const raw = row['카테고리'] ?? row['category'] ?? row['Category'];
            const c = normCat(raw);
            if (c) set.add(c);
        }
    }
    return Array.from(set);
}

// 카테고리 <select>를 CSV 기반으로 채우기
async function populateCategoryOptions() {
    const date = document.getElementById("dateInput").value;
    const time = document.getElementById("timeInput").value; // ""면 전체
    const sel = document.getElementById("categoryInput");

    // 선택 유지용
    const prev = sel.value;

    // 기본값만 우선 세팅
    sel.innerHTML = '<option value="">전체</option>';

    // 날짜가 없으면 더 진행하지 않음
    if (!date) return;

    try {
        const files = await fetchCsvFilenames();
        const gazeFiles  = findFilenamesByCondition(files, "gaze-tracking", date, time);
        const salesFiles = findFilenamesByCondition(files, "real-sale",     date, time);

        if (!gazeFiles.length && !salesFiles.length) return;

        // 해당 날짜/시간(또는 전체 시간)의 CSV들을 모두 합쳐서 카테고리 추출
        const [gazeData, salesData] = await Promise.all([
            gazeFiles.length  ? fetchManyCSVs(gazeFiles)  : Promise.resolve([]),
            salesFiles.length ? fetchManyCSVs(salesFiles) : Promise.resolve([]),
        ]);

        const cats = extractCategories(gazeData, salesData)
            .sort((a, b) => a.localeCompare(b, 'ko')); // 보기 좋게 정렬

        // 옵션 채우기
        for (const c of cats) {
            sel.add(new Option(c, c));
        }

        // 이전 선택이 아직 유효하면 유지
        if (prev && cats.includes(prev)) sel.value = prev;

    } catch (e) {
        console.error("카테고리 옵션 로딩 실패:", e);
        // 실패 시엔 “전체”만 유지
    }
}
