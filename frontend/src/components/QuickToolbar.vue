<template>
  <div class="quick-toolbar" :class="{ 'collapsed': collapsed }">
    <div class="toolbar-toggle" @click="collapsed = !collapsed">
      <el-icon><Tools /></el-icon>
    </div>
    
    <transition name="fade">
      <div v-show="!collapsed" class="toolbar-content">
        <div class="toolbar-title">快捷工具</div>
        <div class="toolbar-items">
          <div 
            v-for="tool in tools" 
            :key="tool.id"
            class="tool-item"
            @click="handleToolClick(tool)"
            :title="tool.name"
          >
            <el-icon :size="20"><component :is="tool.icon" /></el-icon>
            <span class="tool-name">{{ tool.name }}</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Tools, EditPen, Picture, Document, Calendar, 
  MagicStick, Refresh
} from '@element-plus/icons-vue'

const router = useRouter()
const collapsed = ref(false)

const tools = [
  {
    id: 'text',
    name: '文本创作',
    icon: EditPen,
    action: () => router.push({ path: '/workbench', query: { type: 'TEXT' } })
  },
  {
    id: 'image',
    name: '图片生成',
    icon: Picture,
    action: () => router.push({ path: '/workbench', query: { type: 'IMAGE' } })
  },
  {
    id: 'history',
    name: '创作库',
    icon: Document,
    action: () => router.push('/history')
  },
  {
    id: 'calendar',
    name: '创作日历',
    icon: Calendar,
    action: () => router.push('/calendar')
  },
  {
    id: 'inspiration',
    name: '随机灵感',
    icon: MagicStick,
    action: () => {
      router.push('/home')
      setTimeout(() => {
        ElMessage.success('点击"随机灵感生成器"获取创作灵感')
      }, 500)
    }
  },
  {
    id: 'refresh',
    name: '刷新页面',
    icon: Refresh,
    action: () => window.location.reload()
  }
]

const handleToolClick = (tool) => {
  if (tool.action) {
    tool.action()
  }
}
</script>

<style scoped>
.quick-toolbar {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1000;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.quick-toolbar.collapsed {
  width: 50px;
}

.toolbar-toggle {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  transition: all 0.3s;
}

.toolbar-toggle:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: scale(1.05);
}

.toolbar-content {
  padding: 16px;
  min-width: 150px;
}

.toolbar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  text-align: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
}

.tool-item:hover {
  background: #f5f7fa;
  color: #667eea;
  transform: translateX(5px);
}

.tool-name {
  font-size: 14px;
  white-space: nowrap;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .quick-toolbar {
    right: 10px;
    top: auto;
    bottom: 80px;
    transform: none;
  }
  
  .toolbar-content {
    min-width: 120px;
  }
  
  .tool-name {
    font-size: 12px;
  }
}
</style>
