import javax.sound.sampled.*;
import java.io.*;

/**
 * Xử lý giọng đọc kill-streak cho "mạnh" hơn: hạ tông (trầm/uy lực),
 * gằn tiếng (distortion), vang dội kiểu MC trong game (echo), rồi đẩy to.
 *
 * Đọc từ soundgen/raw/<name>.wav  ->  ghi ra res/sound/<name>.wav
 */
public class SoundFx {
    public static void main(String[] args) throws Exception {
        String[] names = {"first_blood", "double_kill", "triple_kill"};
        for (String n : names) {
            process("soundgen/raw/" + n + ".wav", "res/sound/" + n + ".wav");
        }
        System.out.println("Done.");
    }

    static void process(String in, String out) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(in)));
        AudioFormat fmt = ais.getFormat();
        int sr = (int) fmt.getSampleRate();
        byte[] raw = ais.readAllBytes();
        ais.close();

        int nIn = raw.length / 2;
        double[] x = new double[nIn];
        for (int i = 0; i < nIn; i++) {
            int lo = raw[i * 2] & 0xFF;
            int hi = raw[i * 2 + 1];
            x[i] = (short) ((hi << 8) | lo) / 32768.0;
        }

        // 1) Hạ tông + chậm lại một chút -> trầm, uy lực
        double pitch = 0.82; // < 1 = trầm hơn
        int nOut = (int) (nIn / pitch);
        double[] y = new double[nOut];
        for (int i = 0; i < nOut; i++) {
            double sp = i * pitch;
            int i0 = (int) sp;
            double fr = sp - i0;
            double a = x[Math.min(i0, nIn - 1)];
            double b = x[Math.min(i0 + 1, nIn - 1)];
            y[i] = a + (b - a) * fr;
        }

        // 2) Gằn tiếng: distortion mềm (trộn wet/dry để vẫn nghe rõ chữ)
        double drive = 2.4;
        for (int i = 0; i < nOut; i++) {
            double wet = Math.tanh(y[i] * drive);
            y[i] = 0.72 * wet + 0.28 * y[i];
        }

        // 3) Vang dội kiểu sân khấu / MC
        int d1 = (int) (0.075 * sr);
        int d2 = (int) (0.15 * sr);
        double[] e = y.clone();
        for (int i = 0; i < nOut; i++) {
            double v = y[i];
            if (i - d1 >= 0) v += 0.33 * y[i - d1];
            if (i - d2 >= 0) v += 0.18 * y[i - d2];
            e[i] = v;
        }
        y = e;

        // 4) Đẩy to (normalize) + một lớp nén mềm cuối cho chắc tiếng
        double max = 1e-9;
        for (double v : y) max = Math.max(max, Math.abs(v));
        double g = 0.95 / max;
        for (int i = 0; i < nOut; i++) y[i] = Math.tanh(y[i] * g * 1.1);

        // Ghi ra WAV 16-bit mono
        byte[] data = new byte[nOut * 2];
        for (int i = 0; i < nOut; i++) {
            int v = (int) Math.round(Math.max(-1, Math.min(1, y[i])) * 32767);
            data[i * 2] = (byte) (v & 0xFF);
            data[i * 2 + 1] = (byte) ((v >> 8) & 0xFF);
        }
        AudioFormat outFmt = new AudioFormat(sr, 16, 1, true, false);
        try (AudioInputStream o = new AudioInputStream(new ByteArrayInputStream(data), outFmt, nOut)) {
            AudioSystem.write(o, AudioFileFormat.Type.WAVE, new File(out));
        }
        System.out.println("wrote " + out + " (" + String.format("%.2f", nOut / (double) sr) + "s)");
    }
}
