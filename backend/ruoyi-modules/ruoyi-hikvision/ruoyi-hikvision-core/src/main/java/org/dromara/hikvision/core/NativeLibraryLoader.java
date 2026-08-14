package org.dromara.hikvision.core;

import com.sun.jna.Library;
import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hikvision.config.HikvisionProperties;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.sdk.PlayCtrl;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 海康 SDK 原生库加载器。
 * <p>
 * 加载顺序：
 * <ol>
 *     <li>外部目录 {@code hikvision.sdk.native-path}/{platform}（推荐，便于升级原生库）；</li>
 *     <li>classpath 资源 {@code sdk/version/{platform}/}（解压到临时目录）。</li>
 * </ol>
 * 原生库因授权原因不随源码分发，可通过 {@code script/download-sdk.ps1} 从官方 SDK 包中导入。
 *
 * @author hikvision-sdk
 */
@Slf4j
@Component
public class NativeLibraryLoader {

    private final Platform platform;
    private final Path nativeDir;

    public NativeLibraryLoader(HikvisionProperties properties) {
        this.platform = Platform.detect();
        this.nativeDir = resolveNativeDir(properties);
        initJnaLibraryPath();
    }

    /**
     * 解析原生库目录。
     */
    private Path resolveNativeDir(HikvisionProperties properties) {
        String base = properties.getNativePath();
        if (base == null || base.isBlank()) {
            base = "./lib/hikvision";
        }
        Path external = Paths.get(base, platform.getDir()).toAbsolutePath();
        if (Files.isDirectory(external) && hasCoreLibs(external)) {
            log.info("[hikvision] 从外部目录加载原生库: {}", external);
            return external;
        }

        // 回退到 classpath 资源
        Path temp = Paths.get(System.getProperty("java.io.tmpdir"), "hikvision-sdk", platform.getDir());
        try {
            extractFromClasspath(temp);
            if (hasCoreLibs(temp)) {
                log.info("[hikvision] 从 classpath 资源解压原生库到: {}", temp);
                return temp;
            }
        } catch (Exception e) {
            log.warn("[hikvision] 从 classpath 解压原生库失败: {}", e.getMessage());
        }
        log.warn("[hikvision] 未找到平台 [{}] 的原生库，请先运行 script/download-sdk.ps1 导入，外部目录: {}", platform.getDir(), external);
        return external;
    }

    /**
     * 将 jna.library.path 指向原生库目录。
     */
    private void initJnaLibraryPath() {
        String existing = System.getProperty("jna.library.path", "");
        String path = nativeDir.toString();
        System.setProperty("jna.library.path", existing.isBlank() ? path : path + File.pathSeparator + existing);
    }

    /**
     * 判断目录是否包含核心原生库。
     */
    private boolean hasCoreLibs(Path dir) {
        return platform.getCoreLibs().stream().allMatch(name -> Files.exists(dir.resolve(name)));
    }

    /**
     * 从 classpath 资源 {@code sdk/version/{platform}/} 解压所有文件。
     */
    private void extractFromClasspath(Path target) throws Exception {
        String resourceDir = "sdk/version/" + platform.getDir() + "/";
        java.util.List<String> libNames = listResourceFiles(resourceDir);
        if (libNames.isEmpty()) {
            return;
        }
        Files.createDirectories(target);
        for (String name : libNames) {
            try (java.io.InputStream in = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(resourceDir + name)) {
                if (in == null) {
                    continue;
                }
                Path dest = target.resolve(name);
                if (Files.exists(dest)) {
                    continue;
                }
                Files.copy(in, dest);
            }
        }
    }

    /**
     * 列出 classpath 目录下的文件（仅支持打包后的 jar，开发期直接读目录）。
     */
    private java.util.List<String> listResourceFiles(String resourceDir) {
        java.util.List<String> result = new java.util.ArrayList<>();
        try {
            java.net.URL url = NativeLibraryLoader.class.getClassLoader().getResource(resourceDir);
            if (url == null) {
                return result;
            }
            if ("jar".equals(url.getProtocol())) {
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                    java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith(resourceDir) && name.length() > resourceDir.length()) {
                            result.add(name.substring(resourceDir.length()));
                        }
                    }
                }
            } else {
                Path dir = Paths.get(url.toURI());
                try (var stream = Files.list(dir)) {
                    stream.filter(Files::isRegularFile).forEach(p -> result.add(p.getFileName().toString()));
                }
            }
        } catch (Exception e) {
            log.debug("[hikvision] 列出资源文件失败: {}", e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前平台
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * 获取原生库目录
     */
    public Path getNativeDir() {
        return nativeDir;
    }

    /**
     * 加载 HCNetSDK 实例。
     */
    public HCNetSDK loadHCNetSDK() {
        return loadLibrary("HCNetSDK", platform.isWindows() ? "HCNetSDK.dll" : "libhcnetsdk.so", HCNetSDK.class);
    }

    /**
     * 加载 PlayCtrl 实例。
     */
    public PlayCtrl loadPlayCtrl() {
        return loadLibrary("PlayCtrl", platform.isWindows() ? "PlayCtrl.dll" : "libPlayCtrl.so", PlayCtrl.class);
    }

    private <T extends Library> T loadLibrary(String name, String fileName, Class<T> clazz) {
        Path file = nativeDir.resolve(fileName);
        String absolute = file.toString();
        if (Files.exists(file)) {
            Map<String, Object> options = new HashMap<>();
            return Native.load(absolute, clazz, options);
        }
        try {
            return Native.load(name, clazz);
        } catch (UnsatisfiedLinkError e) {
            throw new IllegalStateException("[hikvision] 无法加载原生库 " + name + "，请检查目录: " + nativeDir, e);
        }
    }
}
