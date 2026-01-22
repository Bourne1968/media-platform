<template>
  <div class="inspiration-container">
    <div class="inspiration-header">
      <h1 class="page-title">
        <el-icon><MagicStick /></el-icon>
        灵感中心
      </h1>
      <p class="page-subtitle">发现创作灵感，追踪热门话题，学习爆款案例</p>
    </div>

    <div class="inspiration-content">
      <el-tabs v-model="activeTab" class="inspiration-tabs">
        <!-- 热点话题 -->
        <el-tab-pane label="热点话题" name="topics">
          <div class="tab-header">
            <div class="tab-info">
              <h3 class="tab-title">实时热点追踪</h3>
              <p class="tab-desc">基于大数据分析，为您推荐当前最热门的话题</p>
            </div>
            <el-button type="primary" @click="refreshTopics" :loading="loadingTopics">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>

          <!-- 筛选器 -->
          <div class="filters">
            <el-radio-group v-model="topicFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="hot">超热</el-radio-button>
              <el-radio-button label="medium">热门</el-radio-button>
              <el-radio-button label="new">新话题</el-radio-button>
            </el-radio-group>
            <el-input
              v-model="topicSearch"
              placeholder="搜索话题..."
              size="small"
              style="width: 200px; margin-left: 16px"
              @input="filterTopics"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- 话题列表 -->
          <div class="topics-grid">
            <div
              v-for="topic in filteredTopics"
              :key="topic.id"
              class="topic-card"
              :class="`heat-${topic.heatLevel}`"
              @click="useTopic(topic)"
            >
              <div class="topic-header">
                <div class="topic-rank">#{{ topic.rank }}</div>
                <div class="topic-heat">
                  <el-icon><Fire /></el-icon>
                  <span>{{ topic.heatValue }}</span>
                </div>
              </div>
              <div class="topic-content">
                <h3 class="topic-title">{{ topic.title }}</h3>
                <p class="topic-desc">{{ topic.description }}</p>
                <div class="topic-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ topic.views }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ topic.discussions }}
                  </span>
                  <span class="stat-item">
                    <el-icon><Clock /></el-icon>
                    {{ topic.trend }}
                  </span>
                </div>
              </div>
              <div class="topic-footer">
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
                <el-button type="primary" size="small" @click.stop="useTopic(topic)">
                  立即创作
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 选题推荐 -->
        <el-tab-pane label="选题推荐" name="recommendations">
          <div class="tab-header">
            <div class="tab-info">
              <h3 class="tab-title">智能选题推荐</h3>
              <p class="tab-desc">基于您的创作历史和当前热点，AI为您推荐最适合的创作选题</p>
            </div>
            <el-button type="primary" @click="generateRecommendations" :loading="loadingRecommendations">
              <el-icon><MagicStick /></el-icon>
              生成推荐
            </el-button>
          </div>

          <!-- 推荐设置 -->
          <div class="recommendation-settings">
            <el-form :inline="true" :model="recommendationSettings">
              <el-form-item label="推荐类型">
                <el-radio-group v-model="recommendationSettings.type">
                  <el-radio label="general">通用推荐</el-radio>
                  <el-radio label="personal">个性化推荐</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="平台">
                <el-select v-model="recommendationSettings.platform" placeholder="选择目标平台" style="width: 120px">
                  <el-option label="抖音" value="douyin" />
                  <el-option label="小红书" value="xiaohongshu" />
                  <el-option label="B站" value="bilibili" />
                  <el-option label="公众号" value="wechat" />
                </el-select>
              </el-form-item>
              <el-form-item label="难度">
                <el-select v-model="recommendationSettings.difficulty" placeholder="选择难度" style="width: 120px">
                  <el-option label="简单" value="easy" />
                  <el-option label="中等" value="medium" />
                  <el-option label="挑战" value="hard" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <!-- 推荐结果 -->
          <div class="recommendations-list">
            <div
              v-for="recommendation in recommendations"
              :key="recommendation.id"
              class="recommendation-card"
            >
              <div class="recommendation-header">
                <div class="recommendation-score">
                  <el-rate v-model="recommendation.score" disabled show-score />
                </div>
                <div class="recommendation-type">
                  <el-tag :type="recommendation.type === 'hot' ? 'danger' : 'warning'">
                    {{ recommendation.type === 'hot' ? '热点推荐' : '潜力选题' }}
                  </el-tag>
                </div>
              </div>
              <div class="recommendation-content">
                <h4 class="recommendation-title">{{ recommendation.title }}</h4>
                <p class="recommendation-desc">{{ recommendation.description }}</p>
                <div class="recommendation-analysis">
                  <div class="analysis-item">
                    <span class="label">热度指数：</span>
                    <el-progress :percentage="recommendation.heatIndex" :stroke-width="8" />
                  </div>
                  <div class="analysis-item">
                    <span class="label">竞争度：</span>
                    <el-tag :type="recommendation.competitionLevel === 'low' ? 'success' : recommendation.competitionLevel === 'medium' ? 'warning' : 'danger'">
                      {{ recommendation.competitionLevel === 'low' ? '较低' : recommendation.competitionLevel === 'medium' ? '中等' : '较高' }}
                    </el-tag>
                  </div>
                  <div class="analysis-item">
                    <span class="label">预计播放量：</span>
                    <span class="value">{{ recommendation.expectedViews }}</span>
                  </div>
                </div>
              </div>
              <div class="recommendation-footer">
                <div class="recommendation-tags">
                  <el-tag
                    v-for="tag in recommendation.tags"
                    :key="tag"
                    size="small"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="recommendation-actions">
                  <el-button size="small" @click="viewSimilarTopics(recommendation)">
                    相似选题
                  </el-button>
                  <el-button type="primary" size="small" @click="useRecommendation(recommendation)">
                    开始创作
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 爆款案例 -->
        <el-tab-pane label="爆款案例" name="cases">
          <div class="tab-header">
            <div class="tab-info">
              <h3 class="tab-title">爆款案例库</h3>
              <p class="tab-desc">分析热门内容的成功要素，为您的创作提供参考</p>
            </div>
            <div class="tab-actions">
              <el-input
                v-model="caseSearch"
                placeholder="搜索案例..."
                size="small"
                style="width: 200px"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="refreshCases" :loading="loadingCases">
                <el-icon><Refresh /></el-icon>
                刷新案例
              </el-button>
            </div>
          </div>

          <!-- 案例筛选 -->
          <div class="case-filters">
            <el-radio-group v-model="caseFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="video">视频</el-radio-button>
              <el-radio-button label="article">图文</el-radio-button>
              <el-radio-button label="short">短视频</el-radio-button>
            </el-radio-group>
            <el-select v-model="casePlatform" placeholder="选择平台" size="small" style="width: 120px; margin-left: 16px">
              <el-option label="全部平台" value="all" />
              <el-option label="抖音" value="douyin" />
              <el-option label="小红书" value="xiaohongshu" />
              <el-option label="B站" value="bilibili" />
              <el-option label="公众号" value="wechat" />
            </el-select>
            <el-select v-model="caseSort" placeholder="排序方式" size="small" style="width: 120px; margin-left: 8px">
              <el-option label="播放量" value="views" />
              <el-option label="点赞数" value="likes" />
              <el-option label="发布时间" value="time" />
            </el-select>
          </div>

          <!-- 案例列表 -->
          <div class="cases-grid">
            <div
              v-for="case in filteredCases"
              :key="case.id"
              class="case-card"
              @click="viewCase(case)"
            >
              <div class="case-cover">
                <img :src="case.cover" :alt="case.title" />
                <div class="case-platform-tag">
                  <el-tag size="small" :type="getPlatformType(case.platform)">
                    {{ case.platform }}
                  </el-tag>
                </div>
                <div class="case-stats">
                  <div class="stat">
                    <el-icon><VideoPlay /></el-icon>
                    {{ case.views }}
                  </div>
                  <div class="stat">
                    <el-icon><Like /></el-icon>
                    {{ case.likes }}
                  </div>
                </div>
              </div>
              <div class="case-content">
                <h4 class="case-title">{{ case.title }}</h4>
                <p class="case-desc">{{ case.description }}</p>
                <div class="case-analysis">
                  <div class="analysis-tag">
                    <span class="label">爆款要素：</span>
                    <el-tag
                      v-for="element in case.successElements"
                      :key="element"
                      size="mini"
                      type="success"
                    >
                      {{ element }}
                    </el-tag>
                  </div>
                  <div class="case-author">
                    <span class="label">创作者：</span>
                    <span>{{ case.author }}</span>
                  </div>
                  <div class="case-time">
                    <span class="label">发布时间：</span>
                    <span>{{ case.publishTime }}</span>
                  </div>
                </div>
              </div>
              <div class="case-footer">
                <el-button size="small" @click.stop="analyzeCase(case)">
                  分析要素
                </el-button>
                <el-button type="primary" size="small" @click.stop="useCaseAsTemplate(case)">
                  用作模板
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Refresh,
  Search,
  Fire,
  View,
  ChatDotRound,
  Clock,
  VideoPlay,
  Like
} from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('topics')

// 热点话题数据
const topics = ref([
  {
    id: 1,
    rank: 1,
    title: '春节营销策略大揭秘',
    description: '春节期间如何通过内容营销吸引流量，提升品牌曝光度',
    heatValue: '98.5',
    heatLevel: 'high',
    views: '2.1万',
    discussions: '856',
    trend: '上升中',
    tags: ['节日营销', '春节', '品牌推广']
  },
  {
    id: 2,
    rank: 2,
    title: '短视频脚本写作技巧',
    description: '3分钟短视频如何用最简单的结构讲好一个故事',
    heatValue: '95.2',
    heatLevel: 'high',
    views: '1.8万',
    discussions: '623',
    trend: '稳定',
    tags: ['短视频', '脚本写作', '内容创作']
  },
  {
    id: 3,
    rank: 3,
    title: '小红书封面设计黄金法则',
    description: '高点击率的封面设计应该遵循哪些视觉原则',
    heatValue: '92.8',
    heatLevel: 'medium',
    views: '1.5万',
    discussions: '445',
    trend: '上升中',
    tags: ['小红书', '封面设计', '视觉设计']
  },
  {
    id: 4,
    rank: 4,
    title: 'AI工具在创作中的应用',
    description: '如何利用AI工具提升创作效率和内容质量',
    heatValue: '89.3',
    heatLevel: 'medium',
    views: '1.2万',
    discussions: '334',
    trend: '新话题',
    tags: ['AI工具', '效率提升', '内容创作']
  },
  {
    id: 5,
    rank: 5,
    title: 'B站视频标题优化技巧',
    description: '如何写出吸引点击的B站视频标题',
    heatValue: '87.1',
    heatLevel: 'medium',
    views: '9800',
    discussions: '287',
    trend: '稳定',
    tags: ['B站', '标题优化', '视频标题']
  }
])

// 选题推荐数据
const recommendations = ref([
  {
    id: 1,
    title: '如何用3个技巧提升视频完播率',
    description: '从开头设计、节奏把控、结尾引导三个维度，教你制作高完播率视频内容',
    score: 4.8,
    type: 'hot',
    heatIndex: 95,
    competitionLevel: 'medium',
    expectedViews: '10万-50万',
    tags: ['视频制作', '完播率', '技巧分享']
  },
  {
    id: 2,
    title: '春节营销的5个爆款套路',
    description: '结合节日氛围，打造有温度的品牌营销内容，提升用户参与度',
    score: 4.6,
    type: 'hot',
    heatIndex: 88,
    competitionLevel: 'high',
    expectedViews: '20万-100万',
    tags: ['春节营销', '品牌推广', '用户参与']
  },
  {
    id: 3,
    title: '小红书封面的视觉心理学',
    description: '利用视觉心理学原理，设计出高点击率的封面',
    score: 4.4,
    type: 'potential',
    heatIndex: 72,
    competitionLevel: 'low',
    expectedViews: '5万-20万',
    tags: ['视觉心理学', '封面设计', '点击率']
  }
])

// 爆款案例数据
const cases = ref([
  {
    id: 1,
    title: '3分钟带你了解AI创作的未来',
    description: '用通俗易懂的方式解释AI创作技术的发展趋势',
    cover: 'https://via.placeholder.com/300x200/667eea/ffffff?text=AI+Future',
    platform: 'B站',
    views: '150万',
    likes: '8.5万',
    author: '科技探索者',
    publishTime: '2024-01-15',
    successElements: ['开头吸引', '数据支撑', '未来展望'],
    type: 'video'
  },
  {
    id: 2,
    title: '春节回家，我带了这些礼物',
    description: '真挚的情感表达，加上节日氛围的精心包装',
    cover: 'https://via.placeholder.com/300x200/f093fb/ffffff?text=Spring+Festival',
    platform: '抖音',
    views: '200万',
    likes: '15万',
    author: '生活记录者',
    publishTime: '2024-01-20',
    successElements: ['情感共鸣', '节日氛围', '真实记录'],
    type: 'short'
  },
  {
    id: 3,
    title: '小红书爆款封面的设计秘密',
    description: '通过数据分析，总结出高点击封面的设计规律',
    cover: 'https://via.placeholder.com/300x200/4facfe/ffffff?text=Design+Secret',
    platform: '小红书',
    views: '8.5万',
    likes: '3200',
    author: '设计达人',
    publishTime: '2024-01-18',
    successElements: ['数据分析', '实用技巧', '视觉化展示'],
    type: 'article'
  }
])

// 响应式数据
const loadingTopics = ref(false)
const loadingRecommendations = ref(false)
const loadingCases = ref(false)
const topicFilter = ref('all')
const topicSearch = ref('')
const caseFilter = ref('all')
const casePlatform = ref('all')
const caseSort = ref('views')
const caseSearch = ref('')

const recommendationSettings = reactive({
  type: 'general',
  platform: 'douyin',
  difficulty: 'medium'
})

// 计算属性
const filteredTopics = computed(() => {
  let filtered = topics.value

  // 按热度筛选
  if (topicFilter.value !== 'all') {
    filtered = filtered.filter(topic => {
      switch (topicFilter.value) {
        case 'hot': return topic.heatLevel === 'high'
        case 'medium': return topic.heatLevel === 'medium'
        case 'new': return topic.trend === '新话题'
        default: return true
      }
    })
  }

  // 按关键词搜索
  if (topicSearch.value.trim()) {
    const searchTerm = topicSearch.value.toLowerCase()
    filtered = filtered.filter(topic =>
      topic.title.toLowerCase().includes(searchTerm) ||
      topic.description.toLowerCase().includes(searchTerm) ||
      topic.tags.some(tag => tag.toLowerCase().includes(searchTerm))
    )
  }

  return filtered
})

const filteredCases = computed(() => {
  let filtered = cases.value

  // 按类型筛选
  if (caseFilter.value !== 'all') {
    filtered = filtered.filter(case => case.type === caseFilter.value)
  }

  // 按平台筛选
  if (casePlatform.value !== 'all') {
    filtered = filtered.filter(case => {
      const platformMap = {
        douyin: '抖音',
        xiaohongshu: '小红书',
        bilibili: 'B站',
        wechat: '公众号'
      }
      return case.platform === platformMap[casePlatform.value]
    })
  }

  // 按关键词搜索
  if (caseSearch.value.trim()) {
    const searchTerm = caseSearch.value.toLowerCase()
    filtered = filtered.filter(case =>
      case.title.toLowerCase().includes(searchTerm) ||
      case.description.toLowerCase().includes(searchTerm) ||
      case.successElements.some(element => element.toLowerCase().includes(searchTerm))
    )
  }

  // 排序
  filtered.sort((a, b) => {
    switch (caseSort.value) {
      case 'views':
        return parseInt(b.views.replace(/[^\d]/g, '')) - parseInt(a.views.replace(/[^\d]/g, ''))
      case 'likes':
        return parseInt(b.likes.replace(/[^\d]/g, '')) - parseInt(a.likes.replace(/[^\d]/g, ''))
      case 'time':
        return new Date(b.publishTime) - new Date(a.publishTime)
      default:
        return 0
    }
  })

  return filtered
})

// 方法
const refreshTopics = async () => {
  loadingTopics.value = true
  // 这里应该调用API获取最新话题数据
  setTimeout(() => {
    ElMessage.success('话题数据已刷新')
    loadingTopics.value = false
  }, 1500)
}

const filterTopics = () => {
  // 搜索功能已通过computed实现
}

const useTopic = (topic) => {
  router.push({
    path: '/workbench',
    query: {
      type: 'TEXT',
      prompt: `${topic.title}：${topic.description}`
    }
  })
}

const getTagType = (tag) => {
  const typeMap = {
    '节日营销': 'danger',
    '春节': 'danger',
    '短视频': 'primary',
    '脚本写作': 'success',
    '小红书': 'danger',
    '封面设计': 'warning',
    'AI工具': 'success',
    '效率提升': 'info',
    'B站': 'primary',
    '标题优化': 'warning',
    '视频标题': 'info'
  }
  return typeMap[tag] || 'info'
}

const generateRecommendations = async () => {
  loadingRecommendations.value = true
  // 这里应该调用AI生成推荐的API
  setTimeout(() => {
    ElMessage.success('已为您生成个性化选题推荐')
    loadingRecommendations.value = false
  }, 2000)
}

const useRecommendation = (recommendation) => {
  router.push({
    path: '/workbench',
    query: {
      type: 'TEXT',
      prompt: recommendation.title + '：' + recommendation.description
    }
  })
}

const viewSimilarTopics = (recommendation) => {
  ElMessage.info('相似选题功能开发中...')
}

const refreshCases = async () => {
  loadingCases.value = true
  // 这里应该调用API获取最新案例数据
  setTimeout(() => {
    ElMessage.success('案例库已更新')
    loadingCases.value = false
  }, 1500)
}

const viewCase = (case) => {
  ElMessage.info(`查看案例详情：${case.title}`)
}

const analyzeCase = (case) => {
  ElMessage.info(`分析案例要素：${case.title}`)
}

const useCaseAsTemplate = (case) => {
  router.push({
    path: '/workbench',
    query: {
      type: case.type === 'video' ? 'TEXT' : 'IMAGE',
      prompt: `参考案例"${case.title}"的成功要素：${case.successElements.join('、')}，创作类似内容：${case.description}`
    }
  })
}

const getPlatformType = (platform) => {
  const typeMap = {
    '抖音': 'danger',
    '小红书': 'success',
    'B站': 'primary',
    '公众号': 'info'
  }
  return typeMap[platform] || 'info'
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.inspiration-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
}

.inspiration-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.inspiration-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.inspiration-tabs :deep(.el-tabs__header) {
  margin: 0;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.inspiration-tabs :deep(.el-tabs__item) {
  font-weight: 500;
  padding: 16px 24px;
  border-bottom: 2px solid transparent;
}

.inspiration-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
  border-bottom-color: #667eea;
}

.inspiration-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid #f0f0f0;
}

.tab-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.tab-info p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.tab-actions {
  display: flex;
  gap: 12px;
}

.filters, .case-filters {
  padding: 16px 32px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 话题卡片 */
.topics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
  padding: 32px;
}

.topic-card {
  background: white;
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.topic-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.topic-card.heat-high {
  border-left: 4px solid #f56c6c;
}

.topic-card.heat-medium {
  border-left: 4px solid #409eff;
}

.topic-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.topic-rank {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.topic-heat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
  font-weight: 600;
}

.topic-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.topic-content p {
  color: #606266;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.topic-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 推荐设置 */
.recommendation-settings {
  padding: 16px 32px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 推荐卡片 */
.recommendations-list {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.recommendation-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  padding: 24px;
  transition: all 0.3s ease;
}

.recommendation-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.recommendation-score {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendation-content h4 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.recommendation-content p {
  color: #606266;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.recommendation-analysis {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.analysis-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-item .label {
  font-weight: 500;
  color: #303133;
  min-width: 80px;
}

.analysis-item .value {
  color: #409eff;
  font-weight: 500;
}

.recommendation-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recommendation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.recommendation-actions {
  display: flex;
  gap: 8px;
}

/* 案例卡片 */
.cases-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  padding: 32px;
}

.case-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.case-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.case-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.case-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.case-card:hover .case-cover img {
  transform: scale(1.05);
}

.case-platform-tag {
  position: absolute;
  top: 12px;
  left: 12px;
}

.case-stats {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.case-stats .stat {
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.case-content {
  padding: 20px;
}

.case-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.case-content p {
  color: #606266;
  margin: 0 0 16px 0;
  line-height: 1.6;
  font-size: 14px;
}

.case-analysis {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.analysis-tag {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.analysis-tag .label {
  font-weight: 500;
  color: #303133;
  margin-right: 4px;
}

.case-author, .case-time {
  display: flex;
  align-items: center;
}

.case-author .label,
.case-time .label {
  font-weight: 500;
  color: #303133;
  margin-right: 4px;
}

.case-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .topics-grid, .cases-grid {
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  }

  .tab-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .inspiration-container {
    padding: 16px;
  }

  .topics-grid, .cases-grid {
    grid-template-columns: 1fr;
  }

  .tab-header {
    padding: 16px 20px;
  }

  .topics-grid, .cases-grid {
    padding: 20px;
  }

  .recommendations-list {
    padding: 20px;
  }

  .recommendation-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .topic-footer, .case-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .filters, .case-filters, .recommendation-settings {
    padding: 12px 20px;
  }
}
</style>