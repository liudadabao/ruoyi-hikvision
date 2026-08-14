package org.dromara.hikvision.feature.intercom;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.feature.access.AccessService;
import org.dromara.hikvision.feature.audio.AudioService;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 可视对讲服务。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("intercom")
public class IntercomService {

    private final IsapiService isapiService;
    private final AudioService audioService;
    private final AccessService accessService;

    /**
     * 开始对讲（复用语音对讲）。
     */
    public int startTalk(Long deviceId, int channel) {
        return audioService.startVoiceTalk(deviceId, channel);
    }

    /**
     * 发送对讲音频数据。
     */
    public void sendAudio(Long deviceId, byte[] data) {
        audioService.sendVoiceData(deviceId, data);
    }

    /**
     * 停止对讲。
     */
    public void stopTalk(Long deviceId) {
        audioService.stopVoiceTalk(deviceId);
    }

    /**
     * 远程开锁（门口机开锁）。
     */
    public void unlock(Long deviceId, int doorNo) {
        accessService.openDoor(deviceId, doorNo);
    }

    /**
     * 查询呼叫记录。
     */
    public String listCallRecords(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/VideoIntercom/callStatus?format=json");
    }
}
