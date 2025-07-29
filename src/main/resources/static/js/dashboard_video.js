document.addEventListener("DOMContentLoaded", async function () {
    try {
        const response = await fetch("/api/drive/mp4-list");
        const videos = await response.json();

        const list = document.getElementById("driveVideoList");

        videos.forEach(video => {
            const li = document.createElement("li");
            const a = document.createElement("a");
            a.href = "#";
            a.textContent = `🎥 ${video.name}`;
            a.onclick = () => playDriveVideo(video.name, video.webViewLink);
            li.appendChild(a);
            list.appendChild(li);
        });
    } catch (err) {
        console.error("영상 목록 불러오기 실패:", err);
    }

    // ✅ 기본 미리보기 메시지 넣기
    const container = document.getElementById("videoContainer");
    container.innerHTML = `
        <div style="text-align:center; padding: 50px; color: gray;">
            <h3>🎬 영상 재생을 위해 항목을 클릭해주세요</h3>
            <p>하단 목록에서 원하는 영상을 선택하시면 이곳에 미리보기가 표시됩니다.</p>
            <img src="/images/video_preview_placeholder.png" alt="preview" style="max-width: 400px; margin-top: 20px; opacity: 0.7;">
        </div>
    `;
});

function playDriveVideo(name, webViewLink) {
    if (!webViewLink) {
        console.error("webViewLink 없음:", name);
        return;
    }

    const videoIdMatch = webViewLink.match(/\/d\/(.*?)\//);
    if (!videoIdMatch || !videoIdMatch[1]) {
        console.error("videoId 추출 실패:", webViewLink);
        return;
    }

    const videoId = videoIdMatch[1];
    const container = document.getElementById("videoContainer");
    container.innerHTML = ""; // 비우기

    const iframe = document.createElement("iframe");
    iframe.width = "100%";
    iframe.height = "480";
    iframe.allow = "autoplay";
    iframe.frameBorder = "0";
    iframe.src = `https://drive.google.com/file/d/${videoId}/preview`;

    container.appendChild(iframe);
}


// 페이지 로드시 자동 실행
window.onload = fetchDriveVideos;