package org.dromara.hikvision.feature.vehicle;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.feature.access.AccessService;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 车辆道闸 / 停车场服务。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("vehicle")
public class VehicleService {

    private final IsapiService isapiService;
    private final AccessService accessService;

    /**
     * 控制道闸（开关闸）。
     *
     * @param barrierNo 道闸编号
     * @param command   1-开闸 2-关闸
     */
    public void controlBarrier(Long deviceId, int barrierNo, int command) {
        accessService.controlDoor(deviceId, barrierNo, command);
    }

    /**
     * 查询抓拍记录。
     */
    public String listCaptureRecords(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/Traffic/vctr/capture/records?format=json");
    }

    /**
     * 查询车辆名单（黑白名单）。
     */
    public String listVehicleLists(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/Traffic/vctr/vehicleList?format=json");
    }

    /**
     * 查询过车记录。
     */
    public String listVehicleRecords(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/Traffic/vctr/trafficData?format=json");
    }
}
