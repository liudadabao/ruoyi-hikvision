<template>
  <div class="p-2 app-container">
    <div class="search-wrap">
      <el-card shadow="hover" class="search-panel" :class="{ 'is-collapsed': !showSearch }">
        <template #header>
          <div class="panel-heading search-panel-toggle" @click.stop="showSearch = !showSearch">
            <div>
              <span class="panel-kicker">Search Filters</span>
              <h3>筛选条件</h3>
            </div>
          </div>
        </template>
        <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="query-form">
          <el-form-item label="设备名称" prop="deviceName">
            <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="设备IP" prop="deviceIp">
            <el-input v-model="queryParams.deviceIp" placeholder="请输入设备IP" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card v-loading="loading" shadow="hover" class="table-panel">
      <template #header>
        <div class="toolbar-shell">
          <div class="table-heading">
            <span class="panel-kicker">Device Dataset</span>
            <h3>设备列表</h3>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['hikvision:device:add']" type="primary" plain icon="Plus" @click="handleAdd">
              新增
            </el-button>
            <right-toolbar v-model:show-search="showSearch" :search="false" @query-table="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table :data="deviceList" :border="false">
        <el-table-column label="设备名称" prop="deviceName" min-width="140" show-overflow-tooltip />
        <el-table-column label="设备IP" prop="deviceIp" min-width="120" />
        <el-table-column label="端口" prop="port" width="80" align="center" />
        <el-table-column label="账号" prop="username" width="100" />
        <el-table-column label="序列号" prop="serialNumber" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.serialNumber || '-' }}</template>
        </el-table-column>
        <el-table-column label="通道数" min-width="90" align="center">
          <template #default="{ row }">{{ row.channelCount ?? (row.channelNum + row.ipChannelNum) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'info'">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'online'"
              v-hasPermi="['hikvision:device:edit']"
              link
              type="success"
              icon="Link"
              @click="handleLogin(row)"
            >登录</el-button>
            <el-button
              v-else
              v-hasPermi="['hikvision:device:edit']"
              link
              type="warning"
              icon="SwitchButton"
              @click="handleLogout(row)"
            >登出</el-button>
            <el-button v-hasPermi="['hikvision:device:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)" />
            <el-button v-hasPermi="['hikvision:device:remove']" link type="danger" icon="Delete" @click="handleDelete(row)" />
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px" append-to-body>
      <el-form ref="deviceFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备IP" prop="deviceIp">
          <el-input v-model="form.deviceIp" placeholder="请输入设备IP" />
        </el-form-item>
        <el-form-item label="端口号" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" controls-position="right" />
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HikDevice" lang="ts">
import { addDevice, delDevice, getDevice, listDevice, loginDevice, logoutDevice, updateDevice } from '@/api/hikvision';
import type { HikDeviceForm, HikDeviceQuery, HikDeviceVO } from '@/api/hikvision/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';

const deviceList = ref<HikDeviceVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const deviceFormRef = ref<ElFormInstance>();

const initFormData: HikDeviceForm = {
  deviceId: undefined,
  deviceName: '',
  deviceIp: '',
  port: 8000,
  username: 'admin',
  password: '',
  manufacturer: 'hikvision',
  remark: ''
};

const data = reactive<PageData<HikDeviceForm, HikDeviceQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, deviceName: '', deviceIp: '' },
  rules: {
    deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
    deviceIp: [{ required: true, message: '设备IP不能为空', trigger: 'blur' }],
    port: [{ required: true, message: '端口不能为空', trigger: 'blur' }],
    username: [{ required: true, message: '账号不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);
const { dialog, resetForm, openDialog, showDialog, closeDialog } = useFormDialog({
  form,
  formRef: deviceFormRef,
  initialFormData: initFormData
});
const { resetQuery } = useSearchReset({
  queryFormRef,
  queryParams,
  pageNumKey: 'pageNum',
  afterReset: handleQuery
});

const getList = async () => {
  await withLoading(async () => {
    const res = await listDevice({ ...queryParams.value });
    deviceList.value = res.data?.rows;
    total.value = res.data?.total;
  });
};

const cancel = () => {
  closeDialog();
  resetForm();
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const handleAdd = () => {
  openDialog('添加设备');
};

const handleUpdate = async (row?: Partial<HikDeviceVO>) => {
  resetForm();
  const res = await getDevice(row?.deviceId!);
  Object.assign(form.value, res.data);
  showDialog('修改设备');
};

const submitForm = () => {
  deviceFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      form.value.deviceId ? await updateDevice(form.value) : await addDevice(form.value);
      modal.msgSuccess('操作成功');
      closeDialog();
      await getList();
    }
  });
};

const handleDelete = async (row?: Partial<HikDeviceVO>) => {
  await modal.confirm('是否确认删除设备"' + row?.deviceName + '"？');
  await delDevice(row?.deviceId!);
  await getList();
  modal.msgSuccess('删除成功');
};

const handleLogin = async (row?: Partial<HikDeviceVO>) => {
  await modal.confirm('确认登录设备"' + row?.deviceName + '"？');
  await loginDevice(row?.deviceId!);
  modal.msgSuccess('登录成功');
  await getList();
};

const handleLogout = async (row?: Partial<HikDeviceVO>) => {
  await modal.confirm('确认登出设备"' + row?.deviceName + '"？');
  await logoutDevice(row?.deviceId!);
  modal.msgSuccess('登出成功');
  await getList();
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
