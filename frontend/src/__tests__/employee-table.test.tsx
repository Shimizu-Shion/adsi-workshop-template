import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { EmployeeTable } from '@/components/employees/EmployeeTable'
import type { EmployeeResponse } from '@/lib/types'

const mockEmployees: EmployeeResponse[] = [
  {
    id: 1,
    employeeCode: 'EMP001',
    name: '田中太郎',
    email: 'tanaka@example.com',
    role: 'EMPLOYEE',
    departmentId: 1,
    departmentName: '開発部',
    active: true,
    version: 0,
  },
  {
    id: 2,
    employeeCode: 'EMP002',
    name: '佐藤花子',
    email: 'sato@example.com',
    role: 'ADMIN',
    departmentId: 2,
    departmentName: '営業部',
    active: false,
    version: 1,
  },
]

describe('EmployeeTable', () => {
  it('社員一覧が正しくレンダリングされる', () => {
    render(
      <EmployeeTable employees={mockEmployees} onEdit={vi.fn()} onDeactivate={vi.fn()} />
    )

    expect(screen.getByText('田中太郎')).toBeInTheDocument()
    expect(screen.getByText('佐藤花子')).toBeInTheDocument()
    expect(screen.getByText('EMP001')).toBeInTheDocument()
    expect(screen.getByText('開発部')).toBeInTheDocument()
  })

  it('有効な社員に無効化ボタンが表示される', () => {
    render(
      <EmployeeTable employees={mockEmployees} onEdit={vi.fn()} onDeactivate={vi.fn()} />
    )

    const deactivateButtons = screen.getAllByText('無効化')
    expect(deactivateButtons).toHaveLength(1)
  })

  it('無効化ボタンクリックでコールバックが呼ばれる', () => {
    const onDeactivate = vi.fn()
    render(
      <EmployeeTable employees={mockEmployees} onEdit={vi.fn()} onDeactivate={onDeactivate} />
    )

    fireEvent.click(screen.getByText('無効化'))
    expect(onDeactivate).toHaveBeenCalledWith(mockEmployees[0])
  })

  it('編集ボタンクリックでコールバックが呼ばれる', () => {
    const onEdit = vi.fn()
    render(
      <EmployeeTable employees={mockEmployees} onEdit={onEdit} onDeactivate={vi.fn()} />
    )

    const editButtons = screen.getAllByText('編集')
    fireEvent.click(editButtons[0])
    expect(onEdit).toHaveBeenCalledWith(mockEmployees[0])
  })
})
