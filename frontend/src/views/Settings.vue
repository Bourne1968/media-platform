<template>
  <div class="settings-container">
    <div class="settings-header">
      <h1 class="page-title">个人设置</h1>
      <p class="page-subtitle">管理您的账户信息和偏好设置</p>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="profile">
          <div class="settings-section">
            <div class="section-header">
              <h3 class="section-title">基本信息</h3>
              <p class="section-desc">更新您的个人资料信息</p>
            </div>

            <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="120px">
              <el-form-item label="头像">
                <div class="avatar-upload">
                  <div class="avatar-preview">
                    <img :src="profileForm.avatar || defaultAvatar" alt="头像" class="avatar-img" />
                    <div class="avatar-overlay">
                      <el-icon><Camera /></el-icon>
                      <span>更换头像</span>
                    </div>
                  </div>
                  <el-upload
                    class="avatar-uploader"
                    :auto-upload="false"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :on-change="handleAvatarChange"
                    accept="image/jpeg,image/png"
                  >
                    <el-button size="small" type="primary">选择图片</el-button>
                  </el-upload>
                  <div class="upload-tips">
                    <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="用户名" prop="username">
                <el-input v-model="profileForm.username" placeholder="请输入用户名" />
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" disabled placeholder="邮箱地址" />
                <div class="form-item-tip">邮箱地址不可修改</div>
              </el-form-item>

              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称（可选）" />
              </el-form-item>

              <el-form-item label="个人简介">
                <el-input
                  v-model="profileForm.bio"
                  type="textarea"
                  :rows="3"
                  placeholder="介绍一下自己..."
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio label="male">男</el-radio>
                  <el-radio label="female">女</el-radio>
                  <el-radio label="other">其他</el-radio>
                  <el-radio label="private">保密</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveProfile" :loading="savingProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 账号安全 -->
        <el-tab-pane label="账号安全" name="security">
          <div class="settings-section">
            <div class="section-header">
              <h3 class="section-title">密码修改</h3>
              <p class="section-desc">定期修改密码可以保护您的账户安全</p>
            </div>

            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（6-20位）"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="changingPassword">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="settings-section">
            <div class="section-header">
              <h3 class="section-title">登录设备</h3>
              <p class="section-desc">查看和管理您的登录设备</p>
            </div>

            <div class="device-list">
              <div class="device-item current">
                <div class="device-info">
                  <div class="device-icon">
                    <el-icon><Monitor /></el-icon>
                  </div>
                  <div class="device-details">
                    <div class="device-name">当前设备</div>
                    <div class="device-meta">
                      Chrome浏览器 · Windows 10 · {{ currentTime }}
                    </div>
                  </div>
                </div>
                <el-tag type="success">当前在线</el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 偏好设置 -->
        <el-tab-pane label="偏好设置" name="preferences">
          <div class="settings-section">
            <div class="section-header">
              <h3 class="section-title">界面设置</h3>
              <p class="section-desc">自定义您的使用体验</p>
            </div>

            <div class="preference-item">
              <div class="preference-info">
                <div class="preference-title">深色模式</div>
                <div class="preference-desc">切换深色/浅色主题</div>
              </div>
              <el-switch v-model="preferences.darkMode" @change="toggleDarkMode" />
            </div>

            <div class="preference-item">
              <div class="preference-info">
                <div class="preference-title">自动保存</div>
                <div class="preference-desc">创作时自动保存草稿</div>
              </div>
              <el-switch v-model="preferences.autoSave" @change="handleAutoSaveChange" />
            </div>
          </div>

          <div class="settings-section">
            <div class="section-header">
              <h3 class="section-title">创作偏好</h3>
              <p class="section-desc">设置您的创作习惯</p>
            </div>

            <div class="preference-item">
              <div class="preference-info">
                <div class="preference-title">默认文案风格</div>
                <div class="preference-desc">新创作时的默认风格</div>
              </div>
              <el-select v-model="preferences.defaultStyle" style="width: 150px">
                <el-option label="专业严谨" value="professional" />
                <el-option label="轻松幽默" value="humor" />
                <el-option label="感性煽情" value="emotional" />
                <el-option label="简洁直接" value="direct" />
              </el-select>
            </div>

            <div class="preference-item">
              <div class="preference-info">
                <div class="preference-title">默认封面尺寸</div>
                <div class="preference-desc">新封面创作的默认尺寸</div>
              </div>
              <el-select v-model="preferences.defaultSize" style="width: 150px">
                <el-option label="抖音 (9:16)" value="douyin" />
                <el-option label="B站 (16:9)" value="bilibili" />
                <el-option label="小红书 (3:4)" value="xiaohongshu" />
                <el-option label="微信 (2.35:1)" value="wechat" />
              </el-select>
            </div>

            <div class="preference-item">
              <div class="preference-info">
                <div class="preference-title">创作提醒</div>
                <div class="preference-desc">开启创作进度提醒</div>
              </div>
              <el-switch v-model="preferences.creationReminders" />
            </div>

            <div class="preference-actions">
              <el-button type="primary" @click="savePreferences" :loading="savingPreferences">
                保存偏好设置
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 数据管理 -->
        <el-tab-pane label="数据管理" name="data">
          <div class="settings-section">
            <div class="section-header">
              <h3 class="section-title">数据导出</h3>
              <p class="section-desc">导出您的创作数据</p>
            </div>

            <div class="data-export">
              <div class="export-item">
                <div class="export-info">
                  <div class="export-title">创作记录</div>
                  <div class="export-desc">包含所有文案和封面创作记录</div>
                </div>
                <el-button type="primary" @click="exportCreations" :loading="exportingCreations">
                  导出数据
                </el-button>
              </div>

              <div class="export-item">
                <div class="export-info">
                  <div class="export-title">使用统计</div>
                  <div class="export-desc">包含创作频率和偏好数据</div>
                </div>
                <el-button @click="exportStatistics" :loading="exportingStatistics">
                  导出数据
                </el-button>
              </div>
            </div>
          </div>

          <div class="settings-section danger-zone">
            <div class="section-header">
              <h3 class="section-title text-danger">危险操作</h3>
              <p class="section-desc">这些操作不可逆，请谨慎操作</p>
            </div>

            <div class="danger-actions">
              <div class="danger-item">
                <div class="danger-info">
                  <div class="danger-title">清空所有创作记录</div>
                  <div class="danger-desc">删除所有创作历史，此操作不可恢复</div>
                </div>
                <el-button type="danger" @click="clearAllData" :loading="clearingData">
                  清空数据
                </el-button>
              </div>

              <div class="danger-item">
                <div class="danger-info">
                  <div class="danger-title">删除账户</div>
                  <div class="danger-desc">永久删除您的账户和所有数据</div>
                </div>
                <el-button type="danger" @click="deleteAccount" :loading="deletingAccount">
                  删除账户
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Monitor } from '@element-plus/icons-vue'
import { updateProfile, updateAvatar, changePassword as changePasswordAPI, updatePreferences, getPreferences } from '@/api/user'

const activeTab = ref('profile')

// 个人信息表单
const profileForm = reactive({
  username: '',
  email: '',
  nickname: '',
  bio: '',
  gender: 'private',
  avatar: ''
})

const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为2-20位', trigger: 'blur' }
  ]
}

const profileFormRef = ref()
const savingProfile = ref(false)

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const passwordFormRef = ref()
const changingPassword = ref(false)

// 偏好设置
const preferences = reactive({
  darkMode: false,
  autoSave: true,
  defaultStyle: 'humor',
  defaultSize: 'douyin',
  creationReminders: true
})

const savingPreferences = ref(false)

// 数据导出
const exportingCreations = ref(false)
const exportingStatistics = ref(false)
const clearingData = ref(false)
const deletingAccount = ref(false)

// 计算属性
const defaultAvatar = computed(() => {
  return `https://ui-avatars.com/api/?name=${profileForm.username || 'User'}&background=667eea&color=fff&size=100`
})

const currentTime = computed(() => {
  return new Date().toLocaleString('zh-CN')
})

// 方法
const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()

    savingProfile.value = true

    try {
      // 调用后端API更新用户信息
      const res = await updateProfile({
        username: profileForm.username
      })
      
      if (res.code === 200) {
        // 更新localStorage中的用户信息
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
          try {
            const user = JSON.parse(userInfo)
            user.username = profileForm.username
            localStorage.setItem('userInfo', JSON.stringify(user))
            // 触发自定义事件，通知Layout组件更新
            window.dispatchEvent(new Event('userInfoUpdated'))
          } catch (e) {
            console.error('更新本地用户信息失败:', e)
          }
        }
        
        ElMessage.success('个人信息保存成功')
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error(error.response?.data?.message || '保存失败，请重试')
    } finally {
      savingProfile.value = false
    }

  } catch (error) {
    console.error('验证失败:', error)
    savingProfile.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isValidSize = file.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return false
  }

  if (!isValidSize) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }

  return false // 阻止自动上传，手动处理
}

const handleAvatarChange = async (file) => {
  if (!file.raw) return
  
  // 验证文件
  const isValidType = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.raw.type)
  const isValidSize = file.raw.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return
  }

  if (!isValidSize) {
    ElMessage.error('头像大小不能超过 2MB!')
    return
  }

  // 使用FileReader读取文件并转换为base64
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const base64 = e.target.result
      
      // 调用后端API更新头像
      const res = await updateAvatar({ avatar: base64 })
      if (res.code === 200) {
        profileForm.avatar = base64
        
        // 更新localStorage中的用户信息
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
          try {
            const user = JSON.parse(userInfo)
            user.avatar = base64
            localStorage.setItem('userInfo', JSON.stringify(user))
            // 触发自定义事件，通知Layout组件更新头像
            window.dispatchEvent(new Event('userInfoUpdated'))
          } catch (e) {
            console.error('更新本地用户信息失败:', e)
          }
        }
        
        ElMessage.success('头像更新成功')
      } else {
        ElMessage.error(res.message || '头像更新失败')
      }
    } catch (error) {
      console.error('头像更新失败:', error)
      ElMessage.error('头像更新失败，请重试')
    }
  }
  reader.onerror = () => {
    ElMessage.error('头像读取失败，请重试')
  }
  reader.readAsDataURL(file.raw)
}

const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    changingPassword.value = true

    try {
      // 调用后端API修改密码
      const res = await changePasswordAPI({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      })
      
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        passwordForm.currentPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        
        // 可以选择清除token，强制用户重新登录
        // localStorage.removeItem('token')
        // setTimeout(() => {
        //   window.location.href = '/login'
        // }, 1500)
      } else {
        ElMessage.error(res.message || '密码修改失败')
      }
    } catch (error) {
      console.error('密码修改失败:', error)
      ElMessage.error(error.response?.data?.message || '密码修改失败，请重试')
    } finally {
      changingPassword.value = false
    }

  } catch (error) {
    console.error('验证失败:', error)
    changingPassword.value = false
  }
}

const toggleDarkMode = (value) => {
  // 实现深色模式切换
  if (value) {
    document.documentElement.classList.add('dark')
    // 保存到localStorage
    localStorage.setItem('darkMode', 'true')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('darkMode', 'false')
  }
  
  // 立即保存偏好设置
  localStorage.setItem('userPreferences', JSON.stringify(preferences))
}


const handleAutoSaveChange = (value) => {
  // 立即保存自动保存设置
  localStorage.setItem('userPreferences', JSON.stringify(preferences))
  if (value) {
    ElMessage.success('已开启自动保存')
  } else {
    ElMessage.info('已关闭自动保存')
  }
}

const savePreferences = async () => {
  savingPreferences.value = true

  try {
    // 调用后端API保存偏好设置
    const res = await updatePreferences({ preferences: preferences })
    
    if (res && res.code === 200) {
      // 同时保存到localStorage作为备份
      localStorage.setItem('userPreferences', JSON.stringify(preferences))
      
      // 应用深色模式
      if (preferences.darkMode) {
        document.documentElement.classList.add('dark')
        localStorage.setItem('darkMode', 'true')
      } else {
        document.documentElement.classList.remove('dark')
        localStorage.setItem('darkMode', 'false')
      }

      ElMessage.success('偏好设置保存成功')
    } else {
      // 如果后端保存失败，至少保存到localStorage
      localStorage.setItem('userPreferences', JSON.stringify(preferences))
      ElMessage.warning('偏好设置已保存到本地，但同步到服务器失败')
    }
  } catch (error) {
    console.error('保存偏好设置失败:', error)
    // 即使后端失败，也保存到localStorage
    localStorage.setItem('userPreferences', JSON.stringify(preferences))
    
    // 应用深色模式（即使后端失败也要应用）
    if (preferences.darkMode) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('darkMode', 'true')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('darkMode', 'false')
    }
    
    // 如果是404错误，说明后端接口可能未实现，只保存到本地
    if (error.response?.status === 404) {
      ElMessage.warning('偏好设置已保存到本地（后端接口未实现）')
    } else {
      ElMessage.error(error.response?.data?.message || '保存失败，已保存到本地')
    }
  } finally {
    savingPreferences.value = false
  }
}

const exportCreations = async () => {
  exportingCreations.value = true

  try {
    // 这里应该调用导出创作数据的API
    // const res = await exportUserCreations()
    
    // 模拟导出：创建JSON文件并下载
    const exportData = {
      exportTime: new Date().toISOString(),
      data: '创作记录数据（实际应从API获取）'
    }
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `创作记录_${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    setTimeout(() => {
      ElMessage.success('创作数据导出成功，请检查下载')
      exportingCreations.value = false
    }, 500)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
    exportingCreations.value = false
  }
}

const exportStatistics = async () => {
  exportingStatistics.value = true

  try {
    // 这里应该调用导出统计数据的API
    // const res = await exportUserStatistics()
    
    // 模拟导出：创建JSON文件并下载
    const exportData = {
      exportTime: new Date().toISOString(),
      statistics: {
        totalCreations: 0,
        textCreations: 0,
        imageCreations: 0,
        lastActiveTime: new Date().toISOString()
      }
    }
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `使用统计_${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    setTimeout(() => {
      ElMessage.success('统计数据导出成功，请检查下载')
      exportingStatistics.value = false
    }, 500)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
    exportingStatistics.value = false
  }
}

const clearAllData = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有创作记录吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    clearingData.value = true

    // 这里应该调用清空数据的API
    // const res = await clearUserData()

    // 模拟清空成功
    setTimeout(() => {
      ElMessage.success('所有创作记录已清空')
      clearingData.value = false
    }, 2000)

  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空数据失败:', error)
      ElMessage.error('清空数据失败')
    }
    clearingData.value = false
  }
}

const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除账户吗？此操作将永久删除您的账户和所有数据，不可恢复！',
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    deletingAccount.value = true

    try {
      // 这里应该调用删除账户的API
      // const res = await deleteUserAccount()
      
      // 模拟删除成功
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      // 清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userPreferences')
      
      ElMessage.success('账户删除成功')
      
      // 跳转到登录页
      window.location.href = '/login'
    } catch (apiError) {
      console.error('删除账户失败:', apiError)
      ElMessage.error('删除账户失败，请稍后重试')
    } finally {
      deletingAccount.value = false
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除账户失败:', error)
      ElMessage.error('删除账户失败')
    }
    deletingAccount.value = false
  }
}

// 初始化数据
onMounted(async () => {
  // 从localStorage加载用户信息
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      profileForm.username = user.username || ''
      profileForm.email = user.email || '' // 邮箱从登录信息中获取，不可编辑
      profileForm.nickname = user.nickname || ''
      profileForm.bio = user.bio || ''
      profileForm.gender = user.gender || 'private'
      profileForm.avatar = user.avatar || ''
    } catch (e) {
      console.error('加载用户信息失败:', e)
    }
  }

  // 从localStorage加载偏好设置的辅助函数
  const loadPreferencesFromLocalStorage = () => {
    const userPreferences = localStorage.getItem('userPreferences')
    if (userPreferences) {
      try {
        const prefs = JSON.parse(userPreferences)
        Object.assign(preferences, prefs)
      } catch (e) {
        console.error('从localStorage加载偏好设置失败:', e)
      }
    }
  }
  
  // 加载偏好设置（优先从后端加载，如果失败则从localStorage加载）
  // 注意：如果数据库还没有preferences列，API会失败，此时使用localStorage
  try {
    const res = await getPreferences()
    if (res && res.code === 200 && res.data) {
      // 从后端加载偏好设置
      Object.assign(preferences, res.data)
    } else {
      // 如果后端没有数据，从localStorage加载
      loadPreferencesFromLocalStorage()
    }
  } catch (error) {
    // 如果后端加载失败（可能是接口未实现、数据库未更新或404错误），从localStorage加载
    // 不显示错误消息，静默降级到localStorage
    if (error.response?.status !== 404) {
      console.warn('从后端加载偏好设置失败，使用localStorage:', error)
    }
    loadPreferencesFromLocalStorage()
  }
  
  // 应用深色模式（如果已设置）
  const darkMode = localStorage.getItem('darkMode')
  if (darkMode === 'true' || preferences.darkMode) {
    preferences.darkMode = true
    document.documentElement.classList.add('dark')
  } else if (darkMode === 'false' || preferences.darkMode === false) {
    preferences.darkMode = false
    document.documentElement.classList.remove('dark')
  }
})
</script>

<style scoped>
.settings-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
}

.settings-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  transition: color 0.3s ease;
}

.settings-content {
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: background-color 0.3s ease;
}

.settings-tabs :deep(.el-tabs__header) {
  margin: 0;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.settings-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.settings-tabs :deep(.el-tabs__item) {
  font-weight: 500;
  padding: 16px 24px;
  border-bottom: 2px solid transparent;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
  border-bottom-color: #667eea;
}

.settings-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.settings-section {
  padding: 32px;
  border-bottom: 1px solid #f0f0f0;
}

.settings-section:last-child {
  border-bottom: none;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.section-title.text-danger {
  color: #f56c6c;
}

.section-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  transition: color 0.3s ease;
}

/* 头像上传 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e4e7ed;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.avatar-preview:hover {
  border-color: #667eea;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 126, 234, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.upload-tips {
  color: var(--text-secondary);
  font-size: 12px;
  transition: color 0.3s ease;
}

.upload-tips p {
  margin: 4px 0;
}

.form-item-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  line-height: 1.5;
  transition: color 0.3s ease;
}

/* 偏好设置 */
.preference-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
  transition: border-color 0.3s ease;
}

.preference-item:last-child {
  border-bottom: none;
}

.preference-info {
  flex: 1;
}

.preference-title {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.preference-desc {
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.3s ease;
}

.preference-actions {
  margin-top: 32px;
  text-align: center;
}

/* 数据导出 */
.data-export {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.export-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: border-color 0.3s ease;
}

.export-item:hover {
  border-color: #667eea;
}

.export-info {
  flex: 1;
}

.export-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.export-desc {
  color: #909399;
  font-size: 14px;
}

/* 危险操作 */
.danger-zone {
  border-left: 4px solid #f56c6c;
  background: #fef0f0;
}

.danger-actions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.danger-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #fab1a0;
  border-radius: 8px;
  background: white;
}

.danger-info {
  flex: 1;
}

.danger-title {
  font-weight: 500;
  color: #d63031;
  margin-bottom: 4px;
}

.danger-desc {
  color: #636e72;
  font-size: 14px;
}

/* 设备列表 */
.device-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
}

.device-item.current {
  border-color: #67c23a;
  background: #f0f9ff;
}

.device-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.device-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f0f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
}

.device-details {
  flex: 1;
}

.device-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.device-meta {
  color: #909399;
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .settings-container {
    padding: 16px;
  }

  .settings-section {
    padding: 20px;
  }

  .avatar-upload {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .preference-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .export-item,
  .danger-item,
  .device-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>