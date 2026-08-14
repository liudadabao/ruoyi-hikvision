export interface HikDeviceVO extends BaseEntity {
  deviceId: number | string;
  deviceName: string;
  deviceIp: string;
  port: number;
  username: string;
  password: string;
  deviceType: number;
  manufacturer: string;
  serialNumber: string;
  channelNum: number;
  ipChannelNum: number;
  status: string;
  channelCount: number;
  remark: string;
}

export interface HikDeviceForm {
  deviceId: number | string | undefined;
  deviceName: string;
  deviceIp: string;
  port: number;
  username: string;
  password: string;
  deviceType?: number;
  manufacturer?: string;
  remark?: string;
}

export interface HikDeviceQuery extends PageQuery {
  deviceName?: string;
  deviceIp?: string;
  deviceType?: number;
}

export interface PreviewInfo {
  deviceId: number | string;
  channelNo: number;
  stream: string;
  rtspUrl: string;
  playUrls: Record<string, string>;
  realPlayHandle: number;
}

export interface HikAlarmRecordVO extends BaseEntity {
  id: number | string;
  deviceId: number | string;
  command: number;
  commandName: string;
  deviceIp: string;
  alarmTime: string;
}

export interface HikAlarmRecordQuery extends PageQuery {
  deviceId?: number | string;
  command?: number;
}
