package org.dromara.hikvision.media;

/**
 * 海康设备 RTSP 地址构造工具。
 *
 * @author hikvision-sdk
 */
public class RtspUrlBuilder {

    private RtspUrlBuilder() {
    }

    /**
     * 构造主码流 RTSP 地址。
     */
    public static String main(String ip, int port, String username, String password, int channelNo) {
        return build(ip, port, username, password, channelNo, "01");
    }

    /**
     * 构造子码流 RTSP 地址。
     */
    public static String sub(String ip, int port, String username, String password, int channelNo) {
        return build(ip, port, username, password, channelNo, "02");
    }

    /**
     * 构造 RTSP 地址。
     *
     * @param streamNo 码流序号：01-主码流 02-子码流
     */
    public static String build(String ip, int port, String username, String password, int channelNo, String streamNo) {
        String auth = (username == null || username.isBlank()) ? "" : username + ":" + password + "@";
        return String.format("rtsp://%s%s:%d/Streaming/Channels/%d%s", auth, ip, port, channelNo, streamNo);
    }
}
