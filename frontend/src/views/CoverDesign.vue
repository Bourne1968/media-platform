<template>
    <div class="cover-design-page">
      <div class="cover-design-shell">
        <!-- 侧边栏 -->
        <aside class="side-nav">
          <div class="side-logo">
            <el-icon><Lightning /></el-icon>
            <span>AI Creator Studio</span>
          </div>
          <nav class="side-menu">
            <div
              v-for="item in filteredNavItems"
              :key="item.key"
              class="side-item"
              :class="{ active: activeNav === item.key }"
              @click="handleNavClick(item)"
            >
              <div class="item-left">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
              <span v-if="activeNav === item.key" class="item-dot"></span>
            </div>
          </nav>
          <div class="side-footer">
            <el-button 
              text 
              class="achievement-btn"
              @click="showAchievement = true"
            >
              <el-icon><Trophy /></el-icon>
              <span>成就系统</span>
            </el-button>
            <el-dropdown placement="top-start" @command="handleUserCommand" trigger="click">
              <div class="user-card" style="cursor: pointer;">
                <div class="user-avatar">{{ userInitial }}</div>
                <div>
                  <p class="user-name">{{ username || '用户' }}</p>
                  <p class="user-tier">{{ userTier }}</p>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </aside>
  
        <div class="content-area">
          <div class="main-layout">
            <!-- 左侧控制面板 -->
            <section class="left-panel">
              <!-- 封面类型 -->
              <div class="section">
                <div class="section-title">封面类型</div>
                <div class="type-grid">
                  <div
                    v-for="type in coverTypes"
                    :key="type.value"
                    class="type-card"
                    :class="{ active: form.coverType === type.value }"
                    @click="form.coverType = type.value"
                  >
                    <div class="type-icon">{{ type.icon }}</div>
                    <div class="type-label">{{ type.label }}</div>
                  </div>
                </div>
              </div>
  
              <div class="divider" />

              <!-- 描述输入 -->
              <div class="section">
                <div class="section-title">描述你想要的封面</div>
                <el-input
                  v-model="form.prompt"
                  type="textarea"
                  :rows="8"
                  :maxlength="300"
                  show-word-limit
                  placeholder="请描述封面的主题和风格...
  
  例如：
  • 科技感十足的手机产品图
  • 温馨的美食烹饪场景
  • 简约的知识分享封面
  • 充满活力的运动健身"
                />
              </div>
  
              <!-- 视觉风格 -->
              <div class="section">
                <div class="section-title">视觉风格</div>
                <div class="style-grid">
                  <div
                    v-for="style in visualStyles"
                    :key="style.value"
                    class="style-chip"
                    :class="{ active: form.visualStyle === style.value }"
                    @click="form.visualStyle = style.value"
                  >
                    {{ style.label }}
                  </div>
                </div>
              </div>
  
              <div class="divider" />

              <!-- 配色方案 -->
              <div class="section">
                <div class="section-title">配色方案</div>
                <div class="color-grid">
                  <div
                    v-for="color in colorSchemes"
                    :key="color.value"
                    class="color-card"
                    :class="{ active: form.colorScheme === color.value }"
                    @click="form.colorScheme = color.value"
                  >
                    <div class="color-icon" :style="{ backgroundColor: color.bgColor }">
                      {{ color.icon }}
                    </div>
                    <div class="color-label">{{ color.label }}</div>
                  </div>
                </div>
              </div>
  
              <div class="divider" />

              <!-- 添加文字 -->
              <div class="section">
                <div class="section-title">添加文字 (可选)</div>
                <div class="field">
                  <div class="field-label">主标题</div>
                  <el-input v-model="form.mainTitle" placeholder="输入主标题" />
                </div>
                <div class="field" style="margin-top: 12px">
                  <div class="field-label">副标题</div>
                  <el-input v-model="form.subTitle" placeholder="输入副标题" />
                </div>
              </div>
  
              <div class="divider" />

              <!-- 生成按钮 -->
              <div class="cta-group">
                <el-button
                  type="primary"
                  size="large"
                  :loading="generating"
                  @click="handleGenerate"
                  class="generate-btn"
                >
                  <el-icon><MagicStick /></el-icon>
                  <span v-if="!generating">🎨 生成封面</span>
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
  
            <!-- 右侧预览区域 -->
            <section class="right-panel">
              <div class="preview-header">
                <div>
                  <div class="title-medium">封面预览</div>
                  <div class="caption" v-if="previewStatus === 'success'">
                    为你生成了 {{ generatedImages.length }} 张封面
                  </div>
                  <div class="caption" v-if="form.platform">
                    尺寸: {{ getPlatformSize(form.platform) }}
                  </div>
                </div>
                <div class="preview-actions" v-if="previewStatus === 'success'">
                  <el-button text :icon="Grid" @click="viewMode = 'grid'" :disabled="viewMode === 'grid'">
                    网格视图
                  </el-button>
                  <el-button text :icon="Picture" @click="viewMode = 'single'" :disabled="viewMode === 'single'">
                    单图视图
                  </el-button>
                  <el-button text :icon="Download" @click="handleDownloadAll">
                    全部下载
                  </el-button>
                </div>
              </div>
  
              <div class="preview-body" :class="`status-${previewStatus}`">
                <!-- 空状态 -->
                <template v-if="previewStatus === 'idle'">
                  <div class="empty-state">
                    <div class="emoji">🎨</div>
                    <div class="title-medium">输入描述，AI为你生成</div>
                    <div class="caption">专业级封面设计</div>
                    <el-button type="primary" link>查看案例 →</el-button>
                  </div>
                </template>
  
                <!-- 加载中 -->
                <template v-else-if="previewStatus === 'loading'">
                  <div class="loading-state">
                    <div class="emoji">🎨</div>
                    <div class="title-medium">AI 绘制中...</div>
                    <div class="caption">正在为你生成 {{ form.count }} 张高清封面</div>
                    <el-progress :percentage="progress" :stroke-width="10" status="success" />
                    <div class="progress-text">预计剩余时间: {{ estimatedTime }}秒</div>
                  </div>
                </template>
  
                <!-- 错误状态 -->
                <template v-else-if="previewStatus === 'error'">
                  <div class="error-state">
                    <div class="emoji">⚠️</div>
                    <div class="title-medium">生成失败</div>
                    <div class="caption">{{ errorMessage || '网络错误，请重试' }}</div>
                    <el-button type="primary" @click="handleGenerate">重新生成</el-button>
                  </div>
                </template>
  
                <!-- 生成成功 - 单图视图 -->
                <template v-else-if="previewStatus === 'success' && viewMode === 'single'">
                  <div class="single-view">
                    <div class="image-preview-large">
                      <img 
                        v-if="generatedImages[currentImageIndex]" 
                        :src="generatedImages[currentImageIndex]" 
                        alt="封面预览"
                        @error="handleImageError"
                        @load="handleImageLoad"
                      />
                      <div v-else class="image-error">图片加载中...</div>
                    </div>
                    <div class="image-navigation">
                      <el-button :icon="ArrowLeft" @click="prevImage" :disabled="currentImageIndex === 0">
                        上一张
                      </el-button>
                      <span class="image-counter">预览 ({{ currentImageIndex + 1 }}/{{ generatedImages.length }})</span>
                      <el-button :icon="ArrowRight" @click="nextImage" :disabled="currentImageIndex === generatedImages.length - 1">
                        下一张
                      </el-button>
                    </div>
                    <div class="image-actions">
                      <el-button :icon="Download" @click="handleDownload(currentImageIndex)">下载</el-button>
                      <el-button :icon="EditPen" @click="handleEdit(currentImageIndex)">编辑</el-button>
                      <el-button :icon="Refresh" @click="handleRegenerate(currentImageIndex)">重新生成</el-button>
                    </div>
                  </div>
                </template>
  
                <!-- 生成成功 - 网格视图 -->
                <template v-else-if="previewStatus === 'success' && viewMode === 'grid'">
                  <div class="grid-view">
                    <div
                      v-for="(image, index) in generatedImages"
                      :key="index"
                      class="image-card"
                    >
                      <div class="image-preview">
                        <img 
                          v-if="image"
                          :src="image" 
                          alt="封面预览"
                          @error="handleImageError"
                          @load="handleImageLoad"
                        />
                        <div v-else class="image-error">图片加载中...</div>
                      </div>
                      <div class="image-rating">⭐⭐⭐⭐⭐</div>
                      <div class="image-card-actions">
                        <el-button size="small" text :icon="Download" @click="handleDownload(index)">下载</el-button>
                        <el-button size="small" text :icon="EditPen" @click="handleEdit(index)">编辑</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="result-footer">
                    <el-button type="primary" plain :icon="Refresh" @click="handleGenerate">
                      再生成 {{ form.count }} 张
                    </el-button>
                    <el-button type="success" plain :icon="DocumentAdd" @click="handleSave">
                      保存当前结果
                    </el-button>
                  </div>
                </template>
              </div>
            </section>
          </div>
        </div>
      </div>
    
      <!-- 成就系统 -->
      <AchievementSystem v-model="showAchievement" />
    </div>
  </template>
  
  <script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    ArrowLeft,
    ArrowRight,
    DocumentAdd,
    Download,
    EditPen,
    Grid,
    Lightning,
    MagicStick,
    Picture,
    Refresh,
    SwitchButton,
    Trophy,
    HomeFilled,
    EditPen as EditPenIcon,
    Document,
    Calendar
  } from '@element-plus/icons-vue'
  import { generateImage } from '@/api/ai'
  import { createRecord } from '@/api/creation'
  import AchievementSystem from '@/components/AchievementSystem.vue'
  
  const router = useRouter()
  const route = useRoute()
  
  const generating = ref(false)
  const saving = ref(false)
  const generatedImages = ref([])
  const currentImageIndex = ref(0)
  const viewMode = ref('single') // single | grid
  const advancedCollapse = ref([])
  const errorMessage = ref('')
  const progress = ref(0)
  const estimatedTime = ref(15)
  const activeNav = ref('cover-design')
  const username = ref('')
  const userTier = ref('普通用户')
  const isAdmin = ref(false)
  const showAchievement = ref(false)
  
  const coverTypes = [
    { value: 'video', label: '视频', icon: '🎬' },
    { value: 'poster', label: '海报', icon: '📱' },
    { value: 'article', label: '文章', icon: '📄' }
  ]
  
  const navItems = [
    { key: 'home', label: '首页', icon: HomeFilled, route: '/home' },
    { key: 'workbench', label: 'AI创作工作台', icon: EditPenIcon, route: '/workbench' },
    { key: 'cover-design', label: 'AI封面设计', icon: Picture, route: '/cover-design' },
    { key: 'history', label: '创作库', icon: Document, route: '/history' },
    { key: 'calendar', label: '创作日历', icon: Calendar, route: '/calendar' }
  ]
  
  const visualStyles = [
    { value: 'simple', label: '简约' },
    { value: 'tech', label: '科技' },
    { value: 'fashion', label: '时尚' },
    { value: 'chinese', label: '国潮' },
    { value: 'illustration', label: '插画' },
    { value: 'retro', label: '复古' }
  ]
  
  const colorSchemes = [
    { value: 'orange', label: '活力橙', icon: '🔴', bgColor: '#FF6B35' },
    { value: 'blue', label: '科技蓝', icon: '🔵', bgColor: '#0066FF' },
    { value: 'green', label: '清新绿', icon: '🟢', bgColor: '#00CC88' },
    { value: 'yellow', label: '阳光黄', icon: '🟡', bgColor: '#FFB800' },
    { value: 'black', label: '高级黑', icon: '⚫', bgColor: '#1a1a1a' },
    { value: 'purple', label: '梦幻紫', icon: '🟣', bgColor: '#8B5CF6' },
    { value: 'ai', label: 'AI配色', icon: '🎨', bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
  ]
  
  const countOptions = [1, 3, 5]

  const form = reactive({
    coverType: 'video',
    prompt: '',
    visualStyle: '',
    colorScheme: 'ai',
    mainTitle: '',
    subTitle: '',
    platform: 'douyin',
    count: 1
  })
  
  const hasResult = computed(() => generatedImages.value.length > 0)
  
  const userInitial = computed(() => {
    if (username.value) {
      return username.value.charAt(0).toUpperCase()
    }
    return 'U'
  })
  
  const filteredNavItems = computed(() => {
    return navItems
  })
  
  const previewStatus = computed(() => {
    if (generating.value) return 'loading'
    if (errorMessage.value) return 'error'
    if (!hasResult.value) return 'idle'
    return 'success'
  })
  
  const handleNavClick = (item) => {
    activeNav.value = item.key
    if (item.route && item.route !== route.path) {
      router.push(item.route)
    }
  }
  
  const handleUserCommand = (command) => {
    if (command === 'logout') {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }).catch(() => {})
    }
  }
  
  
  const getPlatformSize = (platform) => {
    const sizes = {
      douyin: '1080x1920',
      bilibili: '1920x1080',
      xiaohongshu: '1080x1440',
      wechat: '1920x816',
      custom: '自定义'
    }
    return sizes[platform] || '1080x1920'
  }
  
  const buildPrompt = () => {
    let prompt = form.prompt.trim()
    
    // 添加封面类型
    const coverTypeMap = {
      video: '视频封面',
      poster: '海报封面',
      article: '文章封面',
      dynamic: '动态封面'
    }
    prompt += `，${coverTypeMap[form.coverType]}`
    
    // 添加视觉风格
    if (form.visualStyle) {
      const styleLabel = visualStyles.find(s => s.value === form.visualStyle)?.label || ''
      if (styleLabel) {
        prompt += `，风格：${styleLabel}`
      }
    }
    
    // 添加配色
    if (form.colorScheme && form.colorScheme !== 'ai') {
      const colorLabel = colorSchemes.find(c => c.value === form.colorScheme)?.label || ''
      prompt += `，配色：${colorLabel}`
    }
    
    // 添加文字
    if (form.mainTitle) {
      prompt += `，主标题：${form.mainTitle}`
    }
    if (form.subTitle) {
      prompt += `，副标题：${form.subTitle}`
    }
    
    // 添加平台尺寸要求
    const sizeMap = {
      douyin: '9:16竖屏比例',
      bilibili: '16:9横屏比例',
      xiaohongshu: '3:4比例',
      wechat: '2.35:1比例'
    }
    if (sizeMap[form.platform]) {
      prompt += `，尺寸：${sizeMap[form.platform]}`
    }
    
    return prompt
  }
  
  const handleGenerate = async () => {
    if (!form.prompt.trim()) {
      ElMessage.warning('请输入封面描述')
      return
    }
    
    generating.value = true
    errorMessage.value = ''
    generatedImages.value = []
    progress.value = 0
    currentImageIndex.value = 0
    
    try {
      // 模拟进度
      const progressInterval = setInterval(() => {
        if (progress.value < 90) {
          progress.value += 10
          estimatedTime.value = Math.max(1, estimatedTime.value - 2)
        }
      }, 500)
      
      const prompt = buildPrompt()
      const res = await generateImage({
        prompt,
        size: getPlatformSize(form.platform).replace('x', '*')
      })
      
      clearInterval(progressInterval)
      progress.value = 100
      
      if (res.code === 200) {
        console.log('图片生成响应数据:', res.data)
        
        // 后端返回的数据结构：{ imageUrls: [...], model: '...' }
        if (res.data.imageUrls && Array.isArray(res.data.imageUrls)) {
          // 如果生成数量大于返回的图片数量，只使用返回的数量
          generatedImages.value = res.data.imageUrls.slice(0, form.count)
        } else if (typeof res.data.url === 'string') {
          // 兼容单个URL的情况
          generatedImages.value = [res.data.url]
        } else if (Array.isArray(res.data.url)) {
          generatedImages.value = res.data.url.slice(0, form.count)
        } else if (res.data.urls && Array.isArray(res.data.urls)) {
          generatedImages.value = res.data.urls.slice(0, form.count)
        } else {
          console.error('无法解析图片URL，响应数据:', res.data)
          throw new Error('图片生成成功但无法获取图片URL，请检查响应数据格式')
        }
        
        console.log('解析后的图片URLs:', generatedImages.value)
        ElMessage.success(`成功生成 ${generatedImages.value.length} 张封面！`)
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
        progress.value = 0
        estimatedTime.value = 15
      }, 1000)
    }
  }
  
  const handleSave = async () => {
    if (!hasResult.value) {
      ElMessage.warning('没有可保存的内容')
      return
    }
    
    saving.value = true
    try {
      const recordData = {
        type: 'IMAGE',
        prompt: form.prompt,
        resultContent: JSON.stringify({
          images: generatedImages.value,
          coverType: form.coverType,
          platform: form.platform,
          visualStyle: form.visualStyle,
          colorScheme: form.colorScheme
        })
      }
      
      const res = await createRecord(recordData, false)
      if (res.code === 200) {
        ElMessage.success('保存成功！')
      }
    } catch (error) {
      console.error('保存失败：', error)
      ElMessage.error('保存失败')
    } finally {
      saving.value = false
    }
  }
  
  const handleDownload = async (index) => {
    const imageUrl = generatedImages.value[index]
    if (!imageUrl) {
      ElMessage.warning('图片URL不存在')
      return
    }

    try {
      ElMessage.info('正在下载图片...')
      
      // 如果是Base64图片，直接下载
      if (imageUrl.startsWith('data:')) {
        downloadBase64Image(imageUrl, `封面-${index + 1}-${Date.now()}.png`)
        ElMessage.success('下载成功')
        return
      }

      // 对于远程URL，需要先获取图片再下载（处理跨域问题）
      const response = await fetch(imageUrl, {
        method: 'GET',
        mode: 'cors',
        credentials: 'omit'
      })

      if (!response.ok) {
        throw new Error(`HTTP错误! 状态: ${response.status}`)
      }

      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      
      // 从URL中提取文件名，或使用默认名称
      const urlObj = new URL(imageUrl)
      const urlPath = urlObj.pathname
      let fileName = `封面-${index + 1}-${Date.now()}`
      
      // 尝试从URL中提取文件名
      if (urlPath) {
        const urlFileName = urlPath.split('/').pop()
        if (urlFileName && urlFileName.includes('.')) {
          const ext = urlFileName.split('.').pop()
          fileName = `封面-${index + 1}-${Date.now()}.${ext}`
        } else {
          // 根据blob类型确定扩展名
          const ext = blob.type.split('/')[1] || 'jpg'
          fileName = `封面-${index + 1}-${Date.now()}.${ext}`
        }
      } else {
        // 根据blob类型确定扩展名
        const ext = blob.type.split('/')[1] || 'jpg'
        fileName = `封面-${index + 1}-${Date.now()}.${ext}`
      }

      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 释放URL对象
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('下载成功')
    } catch (error) {
      console.error('下载失败:', error)
      
      // 如果fetch失败，尝试直接使用链接下载（可能受跨域限制）
      try {
        const link = document.createElement('a')
        link.href = imageUrl
        link.download = `封面-${index + 1}-${Date.now()}.jpg`
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        ElMessage.warning('正在打开图片，请右键保存')
      } catch (fallbackError) {
        console.error('备用下载方法也失败:', fallbackError)
        ElMessage.error('下载失败，请检查网络连接或图片URL')
      }
    }
  }

  // Base64图片下载
  const downloadBase64Image = (base64String, filename) => {
    const link = document.createElement('a')
    link.href = base64String
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
  
  const handleDownloadAll = async () => {
    if (generatedImages.value.length === 0) {
      ElMessage.warning('没有可下载的图片')
      return
    }

    ElMessage.info(`开始下载 ${generatedImages.value.length} 张图片，请稍候...`)
    
    // 使用Promise来逐个下载，避免浏览器阻止多个下载
    for (let i = 0; i < generatedImages.value.length; i++) {
      await handleDownload(i)
      // 每张图片下载后等待一段时间，避免浏览器阻止
      if (i < generatedImages.value.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 500))
      }
    }
    
    ElMessage.success(`已开始下载 ${generatedImages.value.length} 张图片`)
  }
  
  const handleEdit = (index) => {
    ElMessage.info('编辑功能开发中...')
  }
  
  const handleRegenerate = async (index) => {
    try {
      ElMessage.info(`正在重新生成第${index + 1}张图片...`)

      // 为重新生成添加一些随机性，避免生成完全相同的图片
      const basePrompt = form.prompt.trim()
      const randomSeed = Math.random().toString(36).substring(7)
      const regeneratePrompt = `${basePrompt}，请生成一个不同的设计版本 ${randomSeed}`

      const res = await generateImage({
        prompt: regeneratePrompt,
        size: getPlatformSize(form.platform).replace('x', '*')
      })

      if (res.code === 200) {
        console.log('重新生成响应数据:', res.data)

        // 更新指定索引的图片
        let newImageUrl = null
        if (res.data.imageUrls && Array.isArray(res.data.imageUrls) && res.data.imageUrls.length > 0) {
          newImageUrl = res.data.imageUrls[0]
        } else if (typeof res.data.url === 'string') {
          newImageUrl = res.data.url
        } else if (res.data.urls && Array.isArray(res.data.urls) && res.data.urls.length > 0) {
          newImageUrl = res.data.urls[0]
        }

        if (newImageUrl) {
          generatedImages.value[index] = newImageUrl
          ElMessage.success(`第${index + 1}张图片重新生成成功！`)
        } else {
          throw new Error('重新生成成功但无法获取图片URL')
        }
      } else {
        throw new Error(res.message || '重新生成失败')
      }
    } catch (error) {
      console.error('重新生成失败:', error)
      ElMessage.error(error.message || '重新生成失败，请重试')
    }
  }
  
  const prevImage = () => {
    if (currentImageIndex.value > 0) {
      currentImageIndex.value--
    }
  }
  
  const nextImage = () => {
    if (currentImageIndex.value < generatedImages.value.length - 1) {
      currentImageIndex.value++
    }
  }

  const handleImageError = (event) => {
    console.error('图片加载失败:', event.target.src)
    ElMessage.error('图片加载失败，请检查图片URL')
  }

  const handleImageLoad = () => {
    console.log('图片加载成功')
  }

  onMounted(() => {
    // 初始化用户信息
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        username.value = user.username || user.name || '用户'
        userTier.value = user.tier || (user.role === 'ADMIN' ? '管理员' : '普通用户')
        isAdmin.value = user.role === 'ADMIN'
      } catch (e) {
        console.error('解析用户信息失败：', e)
      }
    }
  
    // 根据当前路由设置激活的导航项
    const currentPath = route.path
    const matchedNav = navItems.find(item => item.route === currentPath)
    if (matchedNav) {
      activeNav.value = matchedNav.key
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
  }
  
  .cover-design-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #f8f9ff 0%, #ffffff 100%);
    padding-bottom: 32px;
  }
  
  .cover-design-shell {
    display: flex;
    min-height: 100vh;
  }
  
  .side-nav {
    width: 220px;
    border-right: 1px solid #eef0f3;
    background: #ffffff;
    display: flex;
    flex-direction: column;
    padding: 18px 12px;
    gap: 12px;
    height: 100vh;
    overflow-y: auto;
    position: sticky;
    top: 0;
  }
  
  .side-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-radius: 16px;
    font-weight: 700;
    color: #4b5bd7;
    background: #f6f8ff;
  }
  
  .side-menu {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 4px;
  }
  
  .side-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 14px;
    color: #5f6368;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .side-item:hover {
    background: #f5f7fb;
  }
  
  .side-item.active {
    background: #eef2ff;
    color: #4b5bd7;
    font-weight: 700;
  }
  
  .item-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .item-dot {
    width: 6px;
    height: 6px;
    border-radius: 9999px;
    background: #4b5bd7;
    margin-left: auto;
  }
  
  .side-footer {
    padding: 8px;
    border-top: 1px solid #f0f2f5;
    margin-top: auto;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .achievement-btn {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    color: var(--gray-600);
    transition: all 0.2s ease;
  }
  
  .achievement-btn:hover {
    background: var(--gray-100);
    color: var(--primary-blue);
  }
  
  .user-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .user-card:hover {
    background: #f5f7fb;
  }
  
  .user-avatar {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    background: linear-gradient(135deg, #7c5dfa, #6fa8ff);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ffffff;
    font-weight: 700;
  }
  
  .user-name {
    font-weight: 700;
    color: #1f2a44;
  }
  
  .user-tier {
    font-size: 12px;
    color: #7a8190;
  }
  
  .content-area {
    flex: 1;
    display: flex;
    flex-direction: column;
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
    overflow-y: auto;
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
    font-size: 13px;
  }

  .method-group {
    width: 100%;
  }

  .method-radio-group {
    display: flex;
    gap: 8px;
    width: 100%;
  }

  :deep(.el-radio-button__inner) {
    padding: 10px 20px;
    border-radius: 8px;
    transition: all 0.2s ease;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: linear-gradient(135deg, #0066ff 0%, #0052cc 100%);
    border-color: #0066ff;
    color: #ffffff;
    box-shadow: 0 3px 10px rgba(0, 102, 255, 0.3);
    font-weight: 600;
  }

  .style-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }

  .style-chip {
    padding: 10px 12px;
    background: #ffffff;
    border: 1px solid var(--gray-200);
    border-radius: 8px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s ease;
    color: var(--gray-600);
    font-size: 14px;
  }

  .style-chip:hover {
    border-color: var(--primary-blue);
    background: var(--primary-blue-light);
  }

  .style-chip.active {
    background: var(--primary-blue);
    border-color: var(--primary-blue);
    color: #000000 !important;
    font-weight: 700;
    font-size: 14px;
    box-shadow: 0 2px 8px rgba(0, 102, 255, 0.3);
  }

  .style-chip.active:hover {
    background: var(--primary-blue-dark);
    border-color: var(--primary-blue-dark);
    color: #000000 !important;
  }

  .color-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
  }

  .color-card {
    padding: 10px;
    background: #ffffff;
    border: 2px solid transparent;
    border-radius: 10px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .color-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .color-card.active {
    border-color: var(--primary-blue);
    box-shadow: 0 4px 12px rgba(0, 102, 255, 0.3);
  }

  .color-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    margin: 0 auto 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
  }

  .color-label {
    font-size: 12px;
    color: var(--gray-600);
    font-weight: 500;
  }

  .field {
    margin: 16px 0;
  }

  .field-label {
    font-weight: 600;
    color: var(--gray-900);
    margin-bottom: 8px;
    font-size: 14px;
  }

  .advanced-section {
    background: #ffffff;
    border-radius: 12px;
    padding: 0 12px;
  }

  .chip-group {
    display: flex;
    gap: 8px;
  }

  .chip {
    cursor: pointer;
    border-radius: 8px;
    padding: 6px 16px;
    border: 1px solid var(--gray-200);
  }

  .chip.active {
    background: linear-gradient(135deg, #0066ff 0%, #0052cc 100%);
    color: #ffffff;
    border-color: #0066ff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(0, 102, 255, 0.3);
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
  }

  .generate-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(0, 102, 255, 0.25);
  }

  .generate-btn .el-icon {
    margin-right: 8px;
  }

  .generate-btn span:not(.hotkey) {
    flex: 1;
    text-align: center;
  }

  .hotkey {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }

  .save-btn {
    width: 100%;
    height: 48px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .template-filter {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }

  .filter-tag {
    cursor: pointer;
    padding: 6px 12px;
  }

  .filter-tag.active {
    background: var(--primary-blue);
    color: #ffffff;
    border-color: var(--primary-blue);
  }

  .template-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .template-card {
    padding: 16px;
    background: #ffffff;
    border: 1px solid var(--gray-200);
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;
    text-align: center;
    position: relative;
  }

  .template-card:hover {
    border-color: var(--primary-blue);
    box-shadow: 0 4px 12px rgba(0, 102, 255, 0.15);
  }

  .template-preview {
    width: 100%;
    height: 100px;
    background: var(--gray-100);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    margin-bottom: 8px;
  }

  .template-name {
    font-weight: 600;
    color: var(--gray-900);
    font-size: 14px;
  }

  .template-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: #ff6b35;
    color: #ffffff;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 11px;
  }

  .preview-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .title-medium {
    font-size: 18px;
    font-weight: 700;
    color: var(--gray-900);
    margin-bottom: 4px;
  }

  .caption {
    font-size: 12px;
    color: var(--gray-600);
  }

  .preview-actions {
    display: flex;
    gap: 8px;
  }

  .preview-body {
    border: 1px solid var(--gray-200);
    border-radius: 12px;
    padding: 20px;
    min-height: calc(100vh - 280px);
    background: #ffffff;
  }

  .empty-state,
  .loading-state,
  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    height: 100%;
    text-align: center;
    color: var(--gray-600);
  }

  .emoji {
    font-size: 48px;
  }

  .progress-text {
    font-size: 14px;
    color: var(--gray-600);
    margin-top: 8px;
  }

  .single-view {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .image-preview-large {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background: var(--gray-50);
    border-radius: 12px;
    padding: 20px;
    min-height: 500px;
  }

  .image-preview-large img {
    max-width: 100%;
    max-height: 600px;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  .image-error {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 400px;
    color: var(--gray-600);
    font-size: 14px;
  }

  .image-navigation {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
  }

  .image-counter {
    font-size: 14px;
    color: var(--gray-600);
    font-weight: 500;
  }

  .image-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
  }

  .grid-view {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .image-card {
    border: 1px solid var(--gray-200);
    border-radius: 12px;
    padding: 12px;
    background: #ffffff;
    transition: all 0.2s ease;
  }

  .image-card:hover {
    border-color: var(--primary-blue);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  }

  .image-preview {
    width: 100%;
    aspect-ratio: 9/16;
    background: var(--gray-50);
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 8px;
  }

  .image-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-rating {
    color: #ffb800;
    font-size: 14px;
    margin-bottom: 8px;
  }

  .image-card-actions {
    display: flex;
    gap: 8px;
    justify-content: center;
  }

  .result-footer {
    display: flex;
    gap: 12px;
    margin-top: 24px;
    justify-content: center;
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
      grid-template-columns: 380px 1fr;
      gap: 12px;
      padding: 12px;
    }
    
    .side-nav {
      width: 200px;
    }
    
    .type-grid {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .style-grid {
      grid-template-columns: repeat(3, 1fr);
    }
  }

  @media (max-width: 1024px) {
    .main-layout {
      grid-template-columns: 1fr;
    }
    
    .left-panel {
      height: auto;
      max-height: 600px;
    }
    
    .right-panel {
      min-height: 500px;
    }
    
    .preview-body {
      min-height: 400px;
    }
    
    .cover-design-shell {
      flex-direction: column;
    }
    
    .side-nav {
      width: 100%;
      border-right: none;
      border-bottom: 1px solid #eef0f3;
      flex-direction: row;
      padding: 12px;
      overflow-x: auto;
      height: auto;
    }
    
    .side-menu {
      flex-direction: row;
      flex: 1;
      gap: 8px;
    }
    
    .side-logo {
      flex-shrink: 0;
    }
    
    .side-footer {
      display: none;
    }
  }

  @media (max-width: 768px) {
    .type-grid,
    .color-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
    }
    
    .style-grid {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .grid-view {
      grid-template-columns: 1fr;
    }
  }
</style>