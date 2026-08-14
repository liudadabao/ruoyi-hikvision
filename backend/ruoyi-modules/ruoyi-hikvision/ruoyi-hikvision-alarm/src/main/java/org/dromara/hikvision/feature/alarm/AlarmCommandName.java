package org.dromara.hikvision.feature.alarm;

import java.util.HashMap;
import java.util.Map;

/**
 * 报警命令名称映射。
 *
 * @author hikvision-sdk
 */
public class AlarmCommandName {

    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
        NAMES.put(0x1100, "报警信息主动上传(8000)");
        NAMES.put(0x4000, "报警信息主动上传(9000)");
        NAMES.put(0x4007, "报警信息主动上传(V40)");
        NAMES.put(0x1102, "异常行为检测");
        NAMES.put(0x2800, "交通抓拍结果");
        NAMES.put(0x3050, "交通抓拍终端图片");
        NAMES.put(0x1113, "交通取证报警");
        NAMES.put(0x5002, "门禁主机报警");
        NAMES.put(0x1127, "报告报警上传");
        NAMES.put(0x1123, "报警主机故障报警");
        NAMES.put(0x1124, "报警主机操作事件");
        NAMES.put(0x1125, "防护舱状态");
        NAMES.put(0x1126, "报警输出状态");
        NAMES.put(0x1129, "报警数据上传");
    }

    private AlarmCommandName() {
    }

    /**
     * 获取命令名称，未知时返回十六进制表示。
     */
    public static String of(int command) {
        String name = NAMES.get(command);
        return name != null ? name : String.format("0x%04X", command);
    }
}
