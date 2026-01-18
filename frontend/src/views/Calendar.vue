<template>
  <div class="calendar-container">
    <el-card shadow="hover">
      <template #header>
        <div class="calendar-header">
          <h2>创作日历</h2>
          <div class="header-actions">
            <el-button @click="prevMonth">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="current-month">{{ currentMonthText }}</span>
            <el-button @click="nextMonth">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button type="primary" @click="goToToday">今天</el-button>
          </div>
        </div>
      </template>

      <div class="calendar-content">
        <!-- 统计信息 -->
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" :md="6">
              <div class="stat-card">
                <div class="stat-value">{{ stats.totalRecords }}</div>
                <div class="stat-label">本月创作</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="stat-card">
                <div class="stat-value">{{ stats.activeDays }}</div>
                <div class="stat-label">活跃天数</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="stat-card">
                <div class="stat-value">{{ stats.textRecords }}</div>
                <div class="stat-label">文本创作</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="stat-card">
                <div class="stat-value">{{ stats.imageRecords }}</div>
                <div class="stat-label">图片创作</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 日历视图 -->
        <div class="calendar-wrapper">
          <div class="weekdays">
            <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
          </div>
          <div class="calendar-grid">
            <div
              v-for="(date, index) in calendarDays"
              :key="index"
              class="calendar-day"
              :class="{
                'other-month': !date.isCurrentMonth,
                'today': date.isToday,
                'has-records': date.recordCount > 0,
                'selected': date.isSelected
              }"
              @click="selectDate(date)"
            >
              <div class="day-number">{{ date.day }}</div>
              <div v-if="date.recordCount > 0" class="day-records">
                <span class="record-dot" v-for="n in Math.min(date.recordCount, 3)" :key="n"></span>
                <span v-if="date.recordCount > 3" class="record-count">+{{ date.recordCount - 3 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 选中日期的详情 -->
        <div v-if="selectedDate" class="date-details">
          <h3>{{ selectedDateText }}</h3>
          <div v-if="selectedDateRecords.length > 0" class="records-list">
            <el-card
              v-for="record in selectedDateRecords"
              :key="record.id"
              class="record-item"
              shadow="hover"
            >
              <div class="record-header">
                <el-tag :type="record.type === 'TEXT' ? 'primary' : 'success'" size="small">
                  {{ record.type === 'TEXT' ? '文本' : '图片' }}
                </el-tag>
                <span class="record-time">{{ formatTime(record.createTime) }}</span>
              </div>
              <div class="record-content">
                <p v-if="record.type === 'TEXT'" class="record-text">{{ record.resultContent }}</p>
                <el-image
                  v-else-if="record.imageUrl"
                  :src="record.imageUrl"
                  fit="cover"
                  style="width: 100%; max-height: 200px; border-radius: 8px;"
                  :preview-src-list="[record.imageUrl]"
                />
              </div>
              <div class="record-actions">
                <el-button size="small" text @click="viewRecord(record)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button size="small" text @click="editRecord(record)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button size="small" text type="danger" @click="deleteRecord(record)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </el-card>
          </div>
          <el-empty v-else description="这一天还没有创作记录" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, View, Edit, Delete } from '@element-plus/icons-vue'
import { getRecordList } from '@/api/creation'

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const currentDate = ref(new Date())
const selectedDate = ref(null)
const allRecords = ref([])
const loading = ref(false)

// 当前月份文本
const currentMonthText = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})

// 生成日历天数
const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const today = new Date()
  
  // 获取当月第一天是星期几
  const firstDay = new Date(year, month, 1)
  const firstDayWeek = firstDay.getDay()
  
  // 获取当月最后一天
  const lastDay = new Date(year, month + 1, 0)
  const lastDayDate = lastDay.getDate()
  
  // 获取上个月最后几天
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  
  const days = []
  
  // 上个月的日期
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const date = new Date(year, month - 1, prevMonthLastDay - i)
    days.push({
      date,
      day: prevMonthLastDay - i,
      isCurrentMonth: false,
      isToday: false,
      recordCount: getRecordCountForDate(date)
    })
  }
  
  // 当月的日期
  for (let i = 1; i <= lastDayDate; i++) {
    const date = new Date(year, month, i)
    days.push({
      date,
      day: i,
      isCurrentMonth: true,
      isToday: isSameDay(date, today),
      recordCount: getRecordCountForDate(date)
    })
  }
  
  // 下个月的日期（补齐到42天，6行）
  const remainingDays = 42 - days.length
  for (let i = 1; i <= remainingDays; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      date,
      day: i,
      isCurrentMonth: false,
      isToday: false,
      recordCount: getRecordCountForDate(date)
    })
  }
  
  return days
})

// 统计信息
const stats = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  const monthRecords = allRecords.value.filter(record => {
    const recordDate = new Date(record.createTime)
    return recordDate.getFullYear() === year && recordDate.getMonth() === month
  })
  
  const activeDaysSet = new Set()
  monthRecords.forEach(record => {
    const recordDate = new Date(record.createTime)
    activeDaysSet.add(recordDate.getDate())
  })
  
  return {
    totalRecords: monthRecords.length,
    activeDays: activeDaysSet.size,
    textRecords: monthRecords.filter(r => r.type === 'TEXT').length,
    imageRecords: monthRecords.filter(r => r.type === 'IMAGE').length
  }
})

// 选中日期的记录
const selectedDateRecords = computed(() => {
  if (!selectedDate.value) return []
  
  return allRecords.value.filter(record => {
    return isSameDay(new Date(record.createTime), selectedDate.value.date)
  })
})

const selectedDateText = computed(() => {
  if (!selectedDate.value) return ''
  const date = selectedDate.value.date
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

// 方法
const isSameDay = (date1, date2) => {
  return date1.getFullYear() === date2.getFullYear() &&
         date1.getMonth() === date2.getMonth() &&
         date1.getDate() === date2.getDate()
}

const getRecordCountForDate = (date) => {
  return allRecords.value.filter(record => {
    return isSameDay(new Date(record.createTime), date)
  }).length
}

const prevMonth = () => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() - 1)
  currentDate.value = newDate
  selectedDate.value = null
}

const nextMonth = () => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + 1)
  currentDate.value = newDate
  selectedDate.value = null
}

const goToToday = () => {
  currentDate.value = new Date()
  const today = calendarDays.value.find(day => day.isToday)
  if (today) {
    selectDate(today)
  }
}

const selectDate = (date) => {
  if (date.isSelected) {
    selectedDate.value = null
  } else {
    calendarDays.value.forEach(day => {
      day.isSelected = false
    })
    date.isSelected = true
    selectedDate.value = date
  }
}

const formatTime = (timeStr) => {
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const viewRecord = (record) => {
  // 跳转到历史记录页面并定位到该记录
  ElMessage.info('查看记录功能')
}

const editRecord = (record) => {
  ElMessage.info('编辑记录功能')
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning'
    })
    // 调用删除API
    ElMessage.success('删除成功')
    loadRecords()
  } catch {
    // 用户取消
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getRecordList({
      current: 1,
      size: 1000 // 获取足够多的记录
    })
    if (res.code === 200) {
      allRecords.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载记录失败：', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecords()
  // 默认选中今天
  setTimeout(() => {
    const today = calendarDays.value.find(day => day.isToday)
    if (today) {
      selectDate(today)
    }
  }, 100)
})
</script>

<style scoped>
.calendar-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}

:deep(.el-card) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  min-width: 120px;
  text-align: center;
}

.calendar-content {
  padding: 20px 0;
}

.stats-section {
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  color: white;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.calendar-wrapper {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  margin-bottom: 10px;
}

.weekday {
  text-align: center;
  font-weight: 600;
  color: #606266;
  padding: 10px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}

.calendar-day {
  background: white;
  border-radius: 8px;
  padding: 10px;
  min-height: 80px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.calendar-day:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.calendar-day.other-month {
  opacity: 0.3;
}

.calendar-day.today {
  background: #ecf5ff;
  border-color: #409eff;
  font-weight: 600;
}

.calendar-day.has-records {
  background: #f0f9ff;
}

.calendar-day.selected {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.day-number {
  font-size: 16px;
  margin-bottom: 5px;
}

.day-records {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.record-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
}

.calendar-day.selected .record-dot {
  background: white;
}

.record-count {
  font-size: 12px;
  color: #909399;
}

.calendar-day.selected .record-count {
  color: white;
}

.date-details {
  margin-top: 30px;
}

.date-details h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-item {
  transition: all 0.3s;
}

.record-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.record-content {
  margin-bottom: 12px;
}

.record-text {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.record-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .calendar-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .calendar-day {
    min-height: 60px;
    padding: 8px;
  }
  
  .day-number {
    font-size: 14px;
  }
}
</style>