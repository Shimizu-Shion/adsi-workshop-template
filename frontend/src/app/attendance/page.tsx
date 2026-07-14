'use client'

import { useState, useEffect, useCallback } from 'react'
import { api } from '@/lib/api-client'
import type { AttendanceRecordResponse, MonthlyAttendanceResponse } from '@/lib/types'
import { Button } from '@/components/ui/Button'

export default function AttendancePage() {
  const [records, setRecords] = useState<AttendanceRecordResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const employeeId = 1

  const fetchTodayRecords = useCallback(async () => {
    const today = new Date()
    const yearMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`
    try {
      const data = await api.get<MonthlyAttendanceResponse>(
        `/attendance/records?employeeId=${employeeId}&yearMonth=${yearMonth}`
      )
      const todayStr = today.toISOString().split('T')[0]
      const todayEntry = data.records.find(r => r.date === todayStr)
      setRecords(todayEntry?.records ?? [])
    } catch {
      setRecords([])
    }
  }, [])

  useEffect(() => {
    fetchTodayRecords()
  }, [fetchTodayRecords])

  const handleClockIn = async () => {
    setLoading(true)
    setMessage('')
    try {
      await api.post<AttendanceRecordResponse>(
        `/attendance/clock-in?employeeId=${employeeId}`,
        {}
      )
      setMessage('出勤を記録しました')
      await fetchTodayRecords()
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : '打刻に失敗しました'
      setMessage(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleClockOut = async () => {
    setLoading(true)
    setMessage('')
    try {
      await api.post<AttendanceRecordResponse>(
        `/attendance/clock-out?employeeId=${employeeId}`,
        {}
      )
      setMessage('退勤を記録しました')
      await fetchTodayRecords()
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : '打刻に失敗しました'
      setMessage(msg)
    } finally {
      setLoading(false)
    }
  }

  const hasOpenRecord = records.some(r => r.clockOut === null)

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900 mb-6">打刻</h2>

      <div className="flex gap-4 mb-6">
        <Button onClick={handleClockIn} disabled={loading || hasOpenRecord}>
          出勤
        </Button>
        <Button onClick={handleClockOut} disabled={loading || !hasOpenRecord}>
          退勤
        </Button>
      </div>

      {message && (
        <p className="mb-4 text-sm text-blue-700 bg-blue-50 border border-blue-200 rounded px-3 py-2">
          {message}
        </p>
      )}

      <h3 className="text-lg font-semibold text-gray-800 mb-3">本日の打刻履歴</h3>
      {records.length === 0 ? (
        <p className="text-gray-500">本日の打刻はありません。</p>
      ) : (
        <table className="w-full border-collapse border border-gray-200">
          <thead>
            <tr className="bg-gray-50">
              <th className="border border-gray-200 px-4 py-2 text-left text-sm font-medium text-gray-700">出勤</th>
              <th className="border border-gray-200 px-4 py-2 text-left text-sm font-medium text-gray-700">退勤</th>
            </tr>
          </thead>
          <tbody>
            {records.map(record => (
              <tr key={record.id}>
                <td className="border border-gray-200 px-4 py-2 text-sm">
                  {record.clockIn ? new Date(record.clockIn).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' }) : '-'}
                </td>
                <td className="border border-gray-200 px-4 py-2 text-sm">
                  {record.clockOut ? new Date(record.clockOut).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' }) : '勤務中'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
