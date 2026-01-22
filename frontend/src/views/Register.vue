<template>
  <div class="register-container">
    <div class="register-background">
      <div class="background-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
    <div class="register-content">
      <el-card class="register-card" shadow="always">
        <template #header>
          <div class="card-header">
            <div class="logo-icon">
              <el-icon :size="40"><MagicStick /></el-icon>
            </div>
            <h2>加入AI创作社区</h2>
            <p class="subtitle">Join Our AI Creative Community</p>
          </div>
        </template>
        <el-form
          :model="registerForm"
          :rules="rules"
          ref="registerFormRef"
          label-width="0"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（3-20位字符）"
              size="large"
              class="register-input"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱地址"
              size="large"
              class="register-input"
              :prefix-icon="Message"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6-20位字符）"
              size="large"
              class="register-input"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              class="register-input"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="agreeToTerms">
            <el-checkbox v-model="registerForm.agreeToTerms" class="terms-checkbox">
              我已阅读并同意
              <el-link type="primary" @click="showTerms">《用户服务协议》</el-link>
              和
              <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              @click="handleRegister"
              :loading="registering"
              class="register-btn"
              block
            >
              <span v-if="!registering">立即注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
        </div>
      </el-card>

      <!-- 服务协议弹窗 -->
      <el-dialog
        v-model="termsDialogVisible"
        title="用户服务协议"
        width="600px"
        center
      >
        <div class="terms-content">
          <h3>1. 服务说明</h3>
          <p>AI创作辅助平台为用户提供基于人工智能的内容创作服务，帮助用户提升创作效率。</p>

          <h3>2. 用户义务</h3>
          <p>用户承诺遵守法律法规，不利用平台从事违法违规活动。</p>

          <h3>3. 知识产权</h3>
          <p>用户享有其创作内容的知识产权，平台仅提供技术服务。</p>

          <h3>4. 服务条款</h3>
          <p>平台有权根据需要调整服务内容，用户需定期查看最新条款。</p>
        </div>
        <template #footer>
          <el-button @click="termsDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 隐私政策弹窗 -->
      <el-dialog
        v-model="privacyDialogVisible"
        title="隐私政策"
        width="600px"
        center
      >
        <div class="terms-content">
          <h3>1. 信息收集</h3>
          <p>我们收集用户的注册信息、使用数据，为您提供更好的服务。</p>

          <h3>2. 信息使用</h3>
          <p>用户信息仅用于提供服务，不会用于其他商业用途。</p>

          <h3>3. 信息保护</h3>
          <p>我们采用先进的安全措施保护用户数据安全。</p>

          <h3>4. 信息共享</h3>
          <p>我们不会向第三方出售或出租用户信息。</p>
        </div>
        <template #footer>
          <el-button @click="privacyDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, MagicStick } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()

const registering = ref(false)
const termsDialogVisible = ref(false)
const privacyDialogVisible = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeToTerms: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agreeToTerms: [
    { required: true, message: '请同意用户协议', trigger: 'change', type: 'boolean' }
  ]
}

const registerFormRef = ref()

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()

    registering.value = true

    // 调用注册API
    console.log('开始注册，用户名:', registerForm.username, '邮箱:', registerForm.email)
    
    const res = await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })

    console.log('注册响应:', res)

    if (res.code === 200) {
      // 注册成功，保存token和用户信息
      if (res.data && res.data.token) {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify({
          userId: res.data.userId,
          username: res.data.username,
          role: res.data.role,
          avatar: res.data.avatar
        }))
        console.log('用户信息已保存到localStorage')
      }
      
      ElMessage.success('注册成功！')
      // 跳转到首页
      router.push('/home')
    } else {
      ElMessage.error(res.message || '注册失败，请重试')
    }

    registering.value = false

  } catch (error) {
    console.error('注册失败，详细错误:', error)
    console.error('错误响应:', error.response)
    
    // 更详细的错误信息
    let errorMessage = '注册失败，请重试'
    if (error.response) {
      // 服务器返回了响应
      if (error.response.data) {
        errorMessage = error.response.data.message || error.response.data.msg || errorMessage
      } else {
        errorMessage = `服务器错误: ${error.response.status} ${error.response.statusText}`
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorMessage = '无法连接到服务器，请检查后端服务是否运行'
    } else {
      // 请求配置错误
      errorMessage = error.message || errorMessage
    }
    
    ElMessage.error(errorMessage)
    registering.value = false
  }
}

const showTerms = () => {
  termsDialogVisible.value = true
}

const showPrivacy = () => {
  privacyDialogVisible.value = true
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.register-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.background-shapes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 70%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.register-content {
  width: 100%;
  max-width: 420px;
  z-index: 1;
}

.register-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.card-header {
  text-align: center;
  padding: 20px 0;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
  font-weight: 400;
}

.register-form {
  padding: 0 8px;
}

.register-input {
  margin-bottom: 16px;
}

.register-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.register-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.register-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.terms-checkbox {
  margin-bottom: 8px;
}

.terms-checkbox :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.register-btn {
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.register-footer {
  text-align: center;
  padding: 20px 0 0 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 20px;
  font-size: 14px;
  color: #909399;
}

.register-footer .el-link {
  font-weight: 500;
}

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.6;
  color: #606266;
}

.terms-content h3 {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  margin: 20px 0 10px 0;
}

.terms-content h3:first-child {
  margin-top: 0;
}

.terms-content p {
  margin: 0 0 15px 0;
  text-align: justify;
}

@media (max-width: 480px) {
  .register-container {
    padding: 10px;
  }

  .card-header h2 {
    font-size: 20px;
  }

  .logo-icon {
    width: 60px;
    height: 60px;
  }

  .logo-icon .el-icon {
    font-size: 30px;
  }
}
</style>