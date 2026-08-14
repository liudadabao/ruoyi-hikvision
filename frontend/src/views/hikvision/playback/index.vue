<template>
  <div class="p-2 app-container">
    <el-card shadow="hover" class="table-panel">
      <template #header>
        <div class="panel-heading">
          <span class="panel-kicker">Playback</span>
          <h3>录像回放</h3>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="设备">
          <el-select v-model="deviceId" placeholder="请选择设备" style="width: 220px" @change="loadChannels">
            <el-option v-for="d in devices" :key="d.deviceId" :label="d.deviceName" :value="Number(d.deviceId)" />
          </el-select>
        </el-form-item>
        <el-form-item label="通道">
          <el-select v-model="channelNo" placeholder="请选择通道" style="width: 120px">
            <el-option v-for="i in channelCount" :key="i" :label="'通道 ' + i" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="VideoPlay" @click="handlePlay">开始回放</el-button>
        </el-form-item>
      </el-form>

      <div class="control-bar" v-if="playHandle != null">
        <el-button-group>
          <el-button icon="VideoPlay" @click="control(1)">播放</el-button>
          <el-button icon="VideoPause" @click="control(3)">暂停</el-button>
          <el-button icon="RefreshRight" @click="control(4)">恢复</el-button>
          <el-button icon="CaretRight" @click="control(5)">快放</el-button>
          <el-button icon="CaretRight" @click="control(6)">慢放</el-button>
          <el-button icon="VideoPause" @click="control(7)">正常</el-button>
          <el-button type="danger" icon="SwitchButton" @click="handleStop">停止</el-button>
        </el-button-group>
        <span class="ml-2">回放句柄: {{ playHandle }}</span>
      </div>

      <el-divider content-position="left">下载录像</el-divider>
      <el-form :inline="true">
        <el-form-item label="保存路径">
          <el-input v-model="savePath" placeholder="服务端保存路径，如 /data/hikvision/download.mp4" style="width: 360px" />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" plain icon="Download" @click="handleDownload">下载</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="HikPlayback" lang="ts">
import { listAllDevice } from '@/api/hikvision';
import type { HikDeviceVO } from '@/api/hikvision/types';
import request from '@/utils/request';
import modal from '@/plugins/modal';

const devices = ref<HikDeviceVO[]>([]);
const deviceId = ref<number>();
const channelNo = ref(1);
const channelCount = ref(0);
const timeRange = ref<[string, string]>();
const playHandle = ref<number | null>(null);
const savePath = ref('/data/hikvision/download.mp4');

const loadDevices = async () => {
  const res = await listAllDevice({});
  devices.value = res.data || [];
};

const loadChannels = () => {
  const d = devices.value.find((x) => Number(x.deviceId) === deviceId.value);
  channelCount.value = d ? (d.channelNum ?? 0) + (d.ipChannelNum ?? 0) : 0;
};

const handlePlay = async () => {
  if (!deviceId.value || !timeRange.value) {
    modal.msgWarning('请选择设备与时间范围');
    return;
  }
  const res = await request({
    url: '/hikvision/playback/play/' + deviceId.value,
    method: 'post',
    params: {
      channelNo: channelNo.value,
      startTime: timeRange.value[0],
      endTime: timeRange.value[1],
      streamType: 0
    }
  });
  playHandle.value = res.data;
  modal.msgSuccess('回放已开始');
};

const control = async (code: number) => {
  await request({
    url: '/hikvision/playback/control/' + playHandle.value,
    method: 'post',
    params: { controlCode: code, inValue: 0 }
  });
};

const handleStop = async () => {
  await request({ url: '/hikvision/playback/stop/' + playHandle.value, method: 'post' });
  playHandle.value = null;
};

const handleDownload = async () => {
  if (!deviceId.value || !timeRange.value) {
    modal.msgWarning('请选择设备与时间范围');
    return;
  }
  const res = await request({
    url: '/hikvision/playback/download/' + deviceId.value,
    method: 'post',
    params: {
      channelNo: channelNo.value,
      startTime: timeRange.value[0],
      endTime: timeRange.value[1],
      streamType: 0,
      savePath: savePath.value
    }
  });
  modal.msgSuccess('开始下载，句柄: ' + res.data);
};

onMounted(() => {
  loadDevices();
});
</script>

<style lang="scss" scoped>
.control-bar {
  margin: 16px 0;
}
</style>
