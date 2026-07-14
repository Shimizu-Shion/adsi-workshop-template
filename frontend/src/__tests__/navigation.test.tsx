import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { Navigation } from '@/components/Navigation'

vi.mock('next/link', () => ({
  default: ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  ),
}))

const mockUseAuth = vi.fn()
vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}))

describe('Navigation', () => {
  it('未認証時はログインリンクのみ表示される', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      employee: null,
      logout: vi.fn(),
    })

    render(<Navigation />)

    expect(screen.getByText('ログイン')).toBeInTheDocument()
    expect(screen.queryByText('社員管理')).not.toBeInTheDocument()
  })

  it('一般社員には社員管理リンクが表示されない', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      employee: { name: '田中太郎', role: 'EMPLOYEE' },
      logout: vi.fn(),
    })

    render(<Navigation />)

    expect(screen.getByText('ダッシュボード')).toBeInTheDocument()
    expect(screen.getByText('田中太郎')).toBeInTheDocument()
    expect(screen.queryByText('社員管理')).not.toBeInTheDocument()
  })

  it('管理者には社員管理リンクが表示される', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      employee: { name: '管理者', role: 'ADMIN' },
      logout: vi.fn(),
    })

    render(<Navigation />)

    expect(screen.getByText('社員管理')).toBeInTheDocument()
  })

  it('ログアウトボタンをクリックするとlogoutが呼ばれる', () => {
    const mockLogout = vi.fn()
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      employee: { name: 'テスト', role: 'EMPLOYEE' },
      logout: mockLogout,
    })

    render(<Navigation />)

    fireEvent.click(screen.getByText('ログアウト'))
    expect(mockLogout).toHaveBeenCalled()
  })
})
