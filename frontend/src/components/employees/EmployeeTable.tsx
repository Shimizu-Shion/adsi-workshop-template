'use client'

import type { EmployeeResponse } from '@/lib/types'
import { Button } from '@/components/ui/Button'

interface EmployeeTableProps {
  employees: EmployeeResponse[]
  onEdit: (employee: EmployeeResponse) => void
  onDeactivate: (employee: EmployeeResponse) => void
}

export function EmployeeTable({ employees, onEdit, onDeactivate }: EmployeeTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">社員コード</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">氏名</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">メール</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ロール</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">部門</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">状態</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {employees.map((emp) => (
            <tr key={emp.id}>
              <td className="px-4 py-3 text-sm">{emp.employeeCode}</td>
              <td className="px-4 py-3 text-sm">{emp.name}</td>
              <td className="px-4 py-3 text-sm">{emp.email}</td>
              <td className="px-4 py-3 text-sm">{emp.role}</td>
              <td className="px-4 py-3 text-sm">{emp.departmentName}</td>
              <td className="px-4 py-3 text-sm">
                <span className={`px-2 py-1 rounded text-xs ${emp.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'}`}>
                  {emp.active ? '有効' : '無効'}
                </span>
              </td>
              <td className="px-4 py-3 text-sm space-x-2">
                <Button variant="secondary" size="sm" onClick={() => onEdit(emp)}>
                  編集
                </Button>
                {emp.active && (
                  <Button variant="danger" size="sm" onClick={() => onDeactivate(emp)}>
                    無効化
                  </Button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
