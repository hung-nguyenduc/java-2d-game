import javax.sound.sampled.*;
import java.io.*;

/**
 * Trình sinh âm thanh tổng hợp cho game (chạy 1 lần, không thuộc build game).
 * Xuất ra các file .wav 16-bit mono 44100Hz vào res/sound.
 *
 *  - hit_bia.wav        : tiếng "ding" kim loại khi bắn trúng bia
 *  - hit_enemy.wav      : tiếng "thụp" khi đạn trúng kẻ địch
 *  - luu_roi.wav        : tiếng lựu đạn va kim loại rơi xuống đất
 *  - dinh_dan.wav       : tiếng trầm "hự" khi nhân vật dính đạn
 *  - nhac_tuong_niem.wav: giai điệu kèn trang nghiêm tưởng niệm
 */
public class SoundGen {
    static final int SR = 44100;

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "res/sound";
        new File(out).mkdirs();

        write(out + "/hit_bia.wav", hitBia());
        write(out + "/hit_enemy.wav", hitEnemy());
        write(out + "/luu_roi.wav", luuRoi());
        write(out + "/dinh_dan.wav", dinhDan());
        write(out + "/nhac_tuong_niem.wav", nhacTuongNiem());
        System.out.println("Done.");
    }

    // ---- Tiếng trúng bia: "ding/ping" kim loại sáng, có vài bồi âm lệch hài ----
    static double[] hitBia() {
        double dur = 0.45;
        int n = (int) (SR * dur);
        double[] s = new double[n];
        double base = 1500;
        double[] mult = {1.0, 2.4, 3.9, 5.3};   // lệch hài -> chất kim loại
        double[] amp  = {1.0, 0.5, 0.3, 0.18};
        double[] tau  = {0.20, 0.12, 0.08, 0.05};
        for (int i = 0; i < n; i++) {
            double t = (double) i / SR;
            double v = 0;
            for (int k = 0; k < mult.length; k++)
                v += amp[k] * Math.exp(-t / tau[k]) * Math.sin(2 * Math.PI * base * mult[k] * t);
            // tiếng "tick" va đập rất ngắn ở đầu
            if (t < 0.006) v += 0.6 * (Math.random() * 2 - 1) * (1 - t / 0.006);
            s[i] = v;
        }
        return normalize(s, 0.9);
    }

    // ---- Tiếng trúng quái: "thụp" đạn trúng, gọn và đanh ----
    static double[] hitEnemy() {
        double dur = 0.15;
        int n = (int) (SR * dur);
        double[] s = new double[n];
        double lp = 0; // bộ lọc thông thấp đơn giản cho tiếng ồn
        for (int i = 0; i < n; i++) {
            double t = (double) i / SR;
            double f = 180 * Math.exp(-t / 0.04) + 65;   // cú thụp trầm tụt nhanh
            double thump = Math.exp(-t / 0.05) * Math.sin(2 * Math.PI * f * t);
            double noise = (Math.random() * 2 - 1);
            lp += (noise - lp) * 0.25;                    // làm tối tiếng ồn
            double burst = lp * Math.exp(-t / 0.028) * 0.6;
            s[i] = thump * 0.9 + burst;
        }
        return normalize(s, 0.9);
    }

    // ---- Tiếng lựu đạn rơi: cú va trầm + tiếng kim loại loảng xoảng ----
    static double[] luuRoi() {
        double dur = 0.4;
        int n = (int) (SR * dur);
        double[] s = new double[n];
        double[] mult = {520, 1150, 1870};   // bồi âm kim loại
        double[] amp  = {0.5, 0.32, 0.2};
        double[] tau  = {0.14, 0.10, 0.07};
        double lp = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / SR;
            double f = 150 * Math.exp(-t / 0.05) + 55;
            double thump = Math.exp(-t / 0.06) * Math.sin(2 * Math.PI * f * t);
            double metal = 0;
            for (int k = 0; k < mult.length; k++)
                metal += amp[k] * Math.exp(-t / tau[k]) * Math.sin(2 * Math.PI * mult[k] * t);
            double click = 0;
            if (t < 0.01) { double nz = Math.random() * 2 - 1; lp += (nz - lp) * 0.5; click = lp * (1 - t / 0.01) * 0.6; }
            s[i] = thump * 0.9 + metal * 0.6 + click;
        }
        return normalize(s, 0.9);
    }

    // ---- Tiếng "hự" khi dính đạn: giọng trầm tụt tông, kiểu rên đau ----
    static double[] dinhDan() {
        double dur = 0.3;
        int n = (int) (SR * dur);
        double[] s = new double[n];
        // hai formant gần "uh/hự"
        double f1 = 600, f2 = 1000;
        double bp = 0, lpNoise = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / SR;
            double f0 = 150 * Math.exp(-t / 0.25) + 95;   // cao độ tụt dần
            double v = 0;
            for (int h = 1; h <= 6; h++) {
                double hf = f0 * h;
                // tô đậm các hài gần formant -> giống nguyên âm
                double w = Math.exp(-Math.pow((hf - f1) / 350, 2)) + 0.7 * Math.exp(-Math.pow((hf - f2) / 450, 2));
                v += (w / h) * Math.sin(2 * Math.PI * hf * t);
            }
            // chút hơi thở
            double nz = Math.random() * 2 - 1;
            lpNoise += (nz - lpNoise) * 0.2;
            v += lpNoise * 0.12;
            // envelope: vào nhanh, giữ, tắt dần
            double env;
            double attack = 0.02, rel = 0.12;
            if (t < attack) env = t / attack;
            else env = Math.exp(-(t - attack) / rel);
            s[i] = v * env;
            bp += 0; // (giữ chỗ, không dùng)
        }
        return normalize(s, 0.85);
    }

    // ---- Nhạc tưởng niệm: giai điệu kèn (chỉ nốt bộ ba C-E-G như kèn quân đội) ----
    static double[] nhacTuongNiem() {
        double G4 = 392.00, C5 = 523.25, E5 = 659.25, G5 = 783.99, C6 = 1046.50;
        double beat = 0.6; // chậm, trang nghiêm
        double[][] mel = {
            // Câu 1 - lời gọi
            {G4, 0.75}, {C5, 0.25}, {E5, 1.0}, {C5, 0.5}, {G4, 1.0},
            // Câu 2 - vươn lên
            {G4, 0.75}, {C5, 0.25}, {E5, 1.0}, {G5, 1.5}, {0, 0.5},
            // Câu 3 - cao trào
            {E5, 0.5}, {G5, 0.5}, {C6, 1.5}, {G5, 0.5}, {E5, 0.5}, {C5, 1.0},
            // Câu 4 - lắng lại
            {G4, 0.75}, {C5, 0.25}, {E5, 1.0}, {C5, 1.0}, {G4, 2.0},
        };
        double total = 0;
        for (double[] m : mel) total += m[1] * beat;
        double tail = 1.2;
        int n = (int) (SR * (total + tail));
        double[] s = new double[n];

        int pos = 0;
        for (double[] note : mel) {
            double f = note[0];
            int len = (int) (note[1] * beat * SR);
            if (f > 0) {
                for (int i = 0; i < len && pos + i < n; i++) {
                    double t = (double) i / SR;
                    double dnote = note[1] * beat;
                    double vib = 1 + 0.004 * Math.sin(2 * Math.PI * 5 * t);
                    double v = 0;
                    double[] hamp = {1.0, 0.5, 0.33, 0.22, 0.13, 0.08}; // phổ kiểu kèn đồng
                    for (int h = 0; h < hamp.length; h++)
                        v += hamp[h] * Math.sin(2 * Math.PI * f * (h + 1) * t * vib);
                    // ADSR mềm
                    double a = 0.06, r = 0.18, sus = 0.8;
                    double env;
                    if (t < a) env = t / a;
                    else if (t > dnote - r) env = sus * Math.max(0, (dnote - t) / r);
                    else env = sus + (1 - sus) * Math.exp(-(t - a) / 0.15);
                    s[pos + i] += v * env * 0.5;
                }
            }
            pos += len;
        }
        // reverb đơn giản: vài tiếng vọng nhỏ dần tạo không gian
        double[] r = new double[n];
        int[] delays = {(int)(0.07*SR), (int)(0.13*SR), (int)(0.21*SR)};
        double[] gains = {0.4, 0.25, 0.16};
        for (int i = 0; i < n; i++) {
            double v = s[i];
            for (int d = 0; d < delays.length; d++)
                if (i - delays[d] >= 0) v += gains[d] * s[i - delays[d]];
            r[i] = v;
        }
        // fade-in nhẹ tránh click khi loop
        int fi = (int)(0.01 * SR);
        for (int i = 0; i < fi; i++) r[i] *= (double) i / fi;
        return normalize(r, 0.9);
    }

    // ---- Tiện ích ----
    static double[] normalize(double[] s, double peak) {
        double max = 1e-9;
        for (double v : s) max = Math.max(max, Math.abs(v));
        double g = peak / max;
        for (int i = 0; i < s.length; i++) s[i] *= g;
        return s;
    }

    static void write(String path, double[] s) throws Exception {
        byte[] data = new byte[s.length * 2];
        for (int i = 0; i < s.length; i++) {
            int v = (int) Math.round(Math.max(-1, Math.min(1, s[i])) * 32767);
            data[i * 2] = (byte) (v & 0xFF);
            data[i * 2 + 1] = (byte) ((v >> 8) & 0xFF);
        }
        AudioFormat fmt = new AudioFormat(SR, 16, 1, true, false);
        try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), fmt, s.length)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(path));
        }
        System.out.println("wrote " + path + " (" + String.format("%.2f", s.length / (double) SR) + "s)");
    }
}
