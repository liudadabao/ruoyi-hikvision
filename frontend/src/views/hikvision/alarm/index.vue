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
        <el-form :model="queryParams" :inline="true" class="query-form">
          <el-form-item label="报警时间">
            <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="-"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 360px"
            />
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
        <div class="table-heading">
          <span class="panel-kicker">Alarm Records</span>
          <h3>报警记录</h3>
        </div>
      </template>

      <el-table :data="alarmList" :border="false">
        <el-table-column label="报警命令" prop="commandName" min-width="180" show-overflow-tooltip />
        <el-table-column label="命令类型" prop="command" width="110" align="center">
          <template #default="{ row }">0x{{ Number(row.command).toString(16).toUpperCase() }}</template>
        </el-table-column>
        <el-table-column label="报警设备IP" prop="deviceIp" min-width="130" />
        <el-table-column label="报警时间" prop="alarmTime" min-width="170" />
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup name="HikAlarm" lang="ts">
import { listAlarmRecord } from '@/api/hikvision';
import type { HikAlarmRecordQuery, HikAlarmRecordVO } from '@/api/hikvision/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';

const alarmList = ref<HikAlarmRecordVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);
const dateRange = ref<[string, string]>();

const queryParams = reactive<HikAlarmRecordQuery & { params?: any }>({
  pageNum: 1,
  pageSize: 10
});

const getList = async () => {
  await withLoading(async () => {
    const res = await listAlarmRecord({
      ...queryParams,
      params: dateRange.value ? { beginTime: dateRange.value[0], endTime: dateRange.value[1] } : undefined
    });
    alarmList.value = res.data?.rows;
    total.value = res.data?.total;
  });
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  dateRange.value = undefined;
  handleQuery();
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
