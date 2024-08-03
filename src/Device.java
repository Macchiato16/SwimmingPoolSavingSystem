import java.io.Serializable;
import java.time.LocalDateTime;

// 设备类，实现序列化接口
public class Device implements Serializable {
    private String id; // 设备ID
    private double batteryPercentage; // 电池电量百分比
    private DeviceStatus status; // 设备状态
    private int x; // 设备位置x坐标
    private int y; // 设备位置y坐标
    private LocalDateTime lastSignalTime; // 最后一次信号时间
    private boolean isDrowning; // 是否溺水

    // 构造方法，初始化设备
    public Device(String id) {
        this.id = id;
        this.batteryPercentage = 100.0;
        this.status = DeviceStatus.NORMAL;
        this.x = 0;
        this.y = 0;
        this.lastSignalTime = LocalDateTime.now();
        this.isDrowning = false;
    }

    // 更新设备位置
    public void updatePosition(int x, int y) {
        if (batteryPercentage > 0 && !isDrowning) {
            this.x = x;
            this.y = y;
            this.lastSignalTime = LocalDateTime.now();
        }
    }

    // 更新电池电量
    public void updateBattery(double batteryPercentage) {
        if (batteryPercentage < 0) {
            this.batteryPercentage = 0;
        } else {
            this.batteryPercentage = batteryPercentage;
        }
        if (this.batteryPercentage < 20 && this.status != DeviceStatus.WARNING && this.status != DeviceStatus.ALARM) {
            this.status = DeviceStatus.LOW_BATTERY;
        }
    }

    // 设置溺水状态
    public void setDrowning(boolean isDrowning) {
        this.isDrowning = isDrowning;
    }

    // 检查是否溺水
    public boolean isDrowning() {
        return isDrowning;
    }

    // 检查是否进入警告或报警状态
    public boolean isDrown(long warningTime, long alarmTime) {
        long secondsSinceLastSignal = java.time.Duration.between(lastSignalTime, LocalDateTime.now()).getSeconds();
        if (secondsSinceLastSignal > alarmTime) {
            status = DeviceStatus.ALARM;
            return true;
        } else if (secondsSinceLastSignal > warningTime) {
            status = DeviceStatus.WARNING;
        } else if (this.batteryPercentage < 20) {
            status = DeviceStatus.LOW_BATTERY;
        } else {
            status = DeviceStatus.NORMAL;
        }
        return false;
    }

    // Getter方法和Setter方法
    public String getId() {
        return id;
    }

    public double getBatteryPercentage() {
        return batteryPercentage;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public LocalDateTime getLastSignalTime() {
        return lastSignalTime;
    }
}

// 设备状态枚举
enum DeviceStatus {
    NORMAL, // 正常
    WARNING, // 警告
    ALARM, // 报警
    LOW_BATTERY // 低电量
}
