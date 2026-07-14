'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import type { Role } from '@/lib/types'

interface AuthGuardProps {
  children: React.ReactNode
  requiredRoles?: Role[]
}

export function AuthGuard({ children, requiredRoles }: AuthGuardProps) {
  const { isLoading, isAuthenticated, employee } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isLoading, isAuthenticated, router])

  if (isLoading) {
    return <div className="flex items-center justify-center min-h-[200px]">読み込み中...</div>
  }

  if (!isAuthenticated) {
    return null
  }

  if (requiredRoles && employee && !requiredRoles.includes(employee.role)) {
    return (
      <div className="flex items-center justify-center min-h-[200px] text-red-600">
        アクセス権限がありません
      </div>
    )
  }

  return <>{children}</>
}
