import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'

/**
 * Axios 实例配置
 */
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 */
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = sessionStorage.getItem('auth_token')
    if (token && !config.headers.get('Authorization')) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器：统一处理错误
 */
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, msg, message } = response.data
    // 兼容后端返回 msg 或 message
    const errorMsg = msg || message

    // 后端返回的业务错误码（后端 SUCCESS 为 0）
    if (code !== 0) {
      console.error(`业务错误 [${code}]: ${errorMsg}`)
      
      // 40100 未授权：跳转登录页
      if (code === 40100) {
        const loggedIn = sessionStorage.getItem('auth_logged_in') === '1'
        if (loggedIn) {
          sessionStorage.removeItem('auth_logged_in')
          window.location.href = '/login'
        }
      }
      
      return Promise.reject(new Error(errorMsg || '请求失败'))
    }

    return response
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          console.error('未授权，请重新登录')
          if (sessionStorage.getItem('auth_logged_in') === '1') {
            sessionStorage.removeItem('auth_logged_in')
            window.location.href = '/login'
          }
          break
        case 403:
          console.error('拒绝访问')
          break
        case 404:
          console.error('请求资源不存在')
          break
        case 500:
          console.error('服务器错误')
          break
        default:
          console.error(`请求失败: ${error.message}`)
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接')
    } else {
      console.error('请求配置错误:', error.message)
    }
    
    return Promise.reject(error)
  }
)

/**
 * 封装请求方法
 */
class Request {
  /**
   * GET 请求
   */
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get<ApiResponse<T>>(url, config).then(res => res.data.data)
  }

  /**
   * POST 请求
   */
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.post<ApiResponse<T>>(url, data, config).then(res => res.data.data)
  }

  /**
   * PUT 请求
   */
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.put<ApiResponse<T>>(url, data, config).then(res => res.data.data)
  }

  /**
   * DELETE 请求
   */
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete<ApiResponse<T>>(url, config).then(res => res.data.data)
  }
}

export const request = new Request()
export default instance
