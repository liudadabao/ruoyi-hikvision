package org.dromara.hikvision.feature.access;

import com.sun.jna.ptr.IntByReference;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 门禁管理服务（门控制、卡管理、门禁事件）。
 * <p>
 * 门控制优先走 SDK {@code NET_DVR_RemoteControl}，卡与事件等复杂业务
 * 通过 ISAPI 透传实现，具体 URL 可按设备型号调整。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("access")
public class AccessService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;
    private final IsapiService isapiService;

    /**
     * 门控制。
     *
     * @param doorNo  门编号（从 1 开始）
     * @param command 命令：1-开门 2-关门 3-门常开 4-门常关
     */
    public void controlDoor(Long deviceId, int doorNo, int command) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        IntByReference door = new IntByReference(doorNo);
        boolean ok = hcNetSDK.NET_DVR_RemoteControl(session.getUserId(), command, door.getPointer(), 4);
        if (!ok) {
            throw new ServiceException("门控制失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
    }

    /**
     * 开门。
     */
    public void openDoor(Long deviceId, int doorNo) {
        controlDoor(deviceId, doorNo, 1);
    }

    /**
     * 关门。
     */
    public void closeDoor(Long deviceId, int doorNo) {
        controlDoor(deviceId, doorNo, 2);
    }

    /**
     * 门常开。
     */
    public void alwaysOpen(Long deviceId, int doorNo) {
        controlDoor(deviceId, doorNo, 3);
    }

    /**
     * 门常关。
     */
    public void alwaysClose(Long deviceId, int doorNo) {
        controlDoor(deviceId, doorNo, 4);
    }

    /**
     * 通过 ISAPI 控制门（备用方案，兼容部分型号）。
     */
    public String controlDoorIsapi(Long deviceId, int doorNo, String cmd) {
        String url = "/ISAPI/AccessControl/RemoteControl/door/" + doorNo;
        String body = "{\"cmd\":\"" + cmd + "\"}";
        return isapiService.stdXmlConfig(deviceId, url, body, 1024);
    }

    /**
     * 查询卡列表（ISAPI）。
     *
     * @param searchJson 查询条件 JSON（可传 null）
     */
    public String listCards(Long deviceId, String searchJson) {
        String url = "/ISAPI/AccessControl/CardInfo/records?format=json";
        return isapiService.get(deviceId, url);
    }

    /**
     * 查询门禁事件（ISAPI）。
     */
    public String listAcsEvents(Long deviceId) {
        String url = "/ISAPI/AccessControl/AcsEvent/records?format=json";
        return isapiService.get(deviceId, url);
    }

    /**
     * 查询门列表（ISAPI）。
     */
    public String listDoors(Long deviceId) {
        String url = "/ISAPI/AccessControl/Door/records?format=json";
        return isapiService.get(deviceId, url);
    }
}
