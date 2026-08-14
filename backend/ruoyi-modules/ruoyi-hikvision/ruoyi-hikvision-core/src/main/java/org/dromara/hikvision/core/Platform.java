package org.dromara.hikvision.core;

import lombok.Getter;

import java.util.List;
import java.util.Locale;

/**
 * 运行平台枚举，用于跨平台加载海康 SDK 原生库。
 *
 * @author hikvision-sdk
 */
@Getter
public enum Platform {

    WIN_X86("win32", "windows", false, List.of("HCNetSDK.dll", "PlayCtrl.dll")),
    WIN_X64("win64", "windows", true, List.of("HCNetSDK.dll", "PlayCtrl.dll")),
    LINUX_X86("linux32", "linux", false, List.of("libhcnetsdk.so", "libPlayCtrl.so")),
    LINUX_X64("linux64", "linux", true, List.of("libhcnetsdk.so", "libPlayCtrl.so")),
    LINUX_ARM64("armlinux64", "linux", true, List.of("libhcnetsdk.so", "libPlayCtrl.so"));

    /**
     * 平台目录名（用于原生库资源目录）
     */
    private final String dir;

    /**
     * 操作系统名称前缀
     */
    private final String osName;

    /**
     * 是否为 64 位
     */
    private final boolean arch64;

    /**
     * 核心原生库文件名
     */
    private final List<String> coreLibs;

    Platform(String dir, String osName, boolean arch64, List<String> coreLibs) {
        this.dir = dir;
        this.osName = osName;
        this.arch64 = arch64;
        this.coreLibs = coreLibs;
    }

    /**
     * 检测当前运行平台
     *
     * @return 平台枚举
     */
    public static Platform detect() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean is64 = arch.contains("64") || arch.contains("aarch64") || arch.contains("amd64");
        boolean isArm = arch.contains("aarch64") || arch.contains("arm");

        if (os.contains("win")) {
            return is64 ? WIN_X64 : WIN_X86;
        }
        if (os.contains("linux")) {
            if (isArm) {
                return LINUX_ARM64;
            }
            return is64 ? LINUX_X64 : LINUX_X86;
        }
        // 其它类 Unix 系统按 Linux 处理
        return is64 ? LINUX_X64 : LINUX_X86;
    }

    /**
     * 是否 Windows 平台
     */
    public boolean isWindows() {
        return this == WIN_X86 || this == WIN_X64;
    }
}
