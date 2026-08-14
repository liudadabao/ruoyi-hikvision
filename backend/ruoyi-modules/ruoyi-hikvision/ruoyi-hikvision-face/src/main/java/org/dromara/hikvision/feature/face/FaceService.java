package org.dromara.hikvision.feature.face;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 人脸识别服务（人脸库管理、人脸检索），通过 ISAPI 透传实现。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("face")
public class FaceService {

    private final IsapiService isapiService;

    /**
     * 获取人脸库列表。
     */
    public String listFaceLibraries(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/Intelligent/FDLib?format=json");
    }

    /**
     * 获取人脸库内人员列表。
     */
    public String listFaces(Long deviceId, String fdLibId) {
        return isapiService.get(deviceId, "/ISAPI/Intelligent/FDLib/" + fdLibId + "/FDSearch?format=json");
    }

    /**
     * 获取人脸抓拍/识别记录。
     */
    public String listFaceRecords(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json");
    }
}
