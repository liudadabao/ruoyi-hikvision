package org.dromara.hikvision.sdk.win32;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 兼容海康官方 HCNetSDK.java / PlayCtrl.java 中所引用的旧版
 * {@code com.sun.jna.examples.win32.W32API} 类型的最小实现。
 * <p>
 * 服务端集成不会做本地窗口渲染，GDI32 / USER32 仅用于满足编译要求，
 * 因此这里只提供类型占位与最小结构定义。
 *
 * @author hikvision-sdk
 */
public interface W32API extends Library {

    /**
     * JNA 加载选项，此处为空实现，仅保证官方接口中
     * {@code Native.loadLibrary("gdi32", GDI32.class, DEFAULT_OPTIONS)} 可编译。
     */
    Map<String, Object> DEFAULT_OPTIONS = new HashMap<>();

    /**
     * Windows 句柄基类。
     */
    class HANDLE extends PointerType {
        public HANDLE() {
        }

        public HANDLE(Pointer p) {
            super(p);
        }
    }

    /**
     * 窗口句柄。
     */
    class HWND extends HANDLE {
        public HWND() {
        }

        public HWND(Pointer p) {
            super(p);
        }
    }

    /**
     * 设备上下文句柄。
     */
    class HDC extends HANDLE {
        public HDC() {
        }

        public HDC(Pointer p) {
            super(p);
        }
    }

    /**
     * 矩形结构。
     */
    class RECT extends Structure {
        public int left;
        public int top;
        public int right;
        public int bottom;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("left", "top", "right", "bottom");
        }
    }
}
