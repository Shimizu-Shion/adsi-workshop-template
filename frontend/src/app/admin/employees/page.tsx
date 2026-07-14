'use client'

import { useCallback, useEffect, useState } from 'react'
import { api } from '@/lib/api-client'
import { AuthGuard } from '@/components/AuthGuard'
import { EmployeeTable } from '@/components/employees/EmployeeTable'
import { EmployeeForm } from '@/components/employees/EmployeeForm'
import { Button } from '@/components/ui/Button'
import type { CreateEmployeeRequest, EmployeeResponse, UpdateEmployeeRequest } from '@/lib/types'

export default function AdminEmployeesPage() {
  const [employees, setEmployees] = useState<EmployeeResponse[]>([])
  const [showForm, setShowForm] = useState(false)
  const [editingEmployee, setEditingEmployee] = useState<EmployeeResponse | null>(null)

  const fetchEmployees = useCallback(async () => {
    const data = await api.get<EmployeeResponse[]>('/employees')
    setEmployees(data)
  }, [])

  useEffect(() => {
    fetchEmployees()
  }, [fetchEmployees])

  const handleCreate = async (data: CreateEmployeeRequest) => {
    await api.post('/employees', data)
    setShowForm(false)
    await fetchEmployees()
  }

  const handleUpdate = async (data: CreateEmployeeRequest) => {
    if (!editingEmployee) return
    const updateData: UpdateEmployeeRequest = {
      name: data.name,
      email: data.email,
      role: data.role,
      departmentId: data.departmentId,
      version: 0,
    }
    await api.put(`/employees/${editingEmployee.id}`, updateData)
    setEditingEmployee(null)
    await fetchEmployees()
  }

  const handleDeactivate = async (employee: EmployeeResponse) => {
    if (!confirm(`${employee.name} を無効化しますか？`)) return
    await api.delete(`/employees/${employee.id}`)
    await fetchEmployees()
  }

  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <div>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold">社員管理</h2>
          <Button onClick={() => setShowForm(true)}>社員登録</Button>
        </div>

        {(showForm || editingEmployee) && (
          <div className="mb-6 p-4 border rounded bg-white">
            <h3 className="text-lg font-semibold mb-4">
              {editingEmployee ? '社員編集' : '社員登録'}
            </h3>
            <EmployeeForm
              employee={editingEmployee}
              onSubmit={editingEmployee ? handleUpdate : handleCreate}
              onCancel={() => { setShowForm(false); setEditingEmployee(null) }}
            />
          </div>
        )}

        <EmployeeTable
          employees={employees}
          onEdit={(emp) => setEditingEmployee(emp)}
          onDeactivate={handleDeactivate}
        />
      </div>
    </AuthGuard>
  )
}
