<template>
  <div class="history-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="类型筛选">
          <el-select
            v-model="filterForm.type"
            placeholder="全部类型"
            clearable
            @change="loadRecords"
            style="width: 200px"
          >
            <el-option label="文本创作" value="TEXT" />
            <el-option label="图片生成" value="IMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecords">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div v-loading="loading" class="records-container">
      <el-empty v-if="!loading && records.length === 0" description="暂无创作记录" />
      
      <div v-else class="records-grid">
        <el-card
          v-for="record in records"
          :key="record.id"
          class="record-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <el-tag :type="record.type === 'TEXT' ? 'primary' : 'success'">
                {{ record.type === 'TEXT' ? '文本' : '图片' }}
              </el-tag>
              <span class="create-time">
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
              <el-button
                type="primary"
                size="small"
                @click="copyText(record.resultContent)"
                style="margin-top: 10px"
              >
                <el-icon><CopyDocument /></el-icon>
                复制文案
              </el-button>
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
              />
              <el-button
                v-if="record.imageUrl"
                type="primary"
                size="small"
                @click="copyText(record.imageUrl)"
                style="margin-top: 10px"
              >
                <el-icon><CopyDocument /></el-icon>
                复制链接
              </el-button>
            </div>
          </div>
          
          <template #footer>
            <div class="card-footer">
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(record.id)"
              >
                <el-icon><Delete /></el-icon>
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
import { Search, Refresh, CopyDocument, Delete } from '@element-plus/icons-vue'
import { getRecordList, deleteRecord } from '@/api/creation'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = reactive({
  type: ''
})

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getRecordList({
      type: filterForm.type || undefined,
      current: currentPage.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      records.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载记录失败：', error)
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.type = ''
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
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.records-container {
  min-height: 400px;
}

.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.record-card {
  transition: transform 0.3s;
}

.record-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.create-time {
  color: #909399;
  font-size: 12px;
}

.record-content {
  min-height: 200px;
}

.prompt-section {
  margin-bottom: 15px;
}

.label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
  font-size: 14px;
}

.content {
  color: #303133;
  line-height: 1.6;
  word-break: break-word;
}

.style-section {
  margin-bottom: 15px;
}

.text-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 100px;
  max-height: 300px;
  overflow-y: auto;
}

.image-section {
  text-align: center;
}

.record-image {
  width: 100%;
  max-height: 300px;
  border-radius: 4px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .records-grid {
    grid-template-columns: 1fr;
  }
}
</style>
