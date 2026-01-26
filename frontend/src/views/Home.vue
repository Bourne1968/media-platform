<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-greeting">
          <h1 class="welcome-title">{{ welcomeText }}{{ username ? `，${username}` : '' }}</h1>
          <p class="welcome-subtitle">今天想创作什么内容呢？</p>
        </div>
        <div class="welcome-stats">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalWorks || 0 }}</div>
            <div class="stat-label">总作品数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.todayWorks || 0 }}</div>
            <div class="stat-label">今日创作</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalWords || 0 }}</div>
            <div class="stat-label">累计字数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷功能区 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><Lightning /></el-icon>
          快捷功能
        </h2>
      </div>
      <el-row :gutter="20" class="quick-actions">
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="6">
          <el-card class="quick-card" shadow="hover" @click="goToWorkbench('TEXT')">
            <div class="quick-icon text-icon">
              <el-icon :size="40"><EditPen /></el-icon>
            </div>
            <div class="quick-title">文案助手</div>
            <div class="quick-desc">生成爆款标题和文案</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="6">
          <el-card class="quick-card" shadow="hover" @click="$router.push('/cover-design')">
            <div class="quick-icon image-icon">
              <el-icon :size="40"><Picture /></el-icon>
            </div>
            <div class="quick-title">封面设计</div>
            <div class="quick-desc">AI生成精美封面图</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="6">
          <el-card class="quick-card" shadow="hover" @click="$router.push('/inspiration')">
            <div class="quick-icon inspiration-icon">
              <el-icon :size="40"><Star /></el-icon>
            </div>
            <div class="quick-title">灵感中心</div>
            <div class="quick-desc">发现热门话题和案例</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="6">
          <el-card class="quick-card" shadow="hover" @click="$router.push('/history')">
            <div class="quick-icon history-icon">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="quick-title">创作库</div>
            <div class="quick-desc">查看历史作品</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 今日热门话题 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><TrendCharts /></el-icon>
          今日热门话题
        </h2>
        <el-button text @click="refreshTopics" :loading="topicsLoading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="topics-container">
        <div
          v-for="(topic, index) in hotTopics"
          :key="index"
          class="topic-card"
          :class="`heat-${topic.heatLevel}`"
          @click="useTopic(topic)"
        >
          <div class="topic-header">
            <span class="topic-title">{{ topic.title }}</span>
            <span class="topic-heat" :class="`heat-${topic.heatLevel}`">
              {{ topic.heatText }}
            </span>
          </div>
          <div class="topic-desc">{{ topic.description }}</div>
          <div class="topic-tags">
            <el-tag
              v-for="tag in topic.tags"
              :key="tag"
              size="small"
              :type="getTagType(tag)"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 随机灵感生成器 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><MagicStick /></el-icon>
          随机灵感生成器
        </h2>
        <el-button type="primary" @click="generateInspiration" :loading="inspirationLoading">
          <el-icon><Refresh /></el-icon>
          生成灵感
        </el-button>
      </div>
      <div v-loading="inspirationLoading" class="inspiration-container">
        <div v-if="currentInspiration" class="inspiration-card">
          <div class="inspiration-content">
            <h3>{{ currentInspiration.title }}</h3>
            <p>{{ currentInspiration.description }}</p>
            <div class="inspiration-actions">
              <el-button type="primary" @click="useInspiration(currentInspiration)">
                <el-icon><EditPen /></el-icon>
                使用这个灵感
              </el-button>
              <el-button @click="generateInspiration">
                <el-icon><Refresh /></el-icon>
                换一个
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else-if="!inspirationLoading" description="点击按钮生成创作灵感" />
      </div>
    </div>

    <!-- 节日/热点提醒 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><Bell /></el-icon>
          节日热点提醒
        </h2>
        <el-button text @click="loadUpcomingEvents(true)" :loading="eventsLoading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div v-loading="eventsLoading" class="events-container">
        <div
          v-for="event in upcomingEvents"
          :key="event.id"
          class="event-card"
          :class="event.type"
        >
          <div class="event-date">{{ event.date }}</div>
          <div class="event-title">{{ event.title }}</div>
          <div class="event-desc">{{ event.description }}</div>
          <el-button size="small" text @click="useEvent(event)">
            使用这个主题
          </el-button>
        </div>
        <el-empty v-if="!eventsLoading && upcomingEvents.length === 0" description="暂无节日热点提醒" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen,
  Picture,
  Document,
  Calendar,
  TrendCharts,
  Refresh,
  MagicStick,
  Bell,
  Lightning,
  Star
} from '@element-plus/icons-vue'
import { generateText } from '@/api/ai'

const router = useRouter()

// 用户名
const username = ref('')
const stats = ref({
  totalWorks: 0,
  todayWorks: 0,
  totalWords: 0
})

// 欢迎语（根据时间变化）
const welcomeText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，还在创作吗？'
  if (hour < 12) return '早上好！'
  if (hour < 18) return '下午好！'
  return '晚上好！'
})

// 热门话题
const hotTopics = ref([
  {
    title: '春节营销策略',
    description: '抓住春节流量，打造爆款内容',
    tags: ['节日', '营销', '热点'],
    heatLevel: 'high',
    heatText: '🔥 超热'
  },
  {
    title: '短视频脚本创作',
    description: '3分钟短视频如何抓住观众注意力',
    tags: ['视频', '脚本', '技巧'],
    heatLevel: 'medium',
    heatText: '⭐ 热门'
  },
  {
    title: '小红书封面设计',
    description: '高点击率封面的设计要点',
    tags: ['设计', '小红书', '封面'],
    heatLevel: 'medium',
    heatText: '⭐ 热门'
  },
  {
    title: 'AI工具使用技巧',
    description: '如何用AI提升创作效率',
    tags: ['AI', '工具', '效率'],
    heatLevel: 'low',
    heatText: '💡 推荐'
  }
])

const topicsLoading = ref(false)

// 随机灵感
const currentInspiration = ref(null)
const inspirationLoading = ref(false)

// 节日/热点事件
const upcomingEvents = ref([])
const eventsLoading = ref(false)

// 方法
const goToWorkbench = (type) => {
  router.push({
    path: '/workbench',
    query: { type }
  })
}

const refreshTopics = async () => {
  if (topicsLoading.value) return // 防止重复点击
  
  topicsLoading.value = true
  try {
    // 使用AI生成最新话题（只生成4个）
    const res = await generateText({
      prompt: '给我4个当前最热门的内容创作话题，格式：标题|描述|标签（用逗号分隔），每行一个话题',
      style: '简洁'
    })
    
    if (res.code === 200 && res.data?.content) {
      // 解析AI返回的话题
      const lines = res.data.content.split('\n').filter(line => line.trim())
      const newTopics = lines.slice(0, 4).map((line, index) => {
        const parts = line.split('|')
        return {
          title: parts[0]?.trim() || `热门话题${index + 1}`,
          description: parts[1]?.trim() || '热门内容创作主题',
          tags: parts[2]?.split(',').map(t => t.trim()).filter(t => t) || ['热门'],
          heatLevel: index < 2 ? 'high' : index < 3 ? 'medium' : 'low',
          heatText: index < 2 ? '🔥 超热' : index < 3 ? '⭐ 热门' : '💡 推荐'
        }
      })
      
      if (newTopics.length > 0) {
        hotTopics.value = newTopics
        // 保存到缓存
        sessionStorage.setItem('homeHotTopics', JSON.stringify(hotTopics.value))
        ElMessage.success('话题已刷新')
      } else {
        ElMessage.warning('未获取到新话题，请稍后再试')
      }
    } else {
      ElMessage.warning('刷新失败，请稍后再试')
    }
  } catch (error) {
    console.error('刷新话题失败:', error)
    ElMessage.error('刷新失败，请稍后再试')
  } finally {
    topicsLoading.value = false
  }
}

// 加载节日热点提醒（实时获取）
const loadUpcomingEvents = async (forceRefresh = false) => {
  if (eventsLoading.value) return
  
  // 如果不是强制刷新，先尝试从缓存加载
  if (!forceRefresh) {
    const cachedEvents = sessionStorage.getItem('homeUpcomingEvents')
    if (cachedEvents) {
      try {
        upcomingEvents.value = JSON.parse(cachedEvents)
        return
      } catch (e) {
        console.error('加载缓存事件失败:', e)
      }
    }
  }
  
  eventsLoading.value = true
  try {
    // 固定使用2026年的日期
    const currentYear = 2026
    // 获取系统当前日期，但用于2026年
    const today = new Date()
    const currentMonth = today.getMonth() + 1
    const currentDay = today.getDate()
    
    // 使用AI获取当前和未来一个月的节日热点（明确指定2026年）
    // 注意：这里假设今天是2026年的同一天
    // 重要：2026年春节是2月17日（农历正月初一），请确保日期准确
    const res = await generateText({
      prompt: `假设今天是2026年${currentMonth}月${currentDay}日，给我未来一个月内（2026年）的节日和热点事件。重要提示：2026年春节是2月17日（农历正月初一），请确保所有日期都是2026年的准确日期。格式：日期（如"2026年2月17日"或"2026年2月14日"，必须包含2026年）|节日名称|描述，每行一个`,
      style: '简洁'
    })
    
    if (res.code === 200 && res.data?.content) {
      const lines = res.data.content.split('\n').filter(line => line.trim())
      const events = lines.slice(0, 5).map((line, index) => {
        const parts = line.split('|')
        // 确保日期格式正确，强制使用2026年
        let dateStr = parts[0]?.trim() || ''
        if (dateStr) {
          // 提取月份和日期
          const dateMatch = dateStr.match(/(\d{4})?年?(\d+)月(\d+)日/)
          if (dateMatch) {
            const month = parseInt(dateMatch[2])
            const day = parseInt(dateMatch[3])
            // 强制使用2026年
            dateStr = `2026年${month}月${day}日`
          } else {
            // 如果格式不匹配，尝试只匹配月日
            const monthDayMatch = dateStr.match(/(\d+)月(\d+)日/)
            if (monthDayMatch) {
              const month = parseInt(monthDayMatch[1])
              const day = parseInt(monthDayMatch[2])
              dateStr = `2026年${month}月${day}日`
            } else {
              // 如果都匹配不上，计算未来日期
              const futureDate = new Date(2026, currentMonth - 1, currentDay + index)
              const futureMonth = futureDate.getMonth() + 1
              const futureDay = futureDate.getDate()
              dateStr = `2026年${futureMonth}月${futureDay}日`
            }
          }
        } else {
          // 如果没有日期，计算未来日期
          const futureDate = new Date(2026, currentMonth - 1, currentDay + index)
          const futureMonth = futureDate.getMonth() + 1
          const futureDay = futureDate.getDate()
          dateStr = `2026年${futureMonth}月${futureDay}日`
        }
        
        const title = parts[1]?.trim() || '节日'
        
        // 验证关键节日的日期是否正确
        // 2026年春节是2月17日，如果AI返回的春节日期不对，使用默认数据
        if (title.includes('春节') && dateStr !== '2026年2月17日') {
          console.warn('AI返回的春节日期不准确，将使用默认数据')
          return null // 标记为无效，稍后使用默认数据
        }
        
        return {
          id: index + 1,
          date: dateStr || `2026年${currentMonth}月${currentDay + index}日`,
          title: title,
          description: parts[2]?.trim() || '适合发布相关内容',
          type: 'festival'
        }
      }).filter(event => event !== null) // 过滤掉无效的事件
      
      // 如果有效事件数量不足，或者包含明显错误的日期，使用默认数据
      if (events.length < 3) {
        console.warn('AI返回的事件数量不足或包含错误，使用默认数据')
        loadDefaultEvents()
      } else {
        upcomingEvents.value = events
        // 保存到缓存
        sessionStorage.setItem('homeUpcomingEvents', JSON.stringify(upcomingEvents.value))
      }
    } else {
      loadDefaultEvents()
      // 保存到缓存
      sessionStorage.setItem('homeUpcomingEvents', JSON.stringify(upcomingEvents.value))
    }
  } catch (error) {
    console.error('加载节日热点失败:', error)
    // 如果AI加载失败，使用默认数据
    loadDefaultEvents()
    // 保存到缓存
    sessionStorage.setItem('homeUpcomingEvents', JSON.stringify(upcomingEvents.value))
  } finally {
    eventsLoading.value = false
  }
}

// 加载默认节日数据（作为降级方案）
const loadDefaultEvents = () => {
  // 固定使用2026年
  const currentYear = 2026
  const today = new Date()
  const currentMonth = today.getMonth() + 1
  const currentDay = today.getDate()
  
  // 根据2026年的实际节日日期生成默认节日提醒
  // 2026年重要节日准确日期：
  // 春节：2026年2月17日（农历正月初一）
  // 小年：2026年2月11日（农历腊月二十三）
  // 元宵节：2026年3月4日（农历正月十五）
  
  const defaultEvents = []
  
  // 根据当前月份显示未来几个月的节日（不限制，总是显示相关节日）
  // 1月份的节日
  if (currentMonth <= 1) {
    defaultEvents.push({
      id: 1,
      date: '2026年1月1日',
      title: '元旦',
      description: '新年第一天，适合发布新年主题内容',
      type: 'festival'
    })
  }
  
  // 2月份的节日（春节相关）- 如果当前是1月或2月，显示这些
  if (currentMonth <= 2) {
    defaultEvents.push({
      id: 2,
      date: '2026年2月11日',
      title: '小年',
      description: '传统节日，适合发布节日相关内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 3,
      date: '2026年2月14日',
      title: '情人节',
      description: '情感类内容的最佳时机',
      type: 'festival'
    })
    defaultEvents.push({
      id: 4,
      date: '2026年2月17日',
      title: '春节',
      description: '农历正月初一，年度最大流量节点，提前准备内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 5,
      date: '2026年3月4日',
      title: '元宵节',
      description: '传统节日，适合发布节日相关内容',
      type: 'festival'
    })
  }
  
  // 3月份的节日
  if (currentMonth === 3) {
    defaultEvents.push({
      id: 1,
      date: '2026年3月8日',
      title: '妇女节',
      description: '适合发布女性主题内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 2,
      date: '2026年3月12日',
      title: '植树节',
      description: '环保主题内容的好时机',
      type: 'festival'
    })
  }
  
  // 4月份的节日
  if (currentMonth === 4) {
    defaultEvents.push({
      id: 1,
      date: '2026年4月4日',
      title: '清明节',
      description: '传统节日，适合发布相关主题内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 2,
      date: '2026年4月22日',
      title: '世界地球日',
      description: '环保主题内容的好时机',
      type: 'festival'
    })
  }
  
  // 5月份的节日
  if (currentMonth === 5) {
    defaultEvents.push({
      id: 1,
      date: '2026年5月1日',
      title: '劳动节',
      description: '适合发布劳动主题内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 2,
      date: '2026年5月4日',
      title: '青年节',
      description: '适合发布青年主题内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 3,
      date: '2026年5月10日',
      title: '母亲节',
      description: '情感类内容的最佳时机',
      type: 'festival'
    })
  }
  
  // 6月份的节日
  if (currentMonth === 6) {
    defaultEvents.push({
      id: 1,
      date: '2026年6月1日',
      title: '儿童节',
      description: '适合发布儿童主题内容',
      type: 'festival'
    })
    defaultEvents.push({
      id: 2,
      date: '2026年6月21日',
      title: '父亲节',
      description: '情感类内容的最佳时机',
      type: 'festival'
    })
  }
  
  // 如果默认事件为空，添加一些通用提醒
  if (defaultEvents.length === 0) {
    // 计算未来7天的日期（使用2026年）
    const futureDate = new Date(2026, currentMonth - 1, currentDay + 7)
    const futureMonth = futureDate.getMonth() + 1
    const futureDay = futureDate.getDate()
    
    defaultEvents.push({
      id: 1,
      date: `2026年${futureMonth}月${futureDay}日`,
      title: '周末热点',
      description: '周末是内容发布的高峰期',
      type: 'hotspot'
    })
  }
  
  // 只取前5个
  upcomingEvents.value = defaultEvents.slice(0, 5)
}

const useTopic = (topic) => {
  router.push({
    path: '/workbench',
    query: {
      type: 'TEXT',
      prompt: topic.title + '：' + topic.description
    }
  })
}

const generateInspiration = async () => {
  inspirationLoading.value = true
  try {
    // 使用AI生成随机灵感
    const prompts = [
      '给我一个短视频创作灵感',
      '推荐一个适合今天发布的内容主题',
      '生成一个吸引人的内容创意',
      '给我一个爆款内容的灵感'
    ]
    const randomPrompt = prompts[Math.floor(Math.random() * prompts.length)]
    
    const res = await generateText({
      prompt: randomPrompt,
      style: '轻松活泼'
    })
    
    if (res.code === 200 && res.data?.content) {
      const content = res.data.content
      const lines = content.split('\n').filter(line => line.trim())
      currentInspiration.value = {
        title: lines[0] || '创作灵感',
        description: lines.length > 1 ? lines.slice(1).join('\n') : content
      }
      // 保存到缓存
      sessionStorage.setItem('homeCurrentInspiration', JSON.stringify(currentInspiration.value))
    } else {
      // 如果AI返回格式不正确，使用预设灵感
      throw new Error('AI返回格式不正确')
    }
  } catch (error) {
    console.error('生成灵感失败：', error)
    // 如果AI生成失败，使用预设灵感
    const presetInspirations = [
      {
        title: '如何用3个技巧提升视频完播率',
        description: '从开头设计、节奏把控、结尾引导三个维度，教你制作高完播率视频内容'
      },
      {
        title: '春节营销的5个爆款套路',
        description: '结合节日氛围，打造有温度的品牌营销内容，提升用户参与度'
      },
      {
        title: '小红书封面设计的黄金法则',
        description: '掌握色彩搭配、文字排版、视觉焦点三大要素，设计出高点击率封面'
      }
    ]
    currentInspiration.value = presetInspirations[Math.floor(Math.random() * presetInspirations.length)]
    // 保存到缓存
    sessionStorage.setItem('homeCurrentInspiration', JSON.stringify(currentInspiration.value))
  } finally {
    inspirationLoading.value = false
  }
}

const useInspiration = (inspiration) => {
  router.push({
    path: '/workbench',
    query: {
      type: 'TEXT',
      prompt: inspiration.title + '：' + inspiration.description
    }
  })
}

const useEvent = (event) => {
  router.push({
    path: '/workbench',
    query: {
      type: 'TEXT',
      prompt: `关于${event.title}的内容创作：${event.description}`
    }
  })
}

const getTagType = (tag) => {
  const typeMap = {
    '节日': 'danger',
    '营销': 'warning',
    '热点': 'danger',
    '视频': 'primary',
    '脚本': 'success',
    '技巧': 'info',
    '设计': 'warning',
    '小红书': 'danger',
    '封面': 'primary',
    'AI': 'success',
    '工具': 'info',
    '效率': 'success'
  }
  return typeMap[tag] || 'info'
}

// 检查是否是首次登录（本次会话）
const isFirstLoadInSession = () => {
  const flag = sessionStorage.getItem('homeDataLoaded')
  if (!flag) {
    sessionStorage.setItem('homeDataLoaded', 'true')
    return true
  }
  return false
}

// 从缓存加载数据
const loadCachedData = () => {
  try {
    // 加载缓存的节日热点
    const cachedEvents = sessionStorage.getItem('homeUpcomingEvents')
    if (cachedEvents) {
      upcomingEvents.value = JSON.parse(cachedEvents)
    }
    
    // 加载缓存的随机灵感
    const cachedInspiration = sessionStorage.getItem('homeCurrentInspiration')
    if (cachedInspiration) {
      currentInspiration.value = JSON.parse(cachedInspiration)
    }
    
    // 加载缓存的热门话题
    const cachedTopics = sessionStorage.getItem('homeHotTopics')
    if (cachedTopics) {
      hotTopics.value = JSON.parse(cachedTopics)
    }
  } catch (e) {
    console.error('加载缓存数据失败:', e)
  }
}

// 保存数据到缓存
const saveDataToCache = () => {
  try {
    if (upcomingEvents.value.length > 0) {
      sessionStorage.setItem('homeUpcomingEvents', JSON.stringify(upcomingEvents.value))
    }
    if (currentInspiration.value) {
      sessionStorage.setItem('homeCurrentInspiration', JSON.stringify(currentInspiration.value))
    }
    if (hotTopics.value.length > 0) {
      sessionStorage.setItem('homeHotTopics', JSON.stringify(hotTopics.value))
    }
  } catch (e) {
    console.error('保存缓存数据失败:', e)
  }
}

onMounted(async () => {
  // 加载用户信息
  const userInfo = sessionStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.username || ''
    } catch (e) {
      console.error('加载用户信息失败:', e)
    }
  }
  
  // 加载统计数据（可以从API获取）
  // 这里暂时使用模拟数据
  stats.value = {
    totalWorks: 12,
    todayWorks: 3,
    totalWords: 15680
  }
  
  // 检查是否是首次加载（本次会话）
  const isFirstLoad = isFirstLoadInSession()
  
  if (isFirstLoad) {
    // 首次加载：刷新数据
    // 加载节日热点提醒（实时获取）- 使用try-catch确保不会阻塞页面
    try {
      await loadUpcomingEvents(true) // 强制刷新
      saveDataToCache()
    } catch (error) {
      console.error('加载节日热点失败:', error)
    }
    
    // 自动生成随机灵感 - 使用try-catch确保不会阻塞页面
    try {
      await generateInspiration()
      saveDataToCache()
    } catch (error) {
      console.error('生成随机灵感失败:', error)
    }
  } else {
    // 非首次加载：从缓存恢复数据
    loadCachedData()
  }
  
  // 不自动刷新话题，让用户手动刷新
  // refreshTopics()
})
</script>

<style scoped>
.home-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
  background: var(--bg-color);
  transition: background-color 0.3s ease;
}

.welcome-section {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
  transition: all 0.3s ease;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 40px;
}

.welcome-greeting {
  flex: 1;
  text-align: left;
}

.welcome-title {
  font-size: 36px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.welcome-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
}

.welcome-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-greeting {
    text-align: center;
  }
  
  .welcome-stats {
    justify-content: center;
    gap: 20px;
  }
}

.quick-actions {
  margin-bottom: 0;
}

.quick-card {
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  padding: 20px;
  border-radius: 12px;
  border: none;
  background: var(--card-bg);
  transition: all 0.3s ease;
}

:deep(.el-card) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: var(--card-bg);
}

:deep(.el-card:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
}

.quick-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  color: white;
}

.text-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.image-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.history-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.inspiration-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.calendar-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.quick-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.quick-desc {
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.topics-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.topic-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
}

.topic-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
  border-color: var(--primary-color);
}

.topic-card.heat-high {
  border-left: 4px solid #f56c6c;
}

.topic-card.heat-medium {
  border-left: 4px solid #409eff;
}

.topic-card.heat-low {
  border-left: 4px solid #67c23a;
}

.topic-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.topic-title {
  font-size: 18px;
  font-weight: 600;
  flex: 1;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.topic-heat {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.topic-heat.heat-high {
  background: #fef0f0;
  color: #f56c6c;
}

.topic-heat.heat-medium {
  background: #ecf5ff;
  color: #409eff;
}

.topic-heat.heat-low {
  background: #f0f9ff;
  color: #67c23a;
}

.topic-desc {
  color: var(--text-regular);
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.6;
  transition: color 0.3s ease;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.inspiration-container {
  min-height: 200px;
}

.inspiration-card {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border-radius: 16px;
  padding: 30px;
  color: white;
  transition: all 0.3s ease;
}

.inspiration-content h3 {
  font-size: 24px;
  margin: 0 0 15px 0;
}

.inspiration-content p {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 20px;
  opacity: 0.95;
}

.inspiration-actions {
  display: flex;
  gap: 15px;
}

.events-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.event-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid var(--primary-color);
  transition: all 0.3s ease;
}

.event-card.festival {
  border-left-color: var(--danger-color);
}

.event-date {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.event-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.event-desc {
  font-size: 14px;
  color: var(--text-regular);
  margin-bottom: 15px;
  line-height: 1.6;
  transition: color 0.3s ease;
}

@media (max-width: 768px) {
  .welcome-title {
    font-size: 28px;
  }
  
  .topics-container,
  .events-container {
    grid-template-columns: 1fr;
  }
}
</style>