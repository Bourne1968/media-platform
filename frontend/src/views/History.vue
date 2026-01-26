<template>
  <div class="history-container">
    <!-- 问候语和时间区域 -->
    <div class="greeting-section">
      <div class="greeting-content">
        <h1 class="greeting-text">{{ greeting }}, {{ username }}</h1>
        <p class="date-text">{{ currentDate }}</p>
      </div>
    </div>

    <!-- 快速开始区域 -->
    <div class="quick-start-section">
      <div class="section-header">
        <h2 class="section-title">快速开始</h2>
      </div>
      <div class="quick-cards">
        <div class="quick-card" @click="$router.push({ path: '/workbench', query: { type: 'TEXT' } })">
          <div class="card-icon">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="card-label">写文案</div>
        </div>
        <div class="quick-card" @click="$router.push({ path: '/workbench', query: { type: 'IMAGE' } })">
          <div class="card-icon">
            <el-icon><Picture /></el-icon>
          </div>
          <div class="card-label">做封面</div>
        </div>
        <div class="quick-card" @click="$router.push('/home')">
          <div class="card-icon">
            <el-icon><MagicStick /></el-icon>
          </div>
          <div class="card-label">找灵感</div>
        </div>
        <div class="quick-card" @click="$router.push('/calendar')">
          <div class="card-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="card-label">看数据</div>
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <div class="divider"></div>

    <!-- 最近项目区域 -->
    <div class="recent-projects-section">
      <div class="section-header">
        <h2 class="section-title">最近项目</h2>
        <el-button text @click="viewAllProjects">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="projects-list">
        <div
          v-for="record in recentRecords"
          :key="record.id"
          class="project-item"
          @click="viewProject(record)"
        >
          <div class="project-icon">
            <el-icon v-if="record.type === 'TEXT'"><Document /></el-icon>
            <el-icon v-else><Picture /></el-icon>
          </div>
          <div class="project-content">
            <div class="project-title">{{ getProjectTitle(record) }}</div>
            <div class="project-time">{{ formatRelativeTime(record.createTime) }}</div>
          </div>
          <div class="project-actions">
            <el-button text size="small" @click.stop="copyText(record.type === 'TEXT' ? record.resultContent : record.imageUrl)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
            <el-button text size="small" type="danger" @click.stop="handleDelete(record.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="recentRecords.length === 0" class="empty-projects">
          <el-empty description="暂无创作记录" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <div class="divider"></div>

    <!-- 完整项目列表区域 -->
    <div class="full-projects-section">
      <div class="section-header">
        <h2 class="section-title">全部创作记录</h2>
      </div>

      <!-- 筛选和搜索区域 -->
      <div class="filters-section">
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">内容类型：</label>
            <el-radio-group v-model="filterType" size="small" @change="applyFilters">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="TEXT">文案</el-radio-button>
              <el-radio-button label="IMAGE">封面</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group">
            <label class="filter-label">时间范围：</label>
            <el-select v-model="filterTimeRange" size="small" style="width: 120px" @change="applyFilters">
              <el-option label="全部时间" value="all" />
              <el-option label="今天" value="today" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
              <el-option label="最近3个月" value="3months" />
            </el-select>
          </div>
          <div class="filter-group">
            <label class="filter-label">排序方式：</label>
            <el-select v-model="sortBy" size="small" style="width: 120px" @change="applyFilters">
              <el-option label="创建时间" value="time" />
              <el-option label="标题字母" value="title" />
              <el-option label="类型" value="type" />
            </el-select>
          </div>
        </div>
        <div class="search-row">
          <el-input
            v-model="searchQuery"
            placeholder="搜索创作记录..."
            size="small"
            style="width: 300px"
            @input="applyFilters"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="small" @click="refreshRecords" :loading="refreshing">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 项目列表 -->
      <div class="projects-list-container">
        <div class="projects-grid">
          <div
            v-for="record in filteredRecords"
            :key="record.id"
            class="project-card"
          >
            <div class="project-card-header">
              <div class="project-type">
                <el-tag :type="record.type === 'TEXT' ? 'success' : 'warning'" size="small">
                  {{ record.type === 'TEXT' ? '文案' : '封面' }}
                </el-tag>
              </div>
              <div class="project-time">{{ formatRelativeTime(record.createTime) }}</div>
            </div>
            <div class="project-card-content">
              <div class="project-title">{{ getProjectTitle(record) }}</div>
              <div class="project-preview" v-if="record.type === 'TEXT'">
                {{ getTextPreview(record.resultContent) }}
              </div>
              <div class="project-preview" v-else-if="record.imageUrl">
                <img :src="record.imageUrl" :alt="getProjectTitle(record)" class="preview-image" />
              </div>
              <div class="project-prompt" v-if="record.prompt">
                <span class="prompt-label">创作提示：</span>
                {{ record.prompt.length > 50 ? record.prompt.substring(0, 50) + '...' : record.prompt }}
              </div>
            </div>
            <div class="project-card-footer">
              <div class="project-actions">
                <el-button text size="small" @click="viewProject(record)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button text size="small" @click="editProject(record)">
                  <el-icon><EditPen /></el-icon>
                  编辑
                </el-button>
                <el-button text size="small" @click="copyText(record.type === 'TEXT' ? record.resultContent : record.imageUrl)">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
                <el-dropdown @command="(cmd) => handleProjectAction(cmd, record)" trigger="click">
                  <el-button text size="small">
                    <el-icon><More /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="favorite">
                        <el-icon><Star /></el-icon>
                        收藏
                      </el-dropdown-item>
                      <el-dropdown-item command="export">
                        <el-icon><Download /></el-icon>
                        导出
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" class="danger">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredRecords.length === 0 && !loading" class="empty-state">
          <el-empty
            description="暂无创作记录"
            :image-size="100"
          >
            <el-button type="primary" @click="$router.push('/workbench')">
              立即创作
            </el-button>
          </el-empty>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-skeleton
            v-for="i in 6"
            :key="i"
            :loading="loading"
            animated
            class="project-skeleton"
          >
            <template #template>
              <el-skeleton-item variant="rect" style="width: 100%; height: 200px;" />
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-section" v-if="totalPages > 1">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalRecords"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 分割线 -->
    <div class="divider"></div>

    <!-- 本周统计区域 -->
    <div class="stats-section">
      <div class="section-header">
        <h2 class="section-title">本周统计</h2>
      </div>
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-label">创作次数</div>
          <div class="stat-value">{{ weekStats.total }}</div>
          <div class="stat-change" :class="{ 'increase': weekStats.totalChange > 0, 'decrease': weekStats.totalChange < 0 }">
            {{ weekStats.totalChange > 0 ? '↑' : weekStats.totalChange < 0 ? '↓' : '' }}{{ Math.abs(weekStats.totalChange) }}%
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">完成项目</div>
          <div class="stat-value">{{ weekStats.completed }}</div>
          <div class="stat-change" :class="{ 'increase': weekStats.completedChange > 0, 'decrease': weekStats.completedChange < 0 }">
            {{ weekStats.completedChange > 0 ? '↑' : weekStats.completedChange < 0 ? '↓' : '' }}{{ Math.abs(weekStats.completedChange) }}%
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Picture, MagicStick, TrendCharts, Document, CopyDocument, Delete, ArrowRight, Search, Refresh, View, More, Star, Download } from '@element-plus/icons-vue'
import { getRecordList, deleteRecord } from '@/api/creation'

const router = useRouter()

const username = ref('用户')
const greeting = ref('早上好')
const currentDate = ref('')

const records = ref([])
const loading = ref(false)
const refreshing = ref(false)

// 筛选和搜索
const filterType = ref('')
const filterTimeRange = ref('all')
const sortBy = ref('time')
const searchQuery = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const totalRecords = ref(0)
const totalPages = ref(0)

// 最近项目（显示前6条）
const recentRecords = computed(() => {
  return records.value.slice(0, 6)
})

// 筛选和搜索后的记录
const filteredRecords = computed(() => {
  let filtered = [...records.value]

  // 类型筛选
  if (filterType.value) {
    filtered = filtered.filter(record => record.type === filterType.value)
  }

  // 时间范围筛选
  if (filterTimeRange.value !== 'all') {
    const now = new Date()
    let startDate

    switch (filterTimeRange.value) {
      case 'today':
        startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        break
      case 'week':
        startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
        break
      case 'month':
        startDate = new Date(now.getFullYear(), now.getMonth(), 1)
        break
      case '3months':
        startDate = new Date(now.getFullYear(), now.getMonth() - 3, 1)
        break
      default:
        startDate = null
    }

    if (startDate) {
      filtered = filtered.filter(record => new Date(record.createTime) >= startDate)
    }
  }

  // 关键词搜索
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim()
    filtered = filtered.filter(record => {
      const title = getProjectTitle(record).toLowerCase()
      const prompt = (record.prompt || '').toLowerCase()
      const content = record.type === 'TEXT' ? (record.resultContent || '').toLowerCase() : ''

      return title.includes(query) || prompt.includes(query) || content.includes(query)
    })
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'time':
        return new Date(b.createTime) - new Date(a.createTime)
      case 'title':
        return getProjectTitle(a).localeCompare(getProjectTitle(b))
      case 'type':
        return a.type.localeCompare(b.type)
      default:
        return 0
    }
  })

  // 分页
  totalRecords.value = filtered.length
  totalPages.value = Math.ceil(totalRecords.value / pageSize.value)
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value

  return filtered.slice(startIndex, endIndex)
})

// 本周统计
const weekStats = computed(() => {
  const now = new Date()
  const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - now.getDay())
  weekStart.setHours(0, 0, 0, 0)
  
  const thisWeekRecords = records.value.filter(record => {
    const recordDate = new Date(record.createTime)
    return recordDate >= weekStart
  })

  const lastWeekEnd = new Date(weekStart)
  lastWeekEnd.setDate(lastWeekEnd.getDate() - 1)
  const lastWeekStart = new Date(lastWeekEnd)
  lastWeekStart.setDate(lastWeekStart.getDate() - 6)

  const lastWeekRecords = records.value.filter(record => {
    const recordDate = new Date(record.createTime)
    return recordDate >= lastWeekStart && recordDate <= lastWeekEnd
  })

  const total = thisWeekRecords.length
  const lastWeekTotal = lastWeekRecords.length
  const totalChange = lastWeekTotal > 0 ? Math.round(((total - lastWeekTotal) / lastWeekTotal) * 100) : 0

  const completed = total
  const lastWeekCompleted = lastWeekTotal
  const completedChange = lastWeekCompleted > 0 ? Math.round(((completed - lastWeekCompleted) / lastWeekCompleted) * 100) : 0

  return {
    total,
    completed,
    totalChange,
    completedChange
  }
})

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '深夜好'
}

// 获取当前日期
const getCurrentDate = () => {
  const date = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekday = weekdays[date.getDay()]
  return `今天是 ${year}年${month}月${day}日 ${weekday}`
}

// 获取项目标题
const getProjectTitle = (record) => {
  if (record.type === 'TEXT') {
    const content = record.resultContent || ''
    return content.split('\n')[0] || record.prompt || '文本创作'
  } else {
    return record.prompt || '图片生成'
  }
}

// 格式化相对时间
const formatRelativeTime = (timeStr) => {
  if (!timeStr) return ''
  const now = new Date()
  const time = new Date(timeStr)
  const diff = now - time
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  const year = time.getFullYear()
  const month = time.getMonth() + 1
  const day = time.getDate()
  return `${year}-${month}-${day}`
}

const loadRecords = async () => {
  loading.value = true
  try {
    // 获取所有记录用于本地筛选和搜索
    const res = await getRecordList({ current: 1, size: 1000 })
    if (res.code === 200) {
      records.value = (res.data.records || []).sort((a, b) => {
        return new Date(b.createTime) - new Date(a.createTime)
      })
    }
  } catch (error) {
    console.error('加载记录失败：', error)
  } finally {
    loading.value = false
  }
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

const viewProject = (record) => {
  // 显示项目详情弹窗
  showProjectDetail(record)
}

// 显示项目详情
const showProjectDetail = (record) => {
  const content = record.type === 'TEXT' 
    ? `<div style="text-align: left; max-height: 500px; overflow-y: auto;">
         <div style="margin-bottom: 16px;">
           <el-tag type="${record.type === 'TEXT' ? 'success' : 'warning'}" size="small" style="margin-right: 8px;">
             ${record.type === 'TEXT' ? '文案' : '封面'}
           </el-tag>
           <span style="font-size: 12px; color: #909399;">${formatDateTime(record.createTime)}</span>
         </div>
         <h3 style="margin-top: 0; margin-bottom: 12px; color: #303133; font-size: 16px;">创作提示</h3>
         <div style="margin-bottom: 20px; padding: 12px; background: #f5f7fa; border-radius: 8px; color: #606266; line-height: 1.6;">
           ${record.prompt || '无提示词'}
         </div>
         <h3 style="margin-top: 0; margin-bottom: 12px; color: #303133; font-size: 16px;">生成内容</h3>
         <div style="padding: 12px; background: #f5f7fa; border-radius: 8px; white-space: pre-wrap; line-height: 1.8; color: #303133; font-size: 14px;">
           ${record.resultContent || '无内容'}
         </div>
       </div>`
    : `<div style="text-align: left;">
         <div style="margin-bottom: 16px;">
           <el-tag type="${record.type === 'TEXT' ? 'success' : 'warning'}" size="small" style="margin-right: 8px;">
             ${record.type === 'TEXT' ? '文案' : '封面'}
           </el-tag>
           <span style="font-size: 12px; color: #909399;">${formatDateTime(record.createTime)}</span>
         </div>
         <h3 style="margin-top: 0; margin-bottom: 12px; color: #303133; font-size: 16px;">创作提示</h3>
         <div style="margin-bottom: 20px; padding: 12px; background: #f5f7fa; border-radius: 8px; color: #606266; line-height: 1.6;">
           ${record.prompt || '无提示词'}
         </div>
         <h3 style="margin-top: 0; margin-bottom: 12px; color: #303133; font-size: 16px;">生成的图片</h3>
         <div style="text-align: center; margin-bottom: 12px;">
           <img src="${record.imageUrl}" alt="生成的图片" style="max-width: 100%; max-height: 400px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);" />
         </div>
       </div>`

  ElMessageBox.alert(
    content,
    '项目详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      customClass: 'project-detail-dialog',
      width: '600px'
    }
  )
}

// 格式化日期时间
const formatDateTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const viewAllProjects = () => {
  // 滚动到完整项目列表区域
  document.querySelector('.full-projects-section').scrollIntoView({
    behavior: 'smooth'
  })
}

// 获取文本预览
const getTextPreview = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 应用筛选
const applyFilters = () => {
  currentPage.value = 1 // 重置到第一页
}

// 刷新记录
const refreshRecords = async () => {
  refreshing.value = true
  await loadRecords()
  refreshing.value = false
  ElMessage.success('记录已刷新')
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 编辑项目
const editProject = (record) => {
  const route = record.type === 'TEXT' ? '/workbench' : '/cover-design'
  router.push({
    path: route,
    query: {
      editId: record.id,
      prompt: record.prompt || ''
    }
  })
}

// 处理项目操作
const handleProjectAction = (command, record) => {
  switch (command) {
    case 'favorite':
      // 这里可以调用收藏API
      ElMessage.success('已添加到收藏')
      break
    case 'export':
      // 这里可以调用导出API
      ElMessage.success('导出功能开发中...')
      break
    case 'delete':
      handleDelete(record.id)
      break
  }
}

onMounted(() => {
  // 获取用户信息
  const userInfo = sessionStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.username || '用户'
    } catch (e) {
      username.value = '用户'
    }
  }

  // 设置问候语和时间
  greeting.value = getGreeting()
  currentDate.value = getCurrentDate()

  // 加载记录
  loadRecords()
})
</script>

<style scoped>
.history-container {
  padding: 32px 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}

/* 问候语区域 */
.greeting-section {
  margin-bottom: 32px;
}

.greeting-content {
  padding: 24px 0;
}

.greeting-text {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.date-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 400;
  transition: color 0.3s ease;
}

/* 快速开始区域 */
.quick-start-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: color 0.3s ease;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.quick-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 28px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.quick-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.quick-card:hover::before {
  opacity: 1;
}

.quick-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}

.quick-card:nth-child(1) .card-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.quick-card:nth-child(2) .card-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.quick-card:nth-child(3) .card-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.quick-card:nth-child(4) .card-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  border-radius: 12px;
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
}

.quick-card:hover .card-icon {
  transform: scale(1.1);
}

.card-label {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
  position: relative;
  z-index: 1;
  transition: color 0.3s ease;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, var(--border-color) 20%, var(--border-color) 80%, transparent 100%);
  margin: 32px 0;
  transition: background 0.3s ease;
}

/* 最近项目区域 */
.recent-projects-section {
  margin-bottom: 30px;
}

.projects-list {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.3s ease, background 0.3s ease, border-color 0.3s ease;
}

.projects-list:hover {
  box-shadow: var(--shadow-md);
}

.project-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.project-item::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.project-item:last-child {
  border-bottom: none;
}

.project-item:hover {
  background: var(--bg-hover);
  padding-left: 28px;
}

.project-item:hover::after {
  opacity: 1;
}

.project-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e0eaff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  font-size: 22px;
  margin-right: 16px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.project-item:hover .project-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.05);
}

.project-content {
  flex: 1;
  min-width: 0;
}

.project-title {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.project-item:hover .project-title {
  color: var(--primary-color);
}

.project-time {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.3s ease;
}

.project-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.project-item:hover .project-actions {
  opacity: 1;
}

.empty-projects {
  padding: 40px 20px;
  text-align: center;
}

/* 完整项目列表区域 */
.full-projects-section {
  margin-bottom: 30px;
}

/* 筛选和搜索区域 */
.filters-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.filter-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  transition: color 0.3s ease;
}

.search-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 项目列表容器 */
.projects-list-container {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 0;
}

/* 项目卡片 */
.project-card {
  border-bottom: 1px solid var(--border-light);
  border-right: 1px solid var(--border-light);
  padding: 20px;
  transition: all 0.3s ease;
}

.project-card:hover {
  background: var(--bg-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.project-card:last-child {
  border-bottom: none;
}

.project-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-type .el-tag {
  font-weight: 500;
}

.project-time {
  font-size: 12px;
  color: #909399;
}

.project-card-content {
  margin-bottom: 16px;
}

.project-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.project-preview {
  margin-bottom: 12px;
  color: var(--text-regular);
  line-height: 1.6;
  font-size: 14px;
  transition: color 0.3s ease;
}

.preview-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  transition: border-color 0.3s ease;
}

.project-prompt {
  font-size: 12px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.prompt-label {
  font-weight: 500;
  color: var(--text-regular);
  transition: color 0.3s ease;
}

.project-card-footer {
  border-top: 1px solid var(--border-light);
  padding-top: 12px;
  transition: border-color 0.3s ease;
}

.project-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* 加载状态 */
.loading-state {
  padding: 20px;
}

.project-skeleton {
  margin-bottom: 20px;
}

/* 分页 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 本周统计区域 */
.stats-section {
  margin-bottom: 30px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 28px 24px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
  border-color: var(--primary-color);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  font-weight: 400;
  letter-spacing: 0.3px;
  transition: color 0.3s ease;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
  line-height: 1.2;
}

.stat-change {
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  background: rgba(103, 194, 58, 0.1);
}

.stat-change.increase {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.stat-change.decrease {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

@media (max-width: 1024px) {
  .quick-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .history-container {
    padding: 20px 16px;
  }

  .greeting-section {
    margin-bottom: 24px;
  }

  .greeting-text {
    font-size: 26px;
  }

  .date-text {
    font-size: 14px;
  }

  .section-header {
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 20px;
  }

  .quick-start-section,
  .recent-projects-section,
  .stats-section {
    margin-bottom: 24px;
  }

  .quick-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .quick-card {
    padding: 20px 16px;
  }

  .card-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
    margin-bottom: 12px;
  }

  .card-label {
    font-size: 14px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-card {
    padding: 24px 20px;
  }

  .stat-value {
    font-size: 32px;
  }

  .project-item {
    padding: 16px 20px;
  }

  .project-item:hover {
    padding-left: 24px;
  }

  .project-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
    margin-right: 12px;
  }

  .project-title {
    font-size: 15px;
  }

  .project-time {
    font-size: 12px;
  }

  .divider {
    margin: 24px 0;
  }

  /* 筛选和搜索响应式 */
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .search-row {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-row .el-input {
    width: 100% !important;
  }

  /* 项目卡片响应式 */
  .projects-grid {
    grid-template-columns: 1fr;
  }

  .project-card {
    border-right: none;
  }

  .project-actions {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .history-container {
    padding: 16px 12px;
  }

  .greeting-text {
    font-size: 22px;
  }

  .quick-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .section-title {
    font-size: 18px;
  }

  .stat-value {
    font-size: 28px;
  }
}
</style>
