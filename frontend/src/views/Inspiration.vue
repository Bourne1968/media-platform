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
            <el-button type="primary" @click="refreshTopics(true)" :loading="loadingTopics">
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
            <el-button type="primary" @click="generateRecommendations(true)" :loading="loadingRecommendations">
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
              <el-button type="primary" @click="refreshCases(true)" :loading="loadingCases">
                <el-icon><Refresh /></el-icon>
                刷新案例
              </el-button>
              <div v-if="generatingImages" class="image-generation-status">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>生成封面 ({{ imageGenerationProgress.completed }}/{{ imageGenerationProgress.total }})</span>
              </div>
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
                <img 
                  :src="caseItem.cover" 
                  :alt="caseItem.title"
                  @error="handleImageError($event, caseItem)"
                  loading="lazy"
                />
                <div class="case-cover-placeholder" v-if="!caseItem.cover || caseItem.coverError">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>{{ caseItem.title }}</span>
                </div>
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

    <!-- 案例详情对话框 -->
    <el-dialog v-model="caseDetailVisible" :title="currentCase?.title" width="800px">
      <div v-if="currentCase" class="case-detail">
        <div class="case-detail-cover">
          <img 
            :src="currentCase.cover" 
            :alt="currentCase.title"
            @error="handleImageError($event, currentCase)"
            loading="lazy"
          />
          <div class="case-cover-placeholder" v-if="!currentCase.cover || currentCase.coverError">
            <el-icon :size="64"><Picture /></el-icon>
            <span>{{ currentCase.title }}</span>
          </div>
        </div>
        <el-descriptions :column="2" border class="case-detail-info">
          <el-descriptions-item label="平台">
            <el-tag :type="getPlatformType(currentCase.platform)">
              {{ currentCase.platform }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="currentCase.type === 'video' ? 'primary' : currentCase.type === 'short' ? 'success' : 'info'">
              {{ currentCase.type === 'video' ? '视频' : currentCase.type === 'short' ? '短视频' : '图文' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览量">
            <el-icon><VideoPlay /></el-icon>
            {{ currentCase.views }}
          </el-descriptions-item>
          <el-descriptions-item label="点赞数">
            <el-icon><Star /></el-icon>
            {{ currentCase.likes }}
          </el-descriptions-item>
          <el-descriptions-item label="创作者">{{ currentCase.author }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentCase.publishTime }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentCase.description }}
          </el-descriptions-item>
          <el-descriptions-item label="爆款要素" :span="2">
            <div class="success-elements">
              <el-tag
                v-for="element in currentCase.successElements"
                :key="element"
                type="success"
                size="small"
                style="margin-right: 8px; margin-bottom: 8px;"
              >
                {{ element }}
              </el-tag>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="caseDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="useCaseAsTemplate(currentCase)">
          用作模板
        </el-button>
      </template>
    </el-dialog>

    <!-- 分析要素对话框 -->
    <el-dialog v-model="analyzeDialogVisible" :title="`分析案例：${currentCase?.title}`" width="700px">
      <div v-if="currentCase && analysisResult" class="analysis-content">
        <div v-if="analyzing" class="analyzing">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在分析案例要素...</p>
        </div>
        <div v-else class="analysis-result">
          <h4>成功要素分析</h4>
          <div class="analysis-elements">
            <div
              v-for="(element, index) in analysisResult.elements"
              :key="index"
              class="analysis-element-item"
            >
              <div class="element-header">
                <el-icon><CircleCheck /></el-icon>
                <span class="element-title">{{ element.title }}</span>
              </div>
              <p class="element-desc">{{ element.description }}</p>
            </div>
          </div>
          <div class="analysis-suggestions" v-if="analysisResult.suggestions">
            <h4>创作建议</h4>
            <ul>
              <li v-for="(suggestion, index) in analysisResult.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="analyzeDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="useCaseAsTemplate(currentCase)" v-if="!analyzing">
          用作模板
        </el-button>
      </template>
    </el-dialog>
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
  Star,
  Picture,
  Loading,
  CircleCheck
} from '@element-plus/icons-vue'
import { generateText, generateImage } from '@/api/ai'

const router = useRouter()
const activeTab = ref('topics')

// 案例详情对话框
const caseDetailVisible = ref(false)
const currentCase = ref(null)

// 分析要素对话框
const analyzeDialogVisible = ref(false)
const analyzing = ref(false)
const analysisResult = ref(null)

// 图片生成状态
const generatingImages = ref(false)
const imageGenerationProgress = ref({ total: 0, completed: 0 })

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
// 固定的默认案例数据（存储在本地，不需要调用AI）
const getDefaultCases = () => [
  {
    id: 1,
    title: '3分钟带你了解AI创作的未来',
    description: '用通俗易懂的方式解释AI创作技术的发展趋势',
    cover: 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=400&h=225&fit=crop&q=80',
    platform: 'B站',
    views: '150万',
    likes: '8.5万',
    author: '科技探索者',
    publishTime: '2024-01-15',
    successElements: ['开头吸引', '数据支撑', '未来展望'],
    type: 'video',
    coverError: false
  },
  {
    id: 2,
    title: '春节回家，我带了这些礼物',
    description: '真挚的情感表达，加上节日氛围的精心包装',
    cover: 'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=400&h=225&fit=crop&q=80',
    platform: '抖音',
    views: '200万',
    likes: '15万',
    author: '生活记录者',
    publishTime: '2024-01-20',
    successElements: ['情感共鸣', '节日氛围', '真实记录'],
    type: 'short',
    coverError: false
  },
  {
    id: 3,
    title: '小红书爆款封面的设计秘密',
    description: '通过数据分析，总结出高点击封面的设计规律',
    cover: 'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=400&h=225&fit=crop&q=80',
    platform: '小红书',
    views: '8.5万',
    likes: '3200',
    author: '设计达人',
    publishTime: '2024-01-18',
    successElements: ['数据分析', '实用技巧', '视觉化展示'],
    type: 'article',
    coverError: false
  },
  {
    id: 4,
    title: 'AI创作工具使用教程',
    description: '详细介绍如何使用AI工具提升内容创作效率和质量',
    cover: 'https://via.placeholder.com/400x225/667eea/ffffff?text=AI+工具教程',
    platform: 'B站',
    views: '125万',
    likes: '8.6万',
    author: '创作者A',
    publishTime: '2024-01-15',
    successElements: ['实用性强', '讲解清晰', '案例丰富'],
    type: 'video',
    coverError: false
  },
  {
    id: 5,
    title: '短视频封面设计技巧',
    description: '分享如何设计吸引人的短视频封面，提升视频点击率',
    cover: 'https://via.placeholder.com/400x225/f093fb/ffffff?text=封面设计技巧',
    platform: '抖音',
    views: '98万',
    likes: '12.3万',
    author: '创作者B',
    publishTime: '2024-01-14',
    successElements: ['视觉冲击', '信息明确', '风格统一'],
    type: 'short',
    coverError: false
  },
  {
    id: 6,
    title: '内容营销策略分享',
    description: '深度解析内容营销的核心策略和实战经验',
    cover: 'https://via.placeholder.com/400x225/4facfe/ffffff?text=内容营销',
    platform: '公众号',
    views: '45万',
    likes: '3.2万',
    author: '创作者C',
    publishTime: '2024-01-13',
    successElements: ['深度分析', '实战经验', '可操作性强'],
    type: 'article',
    coverError: false
  },
  {
    id: 7,
    title: '如何用AI写出10万+爆款文案',
    description: '揭秘AI文案创作的核心技巧，让你的内容脱颖而出',
    cover: 'https://via.placeholder.com/400x225/667eea/ffffff?text=AI+文案创作',
    platform: '公众号',
    views: '68万',
    likes: '5.8万',
    author: '文案大师',
    publishTime: '2024-01-12',
    successElements: ['技巧实用', '案例丰富', '可复制性强'],
    type: 'article',
    coverError: false
  },
  {
    id: 8,
    title: 'B站知识区UP主成长指南',
    description: '从0到100万粉丝，知识类UP主的完整成长路径',
    cover: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=225&fit=crop&q=80',
    platform: 'B站',
    views: '156万',
    likes: '12.5万',
    author: '知识分享者',
    publishTime: '2024-01-11',
    successElements: ['路径清晰', '经验分享', '可参考性强'],
    type: 'video',
    coverError: false
  },
  {
    id: 9,
    title: '抖音爆款视频的5个关键要素',
    description: '分析热门短视频的共同特点，帮你打造爆款内容',
    cover: 'https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=400&h=225&fit=crop&q=80',
    platform: '抖音',
    views: '320万',
    likes: '28.6万',
    author: '短视频专家',
    publishTime: '2024-01-10',
    successElements: ['要素明确', '案例丰富', '可操作性强'],
    type: 'short',
    coverError: false
  },
  {
    id: 10,
    title: '小红书种草笔记写作公式',
    description: '掌握这套公式，让你的种草笔记获得更多互动',
    cover: 'https://images.unsplash.com/photo-1558655146-d09347e92766?w=400&h=225&fit=crop&q=80',
    platform: '小红书',
    views: '52万',
    likes: '4.8万',
    author: '种草达人',
    publishTime: '2024-01-09',
    successElements: ['公式清晰', '案例丰富', '实用性强'],
    type: 'article',
    coverError: false
  },
  {
    id: 11,
    title: '公众号10万+阅读量的秘密',
    description: '深度解析高阅读量文章的标题、结构和传播策略',
    cover: 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=400&h=225&fit=crop&q=80',
    platform: '公众号',
    views: '89万',
    likes: '6.5万',
    author: '内容运营',
    publishTime: '2024-01-08',
    successElements: ['策略清晰', '数据支撑', '可复制性强'],
    type: 'article',
    coverError: false
  },
  {
    id: 12,
    title: '从0到1：新手UP主的起步指南',
    description: '给新手的完整指南，帮你快速上手内容创作',
    cover: 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=400&h=225&fit=crop&q=80',
    platform: 'B站',
    views: '78万',
    likes: '5.2万',
    author: 'UP主导师',
    publishTime: '2024-01-07',
    successElements: ['步骤清晰', '新手友好', '实用性强'],
    type: 'video',
    coverError: false
  }
]

// 初始化案例数据（使用默认固定案例）
const cases = ref(getDefaultCases())

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
const refreshTopics = async (forceRefresh = false) => {
  // 如果不是强制刷新，先尝试从缓存加载
  if (!forceRefresh) {
    const cachedTopics = sessionStorage.getItem('inspirationTopics')
    if (cachedTopics) {
      try {
        topics.value = JSON.parse(cachedTopics)
        return
      } catch (e) {
        console.error('加载缓存话题失败:', e)
      }
    }
  }
  
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
        // 保存到缓存
        sessionStorage.setItem('inspirationTopics', JSON.stringify(topics.value))
        ElMessage.success(`成功获取${newTopics.length}个最新热点话题`)
      } catch (parseError) {
        console.error('解析AI返回数据失败:', parseError, '原始内容:', res.data.content)
        // 如果解析失败，使用备用数据
        generateFallbackTopics()
        // 保存到缓存
        sessionStorage.setItem('inspirationTopics', JSON.stringify(topics.value))
        ElMessage.warning('AI数据解析失败，已使用备用数据')
      }
    } else {
      // 如果AI调用失败，使用备用数据
      generateFallbackTopics()
      // 保存到缓存
      sessionStorage.setItem('inspirationTopics', JSON.stringify(topics.value))
      ElMessage.warning('AI获取失败，已使用备用数据')
    }
  } catch (error) {
    console.error('刷新话题失败:', error)
    // 如果出错，使用备用数据
    generateFallbackTopics()
    // 保存到缓存
    sessionStorage.setItem('inspirationTopics', JSON.stringify(topics.value))
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

const generateRecommendations = async (forceRefresh = false) => {
  // 如果不是强制刷新，先尝试从缓存加载
  if (!forceRefresh) {
    const cachedRecommendations = sessionStorage.getItem('inspirationRecommendations')
    if (cachedRecommendations) {
      try {
        recommendations.value = JSON.parse(cachedRecommendations)
        return
      } catch (e) {
        console.error('加载缓存推荐失败:', e)
      }
    }
  }
  
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
        // 保存到缓存
        sessionStorage.setItem('inspirationRecommendations', JSON.stringify(recommendations.value))
        ElMessage.success(`成功生成${filtered.length}个个性化选题推荐`)
      } catch (parseError) {
        console.error('解析AI返回数据失败:', parseError, '原始内容:', res.data.content)
        // 如果解析失败，使用备用数据
        generateFallbackRecommendations()
        // 保存到缓存
        sessionStorage.setItem('inspirationRecommendations', JSON.stringify(recommendations.value))
        ElMessage.warning('AI数据解析失败，已使用备用数据')
      }
    } else {
      // 如果AI调用失败，使用备用数据
      generateFallbackRecommendations()
      // 保存到缓存
      sessionStorage.setItem('inspirationRecommendations', JSON.stringify(recommendations.value))
      ElMessage.warning('AI获取失败，已使用备用数据')
    }
  } catch (error) {
    console.error('生成推荐失败:', error)
    // 如果出错，使用备用数据
    generateFallbackRecommendations()
    // 保存到缓存
    sessionStorage.setItem('inspirationRecommendations', JSON.stringify(recommendations.value))
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

const refreshCases = async (forceRefresh = false) => {
  // 如果不是强制刷新，先尝试从缓存加载
  if (!forceRefresh) {
    const cachedCases = sessionStorage.getItem('inspirationCases')
    if (cachedCases) {
      try {
        cases.value = JSON.parse(cachedCases)
        return
      } catch (e) {
        console.error('加载缓存案例失败:', e)
        // 如果缓存加载失败，使用默认案例
        cases.value = getDefaultCases()
        return
      }
    } else {
      // 如果没有缓存，使用默认案例（不调用AI）
      cases.value = getDefaultCases()
      return
    }
  }
  
  // 只有用户主动刷新（forceRefresh=true）时才调用AI生成新案例
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
        // 保存到缓存
        sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
        ElMessage.success(`成功获取${newCases.length}个最新爆款案例`)
      } catch (parseError) {
        console.error('解析AI返回数据失败:', parseError, '原始内容:', res.data.content)
        // 如果解析失败，使用备用数据
        generateFallbackCases()
        // 保存到缓存
        sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
        ElMessage.warning('AI数据解析失败，已使用备用数据')
      }
    } else {
      // 如果AI调用失败，使用备用数据
      generateFallbackCases()
      // 保存到缓存
      sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
      ElMessage.warning('AI获取失败，已使用备用数据')
    }
  } catch (error) {
    console.error('刷新案例失败:', error)
    // 如果出错，使用备用数据
    generateFallbackCases()
    // 保存到缓存
    sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
    ElMessage.error('刷新失败，已使用备用数据')
  } finally {
    loadingCases.value = false
  }
}

// 生成备用案例数据（当AI调用失败时使用默认案例）
const generateFallbackCases = () => {
  cases.value = getDefaultCases()
}

const viewCase = (caseItem) => {
  currentCase.value = caseItem
  caseDetailVisible.value = true
}

const analyzeCase = async (caseItem) => {
  currentCase.value = caseItem
  analyzeDialogVisible.value = true
  analyzing.value = true
  analysisResult.value = null

  try {
    // 构建分析提示词
    const prompt = `请详细分析以下爆款案例的成功要素，并提供创作建议：

案例标题：${caseItem.title}
案例描述：${caseItem.description}
平台：${caseItem.platform}
类型：${caseItem.type === 'video' ? '视频' : caseItem.type === 'short' ? '短视频' : '图文'}
浏览量：${caseItem.views}
点赞数：${caseItem.likes}
成功要素：${caseItem.successElements.join('、')}

请从以下角度进行分析：
1. 内容策略：分析内容的核心价值和吸引力
2. 形式创新：分析呈现方式的独特之处
3. 传播机制：分析为什么能够获得高传播
4. 用户心理：分析如何触动用户情感

请以JSON格式返回分析结果，格式如下：
{
  "elements": [
    {
      "title": "要素名称",
      "description": "详细说明（100字左右）"
    }
  ],
  "suggestions": [
    "创作建议1",
    "创作建议2",
    "创作建议3"
  ]
}

请直接返回JSON，不要其他说明文字。`

    const res = await generateText({
      prompt: prompt,
      style: null
    })

    if (res.code === 200 && res.data && res.data.content) {
      try {
        let content = res.data.content.trim()
        
        // 如果返回的内容包含markdown代码块，提取JSON部分
        const jsonMatch = content.match(/\{[\s\S]*\}/)
        if (jsonMatch) {
          content = jsonMatch[0]
        }
        
        const result = JSON.parse(content)
        analysisResult.value = {
          elements: result.elements || caseItem.successElements.map(el => ({
            title: el,
            description: '该要素是案例成功的关键因素之一'
          })),
          suggestions: result.suggestions || [
            '参考该案例的内容结构',
            '借鉴其呈现方式',
            '学习其传播策略'
          ]
        }
        ElMessage.success('分析完成')
      } catch (parseError) {
        console.error('解析分析结果失败:', parseError)
        // 使用默认分析结果
        analysisResult.value = {
          elements: caseItem.successElements.map(el => ({
            title: el,
            description: '该要素是案例成功的关键因素之一，值得学习和借鉴'
          })),
          suggestions: [
            `参考该案例的"${caseItem.successElements[0]}"策略`,
            `借鉴其"${caseItem.successElements[1] || '内容形式'}"的呈现方式`,
            '学习其如何吸引用户关注和互动'
          ]
        }
        ElMessage.warning('AI分析解析失败，已使用默认分析')
      }
    } else {
      // 使用默认分析结果
      analysisResult.value = {
        elements: caseItem.successElements.map(el => ({
          title: el,
          description: '该要素是案例成功的关键因素之一，值得学习和借鉴'
        })),
        suggestions: [
          `参考该案例的"${caseItem.successElements[0]}"策略`,
          `借鉴其"${caseItem.successElements[1] || '内容形式'}"的呈现方式`,
          '学习其如何吸引用户关注和互动'
        ]
      }
      ElMessage.warning('AI分析失败，已使用默认分析')
    }
  } catch (error) {
    console.error('分析案例失败:', error)
    // 使用默认分析结果
    analysisResult.value = {
      elements: caseItem.successElements.map(el => ({
        title: el,
        description: '该要素是案例成功的关键因素之一，值得学习和借鉴'
      })),
      suggestions: [
        `参考该案例的"${caseItem.successElements[0]}"策略`,
        `借鉴其"${caseItem.successElements[1] || '内容形式'}"的呈现方式`,
        '学习其如何吸引用户关注和互动'
      ]
    }
    ElMessage.error('分析失败，已使用默认分析')
  } finally {
    analyzing.value = false
  }
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

// 带重试机制的图片生成函数
const generateImageWithRetry = async (caseItem, maxRetries = 3) => {
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      const imagePrompt = buildImagePrompt(caseItem)
      const imageRes = await generateImage({
        prompt: imagePrompt,
        size: '1024*1024'
      })
      
      if (imageRes.code === 200 && imageRes.data && imageRes.data.images && imageRes.data.images.length > 0) {
        return imageRes.data.images[0]
      } else if (imageRes.code !== 200) {
        // 检查是否是限流错误
        if (imageRes.message && (imageRes.message.includes('429') || imageRes.message.includes('rate limit') || imageRes.message.includes('限流'))) {
          throw new Error('RATE_LIMIT')
        }
        throw new Error(`API返回错误: ${imageRes.message || '未知错误'}`)
      }
    } catch (error) {
      // 如果是限流错误，等待更长时间后重试
      if (error.message === 'RATE_LIMIT' || (error.response && error.response.status === 429)) {
        if (attempt < maxRetries) {
          // 限流时等待时间递增：2秒、4秒、8秒
          const waitTime = Math.pow(2, attempt) * 1000
          console.warn(`API限流，等待 ${waitTime/1000} 秒后重试 (${attempt}/${maxRetries})`)
          await new Promise(resolve => setTimeout(resolve, waitTime))
          continue
        } else {
          console.error(`生成案例"${caseItem.title}"的封面图失败: API限流，已达到最大重试次数`)
          return null
        }
      }
      
      // 其他错误
      if (attempt < maxRetries) {
        console.warn(`生成案例"${caseItem.title}"的封面图失败，重试中 (${attempt}/${maxRetries}):`, error)
        await new Promise(resolve => setTimeout(resolve, 1000 * attempt))
        continue
      } else {
        console.error(`生成案例"${caseItem.title}"的封面图失败:`, error)
        return null
      }
    }
  }
  return null
}

// 为案例生成封面图片
const generateCaseImages = async (caseList) => {
  if (!caseList || caseList.length === 0) return
  
  generatingImages.value = true
  imageGenerationProgress.value = { total: caseList.length, completed: 0 }
  
  // 减少批次大小，增加延迟，避免API限流
  const batchSize = 1 // 每批只生成1张图片，避免并发过多
  const batches = []
  
  for (let i = 0; i < caseList.length; i += batchSize) {
    batches.push(caseList.slice(i, i + batchSize))
  }
  
  let successCount = 0
  let rateLimitCount = 0
  
  // 逐批生成图片（串行处理，避免并发）
  for (const batch of batches) {
    for (const caseItem of batch) {
      try {
        const imageUrl = await generateImageWithRetry(caseItem)
        
        if (imageUrl) {
          // 更新案例的封面图片
          const caseIndex = cases.value.findIndex(c => c.id === caseItem.id)
          if (caseIndex !== -1) {
            cases.value[caseIndex].cover = imageUrl
            cases.value[caseIndex].coverError = false
            // 更新缓存
            sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
            successCount++
          }
        } else {
          rateLimitCount++
        }
      } catch (error) {
        console.error(`处理案例"${caseItem.title}"时出错:`, error)
        if (error.message && error.message.includes('RATE_LIMIT')) {
          rateLimitCount++
        }
      } finally {
        imageGenerationProgress.value.completed++
      }
      
      // 每张图片之间延迟3秒，避免触发限流
      if (batches.indexOf(batch) < batches.length - 1 || batch.indexOf(caseItem) < batch.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 3000))
      }
    }
  }
  
  generatingImages.value = false
  
  // 显示结果提示
  if (rateLimitCount > 0) {
    ElMessage.warning(`图片生成完成，但 ${rateLimitCount} 个案例因API限流未能生成图片，请稍后再试`)
  } else if (successCount > 0) {
    ElMessage.success(`已为 ${successCount} 个案例生成封面图片`)
  } else {
    ElMessage.warning('图片生成失败，可能是API限流，请稍后再试')
  }
}

// 构建图片生成提示词
const buildImagePrompt = (caseItem) => {
  const platformStyles = {
    '抖音': '抖音风格，短视频封面，色彩鲜艳，吸引眼球',
    '小红书': '小红书风格，精美图文，清新优雅，高颜值',
    'B站': 'B站风格，视频封面，二次元或知识类，专业感',
    '公众号': '公众号风格，图文并茂，简洁大气，有深度'
  }
  
  const typeStyles = {
    'video': '视频封面',
    'short': '短视频封面',
    'article': '图文封面'
  }
  
  const platformStyle = platformStyles[caseItem.platform] || '社交媒体封面'
  const typeStyle = typeStyles[caseItem.type] || '内容封面'
  
  return `${platformStyle}，${typeStyle}，主题：${caseItem.title}，${caseItem.description}，风格：现代、专业、吸引人，高质量，16:9横版比例`
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

// 处理图片加载错误
const handleImageError = (event, caseItem) => {
  // 标记图片加载失败
  caseItem.coverError = true
  // 隐藏错误的图片
  event.target.style.display = 'none'
  // 可以设置一个默认的占位图
  // event.target.src = 'data:image/svg+xml;base64,...'
}

// 检查是否是首次登录（本次会话）
const isFirstLoadInSession = () => {
  const flag = sessionStorage.getItem('inspirationDataLoaded')
  if (!flag) {
    sessionStorage.setItem('inspirationDataLoaded', 'true')
    return true
  }
  return false
}

// 从缓存加载数据
const loadCachedData = () => {
  try {
    // 加载缓存的热点话题
    const cachedTopics = sessionStorage.getItem('inspirationTopics')
    if (cachedTopics) {
      topics.value = JSON.parse(cachedTopics)
    }
    
    // 加载缓存的选题推荐
    const cachedRecommendations = sessionStorage.getItem('inspirationRecommendations')
    if (cachedRecommendations) {
      recommendations.value = JSON.parse(cachedRecommendations)
    }
    
    // 加载缓存的爆款案例
    const cachedCases = sessionStorage.getItem('inspirationCases')
    if (cachedCases) {
      cases.value = JSON.parse(cachedCases)
    }
  } catch (e) {
    console.error('加载缓存数据失败:', e)
  }
}

// 保存数据到缓存
const saveDataToCache = () => {
  try {
    if (topics.value.length > 0) {
      sessionStorage.setItem('inspirationTopics', JSON.stringify(topics.value))
    }
    if (recommendations.value.length > 0) {
      sessionStorage.setItem('inspirationRecommendations', JSON.stringify(recommendations.value))
    }
    if (cases.value.length > 0) {
      sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
    }
  } catch (e) {
    console.error('保存缓存数据失败:', e)
  }
}

onMounted(async () => {
  // 检查是否是首次加载（本次会话）
  const isFirstLoad = isFirstLoadInSession()
  
  if (isFirstLoad) {
    // 首次加载：使用默认固定案例，不调用AI
    cases.value = getDefaultCases()
    // 保存默认案例到缓存
    sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
  } else {
    // 非首次加载：从缓存恢复数据
    loadCachedData()
    // 如果缓存为空或加载失败，使用默认案例
    if (!cases.value || cases.value.length === 0) {
      cases.value = getDefaultCases()
      sessionStorage.setItem('inspirationCases', JSON.stringify(cases.value))
    }
  }
  
  // 确保所有案例都有 coverError 字段
  cases.value = cases.value.map(c => ({ ...c, coverError: c.coverError || false }))
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
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transition: color 0.3s ease;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  transition: color 0.3s ease;
}

.inspiration-content {
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: background-color 0.3s ease;
}

.inspiration-tabs :deep(.el-tabs__header) {
  margin: 0;
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.inspiration-tabs :deep(.el-tabs__item) {
  font-weight: 500;
  padding: 16px 24px;
  border-bottom: 2px solid transparent;
  transition: color 0.3s ease;
}

.inspiration-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.inspiration-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  transition: border-color 0.3s ease;
}

.tab-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
  transition: color 0.3s ease;
}

.tab-info p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 14px;
  transition: color 0.3s ease;
}

.tab-actions {
  display: flex;
  gap: 12px;
}

.filters, .case-filters {
  padding: 16px 32px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-color);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

/* 话题卡片 */
.topics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
  padding: 32px;
}

.topic-card {
  background: var(--card-bg);
  border-radius: 12px;
  border: 2px solid var(--border-color);
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.topic-card:hover {
  border-color: var(--primary-color);
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
  color: var(--primary-color);
  transition: color 0.3s ease;
}

.topic-heat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--danger-color);
  font-weight: 600;
  transition: color 0.3s ease;
}

.topic-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.topic-content p {
  color: var(--text-regular);
  margin: 0 0 16px 0;
  line-height: 1.6;
  transition: color 0.3s ease;
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
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.3s ease;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: border-color 0.3s ease;
}

/* 推荐设置 */
.recommendation-settings {
  padding: 16px 32px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-color);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

/* 推荐卡片 */
.recommendations-list {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.recommendation-card {
  background: var(--card-bg);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  padding: 24px;
  transition: all 0.3s ease;
}

.recommendation-card:hover {
  border-color: var(--primary-color);
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
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.recommendation-content p {
  color: var(--text-regular);
  margin: 0 0 16px 0;
  line-height: 1.6;
  transition: color 0.3s ease;
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
  color: var(--text-primary);
  min-width: 80px;
  transition: color 0.3s ease;
}

.analysis-item .value {
  color: #409eff;
  font-weight: 500;
}

.recommendation-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: border-color 0.3s ease;
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
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.case-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.case-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.case-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.case-card:hover .case-cover img {
  transform: scale(1.05);
}

.case-cover-placeholder {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  text-align: center;
}

.case-cover-placeholder .el-icon {
  opacity: 0.7;
}

.case-cover-placeholder span {
  font-size: 14px;
  font-weight: 500;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
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
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.case-content p {
  color: var(--text-regular);
  margin: 0 0 16px 0;
  line-height: 1.6;
  font-size: 14px;
  transition: color 0.3s ease;
}

.case-analysis {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.analysis-tag {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.analysis-tag .label {
  font-weight: 500;
  color: var(--text-primary);
  margin-right: 4px;
  transition: color 0.3s ease;
}

.case-author, .case-time {
  display: flex;
  align-items: center;
}

.case-author .label,
.case-time .label {
  font-weight: 500;
  color: var(--text-primary);
  margin-right: 4px;
  transition: color 0.3s ease;
}

.case-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  gap: 8px;
  transition: border-color 0.3s ease;
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

  /* 案例详情对话框样式 */
  .case-detail-cover {
    height: 200px;
  }

  .filters, .case-filters, .recommendation-settings {
    padding: 12px 20px;
  }

  .case-detail-cover {
    height: 200px;
  }
}

/* 案例详情对话框样式 */
.case-detail {
  padding: 0;
}

.case-detail-cover {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
  position: relative;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.case-detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case-detail-info {
  margin-top: 20px;
}

.success-elements {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 分析要素对话框样式 */
.analysis-content {
  padding: 20px 0;
}

.analyzing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.analyzing .el-icon {
  color: var(--primary-color);
}

.analyzing p {
  color: var(--text-secondary);
  font-size: 14px;
}

.analysis-result h4 {
  margin: 0 0 16px 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.analysis-elements {
  margin-bottom: 24px;
}

.analysis-element-item {
  padding: 16px;
  background: var(--bg-card);
  border-radius: 8px;
  margin-bottom: 12px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.analysis-element-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.element-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.element-header .el-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.element-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.element-desc {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.analysis-suggestions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.analysis-suggestions h4 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.analysis-suggestions ul {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
}

.analysis-suggestions li {
  margin-bottom: 8px;
  line-height: 1.6;
  font-size: 14px;
}

/* 图片生成状态样式 */
.image-generation-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  padding: 8px 12px;
  background: var(--bg-card);
  border-radius: 6px;
  border: 1px solid var(--border-color);
}

.image-generation-status .el-icon {
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .image-generation-status {
    font-size: 12px;
    padding: 6px 10px;
  }
}
</style>