<template>
  <div class="p-2 app-container">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="hover" class="table-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-kicker">Door Control</span>
              <h3>门禁控制</h3>
            </div>
          </template>

          <el-form :inline="true" class="query-form">
            <el-form-item label="设备">
              <el-select v-model="deviceId" placeholder="请选择设备" style="width: 200px">
                <el-option v-for="d in devices" :key="d.deviceId" :label="d.deviceName" :value="Number(d.deviceId)" />
              </el-select>
            </el-form-item>
            <el-form-item label="门编号">
              <el-input-number v-model="doorNo" :min="1" :max="64" controls-position="right" />
            </el-form-item>
          </el-form>

          <div class="control-bar">
            <el-button type="success" icon="Unlock" @click="controlDoor(1)">开门</el-button>
            <el-button type="danger" icon="Lock" @click="controlDoor(2)">关门</el-button>
            <el-button type="warning" plain icon="Key" @click="controlDoor(3)">门常开</el-button>
            <el-button type="info" plain icon="Key" @click="controlDoor(4)">门常关</el-button>
          </div>

          <el-divider content-position="left">门列表 / 事件</el-divider>
          <el-tabs v-model="tab">
            <el-tab-pane label="门列表" name="doors">
              <pre class="json-box">{{ doorsResult }}</pre>
            </el-tab-pane>
            <el-tab-pane label="门禁事件" name="events">
              <pre class="json-box">{{ eventsResult }}</pre>
            </el-tab-pane>
            <el-tab-pane label="卡列表" name="cards">
              <pre class="json-box">{{ cardsResult }}</pre>
            </el-tab-pane>
          </el-tabs>
          <div class="toolbar-actions mt-2">
            <el-button type="primary" plain icon="Search" @click="loadDoors">查询门</el-button>
            <el-button type="primary" plain icon="Search" @click="loadEvents">查询事件</el-button>
            <el-button type="primary" plain icon="Search" @click="loadCards">查询卡</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="table-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-kicker">ISAPI Debug</span>
              <h3>ISAPI 调试工具</h3>
            </div>
          </template>

          <el-form label-width="80px">
            <el-form-item label="设备">
              <el-select v-model="isapiDeviceId" placeholder="请选择设备" style="width: 100%">
                <el-option v-for="d in devices" :key="d.deviceId" :label="d.deviceName" :value="Number(d.deviceId)" />
              </el-select>
            </el-form-item>
            <el-form-item label="URL">
              <el-input v-model="isapiUrl" placeholder="/ISAPI/System/deviceInfo" />
            </el-form-item>
            <el-form-item label="请求体">
              <el-input v-model="isapiBody" type="textarea" :rows="4" placeholder="PUT/POST 请求体（可选）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Promotion" @click="sendIsapi">发送</el-button>
            </el-form-item>
            <el-form-item label="响应">
              <pre class="json-box">{{ isapiResult }}</pre>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="HikAccess" lang="ts">
import { listAllDevice } from '@/api/hikvision';
import type { HikDeviceVO } from '@/api/hikvision/types';
import request from '@/utils/request';
import modal from '@/plugins/modal';

const devices = ref<HikDeviceVO[]>([]);
const deviceId = ref<number>();
const doorNo = ref(1);
const tab = ref('doors');
const doorsResult = ref('');
const eventsResult = ref('');
const cardsResult = ref('');

const isapiDeviceId = ref<number>();
const isapiUrl = ref('/ISAPI/System/deviceInfo');
const isapiBody = ref('');
const isapiResult = ref('');

const loadDevices = async () => {
  const res = await listAllDevice({});
  devices.value = res.data || [];
};

const controlDoor = async (command: number) => {
  if (!deviceId.value) {
    modal.msgWarning('请先选择设备');
    return;
  }
  await request({ url: `/hikvision/access/door/${deviceId.value}/${doorNo.value}/${command}`, method: 'post' });
  modal.msgSuccess('门控制指令已发送');
};

const loadDoors = async () => {
  checkDevice();
  const res = await request({ url: '/hikvision/access/doors/' + deviceId.value, method: 'get' });
  doorsResult.value = typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2);
  tab.value = 'doors';
};

const loadEvents = async () => {
  checkDevice();
  const res = await request({ url: '/hikvision/access/events/' + deviceId.value, method: 'get' });
  eventsResult.value = typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2);
  tab.value = 'events';
};

const loadCards = async () => {
  checkDevice();
  const res = await request({ url: '/hikvision/access/cards/' + deviceId.value, method: 'get' });
  cardsResult.value = typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2);
  tab.value = 'cards';
};

const checkDevice = () => {
  if (!deviceId.value) {
    modal.msgWarning('请先选择设备');
    return;
  }
};

const sendIsapi = async () => {
  if (!isapiDeviceId.value || !isapiUrl.value) {
    modal.msgWarning('请填写设备与 URL');
    return;
  }
  const res = await request({
    url: '/hikvision/config/isapi/' + isapiDeviceId.value,
    method: 'post',
    params: { url: isapiUrl.value },
    data: isapiBody.value || null
  });
  isapiResult.value = res.data ?? '（空响应）';
};

onMounted(() => {
  loadDevices();
});
</script>

<style lang="scss" scoped>
.control-bar {
  margin: 8px 0;
}

.json-box {
  max-height: 320px;
  overflow: auto;
  background: var(--el-fill-color-lighter);
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
