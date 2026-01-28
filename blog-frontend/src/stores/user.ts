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

  /**
   * 判断 Token 是否过期
   */
  function isTokenExpired(tokenValue: string): boolean {
    try {
      const payload = tokenValue.split('.')[1]
      if (!payload) return true
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
      const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
      const decoded = JSON.parse(atob(padded)) as { exp?: number }
      if (!decoded.exp || typeof decoded.exp !== 'number') return true
      return Date.now() >= decoded.exp * 1000
    } catch (error) {
      return true
    }
  }

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !isTokenExpired(token.value))
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

  if (token.value && isTokenExpired(token.value)) {
    clearUserData()
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
    logout,
    setToken,
    setUserInfo,
    clearUserData
  }
})
