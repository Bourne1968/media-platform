<template>
  <div class="workbench-page">
    <div class="main-layout">
          <section class="left-panel">
        <div class="section">
          <div class="section-title">创作类型</div>
          <div class="type-grid">
            <div
              v-for="type in creationTypes"
              :key="type.value"
              class="type-card"
              :class="{ active: form.type === type.value }"
              @click="selectCreationType(type.value)"
            >
              <div class="type-icon">{{ type.icon }}</div>
              <div class="type-label">{{ type.label }}</div>
            </div>
          </div>
        </div>

        <div class="divider" />

        <div class="section">
          <div class="section-title">内容描述</div>
          <el-input
            v-model="form.prompt"
            type="textarea"
            :rows="8"
            :maxlength="500"
            show-word-limit
            :placeholder="getPromptPlaceholder()"
          />
        </div>

        <div class="divider" />

        <el-collapse v-model="advancedCollapse" class="section advanced-section">
          <el-collapse-item name="advanced">
            <template #title>
              <div class="section-title">高级选项</div>
            </template>
            <div class="field">
              <div class="field-label">内容风格</div>
              <el-radio-group v-model="form.styleTemplate" class="style-radio-group">
                <el-radio-button
                  v-for="style in styleOptions"
                  :key="style.value"
                  :label="style.value"
                >
                  {{ style.label }}
                </el-radio-button>
              </el-radio-group>
            </div>
            <div class="field">
              <div class="field-label">字数范围</div>
              <div class="slider-row">
                <el-slider
                  v-model="wordRange"
                  range
                  :min="50"
                  :max="500"
                  :step="10"
                  class="word-slider"
                />
                <div class="slider-value">{{ wordRange[0] }} - {{ wordRange[1] }} 字</div>
              </div>
            </div>
            <div class="field">
              <div class="field-label">生成数量</div>
              <div class="chip-group">
                <el-tag
                  v-for="count in countOptions"
                  :key="count"
                  class="chip"
                  :class="{ active: form.count === count }"
                  @click="form.count = count"
                >
                  {{ count }} 条
                </el-tag>
              </div>
            </div>
            <div class="field">
              <div class="field-label">目标平台</div>
              <el-checkbox-group v-model="form.platforms">
                <el-checkbox label="抖音" />
                <el-checkbox label="小红书" />
                <el-checkbox label="B站" />
              </el-checkbox-group>
            </div>
            <div class="field">
              <div class="field-label">关键词（可选）</div>
              <div class="tag-input">
                <el-tag
                  v-for="(tag, index) in form.keywords"
                  :key="tag"
                  closable
                  @close="removeKeyword(index)"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-model="keywordInput"
                  size="small"
                  placeholder="+ 添加关键词"
                  @keyup.enter.native="addKeyword"
                  @blur="addKeyword"
                />
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <div class="divider" />

        <div class="cta-group">
          <el-button
            type="primary"
            size="large"
            :loading="generating"
            @click="handleGenerate"
            class="generate-btn"
          >
            <el-icon><MagicStick /></el-icon>
            <span v-if="!generating">✨ 开始生成</span>
            <span v-else>⚡ 生成中...</span>
            <span class="hotkey">Ctrl/Cmd + Enter</span>
          </el-button>
          <el-button
            type="default"
            size="large"
            class="save-btn"
            :loading="saving"
            :disabled="!hasResult"
            @click="handleSave"
          >
            <el-icon><DocumentAdd /></el-icon>
            保存当前结果
          </el-button>
        </div>

      </section>

          <section class="right-panel">
        <div class="preview-header">
          <div>
            <div class="title-medium">生成结果</div>
            <div class="caption" v-if="previewStatus === 'success'">
              为你生成了 {{ resultCount }} 条{{ form.textMode === 'titles' ? '标题' : '内容' }}
            </div>
          </div>
          <div class="preview-actions">
            <el-button text :icon="CopyDocument" @click="handleCopy" :disabled="!hasResult">
              全部复制
            </el-button>
            <el-button text :icon="Download" :disabled="!hasResult" @click="exportResults">
              批量导出
            </el-button>
          </div>
        </div>

        <div class="preview-body" :class="`status-${previewStatus}`">
          <template v-if="previewStatus === 'idle'">
            <div class="empty-state">
              <div class="emoji">🎨</div>
              <div class="title-medium">在左侧输入创作需求</div>
              <div class="caption">AI 将为你生成精彩内容</div>
              <el-button type="primary" link>查看示例 →</el-button>
            </div>
          </template>

          <template v-else-if="previewStatus === 'loading'">
            <div class="loading-state">
              <div class="emoji">⚡</div>
              <div class="title-medium">AI 创作中...</div>
              <div class="caption">正在为你生成 {{ form.count }} 条{{ form.textMode === 'titles' ? '爆款标题' : '内容' }}</div>
              <el-progress :percentage="progress" :stroke-width="10" status="success" />
              <el-skeleton :rows="4" animated />
            </div>
          </template>

          <template v-else-if="previewStatus === 'error'">
            <div class="error-state">
              <div class="emoji">⚠️</div>
              <div class="title-medium">生成失败</div>
              <div class="caption">{{ errorMessage || '网络错误，请重试' }}</div>
              <el-button type="primary" @click="handleGenerate">重新生成</el-button>
            </div>
          </template>

          <template v-else-if="previewStatus === 'empty'">
            <div class="empty-state">
              <div class="emoji">😔</div>
              <div class="title-medium">暂无结果</div>
              <div class="caption">试试换个描述方式</div>
              <el-button type="primary" link>查看示例</el-button>
            </div>
          </template>

          <template v-else>
            <div class="result-list">
              <div
                v-if="form.textMode === 'titles'"
                v-for="(title, index) in generatedTitles"
                :key="index"
                class="result-card"
                :class="{ active: selectedTitleIndex === index }"
                @click="selectTitle(title, index)"
              >
                <div class="result-head">
                  <div class="result-index">{{ index + 1 }}</div>
                  <div class="result-title">{{ title }}</div>
                  <div class="rating">⭐⭐⭐⭐</div>
                </div>
                <div class="result-meta">
                  <span>字数: {{ title.length }}</span>
                  <span>适合平台: {{ form.platforms.join('、') || '通用' }}</span>
                  <span>预估点击率: 高</span>
                </div>
                <div class="result-actions">
                  <el-button size="small" text :icon="CopyDocument" @click.stop="copyTitle(title)">复制</el-button>
                  <el-button size="small" text :icon="EditPen" @click.stop="startEdit(title)">编辑</el-button>
                  <el-button size="small" text :icon="Refresh" @click.stop="regenerateTitle(index)">重生成</el-button>
                </div>
              </div>

              <div v-else class="result-card">
                <div class="result-head">
                  <div class="result-index">✓</div>
                  <div class="result-title">
                    {{ form.textMode === 'script' ? '视频脚本' : '创作文案' }}
                  </div>
                  <div class="rating">⭐⭐⭐⭐⭐</div>
                </div>
                <div class="result-content">{{ generatedContent }}</div>
                <div class="result-actions">
                  <el-button size="small" text :icon="CopyDocument" @click.stop="handleCopy">复制</el-button>
                  <el-button size="small" text :icon="DocumentAdd" @click.stop="handleSave">保存</el-button>
                </div>
              </div>

              <div class="result-footer">
                <el-button type="primary" plain :icon="Refresh" @click="handleGenerate">
                  再生成 {{ form.count }} 条
                </el-button>
                <el-button type="success" plain :icon="DocumentAdd" @click="handleSave" :disabled="!hasResult">
                  保存当前结果
                </el-button>
              </div>
            </div>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CopyDocument,
  DocumentAdd,
  Download,
  EditPen,
  MagicStick,
  Picture,
  Refresh,
  Tools,
  VideoCameraFilled
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { generateText } from '@/api/ai'
import { createRecord } from '@/api/creation'

const router = useRouter()
const route = useRoute()

const generating = ref(false)
const saving = ref(false)
const generatedContent = ref('')
const generatedTitles = ref([])
const selectedTitleIndex = ref(-1)
const advancedCollapse = ref([])
const errorMessage = ref('')
const progress = ref(30)
const keywordInput = ref('')

const creationTypes = [
  { value: 'titles', label: '标题', icon: '📌' },
  { value: 'script', label: '脚本', icon: '📄' },
  { value: 'single', label: '文案', icon: '✍️' },
  { value: 'comment', label: '评论', icon: '💬' }
]

const styleOptions = [
  { value: 'professional', label: '专业严谨', desc: '适合商务、教育' },
  { value: 'humor', label: '轻松幽默', desc: '适合娱乐、生活' },
  { value: 'emotional', label: '感性煽情', desc: '适合情感、故事' },
  { value: 'direct', label: '简洁直接', desc: '适合快消、电商' }
]

const countOptions = [1, 5, 10, 20]
const wordRange = ref([120, 240])

const form = reactive({
  type: 'titles', // titles | script | single | comment
  prompt: '',
  styleTemplate: 'humor',
  textMode: 'titles',
  count: 1,
  platforms: ['抖音'],
  keywords: []
})

const hasResult = computed(
  () => generatedContent.value || generatedTitles.value.length > 0
)

const resultCount = computed(() =>
  form.textMode === 'titles' ? generatedTitles.value.length : generatedContent.value ? 1 : 0
)


const previewStatus = computed(() => {
  if (generating.value) return 'loading'
  if (errorMessage.value) return 'error'
  if (!hasResult.value) return form.prompt.trim() ? 'empty' : 'idle'
  return 'success'
})

const selectCreationType = (value) => {
  form.type = value
  form.textMode = value === 'titles' ? 'titles' : value === 'script' ? 'script' : 'single'
  resetResult()
}

const addKeyword = () => {
  const text = keywordInput.value.trim()
  if (!text) return
  if (!form.keywords.includes(text)) {
    form.keywords.push(text)
  }
  keywordInput.value = ''
}

const removeKeyword = (index) => {
  form.keywords.splice(index, 1)
}

const getPromptPlaceholder = () => {
  if (form.textMode === 'titles') return '请详细描述你想生成的标题主题...'
  if (form.textMode === 'script') return '请输入视频主题和希望呈现的情节...'
  if (form.type === 'comment') return '请输入需要评论/互动的话题...'
  return '请输入你的创作提示词，例如：写一个关于春天的短视频文案'
}

const buildPrompt = () => {
  const base = form.prompt.trim()
  const styleText = styleOptions.find((item) => item.value === form.styleTemplate)?.label || ''
  const range = `${wordRange.value[0]}-${wordRange.value[1]} 字`
  const keywordText = form.keywords.length ? `关键词：${form.keywords.join('，')}` : ''
  const platformText = form.platforms.length ? `目标平台：${form.platforms.join('、')}` : '目标平台：通用'

  if (form.textMode === 'titles') {
    return `请为以下主题生成 ${form.count} 个吸引人的爆款标题，要求：长度控制在${range}，突出卖点，语气与风格为「${styleText}」。\n${platformText}\n${keywordText}\n主题：${base}\n请直接列出标题，每行一个，不要编号。`
  }
  if (form.textMode === 'script') {
    return `请为以下主题创作一个短视频脚本，包含开场、主体、结尾三个部分，总字数控制在${range}，语气与风格为「${styleText}」。\n${platformText}\n${keywordText}\n主题：${base}\n请结构化输出。`
  }
  return `请基于以下需求创作一篇文案，字数控制在${range}，语气与风格为「${styleText}」。\n${platformText}\n${keywordText}\n需求：${base}`
}

const resetResult = () => {
  generatedContent.value = ''
  generatedTitles.value = []
  selectedTitleIndex.value = -1
  errorMessage.value = ''
}

const handleGenerate = async () => {
  if (!form.prompt.trim()) {
    ElMessage.warning('请输入创作需求')
    return
  }
  generating.value = true
  errorMessage.value = ''
  resetResult()
  try {
    const prompt = buildPrompt()
    const res = await generateText({
      prompt,
      style: form.styleTemplate
    })
    if (res.code === 200) {
      if (form.textMode === 'titles') {
        const content = res.data.content
        const titles = content
          .split('\n')
          .map((line) => line.trim())
          .filter((line) => line && !line.match(/^\d+[\.、]/))
          .slice(0, form.count)
        generatedTitles.value = titles.length ? titles : [content]
        ElMessage.success(`成功生成 ${generatedTitles.value.length} 个标题！`)
      } else {
        generatedContent.value = res.data.content
        ElMessage.success('文案生成成功！')
      }
      progress.value = 100
    } else {
      throw new Error(res.message || '生成失败')
    }
  } catch (error) {
    console.error('生成失败：', error)
    errorMessage.value = error.message || '生成失败，请稍后重试'
    ElMessage.error(errorMessage.value)
  } finally {
    generating.value = false
    setTimeout(() => {
      progress.value = 30
    }, 800)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const recordData = {
      type: form.textMode === 'titles' ? 'TEXT' : 'TEXT',
      prompt: form.prompt,
      styleTemplate: form.styleTemplate || null
    }
    if (form.textMode === 'titles') {
      recordData.resultContent = generatedTitles.value.join('\n')
    } else {
      recordData.resultContent = generatedContent.value
    }
    const res = await createRecord(recordData, false)
    if (res.code === 200) {
      ElMessage.success('保存成功！')
    }
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

const handleCopy = () => {
  if (!hasResult.value) return
  const text =
    form.textMode === 'titles' ? generatedTitles.value.join('\n') : generatedContent.value
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

const selectTitle = (title, index) => {
  selectedTitleIndex.value = index
  generatedContent.value = title
}

const copyTitle = (title) => {
  navigator.clipboard.writeText(title).then(() => {
    ElMessage.success('标题已复制')
  })
}

const regenerateTitle = async () => {
  ElMessage.info('单条重生成开发中...')
}

const startEdit = () => {
  ElMessage.info('编辑功能开发中...')
}

const exportResults = () => {
  ElMessage.info('导出功能开发中...')
}

onMounted(() => {
  // 从路由参数获取初始化值
  if (route.query.type) {
    selectCreationType(route.query.type)
  }
  if (route.query.prompt) {
    form.prompt = route.query.prompt
  }
})
</script>

<style scoped>
:root {
  --primary-blue: #0066ff;
  --primary-blue-light: #f0f7ff;
  --primary-blue-dark: #0052cc;
  --gray-50: #fafafa;
  --gray-100: #f5f5f5;
  --gray-200: #e8e8e8;
  --gray-400: #999999;
  --gray-600: #666666;
  --gray-900: #1a1a1a;
  --success: #00cc88;
  --warning: #ff9500;
  --error: #ff3b30;
  --star-gold: #ffb800;
}

.workbench-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9ff 0%, #ffffff 100%);
  padding-bottom: 32px;
}

.main-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 20px;
  height: calc(100vh - 20px);
  overflow: hidden;
}

.left-panel {
  background: var(--gray-50);
  border-radius: 16px;
  padding: 24px;
  height: calc(100vh - 20px);
  overflow-y: auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.right-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  min-height: calc(100vh - 20px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  color: var(--gray-600);
}

.panel-title .title {
  font-size: 20px;
  font-weight: 700;
  color: var(--gray-900);
}

.panel-title .subtitle {
  color: var(--gray-600);
  font-size: 13px;
}

.divider {
  height: 1px;
  background: var(--gray-200);
  margin: 24px 0;
}

.section-title {
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: 12px;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.type-card {
  padding: 14px 10px;
  background: #ffffff;
  border: 1px solid var(--gray-200);
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--gray-600);
}

.type-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary-blue);
}

.type-card.active {
  background: linear-gradient(135deg, #0066ff 0%, #0052cc 100%);
  border-color: #0066ff;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(0, 102, 255, 0.35);
  font-weight: 700;
  transform: translateY(-2px);
}

.type-icon {
  font-size: 18px;
  margin-bottom: 6px;
}

.type-label {
  font-weight: 600;
}

.style-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

:deep(.el-radio-button__inner) {
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-size: 14px;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #0066ff 0%, #0052cc 100%);
  border-color: #0066ff;
  color: #ffffff;
  box-shadow: 0 3px 10px rgba(0, 102, 255, 0.3);
  font-weight: 600;
}

.style-desc {
  font-size: 12px;
  color: var(--gray-600);
}

::deep(.el-radio-button__inner) {
  width: 100%;
  padding: 10px 12px;
  align-items: flex-start;
  gap: 4px;
  border-radius: 10px !important;
}

::deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--primary-blue-light);
  border-color: var(--primary-blue);
  color: var(--primary-blue);
  box-shadow: 0 0 0 1px var(--primary-blue) inset, 0 4px 12px rgba(0, 102, 255, 0.12);
}

::deep(.el-radio-button__inner:hover) {
  background: #f5f7fb;
}

.advanced-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 0 12px;
}

.field {
  margin: 16px 0;
}

.field:first-child {
  margin-top: 8px;
}

.field:last-child {
  margin-bottom: 8px;
}

.field-label {
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: 6px;
}

.slider-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.word-slider {
  flex: 1;
}

.slider-value {
  width: 120px;
  text-align: right;
  color: var(--gray-600);
}

.chip-group {
  display: flex;
  gap: 8px;
}

.chip {
  cursor: pointer;
  border-radius: 8px;
  padding: 6px 12px;
  border: 1px solid var(--gray-200);
}

.chip.active {
  background: linear-gradient(135deg, #0066ff 0%, #0052cc 100%);
  color: #ffffff;
  border-color: #0066ff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.3);
}

.tag-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #ffffff;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  padding: 8px;
}

.cta-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: stretch;
  width: 100%;
}

.generate-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #0066ff 0%, #764ba2 100%);
  border: none;
  color: #ffffff;
  font-weight: 700;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 10px;
  padding: 0 20px;
  flex-shrink: 0;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 102, 255, 0.25);
}

.generate-btn .el-icon {
  margin-right: 8px;
  flex-shrink: 0;
}

.generate-btn span:not(.hotkey) {
  flex: 1;
  text-align: center;
}

.generate-btn .hotkey {
  flex-shrink: 0;
  margin-left: auto;
}

.save-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}

.hotkey {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.caption {
  font-size: 12px;
  color: var(--gray-600);
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-body {
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  padding: 20px;
  min-height: calc(100vh - 180px);
  background: #ffffff;
}

.empty-state,
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 100%;
  text-align: center;
  color: var(--gray-600);
}

.emoji {
  font-size: 32px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  padding: 16px;
  background: #ffffff;
  transition: all 0.2s ease;
}

.result-card:hover {
  border-color: var(--primary-blue);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.result-card.active {
  background: var(--primary-blue-light);
  border-color: var(--primary-blue);
}

.result-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.result-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0066ff, #764ba2);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.result-title {
  flex: 1;
  font-weight: 600;
  color: var(--gray-900);
}

.rating {
  color: var(--star-gold);
}

.result-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--gray-600);
  margin-bottom: 8px;
}

.result-content {
  white-space: pre-wrap;
  color: var(--gray-900);
  line-height: 1.7;
  margin-bottom: 10px;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.result-footer {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

@media (max-width: 1440px) {
  .main-layout {
    gap: 16px;
    padding: 16px;
  }
  
  .left-panel,
  .right-panel {
    padding: 20px;
  }
}

@media (max-width: 1200px) {
  .main-layout {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    padding: 12px;
  }
  
  .type-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  
  .left-panel {
    height: auto;
    max-height: none;
  }
  
  .right-panel {
    min-height: 500px;
  }
  
  .preview-body {
    min-height: 400px;
  }
  
}

@media (max-width: 768px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .type-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  
  .type-card {
    padding: 10px 8px;
  }
  
  .style-radio-group {
    flex-direction: column;
  }
  
  .slider-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .slider-value {
    width: 100%;
    text-align: left;
    margin-top: 8px;
  }
}
</style>
