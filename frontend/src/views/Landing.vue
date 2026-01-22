<template>
  <div class="landing-page">
    <!-- 导航栏 -->
    <header class="header">
      <nav class="nav">
        <div class="nav-brand">
          <el-icon :size="32"><Lightning /></el-icon>
          <span class="brand-text">AI Creator Studio</span>
        </div>
        <div class="nav-links">
          <a href="#features" class="nav-link">功能特性</a>
          <a href="#pricing" class="nav-link">价格方案</a>
          <a href="#about" class="nav-link">关于我们</a>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">免费注册</el-button>
        </div>
      </nav>
    </header>

    <!-- 英雄区域 -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">
          AI驱动的创作新纪元<br/>
          <span class="hero-subtitle">让创意无限延伸</span>
        </h1>
        <p class="hero-description">
          基于先进的人工智能技术，为自媒体创作者提供全方位的创作辅助工具。
          从文案写作到封面设计，一站式解决您的创作需求。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/register')" class="cta-button">
            <el-icon><MagicStick /></el-icon>
            开始免费创作
          </el-button>
          <el-button size="large" @click="scrollToFeatures" class="demo-button">
            查看演示
            <el-icon><ArrowDown /></el-icon>
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">10,000+</div>
            <div class="stat-label">创作者</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">500,000+</div>
            <div class="stat-label">作品生成</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">95%</div>
            <div class="stat-label">用户满意度</div>
          </div>
        </div>
      </div>
      <div class="hero-visual">
        <div class="floating-cards">
          <div class="card card-1">
            <el-icon><EditPen /></el-icon>
            <span>文案写作</span>
          </div>
          <div class="card card-2">
            <el-icon><Picture /></el-icon>
            <span>封面设计</span>
          </div>
          <div class="card card-3">
            <el-icon><TrendCharts /></el-icon>
            <span>数据分析</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特性区域 -->
    <section id="features" class="features">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">强大功能，创作无忧</h2>
          <p class="section-subtitle">从创意发想到内容发布，全流程AI辅助创作</p>
        </div>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="48"><EditPen /></el-icon>
            </div>
            <h3 class="feature-title">智能文案写作</h3>
            <p class="feature-desc">
              基于AI大模型，支持标题、脚本、文案等多种形式创作。
              内置多种风格模板，快速生成符合平台调性的优质内容。
            </p>
            <div class="feature-tags">
              <el-tag size="small">标题生成</el-tag>
              <el-tag size="small">脚本创作</el-tag>
              <el-tag size="small">风格多样</el-tag>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
            <h3 class="feature-title">AI封面设计</h3>
            <p class="feature-desc">
              输入描述或关键词，AI自动生成精美封面设计。
              支持多种尺寸和风格，完美适配各大平台发布需求。
            </p>
            <div class="feature-tags">
              <el-tag size="small">智能设计</el-tag>
              <el-tag size="small">多尺寸</el-tag>
              <el-tag size="small">快速生成</el-tag>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="48"><TrendCharts /></el-icon>
            </div>
            <h3 class="feature-title">创作数据分析</h3>
            <p class="feature-desc">
              实时跟踪创作数据，提供作品表现分析和趋势洞察。
              帮助创作者优化内容策略，提升创作效率和影响力。
            </p>
            <div class="feature-tags">
              <el-tag size="small">数据统计</el-tag>
              <el-tag size="small">趋势分析</el-tag>
              <el-tag size="small">内容优化</el-tag>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="48"><Calendar /></el-icon>
            </div>
            <h3 class="feature-title">创作日历管理</h3>
            <p class="feature-desc">
              智能规划内容发布计划，支持多平台统一管理。
              提供创作提醒和进度跟踪，确保内容创作按计划执行。
            </p>
            <div class="feature-tags">
              <el-tag size="small">内容规划</el-tag>
              <el-tag size="small">多平台</el-tag>
              <el-tag size="small">进度跟踪</el-tag>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="48"><MagicStick /></el-icon>
            </div>
            <h3 class="feature-title">灵感创意中心</h3>
            <p class="feature-desc">
              实时热点追踪，热门话题推荐，爆款案例分析。
              为创作者提供源源不断的创作灵感和参考素材。
            </p>
            <div class="feature-tags">
              <el-tag size="small">热点追踪</el-tag>
              <el-tag size="small">创意推荐</el-tag>
              <el-tag size="small">案例分析</el-tag>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="48"><Document /></el-icon>
            </div>
            <h3 class="feature-title">素材库管理</h3>
            <p class="feature-desc">
              智能分类存储所有创作内容，支持快速搜索和筛选。
              提供收藏功能，便于复用优秀作品和素材。
            </p>
            <div class="feature-tags">
              <el-tag size="small">智能分类</el-tag>
              <el-tag size="small">快速搜索</el-tag>
              <el-tag size="small">内容复用</el-tag>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 价格方案区域 -->
    <section id="pricing" class="pricing">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">选择适合您的方案</h2>
          <p class="section-subtitle">从免费开始，享受专业创作体验</p>
        </div>

        <div class="pricing-grid">
          <div class="pricing-card">
            <div class="pricing-header">
              <h3 class="plan-name">免费版</h3>
              <div class="plan-price">
                <span class="price">¥0</span>
                <span class="period">/月</span>
              </div>
            </div>
            <div class="pricing-features">
              <div class="feature-item">✓ 每月50次AI创作</div>
              <div class="feature-item">✓ 基础文案写作</div>
              <div class="feature-item">✓ 标准封面设计</div>
              <div class="feature-item">✓ 基础数据统计</div>
              <div class="feature-item">✗ 高级风格模板</div>
              <div class="feature-item">✗ 批量导出功能</div>
            </div>
            <el-button type="default" size="large" class="pricing-button" @click="$router.push('/register')">
              开始使用
            </el-button>
          </div>

          <div class="pricing-card featured">
            <div class="pricing-badge">推荐</div>
            <div class="pricing-header">
              <h3 class="plan-name">专业版</h3>
              <div class="plan-price">
                <span class="price">¥29</span>
                <span class="period">/月</span>
              </div>
            </div>
            <div class="pricing-features">
              <div class="feature-item">✓ 每月500次AI创作</div>
              <div class="feature-item">✓ 高级文案写作</div>
              <div class="feature-item">✓ 高清封面设计</div>
              <div class="feature-item">✓ 完整数据分析</div>
              <div class="feature-item">✓ 所有风格模板</div>
              <div class="feature-item">✓ 批量导出功能</div>
              <div class="feature-item">✓ 优先技术支持</div>
            </div>
            <el-button type="primary" size="large" class="pricing-button" @click="$router.push('/register')">
              立即升级
            </el-button>
          </div>

          <div class="pricing-card">
            <div class="pricing-header">
              <h3 class="plan-name">企业版</h3>
              <div class="plan-price">
                <span class="price">¥99</span>
                <span class="period">/月</span>
              </div>
            </div>
            <div class="pricing-features">
              <div class="feature-item">✓ 每月2000次AI创作</div>
              <div class="feature-item">✓ 企业级AI模型</div>
              <div class="feature-item">✓ 超清封面设计</div>
              <div class="feature-item">✓ 高级数据分析</div>
              <div class="feature-item">✓ 定制风格模板</div>
              <div class="feature-item">✓ API接口调用</div>
              <div class="feature-item">✓ 专属客服支持</div>
              <div class="feature-item">✓ 团队协作功能</div>
            </div>
            <el-button type="default" size="large" class="pricing-button" @click="contactSales">
              联系销售
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 关于我们区域 -->
    <section id="about" class="about">
      <div class="container">
        <div class="about-content">
          <div class="about-text">
            <h2 class="about-title">让创作变得简单而有趣</h2>
            <p class="about-desc">
              我们致力于用人工智能技术重塑内容创作流程，让每一位创作者都能轻松驾驭创作的乐趣。
              从灵感萌发到作品发布，我们为您提供全方位的AI创作辅助工具。
            </p>
            <div class="about-stats">
              <div class="about-stat">
                <div class="stat-number">2024</div>
                <div class="stat-label">成立时间</div>
              </div>
              <div class="about-stat">
                <div class="stat-number">50+</div>
                <div class="stat-label">团队成员</div>
              </div>
              <div class="about-stat">
                <div class="stat-number">10万+</div>
                <div class="stat-label">服务用户</div>
              </div>
            </div>
          </div>
          <div class="about-visual">
            <div class="about-illustration">
              <el-icon :size="120"><MagicStick /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <el-icon :size="32"><Lightning /></el-icon>
            <span>AI Creator Studio</span>
          </div>
          <div class="footer-links">
            <div class="footer-section">
              <h4>产品</h4>
              <a href="#features">功能特性</a>
              <a href="#pricing">价格方案</a>
              <a href="#about">关于我们</a>
            </div>
            <div class="footer-section">
              <h4>支持</h4>
              <a href="#">帮助中心</a>
              <a href="#">API文档</a>
              <a href="#">联系我们</a>
            </div>
            <div class="footer-section">
              <h4>法律</h4>
              <a href="#">隐私政策</a>
              <a href="#">服务协议</a>
              <a href="#">版权声明</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 AI Creator Studio. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  Lightning,
  EditPen,
  Picture,
  TrendCharts,
  Calendar,
  MagicStick,
  Document,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()

const scrollToFeatures = () => {
  document.getElementById('features').scrollIntoView({
    behavior: 'smooth'
  })
}

const contactSales = () => {
  ElMessage.info('请联系我们的销售团队：sales@aicreator.com')
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 导航栏 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.nav {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.brand-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #667eea;
}

/* 英雄区域 */
.hero {
  padding: 120px 20px 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  min-height: 80vh;
  display: flex;
  align-items: center;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 24px;
}

.hero-subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 32px;
}

.hero-description {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 32px;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
}

.cta-button {
  background: white;
  color: #667eea;
  border: none;
  font-weight: 600;
  padding: 0 32px;
  height: 56px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.demo-button {
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.hero-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

.hero-visual {
  position: relative;
}

.floating-cards {
  position: relative;
  height: 400px;
}

.card {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: white;
  font-weight: 600;
  animation: floatCards 6s ease-in-out infinite;
}

.card-1 {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.card-2 {
  top: 50%;
  right: 10%;
  animation-delay: 2s;
}

.card-3 {
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes floatCards {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 通用样式 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 18px;
  color: #666;
  margin: 0;
}

/* 功能特性区域 */
.features {
  padding: 100px 0;
  background: #fafafa;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 40px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  margin-bottom: 24px;
  color: #667eea;
}

.feature-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.feature-desc {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* 价格方案区域 */
.pricing {
  padding: 100px 0;
  background: white;
}

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  max-width: 1000px;
  margin: 0 auto;
}

.pricing-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid #f0f0f0;
  position: relative;
  transition: all 0.3s ease;
}

.pricing-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.pricing-card.featured {
  border-color: #667eea;
  transform: scale(1.05);
}

.pricing-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.pricing-header {
  text-align: center;
  margin-bottom: 32px;
}

.plan-name {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.price {
  font-size: 48px;
  font-weight: 800;
  color: #667eea;
}

.period {
  font-size: 18px;
  color: #666;
}

.pricing-features {
  margin-bottom: 32px;
}

.feature-item {
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.pricing-button {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-weight: 600;
}

/* 关于我们区域 */
.about {
  padding: 100px 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.about-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.about-title {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin-bottom: 24px;
}

.about-desc {
  font-size: 18px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 32px;
}

.about-stats {
  display: flex;
  gap: 40px;
}

.about-stat {
  text-align: center;
}

.about-stat .stat-number {
  font-size: 32px;
  font-weight: 800;
  color: #667eea;
  margin-bottom: 4px;
}

.about-stat .stat-label {
  font-size: 14px;
  color: #666;
}

.about-visual {
  text-align: center;
}

.about-illustration {
  color: #667eea;
  opacity: 0.8;
}

/* 页脚 */
.footer {
  background: #2c3e50;
  color: white;
  padding: 60px 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: 1fr 3fr;
  gap: 40px;
  margin-bottom: 40px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.footer-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #ecf0f1;
}

.footer-section a {
  display: block;
  color: #bdc3c7;
  text-decoration: none;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.footer-section a:hover {
  color: #667eea;
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding-top: 20px;
  text-align: center;
  color: #95a5a6;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-stats {
    justify-content: center;
  }

  .features-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  }

  .about-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .footer-links {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .hero {
    padding: 100px 20px 60px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 24px;
  }

  .hero-stats {
    flex-wrap: wrap;
    gap: 20px;
  }

  .features {
    padding: 60px 0;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .pricing-grid {
    grid-template-columns: 1fr;
  }

  .pricing-card.featured {
    transform: none;
    order: -1;
  }

  .footer-links {
    grid-template-columns: 1fr;
  }

  .about-stats {
    flex-wrap: wrap;
    justify-content: center;
    gap: 20px;
  }
}
</style>