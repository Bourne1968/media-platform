<template>
  <el-drawer
    v-model="visible"
    title="成就系统"
    size="400px"
    direction="rtl"
  >
    <div class="achievement-container">
      <div class="achievement-stats">
        <div class="stat-item">
          <div class="stat-value">{{ totalAchievements }}</div>
          <div class="stat-label">总成就</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ unlockedCount }}</div>
          <div class="stat-label">已解锁</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ totalAchievements > 0 ? Math.round((unlockedCount / totalAchievements) * 100) : 0 }}%</div>
          <div class="stat-label">完成度</div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="achievement-list">
        <div
          v-for="achievement in achievements"
          :key="achievement.id"
          class="achievement-item"
          :class="{ 'unlocked': achievement.unlocked }"
        >
          <div class="achievement-icon">
            <el-icon :size="40">
              <component :is="achievement.unlocked ? achievement.icon : Lock" />
            </el-icon>
          </div>
          <div class="achievement-content">
            <div class="achievement-name">
              {{ achievement.name }}
              <el-icon v-if="achievement.unlocked" class="check-icon" color="#67c23a">
                <Check />
              </el-icon>
            </div>
            <div class="achievement-desc">{{ achievement.description }}</div>
            <div class="achievement-progress" v-if="!achievement.unlocked && achievement.progress !== undefined">
              <el-progress 
                :percentage="achievement.progress" 
                :stroke-width="6"
                :show-text="false"
              />
              <span class="progress-text">{{ achievement.progress }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { 
  Lock, Check, Trophy, Star, 
  Document, Picture, Calendar, MagicStick, View 
} from '@element-plus/icons-vue'
import { getRecordList } from '@/api/creation'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const achievements = ref([
  {
    id: 'first_text',
    name: '初出茅庐',
    description: '完成第一次文本创作',
    icon: Document,
    unlocked: false,
    progress: 0
  },
  {
    id: 'first_image',
    name: '视觉艺术家',
    description: '生成第一张图片',
    icon: Picture,
    unlocked: false,
    progress: 0
  },
  {
    id: 'text_10',
    name: '文案达人',
    description: '完成10次文本创作',
    icon: Star,
    unlocked: false,
    progress: 0
  },
  {
    id: 'image_10',
    name: '图片大师',
    description: '生成10张图片',
    icon: View,
    unlocked: false,
    progress: 0
  },
  {
    id: 'total_50',
    name: '创作新星',
    description: '累计创作50条内容',
    icon: Trophy,
    unlocked: false,
    progress: 0
  },
  {
    id: 'total_100',
    name: '创作之星',
    description: '累计创作100条内容',
    icon: Star,
    unlocked: false,
    progress: 0
  },
  {
    id: 'calendar_7',
    name: '坚持创作',
    description: '连续7天创作',
    icon: Calendar,
    unlocked: false,
    progress: 0
  },
  {
    id: 'inspiration_10',
    name: '灵感收集者',
    description: '使用10次灵感生成器',
    icon: MagicStick,
    unlocked: false,
    progress: 0
  }
])

const totalAchievements = computed(() => achievements.value.length)
const unlockedCount = computed(() => achievements.value.filter(a => a.unlocked).length)

const checkAchievements = async () => {
  try {
    // 检查是否已登录
    const token = sessionStorage.getItem('token')
    if (!token) {
      return
    }
    
    // 获取所有创作记录
    const res = await getRecordList({ current: 1, size: 1000 })
    if (res && res.code === 200 && res.data) {
      const records = res.data.records || []
      
      // 统计数据
      const textCount = records.filter(r => r.type === 'TEXT').length
      const imageCount = records.filter(r => r.type === 'IMAGE').length
      const totalCount = records.length
      
      // 检查成就
      achievements.value.forEach(achievement => {
        switch (achievement.id) {
          case 'first_text':
            achievement.unlocked = textCount > 0
            achievement.progress = textCount > 0 ? 100 : 0
            break
          case 'first_image':
            achievement.unlocked = imageCount > 0
            achievement.progress = imageCount > 0 ? 100 : 0
            break
          case 'text_10':
            achievement.unlocked = textCount >= 10
            achievement.progress = Math.min((textCount / 10) * 100, 100)
            break
          case 'image_10':
            achievement.unlocked = imageCount >= 10
            achievement.progress = Math.min((imageCount / 10) * 100, 100)
            break
          case 'total_50':
            achievement.unlocked = totalCount >= 50
            achievement.progress = Math.min((totalCount / 50) * 100, 100)
            break
          case 'total_100':
            achievement.unlocked = totalCount >= 100
            achievement.progress = Math.min((totalCount / 100) * 100, 100)
            break
          case 'calendar_7':
            // 简化处理：如果有7条以上记录就认为连续创作
            achievement.unlocked = totalCount >= 7
            achievement.progress = Math.min((totalCount / 7) * 100, 100)
            break
          case 'inspiration_10':
            // 这个需要单独统计，暂时简化
            achievement.progress = 0
            break
        }
      })
    }
  } catch (error) {
    // 静默处理错误，不影响页面渲染
    console.error('检查成就失败：', error)
  }
}

onMounted(() => {
  // 延迟检查，确保页面已加载
  setTimeout(() => {
    checkAchievements()
  }, 500)
})

defineExpose({
  checkAchievements
})
</script>

<style scoped>
.achievement-container {
  padding: 20px;
}

.achievement-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.achievement-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #f8f9ff;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.achievement-item.unlocked {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-color: #67c23a;
  box-shadow: 0 2px 12px rgba(103, 194, 58, 0.2);
}

.achievement-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 12px;
  color: #909399;
}

.achievement-item.unlocked .achievement-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.achievement-content {
  flex: 1;
}

.achievement-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.achievement-item:not(.unlocked) .achievement-name {
  color: #909399;
}

.check-icon {
  font-size: 18px;
}

.achievement-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.achievement-item:not(.unlocked) .achievement-desc {
  color: #c0c4cc;
}

.achievement-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  min-width: 40px;
}
</style>
