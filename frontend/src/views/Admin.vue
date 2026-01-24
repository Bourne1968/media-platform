<template>
  <div class="admin-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 数据统计 -->
      <el-tab-pane label="数据统计" name="statistics">
        <div v-loading="statisticsLoading" class="statistics-container">
          <el-row :gutter="20" class="stat-cards">
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="stat-card stat-card-user" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><UserFilled /></el-icon>
                  </div>
                  <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
                  <div class="stat-label">总用户数</div>
                  <div class="stat-desc">
                    <el-icon><ArrowUp /></el-icon>
                    <span>今日新增：{{ statistics.todayNewUsers || 0 }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="stat-card stat-card-record" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="stat-value">{{ statistics.totalRecords || 0 }}</div>
                  <div class="stat-label">总创作记录</div>
                  <div class="stat-desc">
                    <el-icon><ArrowUp /></el-icon>
                    <span>今日新增：{{ statistics.todayNewRecords || 0 }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="stat-card stat-card-text" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><EditPen /></el-icon>
                  </div>
                  <div class="stat-value">{{ statistics.textRecords || 0 }}</div>
                  <div class="stat-label">文本创作</div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="stat-card stat-card-image" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><Picture /></el-icon>
                  </div>
                  <div class="stat-value">{{ statistics.imageRecords || 0 }}</div>
                  <div class="stat-label">图像创作</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 类型统计图表 -->
          <el-card class="chart-card" shadow="hover" style="margin-top: 20px">
            <template #header>
              <div class="card-header">
                <el-icon class="header-icon"><PieChart /></el-icon>
                <span>类型分布</span>
              </div>
            </template>
            <div class="type-chart">
              <div class="type-item">
                <div class="type-label">
                  <el-icon><EditPen /></el-icon>
                  <span>文本创作</span>
                </div>
                <div class="type-progress">
                  <el-progress
                    :percentage="getTextPercentage()"
                    :format="() => `${statistics.textRecords || 0} 条`"
                    :color="'#409eff'"
                    :stroke-width="24"
                  />
                </div>
                <div class="type-value">{{ statistics.textRecords || 0 }}</div>
              </div>
              <div class="type-item" style="margin-top: 16px">
                <div class="type-label">
                  <el-icon><Picture /></el-icon>
                  <span>图像创作</span>
                </div>
                <div class="type-progress">
                  <el-progress
                    :percentage="getImagePercentage()"
                    :format="() => `${statistics.imageRecords || 0} 条`"
                    :color="'#67c23a'"
                    :stroke-width="24"
                  />
                </div>
                <div class="type-value">{{ statistics.imageRecords || 0 }}</div>
              </div>
            </div>
          </el-card>

          <!-- 最近7天统计 -->
          <el-card class="chart-card" shadow="hover" style="margin-top: 20px" v-if="statistics.dailyStatistics">
            <template #header>
              <div class="card-header">
                <el-icon class="header-icon"><TrendCharts /></el-icon>
                <span>最近7天创作趋势</span>
              </div>
            </template>
            <div class="daily-chart">
              <el-table :data="formatDailyStatistics()" border>
                <el-table-column prop="date" label="日期" width="150" />
                <el-table-column prop="count" label="创作数量">
                  <template #default="scope">
                    <div style="display: flex; align-items: center; gap: 8px;">
                      <el-progress 
                        :percentage="getDailyPercentage(scope.row.count)" 
                        :show-text="false"
                        :stroke-width="8"
                        style="flex: 1;"
                      />
                      <span style="min-width: 40px; text-align: right;">{{ scope.row.count }}</span>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div v-loading="usersLoading" class="users-container">
          <!-- 搜索和筛选 -->
          <el-form :inline="true" :model="userFilter" class="filter-form" style="margin-bottom: 20px">
            <el-form-item label="用户名">
              <el-input 
                v-model="userFilter.username" 
                placeholder="搜索用户名" 
                clearable 
                @clear="loadUsers"
                @keyup.enter="loadUsers"
                style="width: 200px"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="userFilter.role" placeholder="全部角色" clearable @change="loadUsers" style="width: 150px">
                <el-option label="管理员" value="ADMIN" />
                <el-option label="普通用户" value="USER" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadUsers" :icon="Search">查询</el-button>
              <el-button @click="resetUserFilter">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="users" border style="width: 100%" @selection-change="handleUserSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" sortable />
            <el-table-column prop="username" label="用户名" sortable />
            <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'">
                  {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180" sortable>
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewUserDetail(scope.row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDeleteUser(scope.row.id)" :disabled="scope.row.role === 'ADMIN'">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 批量操作 -->
          <div v-if="selectedUsers.length > 0" class="batch-actions" style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 8px;">
            <span style="margin-right: 16px;">已选择 {{ selectedUsers.length }} 项</span>
            <el-button type="danger" size="small" @click="handleBatchDeleteUsers">批量删除</el-button>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="usersPage"
              v-model:page-size="usersPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="usersTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadUsers"
              @current-change="loadUsers"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 内容管理 -->
      <el-tab-pane label="内容管理" name="content">
        <div v-loading="contentLoading" class="content-container">
          <el-form :inline="true" :model="contentFilter" class="filter-form" style="margin-bottom: 20px">
            <el-form-item label="类型">
              <el-select v-model="contentFilter.type" placeholder="全部类型" clearable @change="loadContent" style="width: 200px">
                <el-option label="文本创作" value="TEXT" />
                <el-option label="图片生成" value="IMAGE" />
              </el-select>
            </el-form-item>
            <el-form-item label="用户ID">
              <el-input v-model="contentFilter.userId" placeholder="用户ID" clearable @clear="loadContent" style="width: 200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadContent" :icon="Search">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="contentList" border style="width: 100%" @selection-change="handleContentSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" sortable />
            <el-table-column prop="userId" label="用户ID" width="100" sortable />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.type === 'TEXT' ? 'primary' : 'success'">
                  {{ scope.row.type === 'TEXT' ? '文本' : '图片' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="prompt" label="提示词" show-overflow-tooltip min-width="200" />
            <el-table-column prop="createTime" label="创建时间" width="180" sortable>
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewContentDetail(scope.row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDeleteContent(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 批量操作 -->
          <div v-if="selectedContent.length > 0" class="batch-actions" style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 8px;">
            <span style="margin-right: 16px;">已选择 {{ selectedContent.length }} 项</span>
            <el-button type="danger" size="small" @click="handleBatchDeleteContent">批量删除</el-button>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="contentPage"
              v-model:page-size="contentPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="contentTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadContent"
              @current-change="loadContent"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 风格模板管理 -->
      <el-tab-pane label="风格模板" name="templates">
        <div v-loading="templatesLoading" class="templates-container">
          <div class="templates-header" style="margin-bottom: 20px">
            <div>
              <h3 style="margin: 0; color: #303133; font-size: 18px; font-weight: 600">风格模板管理</h3>
              <p style="margin: 4px 0 0 0; color: #909399; font-size: 14px">管理平台可用的风格模板</p>
            </div>
            <el-button type="primary" @click="handleAddTemplate" :icon="Plus" size="large">
              添加模板
            </el-button>
          </div>

          <el-table :data="templatesList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="模板名称" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="prompt" label="提示词" show-overflow-tooltip />
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.enabled ? 'success' : 'info'">
                  {{ scope.row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="100" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEditTemplate(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDeleteTemplate(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 模板编辑对话框 -->
          <el-dialog
            v-model="templateDialogVisible"
            :title="templateDialogTitle"
            width="600px"
            @close="resetTemplateForm"
          >
            <el-form :model="templateForm" label-width="100px">
              <el-form-item label="模板名称" required>
                <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="templateForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
              </el-form-item>
              <el-form-item label="提示词">
                <el-input v-model="templateForm.prompt" type="textarea" :rows="3" placeholder="请输入提示词" />
              </el-form-item>
              <el-form-item label="状态">
                <el-switch v-model="templateForm.enabled" active-text="启用" inactive-text="禁用" />
              </el-form-item>
              <el-form-item label="排序">
                <el-input-number v-model="templateForm.sort" :min="0" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="templateDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSaveTemplate">保存</el-button>
            </template>
          </el-dialog>
        </div>
      </el-tab-pane>

      <!-- 系统信息 -->
      <el-tab-pane label="系统信息" name="system">
        <div class="system-container">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-card class="info-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon class="header-icon"><InfoFilled /></el-icon>
                    <span>系统版本</span>
                  </div>
                </template>
                <div class="info-item">
                  <span class="info-label">平台版本</span>
                  <span class="info-value">v1.0.0</span>
                </div>
                <div class="info-item">
                  <span class="info-label">构建时间</span>
                  <span class="info-value">{{ new Date().toLocaleDateString('zh-CN') }}</span>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-card class="info-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon class="header-icon"><Files /></el-icon>
                    <span>数据库信息</span>
                  </div>
                </template>
                <div class="info-item">
                  <span class="info-label">总用户数</span>
                  <span class="info-value">{{ statistics.totalUsers || 0 }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">总记录数</span>
                  <span class="info-value">{{ statistics.totalRecords || 0 }}</span>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-card class="info-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon class="header-icon"><Monitor /></el-icon>
                    <span>运行状态</span>
                  </div>
                </template>
                <div class="info-item">
                  <span class="info-label">系统状态</span>
                  <el-tag type="success">运行中</el-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">运行时长</span>
                  <span class="info-value">正常</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="userDetailDialogVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-select
              v-model="editUserRole"
              size="small"
              style="width: 140px"
              :disabled="currentUser.role === 'ADMIN'"
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="普通用户" value="USER" />
            </el-select>
            <el-tag :type="currentUser.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ currentUser.role === 'ADMIN' ? '当前：管理员' : '当前：普通用户' }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">
          {{ formatTime(currentUser.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="userDetailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentUser && currentUser.role !== 'ADMIN'"
          type="primary"
          :loading="updateUserRoleLoading"
          @click="handleUpdateUserRole"
        >
          保存角色
        </el-button>
      </template>
    </el-dialog>

    <!-- 内容详情对话框 -->
    <el-dialog v-model="contentDetailDialogVisible" title="内容详情" width="800px">
      <el-descriptions :column="1" border v-if="currentContent">
        <el-descriptions-item label="记录ID">{{ currentContent.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentContent.userId }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="currentContent.type === 'TEXT' ? 'primary' : 'success'">
            {{ currentContent.type === 'TEXT' ? '文本创作' : '图片生成' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提示词">
          <div style="max-height: 100px; overflow-y: auto;">{{ currentContent.prompt }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="生成内容" v-if="currentContent.content">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap;">{{ currentContent.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="图片URL" v-if="currentContent.imageUrl">
          <el-image :src="currentContent.imageUrl" style="max-width: 100%; max-height: 300px;" fit="contain" />
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentContent.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="contentDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, Plus, UserFilled, Document, EditPen, Picture, PieChart, TrendCharts, Search, InfoFilled, Files, Monitor } from '@element-plus/icons-vue'
import { getStatistics } from '@/api/creation'
import { getUserList, deleteUserByAdmin, batchDeleteUsersByAdmin, updateUserRoleByAdmin } from '@/api/user'
import { getRecordList, deleteRecord, getRecordById } from '@/api/creation'
import { getAllTemplates, createTemplate, updateTemplate, deleteTemplate } from '@/api/template'

const activeTab = ref('statistics')
const statisticsLoading = ref(false)
const usersLoading = ref(false)
const contentLoading = ref(false)
const templatesLoading = ref(false)

// 统计数据
const statistics = ref({
  totalUsers: 0,
  totalRecords: 0,
  textRecords: 0,
  imageRecords: 0,
  todayNewUsers: 0,
  todayNewRecords: 0,
  typeStatistics: {},
  dailyStatistics: {}
})

// 用户列表
const users = ref([])
const usersPage = ref(1)
const usersPageSize = ref(10)
const usersTotal = ref(0)
const selectedUsers = ref([])
const userFilter = reactive({
  username: '',
  role: ''
})
const userDetailDialogVisible = ref(false)
const currentUser = ref(null)
const editUserRole = ref('')
const updateUserRoleLoading = ref(false)

// 内容列表
const contentList = ref([])
const contentPage = ref(1)
const contentPageSize = ref(10)
const contentTotal = ref(0)
const selectedContent = ref([])
const contentFilter = reactive({
  type: '',
  userId: ''
})
const contentDetailDialogVisible = ref(false)
const currentContent = ref(null)

// 模板管理
const templatesList = ref([])
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('添加模板')
const templateForm = reactive({
  id: null,
  name: '',
  description: '',
  prompt: '',
  enabled: true,
  sort: 0
})

// 加载统计数据
const loadStatistics = async () => {
  statisticsLoading.value = true
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败：', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    statisticsLoading.value = false
  }
}

// 加载用户列表
const loadUsers = async () => {
  usersLoading.value = true
  try {
    const params = {
      current: usersPage.value,
      size: usersPageSize.value
    }
    if (userFilter.username) {
      params.username = userFilter.username
    }
    if (userFilter.role) {
      params.role = userFilter.role
    }
    const res = await getUserList(params)
    if (res.code === 200) {
      users.value = res.data.records || []
      usersTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载用户列表失败：', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    usersLoading.value = false
  }
}

// 重置用户筛选
const resetUserFilter = () => {
  userFilter.username = ''
  userFilter.role = ''
  usersPage.value = 1
  loadUsers()
}

// 用户选择变化
const handleUserSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 查看用户详情
const handleViewUserDetail = (user) => {
  currentUser.value = user
  editUserRole.value = user.role
  userDetailDialogVisible.value = true
}

// 更新用户角色（管理员）
const handleUpdateUserRole = async () => {
  if (!currentUser.value) return
  if (currentUser.value.role === 'ADMIN') {
    ElMessage.warning('管理员账号不支持在此处修改角色')
    return
  }
  if (!editUserRole.value) {
    ElMessage.warning('请选择角色')
    return
  }
  if (editUserRole.value === currentUser.value.role) {
    ElMessage.info('角色未发生变化')
    return
  }
  try {
    updateUserRoleLoading.value = true
    const res = await updateUserRoleByAdmin(currentUser.value.id, editUserRole.value)
    if (res.code === 200) {
      ElMessage.success('角色更新成功')
      // 同步更新当前用户和列表中的角色
      currentUser.value.role = res.data.role
      const index = users.value.findIndex(u => u.id === currentUser.value.id)
      if (index !== -1) {
        users.value[index].role = res.data.role
      }
    }
  } catch (error) {
    console.error('更新用户角色失败：', error)
    ElMessage.error(error.response?.data?.message || '更新用户角色失败')
  } finally {
    updateUserRoleLoading.value = false
  }
}

// 删除用户
const handleDeleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUserByAdmin(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 如果当前详情弹窗展示的是这个用户，顺便关闭
      if (currentUser.value && currentUser.value.id === id) {
        userDetailDialogVisible.value = false
      }
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败：', error)
    }
  }
}

// 批量删除用户
const handleBatchDeleteUsers = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  // 过滤掉管理员账号，避免误删
  const deletableIds = selectedUsers.value
    .filter(user => user.role !== 'ADMIN')
    .map(user => user.id)

  if (deletableIds.length === 0) {
    ElMessage.warning('选中的用户中不包含可删除的普通用户')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${deletableIds.length} 个普通用户吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await batchDeleteUsersByAdmin(deletableIds)
    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      selectedUsers.value = []
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除用户失败：', error)
    }
  }
}

// 加载内容列表
const loadContent = async () => {
  contentLoading.value = true
  try {
    const params = {
      current: contentPage.value,
      size: contentPageSize.value
    }
    if (contentFilter.type) {
      params.type = contentFilter.type
    }
    if (contentFilter.userId) {
      params.userId = parseInt(contentFilter.userId)
    }
    const res = await getRecordList(params)
    if (res.code === 200) {
      contentList.value = res.data.records || []
      contentTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载内容列表失败：', error)
    ElMessage.error('加载内容列表失败')
  } finally {
    contentLoading.value = false
  }
}

// 内容选择变化
const handleContentSelectionChange = (selection) => {
  selectedContent.value = selection
}

// 查看内容详情
const handleViewContentDetail = async (content) => {
  try {
    // 如果内容信息不完整，尝试获取详情
    if (!content.content && !content.imageUrl) {
      const res = await getRecordById(content.id)
      if (res.code === 200) {
        currentContent.value = res.data
      } else {
        currentContent.value = content
      }
    } else {
      currentContent.value = content
    }
    contentDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取内容详情失败：', error)
    currentContent.value = content
    contentDetailDialogVisible.value = true
  }
}

// 删除内容
const handleDeleteContent = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteRecord(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadContent()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败：', error)
    }
  }
}

// 批量删除内容
const handleBatchDeleteContent = async () => {
  if (selectedContent.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedContent.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    const deletePromises = selectedContent.value.map(item => deleteRecord(item.id))
    await Promise.all(deletePromises)
    
    ElMessage.success('批量删除成功')
    selectedContent.value = []
    loadContent()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败：', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 加载模板列表
const loadTemplates = async () => {
  templatesLoading.value = true
  try {
    const res = await getAllTemplates()
    if (res.code === 200) {
      templatesList.value = res.data || []
    }
  } catch (error) {
    console.error('加载模板列表失败：', error)
    ElMessage.error('加载模板列表失败')
  } finally {
    templatesLoading.value = false
  }
}

// 添加模板
const handleAddTemplate = () => {
  resetTemplateForm()
  templateDialogTitle.value = '添加模板'
  templateDialogVisible.value = true
}

// 编辑模板
const handleEditTemplate = (template) => {
  templateForm.id = template.id
  templateForm.name = template.name
  templateForm.description = template.description || ''
  templateForm.prompt = template.prompt || ''
  templateForm.enabled = template.enabled
  templateForm.sort = template.sort || 0
  templateDialogTitle.value = '编辑模板'
  templateDialogVisible.value = true
}

// 保存模板
const handleSaveTemplate = async () => {
  if (!templateForm.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  
  try {
    const data = {
      name: templateForm.name,
      description: templateForm.description,
      prompt: templateForm.prompt,
      enabled: templateForm.enabled,
      sort: templateForm.sort
    }
    
    let res
    if (templateForm.id) {
      res = await updateTemplate(templateForm.id, data)
    } else {
      res = await createTemplate(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(templateForm.id ? '更新成功' : '创建成功')
      templateDialogVisible.value = false
      loadTemplates()
    }
  } catch (error) {
    console.error('保存模板失败：', error)
  }
}

// 删除模板
const handleDeleteTemplate = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteTemplate(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadTemplates()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败：', error)
    }
  }
}

// 重置模板表单
const resetTemplateForm = () => {
  templateForm.id = null
  templateForm.name = ''
  templateForm.description = ''
  templateForm.prompt = ''
  templateForm.enabled = true
  templateForm.sort = 0
}

// Tab切换
const handleTabChange = (name) => {
  if (name === 'statistics') {
    loadStatistics()
  } else if (name === 'users') {
    loadUsers()
  } else if (name === 'content') {
    loadContent()
  } else if (name === 'templates') {
    loadTemplates()
  }
}

// 计算文本百分比
const getTextPercentage = () => {
  const total = (statistics.value.textRecords || 0) + (statistics.value.imageRecords || 0)
  if (total === 0) return 0
  return Math.round((statistics.value.textRecords / total) * 100)
}

// 计算图像百分比
const getImagePercentage = () => {
  const total = (statistics.value.textRecords || 0) + (statistics.value.imageRecords || 0)
  if (total === 0) return 0
  return Math.round((statistics.value.imageRecords / total) * 100)
}

// 格式化每日统计
const formatDailyStatistics = () => {
  if (!statistics.value.dailyStatistics) return []
  return Object.entries(statistics.value.dailyStatistics)
    .map(([date, count]) => ({
      date,
      count: typeof count === 'number' ? count : 0
    }))
    .sort((a, b) => new Date(a.date) - new Date(b.date))
}

// 计算每日百分比（用于进度条显示）
const getDailyPercentage = (count) => {
  if (!statistics.value.dailyStatistics) return 0
  const counts = Object.values(statistics.value.dailyStatistics).map(c => typeof c === 'number' ? c : 0)
  const maxCount = Math.max(...counts, 1)
  return Math.round((count / maxCount) * 100)
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.admin-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(to bottom, #f8f9ff 0%, #ffffff 100%);
  min-height: calc(100vh - 60px);
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
  background: #fff;
  padding: 0 20px;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-tabs__item) {
  font-weight: 500;
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-tabs__item.is-active) {
  color: #667eea;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  height: 3px;
}

:deep(.el-tabs__content) {
  padding: 24px;
  background: #fff;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.statistics-container {
  min-height: 400px;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent, currentColor, transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card-user {
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

.stat-card-user::before {
  color: #f56c6c;
}

.stat-card-user .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8c8c 100%);
  color: #fff;
}

.stat-card-record {
  background: linear-gradient(135deg, #f0f4ff 0%, #ffffff 100%);
}

.stat-card-record::before {
  color: #667eea;
}

.stat-card-record .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-card-text {
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
}

.stat-card-text::before {
  color: #409eff;
}

.stat-card-text .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.stat-card-image {
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
}

.stat-card-image::before {
  color: #67c23a;
}

.stat-card-image .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-item {
  padding: 24px 20px;
  text-align: center;
  position: relative;
}

.stat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  margin-bottom: 16px;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-label {
  font-size: 15px;
  color: #606266;
  margin-bottom: 12px;
  font-weight: 500;
}

.stat-desc {
  font-size: 13px;
  color: #67c23a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-weight: 500;
}

.chart-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.header-icon {
  color: #667eea;
  font-size: 20px;
}

.type-chart {
  padding: 20px;
}

.daily-chart {
  padding: 10px;
}

.users-container,
.content-container,
.templates-container {
  min-height: 400px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background: #f8f9ff;
}

:deep(.el-table th) {
  background: #f8f9ff;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table tr:hover > td) {
  background: #f0f4ff;
}

.templates-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.filter-form :deep(.el-select__wrapper) {
  border-radius: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.type-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.type-label {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 100px;
  font-weight: 500;
  color: #606266;
}

.type-progress {
  flex: 1;
}

.type-value {
  min-width: 60px;
  text-align: right;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.system-container {
  min-height: 400px;
}
</style>
