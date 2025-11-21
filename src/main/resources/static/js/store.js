// 현재 페이지 URL에서 storeCode 추출
const pathParts = window.location.pathname.split("/");
const storeCode = pathParts[pathParts.length - 1]; // 예: 'A001'

console.log("접속한 storeCode:", storeCode);
console.log()

// 필요한 곳에 storeCode를 사용해 fetch 등에 전달할 수 있음
window.getStoreCode = () => storeCode; // 전역에서 사용 가능

function applyCategoryFilter() {
    // 선택된 카테고리 값 추출
    const selectedCategories = Array.from(document.querySelectorAll('#categoryFilterForm input[name="categories"]:checked'))
        .map(input => input.value);

    // 테이블 행 선택
    const rows
        = document.querySelectorAll('tbody tr');

    rows.forEach(row => {
        const categoryCell = row.children[2]; // 카테고리 셀 (index 2)
        const category = categoryCell.textContent.trim();

        // 선택된 카테고리만 보여주기
        if (selectedCategories.length === 0 || selectedCategories.includes(category)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });

    // 인덱스 다시 번호 붙이기
    updateVisibleRowIndices();

    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById('categoryFilterModal'));
    if (modal) modal.hide();
}

function applyPriceFilter() {
    const minPrice = parseFloat(document.getElementById('minPrice').value);
    const maxPrice = parseFloat(document.getElementById('maxPrice').value);

    const rows = document.querySelectorAll('tbody tr');

    rows.forEach(row => {
        const priceCell = row.children[3]; // 가격 셀 위치: 상품명(0), 카테고리(1), 가격(2) → index 3번이면 앞에 인덱스 열 추가된 경우!
        const price = parseFloat(priceCell.textContent.trim().replace(/[^0-9.]/g, ''));

        const showRow =
            (isNaN(minPrice) || price >= minPrice) &&
            (isNaN(maxPrice) || price <= maxPrice);

        row.style.display = showRow ? '' : 'none';
    });

    // ✅ 필터 후 인덱스 재정렬
    updateVisibleRowIndices();

    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById('priceFilterModal'));
    if (modal) modal.hide();
}

function updateVisibleRowIndices() {
    const visibleRows = Array.from(document.querySelectorAll("tbody tr"))
        .filter(row => row.style.display !== "none");

    visibleRows.forEach((row, idx) => {
        const indexCell = row.querySelector(".index-cell");
        if (indexCell) {
            indexCell.textContent = idx + 1;
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    updateVisibleRowIndices();   // ① 먼저 번호 붙임
});