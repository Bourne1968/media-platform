# 数据库设置指南

## 一、快速设置（推荐）

### 方法1：使用 MySQL 命令行

1. **打开命令行，登录 MySQL：**
   ```bash
   mysql -u root -p
   ```
   输入密码：`Deng2846240558`（根据你的配置）

2. **执行初始化脚本：**
   ```sql
   source D:/86176/Project/media-platform/backend/src/main/resources/sql/init_database.sql
   ```
   
   或者复制整个 `init_database.sql` 文件的内容，在 MySQL 客户端中执行。

### 方法2：使用 MySQL 客户端工具（Navicat、DBeaver、MySQL Workbench 等）

1. **打开你的 MySQL 客户端工具**
2. **连接到 MySQL 服务器**
   - 主机：`localhost`
   - 端口：`3306`
   - 用户名：`root`
   - 密码：`Deng2846240558`

3. **打开并执行 SQL 文件：**
   - 打开文件：`backend/src/main/resources/sql/init_database.sql`
   - 执行整个脚本

## 二、手动设置（分步骤）

### 步骤1：创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `media_platform` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `media_platform`;
```

### 步骤2：创建表

执行 `backend/src/main/resources/sql/schema.sql` 中的所有 CREATE TABLE 语句。

### 步骤3：插入测试数据

```sql
-- 插入风格模板数据
INSERT INTO `style_template` (`name`, `description`, `enabled`, `sort`) VALUES
('轻松活泼', '适合日常、娱乐类内容', 1, 1),
('正式严谨', '适合商务、专业类内容', 1, 2),
('幽默风趣', '适合搞笑、段子类内容', 1, 3),
('文艺清新', '适合文艺、情感类内容', 1, 4),
('吸引眼球', '适合标题、营销类内容', 1, 5);

-- 插入管理员账户（可选）
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', 'admin123', 'ADMIN');
```

## 三、验证数据库设置

执行以下 SQL 验证表是否创建成功：

```sql
USE `media_platform`;

-- 查看所有表
SHOW TABLES;

-- 应该看到以下表：
-- user
-- creation_record
-- style_template
-- favorite (可选)
-- category (可选)
-- record_category (可选)

-- 查看风格模板数据
SELECT * FROM `style_template`;

-- 查看用户数据
SELECT `id`, `username`, `role` FROM `user`;
```

## 四、常见问题

### 1. 数据库连接失败

**错误信息：** `Access denied for user 'root'@'localhost'`

**解决方法：**
- 检查 `application.yml` 中的数据库配置
- 确认 MySQL 服务已启动
- 确认用户名和密码正确

### 2. 表已存在错误

**错误信息：** `Table 'xxx' already exists`

**解决方法：**
- 如果表已存在且结构正确，可以跳过创建表的步骤
- 如果需要重新创建，先删除表：`DROP TABLE IF EXISTS table_name;`

### 3. 外键约束错误

**错误信息：** `Cannot add foreign key constraint`

**解决方法：**
- 确保先创建被引用的表（如 `user` 表）
- 检查表的创建顺序：`user` → `creation_record` → 其他表

### 4. 字符集问题

**错误信息：** 中文乱码

**解决方法：**
- 确保数据库使用 `utf8mb4` 字符集
- 确保表的字符集为 `utf8mb4`
- 确保连接字符串包含 `useUnicode=true&characterEncoding=utf8`

## 五、测试账户

初始化脚本会自动创建以下测试账户：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 管理员账户 |

## 六、默认风格模板

初始化脚本会自动创建以下风格模板：

1. **轻松活泼** - 适合日常、娱乐类内容
2. **正式严谨** - 适合商务、专业类内容
3. **幽默风趣** - 适合搞笑、段子类内容
4. **文艺清新** - 适合文艺、情感类内容
5. **吸引眼球** - 适合标题、营销类内容

## 七、下一步

数据库设置完成后：

1. **重启后端服务**
2. **访问前端页面**测试功能
3. **使用管理员账户登录**测试管理后台

如果遇到问题，请查看后端日志获取详细错误信息。
