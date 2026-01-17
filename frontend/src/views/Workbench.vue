<template>
  <div class="workbench-container">
    <el-row :gutter="20" class="workbench-row">
      <!-- 左侧输入区域 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="input-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><EditPen /></el-icon>
                <span>创作输入</span>
              </div>
            </div>
          </template>
          
          <el-form :model="form" label-width="100px" class="workbench-form">
            <el-form-item label="创作类型">
              <el-radio-group v-model="form.type" @change="handleTypeChange" class="type-radio-group">
                <el-radio-button label="TEXT">
                  <el-icon><Document /></el-icon>
                  <span>文本创作</span>
                </el-radio-button>
                <el-radio-button label="IMAGE">
                  <el-icon><Picture /></el-icon>
                  <span>图片生成</span>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <!-- 文本创作模式选择 -->
            <el-form-item label="创作模式" v-if="form.type === 'TEXT'">
              <el-radio-group v-model="form.textMode" size="default">
                <el-radio-button label="single">单条文案</el-radio-button>
                <el-radio-button label="titles">爆款标题（10条）</el-radio-button>
                <el-radio-button label="script">视频脚本</el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="提示词">
              <el-input
                v-model="form.prompt"
                type="textarea"
                :rows="6"
                :placeholder="getPromptPlaceholder()"
              />
            </el-form-item>
            
            <el-form-item label="风格模板" v-if="form.type === 'TEXT'">
              <el-select
                v-model="form.styleTemplate"
                placeholder="请选择风格模板"
                style="width: 100%"
                clearable
                :loading="templatesLoading"
              >
                <el-option
                  v-for="template in templates"
                  :key="template.id"
                  :label="template.name"
                  :value="template.name"
                >
                  <span>{{ template.name }}</span>
                  <span v-if="template.description" style="color: #909399; font-size: 12px; margin-left: 8px">
                    - {{ template.description }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
            
            <!-- 图片生成增强选项 -->
            <template v-if="form.type === 'IMAGE'">
              <el-form-item label="图片风格">
                <el-select
                  v-model="form.imageStyle"
                  placeholder="选择图片风格"
                  style="width: 100%"
                >
                  <el-option label="默认风格" value="default" />
                  <el-option label="时尚潮流" value="fashion" />
                  <el-option label="科技感" value="tech" />
                  <el-option label="美食摄影" value="food" />
                  <el-option label="教育风格" value="education" />
                  <el-option label="文艺清新" value="artistic" />
                  <el-option label="商务专业" value="business" />
                  <el-option label="卡通插画" value="cartoon" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="图片尺寸">
                <el-radio-group v-model="form.imageSize" size="default">
                  <el-radio-button label="1024*1024">方形 (1:1)</el-radio-button>
                  <el-radio-button label="1280*720">横屏 (16:9)</el-radio-button>
                  <el-radio-button label="720*1280">竖屏 (9:16)</el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="配色方案" v-if="form.imageStyle">
                <el-select
                  v-model="form.colorScheme"
                  placeholder="选择配色方案"
                  style="width: 100%"
                >
                  <el-option label="自动配色" value="auto" />
                  <el-option label="温暖色调" value="warm">
                    <span>温暖色调</span>
                    <span style="display: inline-block; width: 20px; height: 20px; background: linear-gradient(90deg, #ff6b6b, #ffa500); border-radius: 4px; margin-left: 10px; vertical-align: middle;"></span>
                  </el-option>
                  <el-option label="冷色调" value="cool">
                    <span>冷色调</span>
                    <span style="display: inline-block; width: 20px; height: 20px; background: linear-gradient(90deg, #4facfe, #00f2fe); border-radius: 4px; margin-left: 10px; vertical-align: middle;"></span>
                  </el-option>
                  <el-option label="高对比度" value="high-contrast">
                    <span>高对比度</span>
                    <span style="display: inline-block; width: 20px; height: 20px; background: linear-gradient(90deg, #000, #fff); border-radius: 4px; margin-left: 10px; vertical-align: middle;"></span>
                  </el-option>
                  <el-option label="柔和色调" value="soft">
                    <span>柔和色调</span>
                    <span style="display: inline-block; width: 20px; height: 20px; background: linear-gradient(90deg, #f093fb, #f5576c); border-radius: 4px; margin-left: 10px; vertical-align: middle;"></span>
                  </el-option>
                </el-select>
              </el-form-item>
            </template>
            
            <el-form-item>
              <el-button
                type="primary"
                :loading="generating"
                @click="handleGenerate"
                style="width: 100%"
                size="large"
              >
                <el-icon><MagicStick /></el-icon>
                生成{{ form.type === 'TEXT' ? '文案' : '图片' }}
              </el-button>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="success"
                :loading="saving"
                :disabled="!(generatedContent || generatedTitles.length > 0 || generatedImageUrl)"
                @click="handleSave"
                style="width: 100%"
                size="large"
              >
                <el-icon><DocumentAdd /></el-icon>
                保存到历史记录
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <!-- 右侧预览区域 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="preview-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><View /></el-icon>
                <span>预览区域</span>
              </div>
              <div class="header-actions">
                <el-button
                  v-if="(form.type === 'TEXT' && (generatedContent || generatedTitles.length > 0)) || (form.type === 'IMAGE' && generatedImageUrl)"
                  type="primary"
                  @click="handleCopy"
                  size="small"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
            </div>
          </template>
          
          <div v-if="generating" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="form.type === 'TEXT'" class="text-preview">
            <!-- 多条标题展示 -->
            <div v-if="form.textMode === 'titles' && generatedTitles.length > 0" class="titles-container">
              <div
                v-for="(title, index) in generatedTitles"
                :key="index"
                class="title-card"
                @click="selectTitle(title, index)"
                :class="{ selected: selectedTitleIndex === index }"
              >
                <div class="title-number">{{ index + 1 }}</div>
                <div class="title-text">{{ title }}</div>
                <div class="title-actions">
                  <el-button size="small" text @click.stop="copyTitle(title)">
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
                  <el-button size="small" text @click.stop="regenerateTitle(index)">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            <!-- 单条文案或视频脚本展示 -->
            <div v-else-if="generatedContent" class="text-content">
              <div class="content-header" v-if="form.textMode === 'script'">
                <el-tag type="success">视频脚本</el-tag>
              </div>
              <div class="content-body">{{ generatedContent }}</div>
            </div>
            <el-empty v-else description="生成的内容将显示在这里" />
          </div>
          
          <div v-else class="image-preview">
            <div v-if="generatedImageUrl" class="image-content">
              <el-image
                :src="generatedImageUrl"
                fit="contain"
                style="width: 100%; max-height: 500px"
                :preview-src-list="[generatedImageUrl]"
              />
            </div>
            <el-empty v-else description="生成的图片将显示在这里" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, DocumentAdd, CopyDocument, EditPen, Document, Picture, View, Refresh } from '@element-plus/icons-vue'
import { generateText, generateImage } from '@/api/ai'
import { createRecord } from '@/api/creation'
import { getEnabledTemplates } from '@/api/template'

const route = useRoute()

const generating = ref(false)
const saving = ref(false)
const generatedContent = ref('')
const generatedImageUrl = ref('')
const generatedTitles = ref([])
const selectedTitleIndex = ref(-1)
const templates = ref([])
const templatesLoading = ref(false)

const form = reactive({
  type: 'TEXT',
  prompt: '',
  styleTemplate: '',
  textMode: 'single', // single, titles, script
  // 图片生成选项
  imageStyle: 'default',
  imageSize: '1024*1024',
  colorScheme: 'auto'
})

// 加载风格模板列表
const loadTemplates = async () => {
  templatesLoading.value = true
  try {
    const res = await getEnabledTemplates()
    if (res.code === 200) {
      templates.value = res.data || []
    }
  } catch (error) {
    console.error('加载模板列表失败：', error)
  } finally {
    templatesLoading.value = false
  }
}

const handleTypeChange = () => {
  generatedContent.value = ''
  generatedImageUrl.value = ''
  generatedTitles.value = []
  selectedTitleIndex.value = -1
}

const getPromptPlaceholder = () => {
  if (form.type === 'IMAGE') {
    return '请输入图片描述，例如：一只可爱的小猫在花园里玩耍'
  }
  if (form.textMode === 'titles') {
    return '请输入主题，例如：关于春节营销的爆款标题'
  }
  if (form.textMode === 'script') {
    return '请输入视频主题，例如：3分钟短视频介绍AI工具的使用'
  }
  return '请输入你的创作提示词，例如：写一个关于春天的短视频文案'
}

const handleGenerate = async () => {
  if (!form.prompt.trim()) {
    ElMessage.warning('请输入提示词')
    return
  }
  
  generating.value = true
  generatedContent.value = ''
  generatedImageUrl.value = ''
  
  try {
    if (form.type === 'TEXT') {
      let prompt = form.prompt
      
      // 根据模式调整提示词
      if (form.textMode === 'titles') {
        prompt = `请为以下主题生成10个吸引人的爆款标题，要求：1. 每个标题都要有吸引力 2. 适合社交媒体传播 3. 包含数字或情感词汇 4. 长度控制在15-25字之间\n\n主题：${form.prompt}\n\n请直接列出10个标题，每行一个，不要编号。`
      } else if (form.textMode === 'script') {
        prompt = `请为以下主题创作一个3分钟短视频脚本，包含：\n1. 开场（10秒）：吸引观众注意力\n2. 主体内容（2分30秒）：详细介绍主题\n3. 结尾（20秒）：总结并引导互动\n\n主题：${form.prompt}\n\n请按照"开场"、"主体"、"结尾"三个部分来组织内容。`
      }
      
      const res = await generateText({
        prompt: prompt,
        style: form.styleTemplate
      })
      if (res.code === 200) {
        if (form.textMode === 'titles') {
          // 解析多条标题
          const content = res.data.content
          const titles = content
            .split('\n')
            .map(line => line.trim())
            .filter(line => line && !line.match(/^\d+[\.、]/)) // 过滤编号
            .filter(line => line.length > 0)
            .slice(0, 10) // 最多10条
          
          generatedTitles.value = titles.length > 0 ? titles : [content]
          generatedContent.value = ''
          ElMessage.success(`成功生成${generatedTitles.value.length}个标题！`)
        } else {
          generatedContent.value = res.data.content
          generatedTitles.value = []
          ElMessage.success('文案生成成功！')
        }
      }
    } else {
      // 构建增强的图片生成提示词
      let enhancedPrompt = form.prompt
      
      // 添加风格描述
      const styleMap = {
        'fashion': '时尚潮流风格，现代感强，色彩鲜明',
        'tech': '科技感风格，未来感，蓝色调为主',
        'food': '美食摄影风格，诱人食欲，暖色调',
        'education': '教育风格，简洁清晰，专业感',
        'artistic': '文艺清新风格，柔和色调，有艺术感',
        'business': '商务专业风格，正式，稳重',
        'cartoon': '卡通插画风格，可爱，色彩丰富'
      }
      
      if (form.imageStyle !== 'default' && styleMap[form.imageStyle]) {
        enhancedPrompt = `${styleMap[form.imageStyle]}，${enhancedPrompt}`
      }
      
      // 添加配色描述
      const colorMap = {
        'warm': '使用温暖色调，如红色、橙色、黄色',
        'cool': '使用冷色调，如蓝色、青色、绿色',
        'high-contrast': '使用高对比度配色，黑白分明',
        'soft': '使用柔和色调，色彩饱和度较低'
      }
      
      if (form.colorScheme !== 'auto' && colorMap[form.colorScheme]) {
        enhancedPrompt = `${enhancedPrompt}，${colorMap[form.colorScheme]}`
      }
      
      const res = await generateImage({
        prompt: enhancedPrompt,
        size: form.imageSize
      })
      if (res.code === 200 && res.data && res.data.imageUrls && res.data.imageUrls.length > 0) {
        generatedImageUrl.value = res.data.imageUrls[0]
        ElMessage.success('图片生成成功！')
      } else {
        ElMessage.error(res.message || '图片生成失败，请检查提示词或稍后重试')
        console.error('图片生成失败：', res)
      }
    }
  } catch (error) {
    console.error('生成失败：', error)
    const errorMsg = error.response?.data?.message || error.message || '生成失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    generating.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const recordData = {
      type: form.type,
      prompt: form.prompt,
      styleTemplate: form.styleTemplate || null
    }
    
    if (form.type === 'TEXT') {
      if (form.textMode === 'titles' && generatedTitles.value.length > 0) {
        recordData.resultContent = generatedTitles.value.join('\n')
      } else {
        recordData.resultContent = generatedContent.value
      }
    } else {
      recordData.imageUrl = generatedImageUrl.value
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
  if (form.type === 'TEXT') {
    if (form.textMode === 'titles' && generatedTitles.value.length > 0) {
      const text = generatedTitles.value.join('\n')
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('已复制所有标题到剪贴板')
      })
    } else if (generatedContent.value) {
      navigator.clipboard.writeText(generatedContent.value).then(() => {
        ElMessage.success('已复制到剪贴板')
      })
    }
  } else if (form.type === 'IMAGE' && generatedImageUrl.value) {
    navigator.clipboard.writeText(generatedImageUrl.value).then(() => {
      ElMessage.success('图片链接已复制到剪贴板')
    })
  }
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

const regenerateTitle = async (index) => {
  // 重新生成单个标题
  ElMessage.info('重新生成功能开发中...')
}

onMounted(() => {
  loadTemplates()
  
  // 从路由参数中获取初始值
  const route = useRoute()
  if (route.query.type) {
    form.type = route.query.type
  }
  if (route.query.prompt) {
    form.prompt = route.query.prompt
  }
})
</script>

<style scoped>
.workbench-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(to bottom, #f8f9ff 0%, #ffffff 100%);
  min-height: calc(100vh - 60px);
}

.workbench-row {
  min-height: calc(100vh - 120px);
}

.input-card,
.preview-card {
  height: 100%;
  min-height: 650px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.input-card:hover,
.preview-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.header-icon {
  color: #667eea;
  font-size: 20px;
}

.workbench-form {
  padding: 8px 0;
}

.type-radio-group {
  width: 100%;
}

:deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  font-size: 14px;
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-select__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-button--large) {
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--success:hover:not(:disabled)) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

.loading-container {
  padding: 40px 20px;
}

.text-preview {
  min-height: 500px;
  padding: 10px;
}

.text-content {
  padding: 24px;
  line-height: 2;
  font-size: 15px;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.text-content:hover {
  border-color: #667eea;
  box-shadow: inset 0 2px 8px rgba(102, 126, 234, 0.1);
}

.image-preview {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.image-content {
  width: 100%;
  text-align: center;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.image-content:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  transform: scale(1.02);
}

:deep(.el-image) {
  border-radius: 12px;
}

:deep(.el-empty) {
  padding: 60px 20px;
}

:deep(.el-empty__description) {
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.titles-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 600px;
  overflow-y: auto;
  padding: 10px;
}

.title-card {
  background: white;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateX(5px);
}

.title-card.selected {
  border-color: #667eea;
  background: #f0f4ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.title-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.title-card.selected .title-number {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.title-text {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.6;
}

.title-actions {
  display: flex;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s;
}

.title-card:hover .title-actions {
  opacity: 1;
}

.content-header {
  margin-bottom: 15px;
}

.content-body {
  white-space: pre-wrap;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .workbench-container {
    padding: 16px;
  }
  
  .input-card,
  .preview-card {
    min-height: 500px;
    margin-bottom: 20px;
  }
}
</style>
