'use client'

import { useState, useEffect } from 'react'
import { api } from '@/lib/api-client'
import type { MonthlyAttendanceResponse } from '@/lib/types'
import { Button } from '@/components/ui/Button'

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${h}h ${String(m).padStart(2, '0')}m`
}

function formatTime(isoString: string): string {
  return new Date(isoString).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' })
}

export default function MonthlyAttendancePage() {
  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date()
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  })
  const [data, setData] = useState<MonthlyAttendanceResponse | null>(null)
  const [loading, setLoading] = useState(false)

  const employeeId = 1

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true)
      try {
        const result = await api.get<MonthlyAttendanceResponse>(
          `/attendance/records?employeeId=${employeeId}&yearMonth=${yearMonth}`
        )
        setData(result)
      } catch {
        setData(null)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [yearMonth])

  const changeMonth = (offset: number) => {
    const [y, m] = yearMonth.split('-').map(Number)
    const d = new Date(y, m - 1 + offset, 1)
    setYearMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }

  const [year, month] = yearMonth.split('-')
  const displayMonth = `${year}年${parseInt(month)}月`

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900 mb-6">勤怠一覧</h2>

      <div className="flex items-center gap-4 mb-6">
        <Button variant="secondary" size="sm" onClick={() => changeMonth(-1)}>←</Button>
        <span className="text-xl font-semibold min-w-[120px] text-center">{displayMonth}</span>
        <Button variant="secondary" size="sm" onClick={() => changeMonth(1)}>→</Button>
      </div>

      {loading && <p className="text-gray-500">読み込み中...</p>}

      {data && !loading && (
        <>
          <div className="mb-6 grid grid-cols-2 gap-4 max-w-md">
            <div className="bg-blue-50 rounded-lg p-4">
              <p className="text-xs text-blue-600 font-medium">合計実働</p>
              <p className="text-xl font-bold text-blue-900">{formatMinutes(data.totalWorkMinutes)}</p>
            </div>
            <div className="bg-orange-50 rounded-lg p-4">
              <p className="text-xs text-orange-600 font-medium">合計残業</p>
              <p className="text-xl font-bold text-orange-900">{formatMinutes(data.totalOvertimeMinutes)}</p>
            </div>
          </div>

          {data.records.length === 0 ? (
            <p className="text-gray-500">この月の勤怠データはありません。</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b-2 border-gray-300">
                    <th className="px-3 py-3 text-left font-semibold text-gray-700">日付</th>
                    <th className="px-3 py-3 text-left font-semibold text-gray-700">出勤</th>
                    <th className="px-3 py-3 text-left font-semibold text-gray-700">退勤</th>
                    <th className="px-3 py-3 text-right font-semibold text-gray-700">実働</th>
                    <th className="px-3 py-3 text-right font-semibold text-gray-700">残業</th>
                  </tr>
                </thead>
                <tbody>
                  {data.records.map(day => (
                    <tr key={day.date} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="px-3 py-3 font-medium text-gray-900">
                        {new Date(day.date + 'T00:00:00').toLocaleDateString('ja-JP', { month: 'numeric', day: 'numeric', weekday: 'short' })}
                      </td>
                      <td className="px-3 py-3 text-gray-700">
                        {day.records.map((r, i) => (
                          <span key={i}>
                            {r.clockIn ? formatTime(r.clockIn) : '-'}
                            {i < day.records.length - 1 && <br />}
                          </span>
                        ))}
                      </td>
                      <td className="px-3 py-3 text-gray-700">
                        {day.records.map((r, i) => (
                          <span key={i}>
                            {r.clockOut ? formatTime(r.clockOut) : <span className="text-green-600 font-medium">勤務中</span>}
                            {i < day.records.length - 1 && <br />}
                          </span>
                        ))}
                      </td>
                      <td className="px-3 py-3 text-right font-medium text-gray-900">
                        {formatMinutes(day.workMinutes)}
                      </td>
                      <td className="px-3 py-3 text-right">
                        {day.overtimeMinutes > 0
                          ? <span className="text-orange-600 font-medium">{formatMinutes(day.overtimeMinutes)}</span>
                          : <span className="text-gray-400">-</span>
                        }
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  )
}
