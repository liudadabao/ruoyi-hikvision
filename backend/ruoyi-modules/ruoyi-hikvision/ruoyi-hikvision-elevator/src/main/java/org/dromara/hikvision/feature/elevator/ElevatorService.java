package org.dromara.hikvision.feature.elevator;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 电梯门禁（梯控）服务。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("elevator")
public class ElevatorService {

    private final IsapiService isapiService;

    /**
     * 查询梯控楼层列表。
     */
    public String listFloors(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/AccessControl/Elevator/Floor/records?format=json");
    }

    /**
     * 查询梯控权限配置。
     */
    public String listAccessRight(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/AccessControl/Elevator/accessRight?format=json");
    }

    /**
     * 查询梯控事件。
     */
    public String listEvents(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/AccessControl/Elevator/events?format=json");
    }
}
