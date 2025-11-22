// 1) 바코드 → 이동 URL 매핑 테이블
const barcodeRoutes = {
    "37474711": "/store/B06/promotion/promotion_detail1",
    "85039055": "/store/B06/promotion/promotion_detail2",
    "58387552": "/store/B06/promotion/promotion_detail3",
    "54283315": "/store/B06/promotion/promotion_detail4",
    "66799143": "/store/B06/promotion/promotion_detail5",
    "14466259": "/store/B06/promotion/promotion_detail6"
};

// 2) 스캐너 시작 코드
const html5QrCode = new Html5Qrcode("reader");
const config = { fps: 10, qrbox: 250 };

html5QrCode.start(
    { facingMode: "environment" },
    config,
    (decodedText, decodedResult) => {

        // 3) 인식된 바코드값을 매핑 확인 → 이동
        const route = barcodeRoutes[decodedText];

        if (route) {
            window.location.href = route;   // 페이지 이동
        } else {
            alert("등록되지 않은 바코드입니다: " + decodedText);
        }

        html5QrCode.stop();
    },
    (errorMessage) => {
        console.warn("스캔 실패:", errorMessage);
    }
);