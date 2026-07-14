'use client'

import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api } from '@/lib/api-client'
import type { EmployeeResponse, LoginRequest } from '@/lib/types'

interface AuthContextValue {
  employee: EmployeeResponse | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<EmployeeResponse>
  logout: () => Promise<void>
  refetch: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [employee, setEmployee] = useState<EmployeeResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const fetchMe = useCallback(async () => {
    try {
      const data = await api.get<EmployeeResponse>('/auth/me')
      setEmployee(data)
    } catch {
      setEmployee(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMe()
  }, [fetchMe])

  const login = async (request: LoginRequest) => {
    const response = await api.post<{ employee: EmployeeResponse }>('/auth/login', request)
    setEmployee(response.employee)
    return response.employee
  }

  const logout = async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      setEmployee(null)
    }
  }

  return (
    <AuthContext.Provider value={{
      employee,
      isLoading,
      isAuthenticated: !!employee,
      login,
      logout,
      refetch: fetchMe,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuthContext() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider')
  }
  return context
}
