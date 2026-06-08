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
 * Âm lượng nhạc nền và hiệu ứng (SE) điều chỉnh riêng qua setMusicVolume /
 * setSeVolume (giá trị 0.0 -> 1.0).
 *
 * "tên" là tên file trong thư mục res/sound, KHÔNG kèm đuôi .wav.
 * Ví dụ: playMusic("nhac_nen_mainmenu").
 */
public class Sound {

    // Clip riêng cho nhạc nền để có thể dừng/đổi bài
    private Clip musicClip;
    private boolean muted = false;

    // Âm lượng tuyến tính 0.0 -> 1.0 cho nhạc nền và hiệu ứng
    private float musicVolume = 0.6f;
    private float seVolume = 0.85f;

    // Bài nhạc nền gần nhất, để bật lại khi bỏ tắt tiếng (unmute)
    private String lastMusicName = null;

    /** Phát nhạc nền lặp lại. Gọi lại với bài khác sẽ tự đổi bài. */
    public void playMusic(String name) {
        lastMusicName = name;
        stopMusic();
        if (muted) return;
        musicClip = load(name);
        if (musicClip != null) {
            applyGain(musicClip, musicVolume);
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
            applyGain(clip, seVolume);
            // Tự đóng clip sau khi phát xong để giải phóng tài nguyên
            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        }
    }

    /** Bật/tắt toàn bộ âm thanh. Bỏ tắt sẽ tự bật lại nhạc nền gần nhất. */
    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stopMusic();
        } else if (lastMusicName != null) {
            playMusic(lastMusicName);
        }
    }

    public boolean isMuted() {
        return muted;
    }

    /** Đặt âm lượng nhạc nền (0.0 -> 1.0), áp dụng ngay cho bài đang phát. */
    public void setMusicVolume(float v) {
        musicVolume = clamp01(v);
        applyGain(musicClip, musicVolume);
    }

    /** Đặt âm lượng hiệu ứng âm thanh (0.0 -> 1.0). */
    public void setSeVolume(float v) {
        seVolume = clamp01(v);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSeVolume() {
        return seVolume;
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    // Áp dụng âm lượng tuyến tính lên clip thông qua MASTER_GAIN (đơn vị dB)
    private void applyGain(Clip clip, float volume) {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB;
            if (volume <= 0.0001f) {
                dB = gain.getMinimum(); // im hẳn
            } else {
                dB = (float) (20.0 * Math.log10(volume));
                if (dB < gain.getMinimum()) dB = gain.getMinimum();
                if (dB > gain.getMaximum()) dB = gain.getMaximum();
            }
            gain.setValue(dB);
        } catch (Exception ignored) {
            // Một số dòng âm thanh không hỗ trợ MASTER_GAIN -> bỏ qua, vẫn phát bình thường
        }
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
