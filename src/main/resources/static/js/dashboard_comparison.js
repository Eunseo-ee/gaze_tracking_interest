// 백엔드에 있는 드라이브 폴더 내 모든 csv 파일명 가져오기
async function fetchCsvFilenames() {
    const res = await fetch("/api/drive/csv-list");

    if (!res.ok) {
        const errorText = await res.text();
        console.error("CSV 목록 불러오기 실패:", res.status, errorText);
        throw new Error("CSV 목록 불러오기 실패");
    }

    console.log("요청한 URL: /api/drive/csv-list");
    console.log("응답 상태 코드:", res.status);

    return await res.json();
}

function findFilenameByCondition(files, prefix, date, time) {
    const target = `${prefix}_${date}_${time}.csv`;
    return files.find(name => name === target);
}

async function loadConversionTable() {
    const date = document.getElementById("dateInput").value;
    const time = document.getElementById("timeInput").value;
    const category = document.getElementById("categoryInput").value;

    const allFiles = await fetchCsvFilenames();

    const gazeFile = findFilenameByCondition(allFiles, "gaze-tracking", date, time);
    const salesFile = findFilenameByCondition(allFiles, "real-sale", date, time);

    if (!gazeFile || !salesFile) {
        alert("조건에 맞는 파일을 찾을 수 없습니다.");
        return;
    }

    const gazeData = await fetchAndParseCSV(`/api/drive/file?name=${encodeURIComponent(gazeFile)}`);
    const salesData = await fetchAndParseCSV(`/api/drive/file?name=${encodeURIComponent(salesFile)}`);

    const filteredGaze = category ? gazeData.filter(row => row['카테고리'] === category) : gazeData;
    const filteredSales = category ? salesData.filter(row => row['카테고리'] === category) : salesData;

    // 바코드를 기준으로 매칭
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
            상품가격: gaze['상품가격'],
            시선추적수: gazeCount,
            실판매수량: saleCount,
            구매전환율: rate,
            실판매순위: saleRank
        };
    });

    renderConversionTable(merged);
}

async function fetchAndParseCSV(url) {
    const response = await fetch(url);
    const text = await response.text();
    const parsed = Papa.parse(text, {
        header: true,
        skipEmptyLines: true
    });
    return parsed.data;
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
                <td>${row.상품가격}</td>
                <td>${row.시선추적수}</td>
                <td>${row.실판매수량}</td>
                <td>${row.구매전환율}</td>
                <td>${row.실판매순위}</td>
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
    const time = document.getElementById("timeInput").value;
    const category = document.getElementById("categoryInput").value;

    const allFiles = await fetchCsvFilenames();

    const gazeFile = findFilenameByCondition(allFiles, "gaze-tracking", date, time);
    const salesFile = findFilenameByCondition(allFiles, "real-sale", date, time);

    if (!gazeFile || !salesFile) {
        alert("조건에 맞는 파일을 찾을 수 없습니다.");
        return;
    }

    const gazeData = await fetchAndParseCSV(`/api/drive/file?name=${encodeURIComponent(gazeFile)}`);
    const salesData = await fetchAndParseCSV(`/api/drive/file?name=${encodeURIComponent(salesFile)}`);

    const filteredGaze = category
        ? gazeData.filter(row => row['카테고리'] === category)
        : gazeData;

    const filteredSales = category
        ? salesData.filter(row => row['카테고리'] === category)
        : salesData;

    console.log("선택된 날짜:", date);
    console.log("선택된 시간:", time);
    console.log("선택된 카테고리:", category);
    console.log("찾은 gaze 파일:", gazeFile);
    console.log("찾은 sales 파일:", salesFile);
    console.log("gazeData", gazeData);
    console.log("salesData", salesData);

    renderCompareTable(filteredGaze, filteredSales);
}

function renderCompareTable(gazeRows, salesRows) {
    const gazeTbody = document.getElementById("gazeTableBody");
    const salesTbody = document.getElementById("salesTableBody");
    gazeTbody.innerHTML = '';
    salesTbody.innerHTML = '';

    gazeRows.forEach(row => {
        gazeTbody.innerHTML += `<tr><td>${row['index']}</td><td>${row['상품명']}</td><td>${row['카테고리']}</td><td>${row['상품가격']}</td></tr>`;
    });

    salesRows.forEach(row => {
        salesTbody.innerHTML += `<tr><td>${row['index']}</td><td>${row['상품명']}</td><td>${row['카테고리']}</td><td>${row['상품가격']}</td></tr>`;
    });
}
