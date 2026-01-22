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
                  <el-icon><TrendCharts /></el-icon>
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
              v-for="caseItem in filteredCases"
              :key="caseItem.id"
              class="case-card"
              @click="viewCase(caseItem)"
            >
              <div class="case-cover">
                <img :src="caseItem.cover" :alt="caseItem.title" />
                <div class="case-platform-tag">
                  <el-tag size="small" :type="getPlatformType(caseItem.platform)">
                    {{ caseItem.platform }}
                  </el-tag>
                </div>
                <div class="case-stats">
                  <div class="stat">
                    <el-icon><VideoPlay /></el-icon>
                    {{ caseItem.views }}
                  </div>
                  <div class="stat">
                    <el-icon><Star /></el-icon>
                    {{ caseItem.likes }}
                  </div>
                </div>
              </div>
              <div class="case-content">
                <h4 class="case-title">{{ caseItem.title }}</h4>
                <p class="case-desc">{{ caseItem.description }}</p>
                <div class="case-analysis">
                  <div class="analysis-tag">
                    <span class="label">爆款要素：</span>
                    <el-tag
                      v-for="element in caseItem.successElements"
                      :key="element"
                      size="mini"
                      type="success"
                    >
                      {{ element }}
                    </el-tag>
                  </div>
                  <div class="case-author">
                    <span class="label">创作者：</span>
                    <span>{{ caseItem.author }}</span>
                  </div>
                  <div class="case-time">
                    <span class="label">发布时间：</span>
                    <span>{{ caseItem.publishTime }}</span>
                  </div>
                </div>
              </div>
              <div class="case-footer">
                <el-button size="small" @click.stop="analyzeCase(caseItem)">
                  分析要素
                </el-button>
                <el-button type="primary" size="small" @click.stop="useCaseAsTemplate(caseItem)">
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
  TrendCharts,
  View,
  ChatDotRound,
  Clock,
  VideoPlay,
  Star
} from '@element-plus/icons-vue'
import { generateText } from '@/api/ai'

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
  },
  {
    id: 6,
    rank: 6,
    title: '社交媒体内容策略优化',
    description: '如何根据不同平台特点制定有效的内容策略',
    heatValue: '85.6',
    heatLevel: 'medium',
    views: '8500',
    discussions: '245',
    trend: '上升中',
    tags: ['社交媒体', '内容策略', '平台运营']
  },
  {
    id: 7,
    rank: 7,
    title: '内容创作者变现路径',
    description: '内容创作者如何通过多种方式实现变现',
    heatValue: '83.2',
    heatLevel: 'medium',
    views: '7200',
    discussions: '198',
    trend: '稳定',
    tags: ['内容变现', '创作者经济', '商业模式']
  },
  {
    id: 8,
    rank: 8,
    title: '直播带货话术技巧',
    description: '掌握直播带货的核心话术，提升转化率',
    heatValue: '81.5',
    heatLevel: 'medium',
    views: '6800',
    discussions: '176',
    trend: '新话题',
    tags: ['直播带货', '话术技巧', '电商运营']
  },
  {
    id: 9,
    rank: 9,
    title: '品牌故事讲述方法',
    description: '如何通过故事化内容传递品牌价值',
    heatValue: '79.8',
    heatLevel: 'medium',
    views: '6100',
    discussions: '154',
    trend: '稳定',
    tags: ['品牌故事', '内容营销', '品牌建设']
  },
  {
    id: 10,
    rank: 10,
    title: '用户增长策略实战',
    description: '从0到1的用户增长策略和实战案例',
    heatValue: '77.3',
    heatLevel: 'medium',
    views: '5500',
    discussions: '132',
    trend: '上升中',
    tags: ['用户增长', '运营策略', '实战案例']
  },
  {
    id: 11,
    rank: 11,
    title: '内容数据分析与优化',
    description: '如何通过数据分析优化内容效果',
    heatValue: '75.1',
    heatLevel: 'medium',
    views: '4900',
    discussions: '118',
    trend: '稳定',
    tags: ['数据分析', '内容优化', '效果评估']
  },
  {
    id: 12,
    rank: 12,
    title: '跨平台内容分发策略',
    description: '如何将同一内容适配不同平台并最大化传播',
    heatValue: '73.6',
    heatLevel: 'medium',
    views: '4300',
    discussions: '105',
    trend: '新话题',
    tags: ['内容分发', '多平台运营', '传播策略']
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
    filtered = filtered.filter(caseItem => caseItem.type === caseFilter.value)
  }

  // 按平台筛选
  if (casePlatform.value !== 'all') {
    filtered = filtered.filter(caseItem => {
      const platformMap = {
        douyin: '抖音',
        xiaohongshu: '小红书',
        bilibili: 'B站',
        wechat: '公众号'
      }
      return caseItem.platform === platformMap[casePlatform.value]
    })
  }

  // 按关键词搜索
  if (caseSearch.value.trim()) {
    const searchTerm = caseSearch.value.toLowerCase()
    filtered = filtered.filter(caseItem =>
      caseItem.title.toLowerCase().includes(searchTerm) ||
      caseItem.description.toLowerCase().includes(searchTerm) ||
      caseItem.successElements.some(element => element.toLowerCase().includes(searchTerm))
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
  try {
    // 通过AI获取最新的热点话题
    const prompt = `请为我生成15个当前最热门的社交媒体内容创作话题。要求：
1. 话题要贴近当前热点和趋势，具有时效性
2. 每个话题包含：标题、简短描述（30字以内）、热度值（90-99之间）、热度等级（high/medium）、浏览量（如"2.1万"）、讨论数（如"856"）、趋势（上升中/稳定/新话题）、相关标签（3-4个）
3. 请以JSON格式返回，格式如下：
[
  {
    "title": "话题标题",
    "description": "话题描述",
    "heatValue": "98.5",
    "heatLevel": "high",
    "views": "2.1万",
    "discussions": "856",
    "trend": "上升中",
    "tags": ["标签1", "标签2", "标签3"]
  }
]
请直接返回JSON数组，不要其他说明文字。`

    const res = await generateText({
      prompt: prompt,
      style: null
    })

    if (res.code === 200 && res.data && res.data.content) {
      try {
        // 尝试解析AI返回的JSON
        let content = res.data.content.trim()
        
        // 如果返回的内容包含markdown代码块，提取JSON部分
        const jsonMatch = content.match(/\[[\s\S]*\]/)
        if (jsonMatch) {
          content = jsonMatch[0]
        }
        
        const aiTopics = JSON.parse(content)
        
        // 转换为前端需要的格式
        const newTopics = aiTopics.slice(0, 15).map((topic, index) => ({
          id: Date.now() + index,
          rank: index + 1,
          title: topic.title || `话题${index + 1}`,
          description: topic.description || '',
          heatValue: topic.heatValue || (95 - index * 2).toFixed(1),
          heatLevel: topic.heatLevel || (index < 3 ? 'high' : 'medium'),
          views: topic.views || `${(Math.random() * 5 + 1).toFixed(1)}万`,
          discussions: topic.discussions || Math.floor(Math.random() * 1000 + 100).toString(),
          trend: topic.trend || (index % 3 === 0 ? '上升中' : index % 3 === 1 ? '稳定' : '新话题'),
          tags: topic.tags || ['内容创作', '社交媒体', '热点话题']
        }))
        
        topics.value = newTopics
        ElMessage.success(`成功获取${newTopics.length}个最新热点话题`)
      } catch (parseError) {
        console.error('解析AI返回数据失败:', parseError, '原始内容:', res.data.content)
        // 如果解析失败，使用备用数据
        generateFallbackTopics()
        ElMessage.warning('AI数据解析失败，已使用备用数据')
      }
    } else {
      // 如果AI调用失败，使用备用数据
      generateFallbackTopics()
      ElMessage.warning('AI获取失败，已使用备用数据')
    }
  } catch (error) {
    console.error('刷新话题失败:', error)
    // 如果出错，使用备用数据
    generateFallbackTopics()
    ElMessage.error('刷新失败，已使用备用数据')
  } finally {
    loadingTopics.value = false
  }
}

// 生成备用话题数据（当AI调用失败时使用）
const generateFallbackTopics = () => {
  const fallbackTopics = [
    {
      id: Date.now(),
      rank: 1,
      title: 'AI创作工具最新趋势',
      description: '探索AI在内容创作领域的最新应用和发展趋势',
      heatValue: '99.2',
      heatLevel: 'high',
      views: '3.5万',
      discussions: '1200',
      trend: '上升中',
      tags: ['AI工具', '内容创作', '趋势分析']
    },
    {
      id: Date.now() + 1,
      rank: 2,
      title: '短视频营销策略',
      description: '如何通过短视频营销提升品牌影响力和转化率',
      heatValue: '96.8',
      heatLevel: 'high',
      views: '2.8万',
      discussions: '980',
      trend: '稳定',
      tags: ['短视频', '营销策略', '品牌推广']
    },
    {
      id: Date.now() + 2,
      rank: 3,
      title: '内容创作效率提升技巧',
      description: '实用的内容创作工具和方法，帮助提升创作效率',
      heatValue: '94.5',
      heatLevel: 'medium',
      views: '2.2万',
      discussions: '756',
      trend: '上升中',
      tags: ['效率提升', '内容创作', '工具推荐']
    },
    {
      id: Date.now() + 3,
      rank: 4,
      title: '社交媒体封面设计指南',
      description: '不同平台封面设计的最佳实践和设计原则',
      heatValue: '91.3',
      heatLevel: 'medium',
      views: '1.9万',
      discussions: '623',
      trend: '新话题',
      tags: ['封面设计', '社交媒体', '设计指南']
    },
    {
      id: Date.now() + 4,
      rank: 5,
      title: '爆款标题写作公式',
      description: '掌握这些标题写作技巧，让你的内容获得更多点击',
      heatValue: '88.7',
      heatLevel: 'medium',
      views: '1.6万',
      discussions: '512',
      trend: '稳定',
      tags: ['标题写作', '内容优化', '点击率']
    },
    {
      id: Date.now() + 5,
      rank: 6,
      title: '内容创作者IP打造',
      description: '如何打造个人IP，提升影响力和商业价值',
      heatValue: '86.4',
      heatLevel: 'medium',
      views: '1.4万',
      discussions: '445',
      trend: '上升中',
      tags: ['IP打造', '个人品牌', '影响力']
    },
    {
      id: Date.now() + 6,
      rank: 7,
      title: '跨平台内容分发策略',
      description: '如何将同一内容适配不同平台并最大化传播',
      heatValue: '84.1',
      heatLevel: 'medium',
      views: '1.2万',
      discussions: '378',
      trend: '稳定',
      tags: ['内容分发', '多平台运营', '传播策略']
    },
    {
      id: Date.now() + 7,
      rank: 8,
      title: '用户增长与留存技巧',
      description: '通过内容运营实现用户增长和长期留存',
      heatValue: '82.3',
      heatLevel: 'medium',
      views: '1.1万',
      discussions: '332',
      trend: '新话题',
      tags: ['用户增长', '用户留存', '运营技巧']
    },
    {
      id: Date.now() + 8,
      rank: 9,
      title: '内容数据分析与优化',
      description: '如何通过数据分析优化内容效果和传播',
      heatValue: '80.5',
      heatLevel: 'medium',
      views: '9800',
      discussions: '298',
      trend: '稳定',
      tags: ['数据分析', '内容优化', '效果评估']
    },
    {
      id: Date.now() + 9,
      rank: 10,
      title: '直播内容策划与执行',
      description: '如何策划和执行高质量的直播内容',
      heatValue: '78.9',
      heatLevel: 'medium',
      views: '8600',
      discussions: '267',
      trend: '上升中',
      tags: ['直播策划', '内容执行', '直播运营']
    }
  ]
  
  topics.value = fallbackTopics
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
  try {
    // 构建平台和难度描述
    const platformMap = {
      douyin: '抖音',
      xiaohongshu: '小红书',
      bilibili: 'B站',
      wechat: '公众号'
    }
    const difficultyMap = {
      easy: '简单',
      medium: '中等',
      hard: '挑战'
    }
    const typeMap = {
      general: '通用',
      personal: '个性化'
    }
    
    const platformText = platformMap[recommendationSettings.platform] || '抖音'
    const difficultyText = difficultyMap[recommendationSettings.difficulty] || '中等'
    const typeText = typeMap[recommendationSettings.type] || '通用'
    
    // 通过AI生成选题推荐
    const prompt = `请为我生成10个${typeText}的${platformText}平台内容创作选题推荐。要求：
1. 选题要具有时效性和吸引力，适合${platformText}平台特点
2. 难度级别为${difficultyText}
3. 每个选题包含：标题、描述（50字以内）、评分（4.0-5.0之间）、平台、难度、标签（3-4个）、预计创作时间、预期浏览量
4. 请以JSON格式返回，格式如下：
[
  {
    "title": "选题标题",
    "description": "选题描述",
    "score": 4.8,
    "platform": "${recommendationSettings.platform}",
    "difficulty": "${recommendationSettings.difficulty}",
    "tags": ["标签1", "标签2", "标签3"],
    "estimatedTime": "2-3小时",
    "potentialViews": "10-50万"
  }
]
请直接返回JSON数组，不要其他说明文字。`

    const res = await generateText({
      prompt: prompt,
      style: null
    })

    if (res.code === 200 && res.data && res.data.content) {
      try {
        // 尝试解析AI返回的JSON
        let content = res.data.content.trim()
        
        // 如果返回的内容包含markdown代码块，提取JSON部分
        const jsonMatch = content.match(/\[[\s\S]*\]/)
        if (jsonMatch) {
          content = jsonMatch[0]
        }
        
        const aiRecommendations = JSON.parse(content)
        
        // 转换为前端需要的格式
        const newRecommendations = aiRecommendations.slice(0, 10).map((rec, index) => ({
          id: Date.now() + index,
          title: rec.title || `选题${index + 1}`,
          description: rec.description || '',
          score: rec.score || (4.5 + Math.random() * 0.5),
          platform: rec.platform || recommendationSettings.platform,
          difficulty: rec.difficulty || recommendationSettings.difficulty,
          tags: rec.tags || ['内容创作', '选题推荐', '创作灵感'],
          estimatedTime: rec.estimatedTime || '1-2小时',
          potentialViews: rec.potentialViews || '5-20万'
        }))
        
        // 根据设置筛选和排序
        let filtered = newRecommendations
        if (recommendationSettings.platform && recommendationSettings.platform !== 'all') {
          filtered = filtered.filter(r => r.platform === recommendationSettings.platform)
        }
        if (recommendationSettings.difficulty && recommendationSettings.difficulty !== 'all') {
          filtered = filtered.filter(r => r.difficulty === recommendationSettings.difficulty)
        }
        
        recommendations.value = filtered.sort((a, b) => b.score - a.score)
        ElMessage.success(`成功生成${filtered.length}个个性化选题推荐`)
      } catch (parseError) {
        console.error('解析AI返回数据失败:', parseError, '原始内容:', res.data.content)
        // 如果解析失败，使用备用数据
        generateFallbackRecommendations()
        ElMessage.warning('AI数据解析失败，已使用备用数据')
      }
    } else {
      // 如果AI调用失败，使用备用数据
      generateFallbackRecommendations()
      ElMessage.warning('AI获取失败，已使用备用数据')
    }
  } catch (error) {
    console.error('生成推荐失败:', error)
    // 如果出错，使用备用数据
    generateFallbackRecommendations()
    ElMessage.error('生成推荐失败，已使用备用数据')
  } finally {
    loadingRecommendations.value = false
  }
}

// 生成备用选题推荐数据（当AI调用失败时使用）
const generateFallbackRecommendations = () => {
  const fallbackRecommendations = [
    {
      id: Date.now(),
      title: 'AI辅助内容创作实践',
      description: '探索AI在内容创作中的实际应用场景和效果',
      score: 4.8,
      platform: recommendationSettings.platform || 'douyin',
      difficulty: recommendationSettings.difficulty || 'medium',
      tags: ['AI工具', '内容创作', '实践案例'],
      estimatedTime: '2-3小时',
      potentialViews: '10-50万'
    },
    {
      id: Date.now() + 1,
      title: '社交媒体内容策略优化',
      description: '如何根据不同平台特点优化内容策略，提升传播效果',
      score: 4.6,
      platform: recommendationSettings.platform || 'xiaohongshu',
      difficulty: recommendationSettings.difficulty || 'easy',
      tags: ['社交媒体', '内容策略', '平台优化'],
      estimatedTime: '1-2小时',
      potentialViews: '5-20万'
    },
    {
      id: Date.now() + 2,
      title: '短视频脚本创作技巧',
      description: '掌握短视频脚本的核心要素，创作出吸引人的内容',
      score: 4.7,
      platform: recommendationSettings.platform || 'douyin',
      difficulty: recommendationSettings.difficulty || 'medium',
      tags: ['短视频', '脚本创作', '内容技巧'],
      estimatedTime: '1.5-2.5小时',
      potentialViews: '8-30万'
    }
  ]
  
  recommendations.value = fallbackRecommendations
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
  try {
    // 通过AI获取最新的爆款案例
    const prompt = `请为我生成12个当前最热门的社交媒体爆款内容案例。要求：
1. 案例要真实、具有参考价值，涵盖不同平台（抖音、小红书、B站、公众号）
2. 每个案例包含：标题、描述（40字以内）、平台（抖音/小红书/B站/公众号）、类型（video/short/article）、浏览量（如"125万"）、点赞数（如"8.6万"）、发布时间（如"2024-01-15"）、创作者名称、成功要素（3-4个）
3. 请以JSON格式返回，格式如下：
[
  {
    "title": "案例标题",
    "description": "案例描述",
    "platform": "B站",
    "type": "video",
    "views": "125万",
    "likes": "8.6万",
    "publishTime": "2024-01-15",
    "author": "创作者名称",
    "successElements": ["要素1", "要素2", "要素3"]
  }
]
请直接返回JSON数组，不要其他说明文字。`

    const res = await generateText({
      prompt: prompt,
      style: null
    })

    if (res.code === 200 && res.data && res.data.content) {
      try {
        // 尝试解析AI返回的JSON
        let content = res.data.content.trim()
        
        // 如果返回的内容包含markdown代码块，提取JSON部分
        const jsonMatch = content.match(/\[[\s\S]*\]/)
        if (jsonMatch) {
          content = jsonMatch[0]
        }
        
        const aiCases = JSON.parse(content)
        
        // 转换为前端需要的格式
        const newCases = aiCases.slice(0, 12).map((caseItem, index) => {
          // 根据平台生成占位图颜色
          const platformColors = {
            '抖音': 'f093fb',
            '小红书': 'ff6b6b',
            'B站': '667eea',
            '公众号': '4facfe'
          }
          const color = platformColors[caseItem.platform] || '667eea'
          
          return {
            id: Date.now() + index,
            title: caseItem.title || `案例${index + 1}`,
            description: caseItem.description || '',
            cover: `https://via.placeholder.com/400x225/${color}/ffffff?text=${encodeURIComponent(caseItem.title || '案例')}`,
            platform: caseItem.platform || 'B站',
            type: caseItem.type || 'video',
            views: caseItem.views || `${(Math.random() * 200 + 10).toFixed(1)}万`,
            likes: caseItem.likes || `${(Math.random() * 20 + 1).toFixed(1)}万`,
            publishTime: caseItem.publishTime || new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            author: caseItem.author || `创作者${String.fromCharCode(65 + index)}`,
            successElements: caseItem.successElements || ['内容优质', '形式新颖', '传播力强']
          }
        })
        
        cases.value = newCases.sort(() => Math.random() - 0.5)
        ElMessage.success(`成功获取${newCases.length}个最新爆款案例`)
      } catch (parseError) {
        console.error('解析AI返回数据失败:', parseError, '原始内容:', res.data.content)
        // 如果解析失败，使用备用数据
        generateFallbackCases()
        ElMessage.warning('AI数据解析失败，已使用备用数据')
      }
    } else {
      // 如果AI调用失败，使用备用数据
      generateFallbackCases()
      ElMessage.warning('AI获取失败，已使用备用数据')
    }
  } catch (error) {
    console.error('刷新案例失败:', error)
    // 如果出错，使用备用数据
    generateFallbackCases()
    ElMessage.error('刷新失败，已使用备用数据')
  } finally {
    loadingCases.value = false
  }
}

// 生成备用案例数据（当AI调用失败时使用）
const generateFallbackCases = () => {
  const fallbackCases = [
    {
      id: Date.now(),
      title: 'AI创作工具使用教程',
      description: '详细介绍如何使用AI工具提升内容创作效率和质量',
      cover: 'https://via.placeholder.com/400x225/667eea/ffffff?text=AI+创作工具',
      platform: 'B站',
      type: 'video',
      views: '125万',
      likes: '8.6万',
      publishTime: '2024-01-15',
      author: '创作者A',
      successElements: ['实用性强', '讲解清晰', '案例丰富']
    },
    {
      id: Date.now() + 1,
      title: '短视频封面设计技巧',
      description: '分享如何设计吸引人的短视频封面，提升视频点击率',
      cover: 'https://via.placeholder.com/400x225/f093fb/ffffff?text=封面设计',
      platform: '抖音',
      type: 'short',
      views: '98万',
      likes: '12.3万',
      publishTime: '2024-01-14',
      author: '创作者B',
      successElements: ['视觉冲击', '信息明确', '风格统一']
    },
    {
      id: Date.now() + 2,
      title: '内容营销策略分享',
      description: '深度解析内容营销的核心策略和实战经验',
      cover: 'https://via.placeholder.com/400x225/4facfe/ffffff?text=内容营销',
      platform: '公众号',
      type: 'article',
      views: '45万',
      likes: '3.2万',
      publishTime: '2024-01-13',
      author: '创作者C',
      successElements: ['深度分析', '实战经验', '可操作性强']
    },
    {
      id: Date.now() + 3,
      title: '小红书爆款封面设计',
      description: '分析小红书高点击率封面的设计规律和技巧',
      cover: 'https://via.placeholder.com/400x225/ff6b6b/ffffff?text=小红书封面',
      platform: '小红书',
      type: 'article',
      views: '68万',
      likes: '5.8万',
      publishTime: '2024-01-12',
      author: '创作者D',
      successElements: ['视觉美观', '信息清晰', '风格统一']
    }
  ]
  
  cases.value = fallbackCases
}

const viewCase = (caseItem) => {
  ElMessage.info(`查看案例详情：${caseItem.title}`)
}

const analyzeCase = (caseItem) => {
  ElMessage.info(`分析案例要素：${caseItem.title}`)
}

const useCaseAsTemplate = (caseItem) => {
  router.push({
    path: '/workbench',
    query: {
      type: caseItem.type === 'video' ? 'TEXT' : 'IMAGE',
      prompt: `参考案例"${caseItem.title}"的成功要素：${caseItem.successElements.join('、')}，创作类似内容：${caseItem.description}`
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