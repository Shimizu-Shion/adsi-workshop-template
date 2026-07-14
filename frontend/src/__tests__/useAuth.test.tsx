import { renderHook, waitFor, act } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { AuthProvider, useAuthContext } from '@/components/AuthProvider'
import type { ReactNode } from 'react'

const mockApi = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
}

vi.mock('@/lib/api-client', () => ({
  api: {
    get: (...args: unknown[]) => mockApi.get(...args),
    post: (...args: unknown[]) => mockApi.post(...args),
    put: (...args: unknown[]) => mockApi.put(...args),
    delete: (...args: unknown[]) => mockApi.delete(...args),
  },
  ApiError: class extends Error {
    status: number
    body: unknown
    constructor(status: number, body: unknown) {
      super(`API Error: ${status}`)
      this.status = status
      this.body = body
    }
  },
}))

function wrapper({ children }: { children: ReactNode }) {
  return <AuthProvider>{children}</AuthProvider>
}

describe('useAuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('初期状態はisLoading: trueで/auth/meを呼ぶ', async () => {
    mockApi.get.mockRejectedValue(new Error('401'))

    const { result } = renderHook(() => useAuthContext(), { wrapper })

    expect(result.current.isLoading).toBe(true)

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false)
    })
    expect(result.current.isAuthenticated).toBe(false)
  })

  it('/auth/meが成功すると認証済み状態になる', async () => {
    const employee = { id: 1, name: 'テスト', role: 'ADMIN' }
    mockApi.get.mockResolvedValue(employee)

    const { result } = renderHook(() => useAuthContext(), { wrapper })

    await waitFor(() => {
      expect(result.current.isAuthenticated).toBe(true)
    })
    expect(result.current.employee).toEqual(employee)
  })

  it('loginが成功すると認証済み状態になる', async () => {
    mockApi.get.mockRejectedValue(new Error('401'))
    const employee = { id: 1, name: 'テスト', role: 'ADMIN' }
    mockApi.post.mockResolvedValue({ employee })

    const { result } = renderHook(() => useAuthContext(), { wrapper })

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false)
    })

    await act(async () => {
      await result.current.login({ employeeCode: 'admin', password: 'password' })
    })

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.employee).toEqual(employee)
  })

  it('logoutすると未認証状態になる', async () => {
    const employee = { id: 1, name: 'テスト', role: 'ADMIN' }
    mockApi.get.mockResolvedValue(employee)
    mockApi.post.mockResolvedValue(undefined)

    const { result } = renderHook(() => useAuthContext(), { wrapper })

    await waitFor(() => {
      expect(result.current.isAuthenticated).toBe(true)
    })

    await act(async () => {
      await result.current.logout()
    })

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.employee).toBeNull()
  })
})
