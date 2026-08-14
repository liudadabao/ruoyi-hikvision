package org.dromara.hikvision.feature.isapi;

import com.sun.jna.Memory;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * ISAPI 协议透传服务（NET_DVR_STDXMLConfig 通用封装）。
 * <p>
 * 可通过 ISAPI 实现对设备能力的统一访问，URL 如 {@code /ISAPI/System/deviceInfo}。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("config")
public class IsapiService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;

    /**
     * ISAPI 透传。
     *
     * @param deviceId   设备主键
     * @param requestUrl ISAPI 请求地址
     * @param inputXml   请求体（GET 可传 null）
     * @param outBufSize 输出缓冲区大小
     * @return 响应 XML 字符串
     */
    public String stdXmlConfig(Long deviceId, String requestUrl, String inputXml, int outBufSize) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT input = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT output = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();

        byte[] urlBytes = requestUrl.getBytes(Charset.forName("GBK"));
        Memory urlMem = new Memory(urlBytes.length + 1);
        urlMem.write(0, urlBytes, 0, urlBytes.length);
        urlMem.setByte(urlBytes.length, (byte) 0);

        Memory inMem = null;
        if (inputXml != null) {
            byte[] inBytes = inputXml.getBytes(Charset.forName("GBK"));
            inMem = new Memory(inBytes.length + 1);
            inMem.write(0, inBytes, 0, inBytes.length);
            inMem.setByte(inBytes.length, (byte) 0);
        }

        Memory outMem = new Memory(outBufSize);

        input.dwSize = input.size();
        input.lpRequestUrl = urlMem;
        input.dwRequestUrlLen = urlBytes.length + 1;
        input.lpInBuffer = inMem;
        input.dwInBufferSize = inMem == null ? 0 : (int) inMem.size();
        input.dwRecvTimeOut = 5000;
        input.write();

        output.dwSize = output.size();
        output.lpOutBuffer = outMem;
        output.dwOutBufferSize = outBufSize;
        output.write();

        boolean ok = hcNetSDK.NET_DVR_STDXMLConfig(session.getUserId(), input, output);
        if (!ok) {
            throw new ServiceException("ISAPI 请求失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        output.read();
        int len = output.dwReturnedXMLSize;
        if (len <= 0) {
            return "";
        }
        byte[] result = outMem.getByteArray(0, Math.min(len, outBufSize));
        return new String(result, 0, Math.min(len, outBufSize), StandardCharsets.UTF_8);
    }

    /**
     * 快捷 GET 请求。
     */
    public String get(Long deviceId, String requestUrl) {
        return stdXmlConfig(deviceId, requestUrl, null, 1024 * 1024);
    }
}
