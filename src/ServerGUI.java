import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import javax.swing.SwingUtilities;

// 服务器图形用户界面类
public class ServerGUI extends JFrame {
    private JPanel devicePanel; // 显示设备信息的面板
    private Properties config = new Properties(); // 配置文件

    // 构造方法，初始化GUI
    public ServerGUI() {
        loadConfig(); // 加载配置文件
        initializeLoginUI(); // 初始化登录界面
    }

    // 加载配置文件
    private void loadConfig() {
        try (FileInputStream input = new FileInputStream("config.properties")) {
            config.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 初始化登录界面
    private void initializeLoginUI() {
        setTitle("游泳馆安全系统 - 登录");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 窗口居中显示

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel userLabel = new JLabel("用户名:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(userLabel, constraints);

        JTextField userText = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(userText, constraints);

        JLabel passwordLabel = new JLabel("密码:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(passwordLabel, constraints);

        JPasswordField passwordText = new JPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(passwordText, constraints);

        JButton loginButton = new JButton("登录");
        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(loginButton, constraints);

        // 登录按钮的事件处理
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                if (validateLogin(username, password)) {
                    JOptionPane.showMessageDialog(null, "登录成功!");
                    initializeMainUI(); // 初始化主界面
                    startServer(); // 启动服务器
                } else {
                    JOptionPane.showMessageDialog(null, "用户名或密码错误，密码要求至少一个大写字母，一个小写字母和一个数字，且最短长度为八位.");
                }
            }
        });

        add(panel);
        setVisible(true);
    }

    // 初始化主界面
    private void initializeMainUI() {
        getContentPane().removeAll(); // 清除登录界面组件
        setTitle("游泳馆安全系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 窗口居中显示

        devicePanel = new JPanel();
        devicePanel.setLayout(new GridLayout(0, 2, 10, 10)); // 网格布局，每行2列，水平和垂直间隙为10
        JScrollPane scrollPane = new JScrollPane(devicePanel); // 使用滚动面板
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // 验证登录信息
    private boolean validateLogin(String username, String password) {
        String configUsername = config.getProperty("username");
        String configPassword = config.getProperty("password");

        if (!username.equals(configUsername) || !password.equals(configPassword)) {
            return false;
        }

        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    // 启动服务器
    private void startServer() {
        Server server = new Server();
        server.setUpdateCallback(devices -> SwingUtilities.invokeLater(() -> updateDeviceStatus(devices)));
        new Thread(server::start).start();
    }

    // 更新设备状态
    public void updateDeviceStatus(Map<String, Device> devices) {
        devicePanel.removeAll(); // 清除面板中的旧设备信息
        for (Device device : devices.values()) {
            JPanel deviceInfoPanel = createDeviceInfoPanel(device);
            devicePanel.add(deviceInfoPanel);
        }
        devicePanel.revalidate();
        devicePanel.repaint();
    }

    // 创建设备信息面板
    private JPanel createDeviceInfoPanel(Device device) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1)); // 4行1列

        JLabel idLabel = new JLabel("ID: " + device.getId());
        JLabel batteryLabel = new JLabel("电量: " + (int) device.getBatteryPercentage() + "%");
        JLabel positionLabel = new JLabel("位置: (" + device.getX() + ", " + device.getY() + ")");
        JLabel statusLabel = new JLabel("状态: " + device.getStatus());

        panel.add(idLabel);
        panel.add(batteryLabel);
        panel.add(positionLabel);
        panel.add(statusLabel);

        panel.setOpaque(true);
        switch (device.getStatus()) {
            case WARNING:
                panel.setBackground(Color.YELLOW);
                break;
            case ALARM:
                panel.setBackground(Color.RED);
                break;
            case LOW_BATTERY:
                panel.setBackground(Color.BLUE);
                break;
            default:
                panel.setBackground(Color.WHITE);
                break;
        }

        return panel;
    }

    // 主方法，启动GUI
    public static void main(String[] args) {
        new ServerGUI();
    }
}
