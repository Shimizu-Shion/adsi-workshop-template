import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import LoginPage from '@/app/login/page'

const mockPush = vi.fn()
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockLogin = vi.fn()
vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => ({
    login: mockLogin,
    employee: null,
    isLoading: false,
    isAuthenticated: false,
  }),
}))

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('社員コードとパスワードの入力欄が表示される', () => {
    render(<LoginPage />)

    expect(screen.getByLabelText('社員コード')).toBeInTheDocument()
    expect(screen.getByLabelText('パスワード')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'ログイン' })).toBeInTheDocument()
  })

  it('ログイン成功時にトップページへリダイレクト', async () => {
    mockLogin.mockResolvedValue({ id: 1, name: 'テスト' })
    render(<LoginPage />)

    fireEvent.change(screen.getByLabelText('社員コード'), { target: { value: 'TEST001' } })
    fireEvent.change(screen.getByLabelText('パスワード'), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: 'ログイン' }))

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith('/')
    })
  })

  it('認証エラー時にエラーメッセージが表示される', async () => {
    const { ApiError } = await import('@/lib/api-client')
    mockLogin.mockRejectedValue(new ApiError(401, null))
    render(<LoginPage />)

    fireEvent.change(screen.getByLabelText('社員コード'), { target: { value: 'TEST001' } })
    fireEvent.change(screen.getByLabelText('パスワード'), { target: { value: 'wrong' } })
    fireEvent.click(screen.getByRole('button', { name: 'ログイン' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('社員コードまたはパスワードが正しくありません')
    })
  })
})
