<template>
  <div class="history-container">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><Document /></el-icon>
          <h2>历史记录</h2>
        </div>
        <p class="header-subtitle">查看和管理您的创作历史</p>
      </div>
    </div>
    
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索提示词或内容"
            clearable
            style="width: 300px"
            @keyup.enter="loadRecords"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="类型筛选">
          <el-select
            v-model="filterForm.type"
            placeholder="全部类型"
            clearable
            @change="loadRecords"
            style="width: 150px"
            class="filter-select"
          >
            <el-option label="文本创作" value="TEXT" />
            <el-option label="图片生成" value="IMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="视图">
          <el-radio-group v-model="viewMode" size="default">
            <el-radio-button label="grid">网格</el-radio-button>
            <el-radio-button label="timeline">时间轴</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecords" :icon="Search">
            查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilter" :icon="Refresh">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div v-loading="loading" class="records-container">
      <el-empty v-if="!loading && records.length === 0" description="暂无创作记录" />
      
      <!-- 时间轴视图 -->
      <div v-if="viewMode === 'timeline'" class="timeline-container">
        <el-timeline>
          <el-timeline-item
            v-for="record in records"
            :key="record.id"
            :timestamp="formatTime(record.createTime)"
            placement="top"
            :type="record.type === 'TEXT' ? 'primary' : 'success'"
            :icon="record.type === 'TEXT' ? Document : Picture"
          >
            <el-card class="timeline-card" shadow="hover">
              <div class="timeline-header">
                <el-tag :type="record.type === 'TEXT' ? 'primary' : 'success'" effect="dark">
                  {{ record.type === 'TEXT' ? '文本' : '图片' }}
                </el-tag>
                <el-tag v-if="record.styleTemplate" size="small" type="info" style="margin-left: 8px">
                  {{ record.styleTemplate }}
                </el-tag>
              </div>
              <div class="timeline-content">
                <div class="prompt-text">
                  <strong>提示词：</strong>{{ record.prompt }}
                </div>
                <el-divider />
                <div v-if="record.type === 'TEXT'" class="result-text">
                  {{ record.resultContent }}
                </div>
                <el-image
                  v-else-if="record.imageUrl"
                  :src="record.imageUrl"
                  fit="cover"
                  style="width: 100%; max-height: 300px; border-radius: 8px;"
                  :preview-src-list="[record.imageUrl]"
                />
              </div>
              <div class="timeline-actions">
                <el-button size="small" @click="copyText(record.type === 'TEXT' ? record.resultContent : record.imageUrl)">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(record.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <!-- 网格视图 -->
      <div v-else class="records-grid">
        <el-card
          v-for="record in records"
          :key="record.id"
          class="record-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-tag :type="record.type === 'TEXT' ? 'primary' : 'success'" effect="dark" size="large">
                  <el-icon v-if="record.type === 'TEXT'"><Document /></el-icon>
                  <el-icon v-else><Picture /></el-icon>
                  <span style="margin-left: 4px">{{ record.type === 'TEXT' ? '文本' : '图片' }}</span>
                </el-tag>
                <el-tag v-if="record.styleTemplate" size="small" type="info" style="margin-left: 8px">
                  {{ record.styleTemplate }}
                </el-tag>
              </div>
              <span class="create-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(record.createTime) }}
              </span>
            </div>
          </template>
          
          <div class="record-content">
            <div class="prompt-section">
              <div class="label">提示词：</div>
              <div class="content">{{ record.prompt }}</div>
            </div>
            
            <div v-if="record.styleTemplate" class="style-section">
              <el-tag size="small" type="info">{{ record.styleTemplate }}</el-tag>
            </div>
            
            <el-divider />
            
            <!-- 文本类型 -->
            <div v-if="record.type === 'TEXT'" class="text-section">
              <div class="label">生成内容：</div>
              <div class="text-content">{{ record.resultContent }}</div>
            </div>
            
            <!-- 图片类型 -->
            <div v-else class="image-section">
              <div class="label">生成图片：</div>
              <el-image
                v-if="record.imageUrl"
                :src="record.imageUrl"
                fit="cover"
                class="record-image"
                :preview-src-list="[record.imageUrl]"
                lazy
              />
            </div>
          </div>
          
          <template #footer>
            <div class="card-footer">
              <el-button
                type="primary"
                size="small"
                @click="copyText(record.type === 'TEXT' ? record.resultContent : record.imageUrl)"
                :icon="CopyDocument"
              >
                复制{{ record.type === 'TEXT' ? '文案' : '链接' }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(record.id)"
                :icon="Delete"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-card>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, CopyDocument, Delete, Document, Picture, Clock } from '@element-plus/icons-vue'
import { getRecordList, deleteRecord } from '@/api/creation'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const viewMode = ref('grid') // grid 或 timeline

const filterForm = reactive({
  type: '',
  keyword: ''
})

const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (filterForm.type) {
      params.type = filterForm.type
    }
    if (filterForm.keyword) {
      params.keyword = filterForm.keyword
    }
    
    const res = await getRecordList(params)
    if (res.code === 200) {
      records.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('加载记录成功：', {
        records: records.value.length,
        total: total.value,
        current: currentPage.value,
        size: pageSize.value
      })
    } else {
      ElMessage.error(res.message || '加载记录失败')
      console.error('加载记录失败：', res)
    }
  } catch (error) {
    console.error('加载记录异常：', error)
    ElMessage.error('加载记录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.type = ''
  filterForm.keyword = ''
  currentPage.value = 1
  loadRecords()
}

const copyText = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteRecord(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadRecords()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败：', error)
    }
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.history-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(to bottom, #f8f9ff 0%, #ffffff 100%);
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
  padding: 24px 0;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #667eea;
  font-size: 28px;
}

.header-title h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
  margin-left: 40px;
}

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.filter-form {
  margin: 0;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.records-container {
  min-height: 400px;
}

.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.record-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
}

.record-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-time {
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.record-content {
  min-height: 200px;
}

.prompt-section {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9ff;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.content {
  color: #303133;
  line-height: 1.8;
  word-break: break-word;
  font-size: 14px;
}

.style-section {
  margin-bottom: 16px;
}

.text-content {
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  line-height: 2;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 120px;
  max-height: 350px;
  overflow-y: auto;
  font-size: 14px;
  color: #303133;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.text-content:hover {
  border-color: #667eea;
}

.image-section {
  text-align: center;
  padding: 12px;
}

.record-image {
  width: 100%;
  max-height: 350px;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.record-image:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card__footer) {
  padding: 16px 20px;
  background: #fafafa;
}

.timeline-container {
  padding: 20px 0;
}

.timeline-card {
  margin-left: 20px;
  border-radius: 12px;
  transition: all 0.3s;
}

.timeline-card:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.timeline-header {
  margin-bottom: 12px;
}

.timeline-content {
  margin: 16px 0;
}

.prompt-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.result-text {
  background: #f8f9ff;
  padding: 16px;
  border-radius: 8px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.timeline-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .history-container {
    padding: 16px;
  }
  
  .records-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .header-title h2 {
    font-size: 22px;
  }
  
  .header-subtitle {
    margin-left: 0;
  }
  
  .timeline-card {
    margin-left: 0;
  }
}
</style>
