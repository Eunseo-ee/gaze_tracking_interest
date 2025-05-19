function playVideo(anchor) {
    const fileName = anchor.getAttribute("data-src"); // 여기서 filename을 가져옴
    const video = document.getElementById("videoPlayer");
    const source = document.getElementById("videoSource");

    source.src = "/uploads/" + encodeURIComponent(fileName);

    video.pause();
    video.load();

    video.oncanplay = () => {
        video.play().catch((e) => console.warn("Play 중단:", e));
    };
}
