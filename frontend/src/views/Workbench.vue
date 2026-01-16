<template>
  <div class="workbench-container">
    <el-row :gutter="20" class="workbench-row">
      <!-- 左侧输入区域 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="input-card">
          <template #header>
            <div class="card-header">
              <span>创作输入</span>
            </div>
          </template>
          
          <el-form :model="form" label-width="100px">
            <el-form-item label="创作类型">
              <el-radio-group v-model="form.type" @change="handleTypeChange">
                <el-radio-button label="TEXT">文本创作</el-radio-button>
                <el-radio-button label="IMAGE">图片生成</el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="提示词">
              <el-input
                v-model="form.prompt"
                type="textarea"
                :rows="6"
                placeholder="请输入你的创作提示词，例如：写一个关于春天的短视频文案"
              />
            </el-form-item>
            
            <el-form-item label="风格模板" v-if="form.type === 'TEXT'">
              <el-select
                v-model="form.styleTemplate"
                placeholder="请选择风格模板"
                style="width: 100%"
                clearable
              >
                <el-option label="轻松活泼" value="轻松活泼" />
                <el-option label="正式严谨" value="正式严谨" />
                <el-option label="幽默风趣" value="幽默风趣" />
                <el-option label="文艺清新" value="文艺清新" />
                <el-option label="吸引眼球" value="吸引眼球" />
              </el-select>
            </el-form-item>
            
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
                :disabled="!generatedContent"
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
        <el-card class="preview-card">
          <template #header>
            <div class="card-header">
              <span>预览区域</span>
              <el-button
                v-if="generatedContent"
                type="text"
                @click="handleCopy"
                size="small"
              >
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>
          </template>
          
          <div v-if="generating" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="form.type === 'TEXT'" class="text-preview">
            <div v-if="generatedContent" class="text-content">
              {{ generatedContent }}
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, DocumentAdd, CopyDocument } from '@element-plus/icons-vue'
import { generateText, generateImage } from '@/api/ai'
import { createRecord } from '@/api/creation'

const generating = ref(false)
const saving = ref(false)
const generatedContent = ref('')
const generatedImageUrl = ref('')

const form = reactive({
  type: 'TEXT',
  prompt: '',
  styleTemplate: ''
})

const handleTypeChange = () => {
  generatedContent.value = ''
  generatedImageUrl.value = ''
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
      const res = await generateText({
        prompt: form.prompt,
        style: form.styleTemplate
      })
      if (res.code === 200) {
        generatedContent.value = res.data.content
        ElMessage.success('文案生成成功！')
      }
    } else {
      const res = await generateImage({
        prompt: form.prompt
      })
      if (res.code === 200 && res.data.imageUrls && res.data.imageUrls.length > 0) {
        generatedImageUrl.value = res.data.imageUrls[0]
        ElMessage.success('图片生成成功！')
      }
    }
  } catch (error) {
    console.error('生成失败：', error)
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
      recordData.resultContent = generatedContent.value
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
  if (form.type === 'TEXT' && generatedContent.value) {
    navigator.clipboard.writeText(generatedContent.value).then(() => {
      ElMessage.success('已复制到剪贴板')
    })
  } else if (form.type === 'IMAGE' && generatedImageUrl.value) {
    navigator.clipboard.writeText(generatedImageUrl.value).then(() => {
      ElMessage.success('图片链接已复制到剪贴板')
    })
  }
}
</script>

<style scoped>
.workbench-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.workbench-row {
  min-height: calc(100vh - 120px);
}

.input-card,
.preview-card {
  height: 100%;
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.loading-container {
  padding: 20px;
}

.text-preview {
  min-height: 400px;
}

.text-content {
  padding: 20px;
  line-height: 1.8;
  font-size: 15px;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f5f7fa;
  border-radius: 4px;
}

.image-preview {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-content {
  width: 100%;
  text-align: center;
}
</style>
