'use client'

import Link from 'next/link'
import { useAuth } from '@/hooks/useAuth'

import type { Role } from '@/lib/types'

interface NavItem {
  href: string
  label: string
  roles: Role[] | null
}

const NAV_ITEMS: NavItem[] = [
  { href: '/', label: 'ダッシュボード', roles: null },
  { href: '/attendance', label: '打刻', roles: null },
  { href: '/attendance/monthly', label: '勤怠一覧', roles: null },
  { href: '/leave/request', label: '有給休暇', roles: null },
  { href: '/admin/employees', label: '社員管理', roles: ['ADMIN'] },
]

export function Navigation() {
  const { isAuthenticated, employee, logout } = useAuth()

  if (!isAuthenticated) {
    return (
      <nav className="flex gap-4">
        <Link href="/login" className="text-sm text-gray-600 hover:text-gray-900 transition-colors">
          ログイン
        </Link>
      </nav>
    )
  }

  const visibleItems = NAV_ITEMS.filter(
    (item) => !item.roles || (employee && item.roles.includes(employee.role))
  )

  return (
    <nav className="flex items-center gap-4">
      {visibleItems.map((item) => (
        <Link
          key={item.href}
          href={item.href}
          className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
        >
          {item.label}
        </Link>
      ))}
      <span className="text-sm text-gray-500">{employee?.name}</span>
      <button
        onClick={logout}
        className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
      >
        ログアウト
      </button>
    </nav>
  )
}
