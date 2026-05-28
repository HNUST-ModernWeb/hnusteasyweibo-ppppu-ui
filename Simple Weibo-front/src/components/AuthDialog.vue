<script setup>
import { ref, watch } from 'vue'
import { login, register } from '../api'
import { useUserStore } from '../store/user'

const props = defineProps({
  visible: { type: Boolean, default: false },
})
const emit = defineEmits(['update:visible', 'success'])
const userStore = useUserStore()

const activeTab = ref('login')
const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', confirmPassword: '' })
const loading = ref(false)
const error = ref('')

watch(
  () => props.visible,
  (val) => {
    if (val) {
      activeTab.value = 'login'
      error.value = ''
      loginForm.value = { username: '', password: '' }
      registerForm.value = { username: '', password: '', confirmPassword: '' }
    }
  },
)

const close = () => emit('update:visible', false)

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    console.log('登录请求:', loginForm.value)
    const res = await login(loginForm.value)
    console.log('登录响应:', res)
    userStore.setUserInfo(res.user, res.token)
    close()
    emit('success')
  } catch (e) {
    console.error('登录错误:', e)
    error.value = e.response?.data?.message || e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    error.value = '请输入用户名和密码'
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    error.value = '两次密码不一致'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await register(registerForm.value)
    userStore.setUserInfo(res.user, res.token)
    close()
    emit('success')
  } catch (e) {
    error.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}

</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="dialog-overlay" @click.self="close">
      <div class="dialog">
        <div class="dialog-header">
          <button :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">
            登录
          </button>
          <button :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">
            注册
          </button>
          <span class="close" @click="close">&times;</span>
        </div>
        <div class="dialog-body">
          <div v-if="error" class="error">{{ error }}</div>
          <div v-show="activeTab === 'login'" class="tab-content">
            <input v-model="loginForm.username" type="text" placeholder="用户名" />
            <input v-model="loginForm.password" type="password" placeholder="密码" />
            <button class="btn-primary" @click="handleLogin" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </div>
          <div v-show="activeTab === 'register'" class="tab-content">
            <input v-model="registerForm.username" type="text" placeholder="用户名" />
            <input v-model="registerForm.password" type="password" placeholder="密码" />
            <input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" />
            <button class="btn-primary" @click="handleRegister" :disabled="loading">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog {
  background: #fff;
  border-radius: 8px;
  width: 380px;
  max-width: 90%;
}
.dialog-header {
  display: flex;
  padding: 0 16px;
  border-bottom: 1px solid #eee;
}
.dialog-header button {
  padding: 12px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  position: relative;
}
.dialog-header button.active {
  color: #1890ff;
  font-weight: bold;
}
.dialog-header button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #1890ff;
}
.close {
  margin-left: auto;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  line-height: 44px;
}
.dialog-body {
  padding: 20px;
}
.error {
  color: #f56c6c;
  font-size: 12px;
  margin-bottom: 10px;
}
.tab-content input {
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
.btn-primary {
  width: 100%;
  padding: 10px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
