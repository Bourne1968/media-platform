# 管理员账号使用指南

## 一、默认管理员账号

根据数据库初始化脚本，系统有一个默认的管理员账号：

- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: `ADMIN`

## 二、如何创建管理员账号

### 方法1：通过数据库直接创建（推荐）

1. **连接数据库**
   ```bash
   mysql -u root -p
   ```

2. **使用数据库**
   ```sql
   USE media_platform;
   ```

3. **插入管理员账号**
   ```sql
   INSERT INTO `user` (`username`, `password`, `role`, `create_time`, `update_time`) 
   VALUES ('admin', 'admin123', 'ADMIN', NOW(), NOW())
   ON DUPLICATE KEY UPDATE `username`=`username`;
   ```

   **注意**：当前系统使用明文密码（未加密），所以直接插入 `'admin123'` 即可。

### 方法2：通过注册接口创建（需要修改角色）

1. **先注册一个普通账号**
   - 访问注册页面
   - 注册用户名和密码

2. **在数据库中修改角色**
   ```sql
   UPDATE `user` SET `role` = 'ADMIN' WHERE `username` = '你的用户名';
   ```

### 方法3：使用初始化脚本

执行完整的数据库初始化脚本：
```bash
mysql -u root -p < backend/src/main/resources/sql/init_database.sql
```

或者在 MySQL 客户端中执行：
```sql
source backend/src/main/resources/sql/init_database.sql
```

## 三、登录管理员账号

1. **访问登录页面**
   - 打开浏览器访问：`http://localhost:5173/login`

2. **输入管理员账号**
   - 用户名：`admin`
   - 密码：`admin123`

3. **登录成功后**
   - 系统会自动跳转到工作台
   - 在左侧导航栏会看到"管理员"菜单项（只有管理员可见）
   - 点击"管理员"即可进入管理后台

## 四、管理员功能

管理员后台包含以下功能模块：

1. **数据统计**
   - 总用户数统计
   - 创作记录统计
   - 类型分布图表
   - 最近7天创作趋势

2. **用户管理**
   - 查看所有用户列表
   - 搜索和筛选用户
   - 查看用户详情
   - 删除用户（需后端支持）

3. **内容管理**
   - 查看所有创作记录
   - 按类型和用户筛选
   - 查看内容详情
   - 删除创作记录
   - 批量删除

4. **风格模板管理**
   - 查看所有模板
   - 添加新模板
   - 编辑模板
   - 启用/禁用模板
   - 删除模板

5. **系统信息**
   - 系统版本信息
   - 数据库统计
   - 运行状态

## 五、常见问题

### Q1: 登录后看不到"管理员"菜单？

**原因**：
- 账号角色不是 `ADMIN`
- 用户信息未正确加载

**解决方法**：
1. 检查数据库中用户角色：
   ```sql
   SELECT username, role FROM user WHERE username = 'admin';
   ```
   应该显示 `role = 'ADMIN'`

2. 清除浏览器缓存和 localStorage，重新登录

3. 检查浏览器控制台是否有错误信息

### Q2: 无法访问 `/admin` 路由？

**原因**：
- 路由守卫检查失败
- Token 过期或无效

**解决方法**：
1. 重新登录获取新的 Token
2. 检查 `localStorage` 中是否有 `token` 和 `userInfo`
3. 确认 `userInfo` 中的 `role` 字段为 `'ADMIN'`

### Q3: 密码忘记了怎么办？

**解决方法**：
1. 直接在数据库中重置密码：
   ```sql
   UPDATE `user` SET `password` = '新密码' WHERE `username` = 'admin';
   ```

2. 或者删除旧账号，重新创建：
   ```sql
   DELETE FROM `user` WHERE `username` = 'admin';
   INSERT INTO `user` (`username`, `password`, `role`, `create_time`, `update_time`) 
   VALUES ('admin', '新密码', 'ADMIN', NOW(), NOW());
   ```

### Q4: 如何创建多个管理员账号？

**方法**：
```sql
INSERT INTO `user` (`username`, `password`, `role`, `create_time`, `update_time`) 
VALUES ('admin2', 'password123', 'ADMIN', NOW(), NOW());
```

### Q5: 前端页面显示不出来？

**可能原因**：
1. 前端服务未启动
2. 路由配置错误
3. 组件加载失败

**解决方法**：
1. 检查前端服务是否运行：
   ```bash
   cd frontend
   npm run dev
   ```

2. 检查浏览器控制台错误信息

3. 检查路由配置是否正确

4. 清除浏览器缓存，强制刷新（Ctrl+F5）

## 六、安全建议

⚠️ **重要提示**：当前系统使用明文密码存储，仅适用于开发环境！

生产环境建议：
1. 使用 BCrypt 等加密算法加密密码
2. 修改默认管理员密码
3. 启用 HTTPS
4. 添加登录失败次数限制
5. 添加验证码功能

## 七、验证管理员账号

执行以下 SQL 验证管理员账号是否存在：

```sql
SELECT id, username, role, create_time 
FROM `user` 
WHERE `role` = 'ADMIN';
```

应该能看到至少一条记录，`role` 字段为 `'ADMIN'`。
