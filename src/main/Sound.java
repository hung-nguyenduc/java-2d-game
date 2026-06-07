package main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Quản lý âm thanh cho game. Dùng javax.sound.sampled (có sẵn trong Java, chỉ
 * chơi được file .wav).
 *
 * - playMusic(tên): phát nhạc nền lặp vô hạn (tự dừng nhạc cũ trước).
 * - stopMusic(): dừng nhạc nền.
 * - playSE(tên): phát một hiệu ứng âm thanh 1 lần (click, mở cửa...).
 *
 * "tên" là tên file trong thư mục res/sound, KHÔNG kèm đuôi .wav.
 * Ví dụ: playMusic("nhac_nen_mainmenu").
 */
public class Sound {

    // Clip riêng cho nhạc nền để có thể dừng/đổi bài
    private Clip musicClip;
    private boolean muted = false;

    /** Phát nhạc nền lặp lại. Gọi lại với bài khác sẽ tự đổi bài. */
    public void playMusic(String name) {
        stopMusic();
        if (muted) return;
        musicClip = load(name);
        if (musicClip != null) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        }
    }

    /** Dừng nhạc nền hiện tại. */
    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }

    /** Phát một hiệu ứng âm thanh 1 lần (không lặp). */
    public void playSE(String name) {
        if (muted) return;
        Clip clip = load(name);
        if (clip != null) {
            // Tự đóng clip sau khi phát xong để giải phóng tài nguyên
            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        }
    }

    /** Bật/tắt toàn bộ âm thanh. */
    public void toggleMute() {
        muted = !muted;
        if (muted) stopMusic();
    }

    public boolean isMuted() {
        return muted;
    }

    // Nạp 1 file wav từ res/sound thành một Clip sẵn sàng phát
    private Clip load(String name) {
        try {
            URL url = getClass().getResource("/sound/" + name + ".wav");
            if (url == null) {
                System.err.println("Sound: không tìm thấy file /sound/" + name + ".wav");
                return null;
            }
            InputStream raw = new BufferedInputStream(url.openStream());
            AudioInputStream ais = AudioSystem.getAudioInputStream(raw);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            System.err.println("Sound: lỗi khi nạp " + name + ": " + e.getMessage());
            return null;
        }
    }
}
