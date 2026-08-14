package org.dromara.hikvision.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 海康 SDK 错误码枚举
 * <p>
 * 对应 {@code NET_DVR_GetLastError()} 返回的错误码（正值），
 * 函数本身（登录/取流等）失败时返回 0 或 -1。
 *
 * @author hikvision-sdk
 */
@Getter
@AllArgsConstructor
public enum HikErrorCode {

    NO_ERROR(0, "没有错误"),

    PASSWORD_ERROR(1, "用户名或密码错误"),
    NO_ENOUGH_PRI(2, "权限不足"),
    NO_INIT(3, "SDK 未初始化"),
    CHANNEL_ERROR(4, "通道号错误"),
    OVER_MAX_LINK(5, "连接数超过最大值"),
    VERSION_NO_MATCH(6, "版本不匹配"),
    NETWORK_FAIL_CONNECT(7, "连接服务器失败"),
    NETWORK_SEND_ERROR(8, "向服务器发送失败"),
    NETWORK_RECV_ERROR(9, "从服务器接收数据失败"),
    NETWORK_RECV_TIMEOUT(10, "从服务器接收数据超时"),
    NETWORK_ERROR_DATA(11, "发送数据错误"),
    ORDER_ERROR(12, "命令错误"),
    OPER_NO_PERMIT(13, "无此操作权限"),
    COMMAND_TIMEOUT(14, "命令超时"),
    ERROR_SERIAL_PORT(15, "串口号错误"),
    ERROR_ALARM_PORT(16, "报警端口错误"),
    PARAMETER_ERROR(17, "参数错误"),
    CHAN_EXCEPTION(18, "服务器通道处于错误状态"),
    NO_DISK(19, "没有硬盘"),
    ERROR_DISK_NUM(20, "硬盘号错误"),
    DISK_FULL(21, "硬盘已满"),
    DISK_ERROR(22, "硬盘错误"),
    NO_SUPPORT(23, "服务器不支持该功能"),
    BUSY(24, "服务器忙"),
    MODIFY_FAIL(25, "修改失败"),
    PASSWORD_FORMAT_ERROR(26, "密码输入格式不正确"),
    DISK_FORMATTING(27, "硬盘正在格式化"),
    DVR_NO_RESOURCE(28, "设备资源不足"),
    DVR_OPERATE_FAILED(29, "设备操作失败"),
    OPEN_HOST_SOUND_FAIL(30, "语音对讲失败"),
    DVR_VOICE_OPENED(31, "语音对讲已打开"),
    TIME_INPUT_ERROR(32, "时间输入错误"),
    NO_SPEC_FILE(33, "回放时没有指定的文件"),
    CREATE_FILE_ERROR(34, "创建文件出错"),
    FILE_OPEN_FAIL(35, "打开文件出错"),
    OPER_NOT_FINISH(36, "上次操作还没有完成"),
    GET_PLAY_TIME_FAIL(37, "获取当前播放时间出错"),
    SET_PLAY_TIME_FAIL(38, "设置当前播放时间出错"),
    FILE_NAME_CHECK_ERROR(39, "文件名错误"),
    FILE_FORMAT_ERROR(40, "文件格式错误"),
    DIR_ERROR(41, "目录不存在"),
    ALLOC_RESOURCE_ERROR(42, "资源分配错误"),
    AUDIO_MODE_ERROR(43, "声卡模式错误"),
    NO_ENOUGH_BUF(44, "缓冲区太小"),
    CREATE_SOCKET_ERROR(45, "创建 SOCKET 出错"),
    SET_SOCKET_ERROR(46, "设置 SOCKET 出错"),
    LINK_NUM_ERROR(47, "连接数达到最大"),
    NULL_POINTER_ERROR(48, "空指针"),
    DEVICE_NAME_ERROR(49, "设备名错误"),
    ERR_NO_STRUCT(50, "没有对应结构"),
    USERNAME_NOT_EXIST(55, "用户名不存在"),
    NOT_SUPPORT(56, "设备不支持该操作"),
    OPERATION_ILLEGAL(57, "操作非法"),
    USER_LOCKED(58, "用户被锁定"),
    PASSWORD_EXPIRED(59, "密码已过期"),
    VERIFY_FAIL(62, "校验失败"),
    NOT_FIND_FILE(63, "没有找到文件"),
    UNKNOWN(-1, "未知错误");

    private static final Map<Integer, HikErrorCode> CACHE = new HashMap<>();

    static {
        for (HikErrorCode code : values()) {
            CACHE.put(code.getCode(), code);
        }
    }

    private final int code;
    private final String message;

    /**
     * 根据错误码获取枚举，未匹配返回 UNKNOWN
     *
     * @param code 错误码
     * @return 对应枚举
     */
    public static HikErrorCode fromCode(int code) {
        return CACHE.getOrDefault(code, UNKNOWN);
    }

    /**
     * 获取格式化错误信息
     *
     * @param code 错误码
     * @return 错误信息
     */
    public static String message(int code) {
        HikErrorCode error = fromCode(code);
        return String.format("[%d] %s", code, error.getMessage());
    }
}
