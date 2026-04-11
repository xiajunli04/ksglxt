<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Calendar,
  CircleCheck,
  School,
  Bell,
  Search,
  Document,
  Clock,
  Check,
  ArrowRight,
  Microphone,
  Mute,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const scrolled = ref(false)
const isMuted = ref(true)
const videoRef = ref<HTMLVideoElement | null>(null)

const handleEnter = () => {
  if (userStore.isLoggedIn) {
    router.push('/admin/dashboard')
  } else {
    router.push('/login')
  }
}

const toggleMute = () => {
  isMuted.value = !isMuted.value
  if (videoRef.value) {
    videoRef.value.muted = isMuted.value
  }
}

const handleScroll = () => {
  scrolled.value = window.scrollY > 50
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="landing-page">
    <!-- Header -->
    <header class="landing-header" :class="{ scrolled }">
      <div class="header-inner">
        <div class="header-logo">
          <img src="/logo.jpg" alt="logo" class="header-logo-img" />
          <span class="logo-text">广软课室申请系统</span>
        </div>
      </div>
    </header>

    <!-- Hero Section -->
    <section class="hero-section">
      <video ref="videoRef" class="hero-video" autoplay muted loop playsinline>
        <source src="/228.mp4" type="video/mp4" />
      </video>
      <div class="hero-overlay"></div>
      <button class="sound-toggle" @click="toggleMute">
        <el-icon :size="20" color="#fff">
          <Mute v-if="isMuted" />
          <Microphone v-else />
        </el-icon>
      </button>
      <div class="hero-content">
        <h1 class="hero-title">广软课室申请系统</h1>
        <p class="hero-subtitle">让每一间课室都能被高效利用</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="handleEnter">
            进入系统
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" round @click="$el.querySelector('.features-section')?.scrollIntoView({ behavior: 'smooth' })">
            了解更多
          </el-button>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <h2 class="section-title">核心功能</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon" style="background: #ECF5FF">
            <el-icon :size="32" color="#409EFF"><Calendar /></el-icon>
          </div>
          <h3 class="feature-name">在线预约</h3>
          <p class="feature-desc">在线选择课室和时段，一键提交预约申请</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" style="background: #F0F9EB">
            <el-icon :size="32" color="#67C23A"><CircleCheck /></el-icon>
          </div>
          <h3 class="feature-name">智能审批</h3>
          <p class="feature-desc">实时审批进度追踪，审批结果即时通知</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" style="background: #FDF6EC">
            <el-icon :size="32" color="#E6A23C"><School /></el-icon>
          </div>
          <h3 class="feature-name">课室管理</h3>
          <p class="feature-desc">可视化课室资源管理，状态实时更新</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" style="background: #F4F4F5">
            <el-icon :size="32" color="#909399"><Bell /></el-icon>
          </div>
          <h3 class="feature-name">公告通知</h3>
          <p class="feature-desc">重要通知即时推送，审批结果消息提醒</p>
        </div>
      </div>
    </section>

    <!-- Steps Section -->
    <section class="steps-section">
      <h2 class="section-title">使用流程</h2>
      <div class="steps-grid">
        <div class="step-item">
          <div class="step-icon">
            <el-icon :size="36" color="#409EFF"><Search /></el-icon>
          </div>
          <div class="step-num">01</div>
          <h3 class="step-name">选择课室</h3>
          <p class="step-desc">浏览可用课室，查看空闲时段</p>
        </div>
        <div class="step-arrow">
          <div class="arrow-line"></div>
        </div>
        <div class="step-item">
          <div class="step-icon">
            <el-icon :size="36" color="#409EFF"><Document /></el-icon>
          </div>
          <div class="step-num">02</div>
          <h3 class="step-name">提交申请</h3>
          <p class="step-desc">填写预约信息，提交在线申请</p>
        </div>
        <div class="step-arrow">
          <div class="arrow-line"></div>
        </div>
        <div class="step-item">
          <div class="step-icon">
            <el-icon :size="36" color="#409EFF"><Clock /></el-icon>
          </div>
          <div class="step-num">03</div>
          <h3 class="step-name">等待审批</h3>
          <p class="step-desc">管理员实时审批，进度可追踪</p>
        </div>
        <div class="step-arrow">
          <div class="arrow-line"></div>
        </div>
        <div class="step-item">
          <div class="step-icon">
            <el-icon :size="36" color="#409EFF"><Check /></el-icon>
          </div>
          <div class="step-num">04</div>
          <h3 class="step-name">预约完成</h3>
          <p class="step-desc">审批通过后按时使用课室</p>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="landing-footer">
      <p>广州软件学院 &copy; 2026 广软课室申请系统</p>
    </footer>
  </div>
</template>

<style scoped>
.landing-page {
  min-height: 100vh;
  background: #fff;
}

/* Header */
.landing-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 60px;
  transition: all 0.3s;
  background: transparent;
}

.landing-header.scrolled {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-logo-img {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: cover;
}

.header-logo .logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* Hero */
.hero-section {
  position: relative;
  height: 100vh;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1;
}

.sound-toggle {
  position: absolute;
  bottom: 24px;
  right: 24px;
  z-index: 2;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.sound-toggle:hover {
  background: rgba(255, 255, 255, 0.35);
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 0 24px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 20px;
  margin: 0 0 40px;
  opacity: 0.9;
  font-weight: 300;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.hero-actions .el-button--default {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.15);
}

.hero-actions .el-button--default:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: #fff;
  color: #fff;
}

/* Features */
.features-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
}

.section-title {
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 48px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px 24px;
  text-align: center;
  border: 1px solid #EBEEF5;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.feature-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.feature-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.6;
}

/* Steps */
.steps-section {
  background: #F5F7FA;
  padding: 80px 24px;
}

.steps-grid {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 0;
}

.step-item {
  flex: 0 0 160px;
  text-align: center;
}

.step-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.step-num {
  font-size: 12px;
  color: #C0C4CC;
  margin-bottom: 4px;
  font-weight: 600;
}

.step-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px;
}

.step-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}

.step-arrow {
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  padding-top: 36px;
}

.arrow-line {
  width: 100%;
  height: 2px;
  background: repeating-linear-gradient(
    90deg,
    #DCDFE6 0px,
    #DCDFE6 6px,
    transparent 6px,
    transparent 12px
  );
}

/* Footer */
.landing-footer {
  background: #303133;
  padding: 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.landing-footer p {
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .step-arrow {
    display: none;
  }

  .steps-grid {
    flex-wrap: wrap;
    gap: 24px;
  }
}
</style>
