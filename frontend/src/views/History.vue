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
import { EditPen, Picture, MagicStick, TrendCharts, Document, CopyDocument, Delete, ArrowRight } from '@element-plus/icons-vue'
import { getRecordList, deleteRecord } from '@/api/creation'

const router = useRouter()

const username = ref('用户')
const greeting = ref('早上好')
const currentDate = ref('')

const records = ref([])
const loading = ref(false)

// 最近项目（显示前6条）
const recentRecords = computed(() => {
  return records.value.slice(0, 6)
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
  // 可以跳转到详情页或显示详情弹窗
  ElMessage.info('查看项目详情功能开发中...')
}

const viewAllProjects = () => {
  // 这里可以跳转到完整的项目列表页面，或者展开显示更多
  ElMessage.info('查看更多项目功能开发中...')
}

onMounted(() => {
  // 获取用户信息
  const userInfo = localStorage.getItem('userInfo')
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
  color: #303133;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.date-text {
  font-size: 16px;
  color: #909399;
  margin: 0;
  font-weight: 400;
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
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.quick-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 28px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
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
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
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
  color: #303133;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #e4e7ed 20%, #e4e7ed 80%, transparent 100%);
  margin: 32px 0;
}

/* 最近项目区域 */
.recent-projects-section {
  margin-bottom: 30px;
}

.projects-list {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;
}

.projects-list:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.project-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #f5f7fa;
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
  background: linear-gradient(90deg, #f8f9ff 0%, rgba(248, 249, 255, 0.5) 100%);
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
  color: #303133;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.project-item:hover .project-title {
  color: #667eea;
}

.project-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
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
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 28px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
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
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  transform: translateY(-4px);
  border-color: #c5d7ff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
  font-weight: 400;
  letter-spacing: 0.3px;
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
