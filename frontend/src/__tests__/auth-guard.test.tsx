import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { AuthGuard } from '@/components/AuthGuard'

const mockPush = vi.fn()
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockUseAuth = vi.fn()
vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}))

describe('AuthGuard', () => {
  it('読み込み中は「読み込み中...」と表示される', () => {
    mockUseAuth.mockReturnValue({ isLoading: true, isAuthenticated: false, employee: null })

    render(<AuthGuard><div>子要素</div></AuthGuard>)

    expect(screen.getByText('読み込み中...')).toBeInTheDocument()
  })

  it('認証済みの場合は子要素が表示される', () => {
    mockUseAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: true,
      employee: { role: 'EMPLOYEE' },
    })

    render(<AuthGuard><div>保護されたコンテンツ</div></AuthGuard>)

    expect(screen.getByText('保護されたコンテンツ')).toBeInTheDocument()
  })

  it('未認証の場合は何も表示されない', () => {
    mockUseAuth.mockReturnValue({ isLoading: false, isAuthenticated: false, employee: null })

    const { container } = render(<AuthGuard><div>保護されたコンテンツ</div></AuthGuard>)

    expect(container).toBeEmptyDOMElement()
  })

  it('ロールが不足している場合はアクセス権限エラーが表示される', () => {
    mockUseAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: true,
      employee: { role: 'EMPLOYEE' },
    })

    render(<AuthGuard requiredRoles={['ADMIN']}><div>管理者コンテンツ</div></AuthGuard>)

    expect(screen.getByText('アクセス権限がありません')).toBeInTheDocument()
  })

  it('必要なロールを持っている場合は子要素が表示される', () => {
    mockUseAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: true,
      employee: { role: 'ADMIN' },
    })

    render(<AuthGuard requiredRoles={['ADMIN']}><div>管理者コンテンツ</div></AuthGuard>)

    expect(screen.getByText('管理者コンテンツ')).toBeInTheDocument()
  })
})
