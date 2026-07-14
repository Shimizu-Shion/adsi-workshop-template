'use client'

import { AuthGuard } from '@/components/AuthGuard'
import { useAuth } from '@/hooks/useAuth'

export default function DashboardPage() {
  const { employee } = useAuth()

  return (
    <AuthGuard>
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-4">ダッシュボード</h2>
        <p className="text-gray-600">
          {employee?.name} さん、こんにちは。
        </p>
      </div>
    </AuthGuard>
  )
}
