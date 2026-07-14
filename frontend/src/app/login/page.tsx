'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { ApiError } from '@/lib/api-client'

export default function LoginPage() {
  const [employeeCode, setEmployeeCode] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { login, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.push('/')
    }
  }, [isLoading, isAuthenticated, router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      await login({ employeeCode, password })
      router.push('/')
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setError('社員コードまたはパスワードが正しくありません')
      } else if (err instanceof ApiError) {
        setError(`API Error: ${err.status} - ${JSON.stringify(err.body)}`)
      } else {
        setError(`エラー: ${err instanceof Error ? err.message : String(err)}`)
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <div className="w-full max-w-sm">
        <h2 className="text-2xl font-bold text-center mb-6">ログイン</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="employeeCode" className="block text-sm font-medium text-gray-700 mb-1">
              社員コード
            </label>
            <Input
              id="employeeCode"
              type="text"
              value={employeeCode}
              onChange={(e) => setEmployeeCode(e.target.value)}
              required
              autoComplete="username"
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
              パスワード
            </label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </div>
          {error && (
            <p className="text-sm text-red-600" role="alert">{error}</p>
          )}
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? 'ログイン中...' : 'ログイン'}
          </Button>
        </form>
      </div>
    </div>
  )
}
