const barcodeRoutes = {
    "37474711": "/store/B06/promotion/promotion_detail1",
    "85039055": "/store/B06/promotion/promotion_detail2",
    "58387552": "/store/B06/promotion/promotion_detail3",
    "54283315": "/store/B06/promotion/promotion_detail4",
    "66799143": "/store/B06/promotion/promotion_detail5",
    "14466259": "/store/B06/promotion/promotion_detail6"
};

const html5QrCode = new Html5Qrcode("reader");
const config = { fps: 10, qrbox: 250 };

html5QrCode.start(
    { facingMode: "environment" },
    config,
    async (decodedText, decodedResult) => {

        const route = barcodeRoutes[decodedText];
        const overlay = document.getElementById("barcode-overlay");

        // ---- 스캔 중지 (Promise 기다림) ----
        await html5QrCode.stop();

        // ---- 오버레이 표시 ----
        overlay.textContent = `인식된 바코드: ${decodedText}`;
        overlay.style.display = "block";

        // ---- 2초 후 이동 ----
        setTimeout(() => {
            if (route) {
                window.location.href = route;
            } else {
                overlay.textContent = `등록되지 않은 바코드입니다: ${decodedText}`;
                setTimeout(() => {
                    overlay.style.display = "none";
                }, 2000);
            }
        }, 2000);
    },
    (errorMessage) => {
        console.warn("스캔 실패:", errorMessage);
    }
);
