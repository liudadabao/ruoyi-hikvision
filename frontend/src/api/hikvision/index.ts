import type { PageResult } from '@/api/types';
import type { AxiosPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type {
  HikAlarmRecordQuery,
  HikAlarmRecordVO,
  HikDeviceForm,
  HikDeviceQuery,
  HikDeviceVO,
  PreviewInfo
} from './types';

// 分页查询设备列表
export function listDevice(query: HikDeviceQuery): AxiosPromise<PageResult<HikDeviceVO>> {
  return request({ url: '/hikvision/device/list', method: 'get', params: query });
}

// 查询全部设备
export function listAllDevice(query: HikDeviceQuery): AxiosPromise<HikDeviceVO[]> {
  return request({ url: '/hikvision/device/all', method: 'get', params: query });
}

// 查询设备详情
export function getDevice(deviceId: string | number): AxiosPromise<HikDeviceVO> {
  return request({ url: '/hikvision/device/' + deviceId, method: 'get' });
}

// 新增设备
export function addDevice(data: HikDeviceForm) {
  return request({ url: '/hikvision/device', method: 'post', data });
}

// 修改设备
export function updateDevice(data: HikDeviceForm) {
  return request({ url: '/hikvision/device', method: 'put', data });
}

// 删除设备
export function delDevice(deviceIds: string | number | Array<string | number>) {
  return request({ url: '/hikvision/device/' + deviceIds, method: 'delete' });
}

// 登录设备
export function loginDevice(deviceId: string | number) {
  return request({ url: '/hikvision/device/login/' + deviceId, method: 'post' });
}

// 登出设备
export function logoutDevice(deviceId: string | number) {
  return request({ url: '/hikvision/device/logout/' + deviceId, method: 'post' });
}

// 开始预览
export function startPreview(deviceId: string | number, channelNo: number, streamType = 0): AxiosPromise<PreviewInfo> {
  return request({
    url: '/hikvision/preview/start/' + deviceId,
    method: 'get',
    params: { channelNo, streamType }
  });
}

// 停止预览
export function stopPreview(deviceId: string | number, channelNo: number, streamType = 0) {
  return request({
    url: '/hikvision/preview/stop/' + deviceId,
    method: 'get',
    params: { channelNo, streamType }
  });
}

// 设备布防
export function setupAlarm(deviceId: string | number) {
  return request({ url: '/hikvision/alarm/setup/' + deviceId, method: 'post' });
}

// 撤销布防
export function closeAlarm(deviceId: string | number) {
  return request({ url: '/hikvision/alarm/close/' + deviceId, method: 'post' });
}

// 分页查询报警记录
export function listAlarmRecord(query: HikAlarmRecordQuery): AxiosPromise<PageResult<HikAlarmRecordVO>> {
  return request({ url: '/hikvision/alarm/record/list', method: 'get', params: query });
}

// 云台控制
export function ptzControl(deviceId: string | number, channelNo: number, command: number, stop = 0, speed = 5) {
  return request({
    url: '/hikvision/ptz/control/' + deviceId,
    method: 'post',
    params: { channelNo, command, stop, speed }
  });
}
