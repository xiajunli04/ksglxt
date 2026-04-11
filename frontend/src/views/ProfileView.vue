<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateProfile, updatePassword, uploadAvatar } from '@/api/user'

const userStore = useUserStore()

const activeTab = ref('info')

// Profile edit
const profileLoading = ref(false)
const profileFormRef = ref()
const profileForm = reactive({
  nickName: '',
  phone: '',
  email: '',
})

const profileRules = {
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '请输入正确的邮箱', trigger: 'blur' },
  ],
}

// Password edit
const passwordLoading = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar as string
  if (avatar) return avatar
  return ''
})

const roleMap: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
}

const initProfileForm = () => {
  if (userStore.userInfo) {
    profileForm.nickName = (userStore.userInfo.nickName as string) || ''
    profileForm.phone = (userStore.userInfo.phone as string) || ''
    profileForm.email = (userStore.userInfo.email as string) || ''
  }
}

const handleProfileSubmit = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('更新成功')
    userStore.getUserInfo()
  } catch {
    // handled
  } finally {
    profileLoading.value = false
  }
}

const handlePasswordSubmit = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch {
    // handled
  } finally {
    passwordLoading.value = false
  }
}

const handleAvatarUpload = async (options: { file: File; onSuccess: (resp: unknown) => void; onError: (err: unknown) => void }) => {
  try {
    const res = await uploadAvatar(options.file)
    options.onSuccess(res)
    ElMessage.success('头像上传成功')
    userStore.getUserInfo()
  } catch (e) {
    options.onError(e)
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type) ||
    /\.(jpg|jpeg|png)$/i.test(file.name)
  if (!isImage) {
    ElMessage.error('头像只能是 JPG/PNG 格式')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

onMounted(async () => {
  await userStore.getUserInfo()
  initProfileForm()
  const tab = new URLSearchParams(window.location.search).get('tab')
  if (tab) activeTab.value = tab
})
</script>

<template>
  <div class="profile-view">
    <el-row :gutter="20">
      <!-- Left: avatar and basic info -->
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="profile-header">
            <div class="avatar-wrap">
              <el-avatar v-if="avatarUrl" :size="80" :src="avatarUrl" />
              <el-avatar v-else :size="80">
                {{ (userStore.userInfo?.nickName as string)?.charAt(0) || 'U' }}
              </el-avatar>
              <el-upload
                :auto-upload="true"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                class="avatar-upload"
              >
                <el-button :icon="Upload" circle size="small" />
              </el-upload>
            </div>
            <h3 class="profile-name">{{ userStore.userInfo?.nickName || '用户' }}</h3>
            <p class="profile-role">{{ roleMap[userStore.userRole] || userStore.userRole }}</p>
            <el-descriptions :column="1" class="profile-desc">
              <el-descriptions-item label="用户名">{{ userStore.userInfo?.userName }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- Right: edit forms -->
      <el-col :span="16">
        <el-card shadow="hover">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="修改信息" name="info">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="80px"
                style="max-width: 500px; margin-top: 20px"
              >
                <el-form-item label="昵称" prop="nickName">
                  <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="profileLoading" @click="handleProfileSubmit">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                style="max-width: 500px; margin-top: 20px"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="passwordLoading" @click="handlePasswordSubmit">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-wrap {
  position: relative;
  margin-bottom: 16px;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: -8px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.profile-role {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

.profile-desc {
  width: 100%;
}
</style>
