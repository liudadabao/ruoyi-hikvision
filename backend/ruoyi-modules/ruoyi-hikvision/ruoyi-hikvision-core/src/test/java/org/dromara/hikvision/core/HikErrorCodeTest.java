package org.dromara.hikvision.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@link HikErrorCode} 单元测试
 *
 * @author hikvision-sdk
 */
@Tag("dev")
class HikErrorCodeTest {

    @Test
    void testFromCode() {
        Assertions.assertEquals(HikErrorCode.PASSWORD_ERROR, HikErrorCode.fromCode(1));
        Assertions.assertEquals(HikErrorCode.NETWORK_FAIL_CONNECT, HikErrorCode.fromCode(7));
        Assertions.assertEquals(HikErrorCode.NO_ERROR, HikErrorCode.fromCode(0));
        Assertions.assertEquals(HikErrorCode.UNKNOWN, HikErrorCode.fromCode(-999));
    }

    @Test
    void testMessage() {
        Assertions.assertTrue(HikErrorCode.message(1).contains("用户名或密码错误"));
        Assertions.assertTrue(HikErrorCode.message(0).contains("没有错误"));
    }
}
