<template>
  <div class="p-2 app-container">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="tree-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-kicker">Devices</span>
              <h3>设备通道</h3>
            </div>
          </template>
          <el-input v-model="filterText" placeholder="输入关键字过滤" clearable class="mb-2" />
          <el-scrollbar height="600px">
            <el-tree
              :data="treeData"
              :props="{ label: 'label', children: 'children' }"
              node-key="key"
              :filter-node-method="filterNode"
              default-expand-all
              @node-click="handleNodeClick"
            />
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card shadow="hover" class="player-panel">
          <template #header>
            <div class="toolbar-shell">
              <div class="table-heading">
                <span class="panel-kicker">Live Preview</span>
                <h3>实时预览{{ current ? ' - ' + current.label : '' }}</h3>
              </div>
              <div class="toolbar-actions">
                <el-select v-model="streamType" style="width: 120px" class="mr-2">
                  <el-option label="主码流" :value="0" />
                  <el-option label="子码流" :value="1" />
                </el-select>
                <el-button type="primary" icon="VideoPlay" :disabled="!current" @click="handleStart">开始播放</el-button>
                <el-button type="danger" plain icon="VideoPause" :disabled="!playing" @click="handleStop">停止</el-button>
              </div>
            </div>
          </template>

          <div class="video-wrap">
            <video v-if="playUrl" :key="playUrl" class="video-box" controls autoplay muted :src="playUrl"></video>
            <el-empty v-else description="请选择左侧通道并点击开始播放" />
          </div>

          <el-divider content-position="left">播放地址</el-divider>
          <el-descriptions v-if="playUrls" :column="2" border size="small">
            <el-descriptions-item label="HTTP-FLV">{{ playUrls.flv }}</el-descriptions-item>
            <el-descriptions-item label="HLS">{{ playUrls.hls }}</el-descriptions-item>
            <el-descriptions-item label="RTSP">{{ playUrls.rtsp }}</el-descriptions-item>
            <el-descriptions-item label="WebRTC">{{ playUrls.webrtc }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="HikPreview" lang="ts">
import { listAllDevice, startPreview, stopPreview } from '@/api/hikvision';
import type { HikDeviceVO } from '@/api/hikvision/types';
import modal from '@/plugins/modal';

interface TreeNode {
  key: string;
  label: string;
  deviceId?: number;
  channelNo?: number;
  children?: TreeNode[];
}

const filterText = ref('');
const streamType = ref(0);
const treeData = ref<TreeNode[]>([]);
const current = ref<TreeNode | null>(null);
const playUrl = ref('');
const playUrls = ref<Record<string, string>>();
const playing = ref(false);

const filterNode = (value: string, data: TreeNode) => {
  if (!value) return true;
  return data.label.includes(value);
};

const loadDevices = async () => {
  const res = await listAllDevice({});
  const devices = res.data || [];
  treeData.value = devices.map((d: HikDeviceVO) => ({
    key: 'd' + d.deviceId,
    label: `${d.deviceName} (${d.deviceIp}) ${d.status === 'online' ? '●在线' : '○离线'}`,
    deviceId: Number(d.deviceId),
    children: Array.from({ length: Math.max(0, (d.channelNum ?? 0) + (d.ipChannelNum ?? 0)) }, (_, i) => ({
      key: 'c' + d.deviceId + '_' + (i + 1),
      label: `通道 ${i + 1}`,
      channelNo: i + 1
    }))
  }));
};

const handleNodeClick = (data: TreeNode) => {
  if (data.channelNo != null) {
    current.value = data;
  }
};

const handleStart = async () => {
  if (!current.value) {
    modal.msgWarning('请先选择通道');
    return;
  }
  const res = await startPreview(current.value.deviceId!, current.value.channelNo!, streamType.value);
  const info = res.data;
  playUrls.value = info.playUrls;
  playUrl.value = info.playUrls?.hls || info.rtspUrl;
  playing.value = true;
};

const handleStop = async () => {
  if (current.value) {
    await stopPreview(current.value.deviceId!, current.value.channelNo!, streamType.value);
  }
  playUrl.value = '';
  playUrls.value = undefined;
  playing.value = false;
};

watch(filterText, (val) => {
  // el-tree 过滤
});

onMounted(() => {
  loadDevices();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;

.video-wrap {
  height: 360px;
  background: #000;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.video-box {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style>
