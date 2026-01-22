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
                    action=""
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :on-success="handleAvatarSuccess"
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

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
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
                <div class="preference-title">语言</div>
                <div class="preference-desc">选择界面语言</div>
              </div>
              <el-select v-model="preferences.language" style="width: 120px">
                <el-option label="中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </div>

            <div class="preference-item">
              <div class="preference-info">
                <div class="preference-title">自动保存</div>
                <div class="preference-desc">创作时自动保存草稿</div>
              </div>
              <el-switch v-model="preferences.autoSave" />
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
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
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
  language: 'zh-CN',
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

    // 这里应该调用更新用户信息的API
    // const res = await updateUserProfile(profileForm)

    // 模拟保存成功
    setTimeout(() => {
      ElMessage.success('个人信息保存成功')
      savingProfile.value = false
    }, 1000)

  } catch (error) {
    console.error('保存失败:', error)
    savingProfile.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png'].includes(file.type)
  const isValidSize = file.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return false
  }

  if (!isValidSize) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }

  return true
}

const handleAvatarSuccess = (response, file) => {
  // 这里应该处理上传成功的逻辑
  profileForm.avatar = URL.createObjectURL(file.raw)
  ElMessage.success('头像上传成功')
}

const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    changingPassword.value = true

    // 这里应该调用修改密码的API
    // const res = await changeUserPassword(passwordForm)

    // 模拟修改成功
    setTimeout(() => {
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      changingPassword.value = false
    }, 1000)

  } catch (error) {
    console.error('密码修改失败:', error)
    changingPassword.value = false
  }
}

const toggleDarkMode = (value) => {
  // 这里可以实现深色模式切换
  if (value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

const savePreferences = async () => {
  savingPreferences.value = true

  // 这里应该调用保存偏好设置的API
  // const res = await saveUserPreferences(preferences)

  // 模拟保存成功
  setTimeout(() => {
    ElMessage.success('偏好设置保存成功')
    savingPreferences.value = false
  }, 500)
}

const exportCreations = async () => {
  exportingCreations.value = true

  // 这里应该调用导出创作数据的API
  // const res = await exportUserCreations()

  // 模拟导出
  setTimeout(() => {
    ElMessage.success('创作数据导出成功，请检查下载')
    exportingCreations.value = false
  }, 1500)
}

const exportStatistics = async () => {
  exportingStatistics.value = true

  // 这里应该调用导出统计数据的API
  // const res = await exportUserStatistics()

  // 模拟导出
  setTimeout(() => {
    ElMessage.success('统计数据导出成功，请检查下载')
    exportingStatistics.value = false
  }, 1000)
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

    // 这里应该调用删除账户的API
    // const res = await deleteUserAccount()

    // 模拟删除成功
    setTimeout(() => {
      ElMessage.success('账户删除成功')
      // 这里应该跳转到登录页并清除本地存储
      deletingAccount.value = false
    }, 2000)

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除账户失败:', error)
      ElMessage.error('删除账户失败')
    }
    deletingAccount.value = false
  }
}

// 初始化数据
onMounted(() => {
  // 从localStorage或其他地方加载用户信息
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      profileForm.username = user.username || ''
      profileForm.email = user.email || ''
      profileForm.nickname = user.nickname || ''
      profileForm.bio = user.bio || ''
      profileForm.gender = user.gender || 'private'
      profileForm.avatar = user.avatar || ''
    } catch (e) {
      console.error('加载用户信息失败:', e)
    }
  }

  // 加载偏好设置
  const userPreferences = localStorage.getItem('userPreferences')
  if (userPreferences) {
    try {
      const prefs = JSON.parse(userPreferences)
      Object.assign(preferences, prefs)
    } catch (e) {
      console.error('加载偏好设置失败:', e)
    }
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
  color: #909399;
  margin: 0;
}

.settings-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
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
  color: #909399;
  font-size: 14px;
  margin: 0;
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
  color: #909399;
  font-size: 12px;
}

.upload-tips p {
  margin: 4px 0;
}

/* 偏好设置 */
.preference-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.preference-item:last-child {
  border-bottom: none;
}

.preference-info {
  flex: 1;
}

.preference-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.preference-desc {
  color: #909399;
  font-size: 14px;
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