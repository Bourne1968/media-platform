<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">{{ welcomeText }}</h1>
        <p class="welcome-subtitle">今天想创作什么内容呢？</p>
      </div>
    </div>

    <!-- 快捷功能区 -->
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
        <el-card class="quick-card" shadow="hover" @click="goToWorkbench('IMAGE')">
          <div class="quick-icon image-icon">
            <el-icon :size="40"><Picture /></el-icon>
          </div>
          <div class="quick-title">封面设计</div>
          <div class="quick-desc">AI生成精美封面图</div>
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
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="6">
        <el-card class="quick-card" shadow="hover" @click="$router.push('/calendar')">
          <div class="quick-icon calendar-icon">
            <el-icon :size="40"><Calendar /></el-icon>
          </div>
          <div class="quick-title">创作日历</div>
          <div class="quick-desc">规划内容发布</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日热门话题 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><TrendCharts /></el-icon>
          今日热门话题
        </h2>
        <el-button text @click="refreshTopics">
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
      <el-empty v-else description="点击按钮生成创作灵感" />
    </div>

    <!-- 节日/热点提醒 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><Bell /></el-icon>
          节日热点提醒
        </h2>
      </div>
      <div class="events-container">
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
  Bell
} from '@element-plus/icons-vue'
import { generateText } from '@/api/ai'

const router = useRouter()

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

// 随机灵感
const currentInspiration = ref(null)
const inspirationLoading = ref(false)

// 节日/热点事件
const upcomingEvents = ref([
  {
    id: 1,
    date: '1月20日',
    title: '小年',
    description: '传统节日，适合发布节日相关内容',
    type: 'festival'
  },
  {
    id: 2,
    date: '1月25日',
    title: '春节',
    description: '年度最大流量节点，提前准备内容',
    type: 'festival'
  },
  {
    id: 3,
    date: '2月14日',
    title: '情人节',
    description: '情感类内容的最佳时机',
    type: 'festival'
  }
])

// 方法
const goToWorkbench = (type) => {
  router.push({
    path: '/workbench',
    query: { type }
  })
}

const refreshTopics = () => {
  ElMessage.success('话题已刷新')
  // 这里可以调用API获取最新话题
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
    
    if (res.code === 200) {
      currentInspiration.value = {
        title: res.data.content.split('\n')[0] || '创作灵感',
        description: res.data.content
      }
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

onMounted(() => {
  // 可以在这里加载数据
})
</script>

<style scoped>
.home-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
  text-align: center;
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

.quick-actions {
  margin-bottom: 40px;
}

.quick-card {
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  padding: 20px;
  border-radius: 12px;
  border: none;
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

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
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

.calendar-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.quick-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.quick-desc {
  font-size: 14px;
  color: #909399;
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
}

.topics-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.topic-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.topic-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
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
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.6;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.inspiration-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px;
  color: white;
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
  background: white;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #409eff;
}

.event-card.festival {
  border-left-color: #f56c6c;
}

.event-date {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.event-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.event-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.6;
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