package org.dromara.hikvision.media;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@link RtspUrlBuilder} 单元测试
 *
 * @author hikvision-sdk
 */
@Tag("dev")
class RtspUrlBuilderTest {

    @Test
    void testMainStream() {
        String url = RtspUrlBuilder.main("192.168.1.64", 8000, "admin", "123456", 1);
        Assertions.assertEquals("rtsp://admin:123456@192.168.1.64:8000/Streaming/Channels/101", url);
    }

    @Test
    void testSubStream() {
        String url = RtspUrlBuilder.sub("192.168.1.64", 554, "admin", "123456", 2);
        Assertions.assertEquals("rtsp://admin:123456@192.168.1.64:554/Streaming/Channels/202", url);
    }

    @Test
    void testWithoutAuth() {
        String url = RtspUrlBuilder.main("192.168.1.64", 8000, null, null, 1);
        Assertions.assertEquals("rtsp://192.168.1.64:8000/Streaming/Channels/101", url);
    }
}
