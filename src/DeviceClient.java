import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;
import java.io.FileInputStream;
import java.io.IOException;

// 设备客户端类
public class DeviceClient {
    private static String SERVER_ADDRESS = "localhost"; // 服务器地址
    private static int SERVER_PORT; // 服务器端口
    private static double DROWNING_PROBABILITY; // 溺水概率
    private static double BATTERY_DRAIN_RATE; // 电池消耗率
    private static Properties config = new Properties(); // 配置文件-

    // 主方法
    public static void main(String[] args) {
        loadConfig(); // 加载配置文件
        int deviceCount = Integer.parseInt(config.getProperty("device_count")); // 设备数量
        SERVER_PORT = Integer.parseInt(config.getProperty("server_port"));
        DROWNING_PROBABILITY = Double.parseDouble(config.getProperty("drowning_probability"));
        BATTERY_DRAIN_RATE = Double.parseDouble(config.getProperty("battery_drain_rate"));

        for (int i = 1; i <= deviceCount; i++) {
            new Thread(new DeviceSimulator(i)).start(); // 启动设备模拟线程
        }
    }

    // 加载配置文件
    private static void loadConfig() {
        try (FileInputStream input = new FileInputStream("config.properties")) {
            config.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 设备模拟器类，实现Runnable接口
    private static class DeviceSimulator implements Runnable {
        private final int deviceId;
        private boolean isDrowning = false;

        public DeviceSimulator(int deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            Socket socket = null;
            ObjectOutputStream out = null;

            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());

                String deviceName = "Device" + deviceId;
                out.writeUTF(deviceName);
                out.flush();

                Random random = new Random();
                double battery = 100.0;

                while (battery > 0) {
                    if (!isDrowning && random.nextDouble() < DROWNING_PROBABILITY) {
                        isDrowning = true;
                        System.out.println("设备 " + deviceName + " 溺水了.");
                    }

                    int x = isDrowning ? -1 : random.nextInt(101); // 溺水时 x 设置为 -1 表示不发送位置信息
                    int y = isDrowning ? -1 : random.nextInt(26);  // 溺水时 y 设置为 -1 表示不发送位置信息
                    battery -= BATTERY_DRAIN_RATE;
                    if (battery < 0) battery = 0;
                    String message = isDrowning ? String.format("%s %.2f", deviceName, battery) :
                                                  String.format("%s %.2f %d %d", deviceName, battery, x, y);
                    out.writeUTF(message);
                    out.flush();

                    Thread.sleep(Integer.parseInt(config.getProperty("signal_interval")) * 1000);
                }
                System.out.println("设备 " + deviceName + " 电量耗尽，不再更新状态.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
