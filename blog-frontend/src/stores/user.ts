import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginResponse } from '@/types/api'
import { authApi } from '@/api/auth'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roles?.includes('ADMIN') || false)

  /**
   * 登录
   */
  async function login(username: string, password: string) {
    try {
      const res: LoginResponse = await authApi.login({ username, password })
      setToken(res.token)
      setUserInfo(res.userInfo)
      return res
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  /**
   * 注册
   */
  async function register(username: string, password: string, email: string, nickname: string) {
    try {
      const res: LoginResponse = await authApi.register({ username, password, email, nickname })
      setToken(res.token)
      setUserInfo(res.userInfo)
      return res
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }

  /**
   * 访客登录
   */
  async function guestLogin() {
    try {
      const res: LoginResponse = await authApi.guestLogin()
      setToken(res.token)
      setUserInfo(res.userInfo)
      return res
    } catch (error) {
      console.error('访客登录失败:', error)
      throw error
    }
  }

  /**
   * 退出登录
   */
  async function logout() {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      clearUserData()
    }
  }

  /**
   * 设置 Token
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   */
  function setUserInfo(user: UserInfo) {
    userInfo.value = user
    localStorage.setItem('user', JSON.stringify(user))
  }

  /**
   * 清除用户数据
   */
  function clearUserData() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  /**
   * 从 localStorage 恢复用户信息
   */
  function restoreUserInfo() {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        userInfo.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        clearUserData()
      }
    }
  }

  // 初始化时恢复用户信息
  restoreUserInfo()

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    guestLogin,
    logout,
    setToken,
    setUserInfo,
    clearUserData
  }
})
