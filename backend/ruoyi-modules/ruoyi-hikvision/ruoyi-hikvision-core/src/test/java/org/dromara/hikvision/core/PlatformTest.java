package org.dromara.hikvision.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * {@link Platform} 单元测试
 *
 * @author hikvision-sdk
 */
@Tag("dev")
class PlatformTest {

    @Test
    void testDetectNotNull() {
        Platform platform = Platform.detect();
        Assertions.assertNotNull(platform);
        Assertions.assertNotNull(platform.getDir());
        Assertions.assertFalse(platform.getCoreLibs().isEmpty());
    }

    @Test
    void testPlatformDirs() {
        Assertions.assertEquals("win32", Platform.WIN_X86.getDir());
        Assertions.assertEquals("win64", Platform.WIN_X64.getDir());
        Assertions.assertEquals("linux32", Platform.LINUX_X86.getDir());
        Assertions.assertEquals("linux64", Platform.LINUX_X64.getDir());
        Assertions.assertEquals("armlinux64", Platform.LINUX_ARM64.getDir());
    }

    @Test
    void testCoreLibs() {
        Assertions.assertEquals(List.of("HCNetSDK.dll", "PlayCtrl.dll"), Platform.WIN_X64.getCoreLibs());
        Assertions.assertEquals(List.of("libhcnetsdk.so", "libPlayCtrl.so"), Platform.LINUX_X64.getCoreLibs());
    }

    @Test
    void testIsWindows() {
        Assertions.assertTrue(Platform.WIN_X64.isWindows());
        Assertions.assertFalse(Platform.LINUX_X64.isWindows());
    }
}
