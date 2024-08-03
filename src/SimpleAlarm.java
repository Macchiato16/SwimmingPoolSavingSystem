import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

// 实现报警接口的类
public class SimpleAlarm implements Alarm {
    // 实现报警方法
    @Override
    public void alert() {
        System.out.println("报警，有人溺水");
        playAlarmSound();
    }

    // 播放报警声音
    private void playAlarmSound() {
        try {
            // 指定音频文件的路径
            File soundFile = new File("alarm.wav");
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                System.out.println("播放报警声音...");
            } else {
                System.out.println("音频文件未找到！");
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
