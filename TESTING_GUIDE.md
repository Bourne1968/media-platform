# 测试指南

## 一、数据库准备

### 1. 执行 SQL 脚本
执行 `backend/src/main/resources/sql/schema.sql` 中的表创建语句，确保以下表已创建：
- `user` - 用户表（应该已存在）
- `creation_record` - 创作记录表（应该已存在）
- `style_template` - 风格模板表（**新增**）
- `favorite` - 收藏表（可选，暂未实现）
- `category` - 分类表（可选，暂未实现）
- `record_category` - 记录分类关联表（可选，暂未实现）

### 2. 初始化风格模板数据（推荐）
执行以下 SQL 插入一些默认模板：

```sql
INSERT INTO style_template (name, description, enabled, sort, create_time, update_time) VALUES
('轻松活泼', '适合日常、娱乐类内容', 1, 1, NOW(), NOW()),
('正式严谨', '适合商务、专业类内容', 1, 2, NOW(), NOW()),
('幽默风趣', '适合搞笑、段子类内容', 1, 3, NOW(), NOW()),
('文艺清新', '适合文艺、情感类内容', 1, 4, NOW(), NOW()),
('吸引眼球', '适合标题、营销类内容', 1, 5, NOW(), NOW());
```

### 3. 创建测试管理员账户（可选）
如果需要测试管理后台，创建一个管理员账户：

```sql
INSERT INTO user (username, password, role, create_time, update_time) VALUES
('admin', 'admin123', 'ADMIN', NOW(), NOW());
```

## 二、后端测试

### 1. 启动后端服务
```bash
cd backend
mvn spring-boot:run
```

或在 IDEA 中运行 `BackendApplication`

### 2. 验证后端接口
- 后端服务地址：`http://localhost:8081`
- API 基础路径：`http://localhost:8081/api`

#### 测试接口列表：

**公开接口（无需Token）：**
- ✅ `POST /api/user/login` - 用户登录
- ✅ `POST /api/user/register` - 用户注册
- ✅ `GET /api/style-template/enabled` - 获取启用的风格模板列表

**需要Token的接口：**
- ✅ `GET /api/creation` - 获取创作记录列表
- ✅ `POST /api/creation` - 创建创作记录
- ✅ `GET /api/ai/text` - 生成文本
- ✅ `POST /api/ai/image` - 生成图片

**管理员接口（需要ADMIN角色）：**
- ✅ `GET /api/user/list` - 获取用户列表
- ✅ `GET /api/creation/statistics` - 获取统计数据
- ✅ `GET /api/style-template` - 获取所有模板（管理员）
- ✅ `POST /api/style-template` - 创建模板（管理员）
- ✅ `PUT /api/style-template/{id}` - 更新模板（管理员）
- ✅ `DELETE /api/style-template/{id}` - 删除模板（管理员）

### 3. 使用 Postman 或 curl 测试

#### 测试登录：
```bash
curl -X POST http://localhost:8081/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

#### 测试获取模板列表（公开接口）：
```bash
curl http://localhost:8081/api/style-template/enabled
```

#### 测试获取统计数据（需要管理员Token）：
```bash
curl http://localhost:8081/api/creation/statistics \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 三、前端测试

### 1. 启动前端服务
```bash
cd frontend
npm install  # 如果还没安装依赖
npm run dev
```

前端服务地址：`http://localhost:5173`

### 2. 功能测试清单

#### ✅ 基础功能测试

**A. 用户登录注册**
1. 访问 `http://localhost:5173/login`
2. 测试注册新用户
3. 测试登录功能
4. 验证登录后跳转到工作台

**B. 创作工作台**
1. 进入"创作工作台"页面
2. 测试文本创作：
   - 输入提示词
   - **验证风格模板下拉框是否有数据**（从后端API加载）
   - 选择风格模板
   - 点击生成按钮
   - 验证生成结果
   - 点击保存到历史记录
3. 测试图片生成：
   - 切换到"图片生成"
   - 输入提示词
   - 点击生成按钮
   - 验证生成结果

**C. 历史记录**
1. 进入"历史记录"页面
2. 验证可以看到之前保存的记录
3. 测试筛选功能（按类型）
4. 测试删除功能

#### ✅ 管理后台测试（需要管理员账户）

**A. 访问管理后台**
1. 使用管理员账户登录
2. 验证导航菜单中显示"管理后台"选项
3. 点击进入管理后台

**B. 数据统计标签页**
1. 验证统计卡片显示数据：
   - 总用户数
   - 总创作记录
   - 文本创作数
   - 图像创作数
   - 今日新增数据
2. 验证类型分布图表
3. 验证最近7天趋势表格

**C. 用户管理标签页**
1. 验证用户列表显示
2. 验证分页功能
3. 验证角色显示（管理员/普通用户）

**D. 内容管理标签页**
1. 验证所有用户的创作记录显示
2. 测试筛选功能（类型、用户ID）
3. 测试删除功能

**E. 风格模板管理标签页** ⭐ **新增功能**
1. 验证模板列表显示
2. 测试添加模板：
   - 点击"添加模板"按钮
   - 填写模板信息（名称、描述、提示词等）
   - 设置启用状态和排序
   - 保存
3. 测试编辑模板：
   - 点击"编辑"按钮
   - 修改模板信息
   - 保存
4. 测试删除模板：
   - 点击"删除"按钮
   - 确认删除
5. 验证模板启用/禁用后，工作台的模板下拉框会相应更新

#### ✅ 风格模板功能测试

**A. 工作台模板加载**
1. 在工作台选择"文本创作"
2. 验证"风格模板"下拉框从后端API加载数据
3. 验证模板选项显示模板名称和描述
4. 选择一个模板并生成内容，验证模板生效

**B. 模板管理**
1. 在管理后台的"风格模板"标签页
2. 添加新模板后，验证工作台能立即看到新模板
3. 禁用模板后，验证工作台看不到该模板
4. 启用模板后，验证工作台能看到该模板

## 四、常见问题排查

### 1. 风格模板下拉框为空
**原因：** 数据库中 `style_template` 表没有数据或表不存在
**解决：**
- 检查表是否创建
- 执行初始化SQL插入测试数据
- 检查后端接口 `/api/style-template/enabled` 是否正常

### 2. 管理后台无法访问
**原因：** 用户角色不是 `ADMIN`
**解决：**
- 检查数据库用户表的 `role` 字段是否为 `ADMIN`
- 重新登录获取新的Token（Token中包含角色信息）

### 3. 后端接口返回401/403错误
**原因：** Token无效或权限不足
**解决：**
- 检查请求头是否包含 `Authorization: Bearer YOUR_TOKEN`
- 检查Token是否过期，重新登录获取新Token
- 管理员接口需要角色为 `ADMIN`

### 4. 跨域错误（CORS）
**原因：** 前后端端口不同
**解决：**
- 检查 `WebConfig` 中的跨域配置
- 确认前端请求的API地址正确

### 5. 数据库连接错误
**原因：** 数据库配置不正确
**解决：**
- 检查 `application.yml` 中的数据库配置
- 确认数据库服务已启动
- 确认数据库名称、用户名、密码正确

## 五、测试数据建议

### 创建测试用户
```sql
-- 普通用户
INSERT INTO user (username, password, role, create_time, update_time) VALUES
('user1', '123456', 'USER', NOW(), NOW());

-- 管理员
INSERT INTO user (username, password, role, create_time, update_time) VALUES
('admin', 'admin123', 'ADMIN', NOW(), NOW());
```

### 创建测试创作记录
使用前端界面创建一些测试记录，用于测试：
- 历史记录列表
- 数据统计功能
- 内容管理功能

## 六、预期测试结果

### ✅ 成功标志
1. 后端服务正常启动，无编译错误
2. 前端服务正常启动，页面正常显示
3. 用户注册/登录功能正常
4. AI生成功能正常（需要配置正确的AI API Key）
5. 风格模板功能正常（模板列表动态加载）
6. 管理后台所有功能正常（管理员账户）
7. 统计数据正确显示
8. 模板管理CRUD功能正常

### ⚠️ 注意事项
1. AI生成功能需要有效的API Key（在 `application.yml` 中配置）
2. 如果AI API不可用，生成功能会失败，但不影响其他功能测试
3. 数据库表需要先创建，否则相关功能无法使用

## 七、下一步
测试通过后，可以：
1. 继续实现收藏和分类功能
2. 完善AI生成功能（流式输出等）
3. 优化界面和用户体验
4. 添加更多统计图表
5. 实现热点推荐功能
