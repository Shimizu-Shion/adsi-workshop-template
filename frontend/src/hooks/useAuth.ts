'use client'

import { useCallback, useEffect, useState } from 'react'
import { api } from '@/lib/api-client'
import type { EmployeeResponse, LoginRequest } from '@/lib/types'

interface AuthState {
  employee: EmployeeResponse | null
  isLoading: boolean
  isAuthenticated: boolean
}

export function useAuth() {
  const [state, setState] = useState<AuthState>({
    employee: null,
    isLoading: true,
    isAuthenticated: false,
  })

  const fetchMe = useCallback(async () => {
    try {
      const employee = await api.get<EmployeeResponse>('/auth/me')
      setState({ employee, isLoading: false, isAuthenticated: true })
    } catch {
      setState({ employee: null, isLoading: false, isAuthenticated: false })
    }
  }, [])

  useEffect(() => {
    fetchMe()
  }, [fetchMe])

  const login = async (request: LoginRequest) => {
    const response = await api.post<{ employee: EmployeeResponse }>('/auth/login', request)
    setState({ employee: response.employee, isLoading: false, isAuthenticated: true })
    return response.employee
  }

  const logout = async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      setState({ employee: null, isLoading: false, isAuthenticated: false })
    }
  }

  return {
    ...state,
    login,
    logout,
    refetch: fetchMe,
  }
}
