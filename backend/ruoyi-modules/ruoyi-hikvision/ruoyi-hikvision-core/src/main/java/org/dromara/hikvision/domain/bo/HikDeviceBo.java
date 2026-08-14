package org.dromara.hikvision.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.hikvision.domain.HikDevice;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 海康设备业务对象 sys_hik_device
 *
 * @author hikvision-sdk
 */
@Data
@AutoMapper(target = HikDevice.class, reverseConvertGenerate = false)
public class HikDeviceBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备主键
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    /**
     * 设备 IP 地址
     */
    @NotBlank(message = "设备 IP 不能为空")
    private String deviceIp;

    /**
     * 端口号
     */
    @NotNull(message = "端口号不能为空")
    private Integer port;

    /**
     * 登录账号
     */
    @NotBlank(message = "登录账号不能为空")
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 厂家
     */
    private String manufacturer;

    /**
     * 备注
     */
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();
}
