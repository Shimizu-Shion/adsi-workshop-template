'use client'

import { useCallback, useEffect, useState } from 'react'
import { api, ApiError } from '@/lib/api-client'
import { AuthGuard } from '@/components/AuthGuard'
import { EmployeeTable } from '@/components/employees/EmployeeTable'
import { EmployeeForm } from '@/components/employees/EmployeeForm'
import { Button } from '@/components/ui/Button'
import type { CreateEmployeeRequest, EmployeeResponse, UpdateEmployeeRequest } from '@/lib/types'

export default function AdminEmployeesPage() {
  const [employees, setEmployees] = useState<EmployeeResponse[]>([])
  const [formMode, setFormMode] = useState<'hidden' | 'create' | 'edit'>('hidden')
  const [editingEmployee, setEditingEmployee] = useState<EmployeeResponse | null>(null)
  const [error, setError] = useState('')

  const fetchEmployees = useCallback(async () => {
    try {
      const data = await api.get<EmployeeResponse[]>('/employees')
      setEmployees(data)
    } catch (err) {
      setError('社員一覧の取得に失敗しました')
    }
  }, [])

  useEffect(() => {
    fetchEmployees()
  }, [fetchEmployees])

  const handleCreate = async (data: CreateEmployeeRequest) => {
    setError('')
    try {
      await api.post('/employees', data)
      setFormMode('hidden')
      await fetchEmployees()
    } catch (err) {
      if (err instanceof ApiError) {
        throw new Error(err.body && typeof err.body === 'object' && 'message' in err.body
          ? (err.body as { message: string }).message
          : '社員の登録に失敗しました')
      }
      throw err
    }
  }

  const handleUpdate = async (data: CreateEmployeeRequest) => {
    if (!editingEmployee) return
    setError('')
    try {
      const updateData: UpdateEmployeeRequest = {
        name: data.name,
        email: data.email,
        role: data.role,
        departmentId: data.departmentId,
        version: editingEmployee.version,
      }
      await api.put(`/employees/${editingEmployee.id}`, updateData)
      setFormMode('hidden')
      setEditingEmployee(null)
      await fetchEmployees()
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        throw new Error('他のユーザーにより更新されました。画面を再読み込みしてください。')
      }
      if (err instanceof ApiError) {
        throw new Error(err.body && typeof err.body === 'object' && 'message' in err.body
          ? (err.body as { message: string }).message
          : '社員の更新に失敗しました')
      }
      throw err
    }
  }

  const handleDeactivate = async (employee: EmployeeResponse) => {
    if (!confirm(`${employee.name} を無効化しますか？`)) return
    setError('')
    try {
      await api.delete(`/employees/${employee.id}`)
      await fetchEmployees()
    } catch {
      setError('社員の無効化に失敗しました')
    }
  }

  const openCreateForm = () => {
    setEditingEmployee(null)
    setFormMode('create')
  }

  const openEditForm = (emp: EmployeeResponse) => {
    setEditingEmployee(emp)
    setFormMode('edit')
  }

  const closeForm = () => {
    setFormMode('hidden')
    setEditingEmployee(null)
  }

  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <div>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold">社員管理</h2>
          <Button onClick={openCreateForm}>社員登録</Button>
        </div>

        {error && (
          <p className="mb-4 text-sm text-red-600" role="alert">{error}</p>
        )}

        {formMode !== 'hidden' && (
          <div className="mb-6 p-4 border rounded bg-white">
            <h3 className="text-lg font-semibold mb-4">
              {formMode === 'edit' ? '社員編集' : '社員登録'}
            </h3>
            <EmployeeForm
              employee={editingEmployee}
              onSubmit={formMode === 'edit' ? handleUpdate : handleCreate}
              onCancel={closeForm}
            />
          </div>
        )}

        <EmployeeTable
          employees={employees}
          onEdit={openEditForm}
          onDeactivate={handleDeactivate}
        />
      </div>
    </AuthGuard>
  )
}
