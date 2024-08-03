import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

// 服务器类
public class Server {
    private static int PORT; // 服务器端口
    private Map<String, Device> devices = new ConcurrentHashMap<>(); // 设备列表
    private Properties config = new Properties(); // 配置文件
    private Consumer<Map<String, Device>> updateCallback; // 更新回调函数

    // 主方法
    public static void main(String[] args) {
        new Server().start(); // 启动服务器
    }

    // 设置更新回调函数
    public void setUpdateCallback(Consumer<Map<String, Device>> callback) {
        this.updateCallback = callback;
    }

    // 启动服务器
    public void start() {
        loadConfig(); // 加载配置文件
        PORT = Integer.parseInt(config.getProperty("server_port"));
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动...");
            while (true) {
                new ClientHandler(serverSocket.accept()).start(); // 接受客户端连接并启动处理线程
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载配置文件
    private void loadConfig() {
        try (InputStream input = new FileInputStream("config.properties")) {
            config.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 客户端处理线程类
    private class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream in = null;
            ObjectOutputStream out = null;

            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                String deviceId = in.readUTF();
                Device device = devices.getOrDefault(deviceId, new Device(deviceId));
                devices.put(deviceId, device);

                while (true) {
                    try {
                        String message = in.readUTF();
                        String[] parts = message.split(" ");
                        double battery = Double.parseDouble(parts[1]);

                        if (parts.length == 4) {
                            int x = Integer.parseInt(parts[2]);
                            int y = Integer.parseInt(parts[3]);
                            device.updatePosition(x, y);
                        }
                        device.updateBattery(battery);

                        boolean isDrown = device.isDrown(Long.parseLong(config.getProperty("warning_time")), Long.parseLong(config.getProperty("alarm_time")));

                        out.writeObject(device);
                        out.flush();

                        System.out.printf("设备ID: %s, 电量: %.2f%%, 位置: (%d, %d), 状态: %s\n",
                                device.getId(), device.getBatteryPercentage(), device.getX(), device.getY(), device.getStatus());

                        if (updateCallback != null) {
                            updateCallback.accept(devices);
                        }

                        if (isDrown && device.getStatus() == DeviceStatus.ALARM) {
                            Alarm alarm = new SimpleAlarm();
                            alarm.alert();
                        }
                    } catch (EOFException | SocketException e) {
                        System.out.println("设备 " + deviceId + " 断开连接.");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
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
