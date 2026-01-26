<template>
  <div v-if="showGuide" class="guide-overlay" @click="closeGuide">
    <div class="guide-content" @click.stop>
      <div class="guide-header">
        <h3>欢迎使用 AI 创作平台！</h3>
        <el-button text @click="closeGuide">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="创作灵感" description="获取创作灵感" />
        <el-step title="工作台" description="开始创作" />
        <el-step title="创作库" description="管理作品" />
        <el-step title="创作日历" description="查看统计" />
      </el-steps>
      
      <div class="guide-body">
        <div v-if="currentStep === 0" class="step-content">
          <el-icon :size="60" color="#667eea"><HomeFilled /></el-icon>
          <h4>创作灵感中心</h4>
          <p>在这里您可以：</p>
          <ul>
            <li>查看今日热门话题</li>
            <li>获取随机创作灵感</li>
            <li>了解节日和热点提醒</li>
          </ul>
        </div>
        
        <div v-if="currentStep === 1" class="step-content">
          <el-icon :size="60" color="#667eea"><EditPen /></el-icon>
          <h4>AI文案生成页</h4>
          <p>在这里您可以：</p>
          <ul>
            <li>生成文本内容（单条文案、爆款标题、视频脚本）</li>
            <li>生成图片（支持多种风格和尺寸）</li>
            <li>选择风格模板</li>
            <li>保存创作到历史记录</li>
          </ul>
        </div>
        
        <div v-if="currentStep === 2" class="step-content">
          <el-icon :size="60" color="#667eea"><Document /></el-icon>
          <h4>我的创作库</h4>
          <p>在这里您可以：</p>
          <ul>
            <li>查看所有创作历史</li>
            <li>搜索和筛选作品</li>
            <li>切换网格/时间轴视图</li>
            <li>管理和删除作品</li>
          </ul>
        </div>
        
        <div v-if="currentStep === 3" class="step-content">
          <el-icon :size="60" color="#667eea"><Calendar /></el-icon>
          <h4>创作日历</h4>
          <p>在这里您可以：</p>
          <ul>
            <li>查看创作统计</li>
            <li>按日期查看作品</li>
            <li>了解创作活跃度</li>
          </ul>
        </div>
      </div>
      
      <div class="guide-footer">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 3" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-else type="success" @click="finishGuide">开始使用</el-button>
        <el-button text @click="skipGuide">跳过引导</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Close, HomeFilled, EditPen, Document, Calendar } from '@element-plus/icons-vue'

const showGuide = ref(false)
const currentStep = ref(0)

const checkFirstVisit = () => {
  // 只在已登录时显示引导
  const token = sessionStorage.getItem('token')
  if (!token) {
    return
  }
  
  const hasVisited = localStorage.getItem('hasVisited')
  if (!hasVisited) {
    showGuide.value = true
  }
}

const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const finishGuide = () => {
  localStorage.setItem('hasVisited', 'true')
  showGuide.value = false
}

const skipGuide = () => {
  localStorage.setItem('hasVisited', 'true')
  showGuide.value = false
}

const closeGuide = () => {
  showGuide.value = false
}

onMounted(() => {
  // 延迟显示，让页面先加载
  setTimeout(() => {
    checkFirstVisit()
  }, 1000)
})
</script>

<style scoped>
.guide-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.guide-content {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.guide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.guide-header h3 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.guide-body {
  margin: 30px 0;
  min-height: 200px;
}

.step-content {
  text-align: center;
  padding: 20px;
}

.step-content h4 {
  margin: 20px 0 15px;
  font-size: 20px;
  color: #303133;
}

.step-content p {
  color: #606266;
  margin-bottom: 15px;
}

.step-content ul {
  text-align: left;
  display: inline-block;
  color: #606266;
  line-height: 2;
}

.step-content li {
  margin-bottom: 8px;
}

.guide-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-steps) {
  margin-bottom: 30px;
}
</style>
