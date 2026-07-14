import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { EmployeeForm } from '@/components/employees/EmployeeForm'

describe('EmployeeForm', () => {
  it('新規登録時は全フィールドが空で表示される', () => {
    render(<EmployeeForm onSubmit={vi.fn()} onCancel={vi.fn()} />)

    expect(screen.getByLabelText('社員コード')).toHaveValue('')
    expect(screen.getByLabelText('氏名')).toHaveValue('')
    expect(screen.getByLabelText('メールアドレス')).toHaveValue('')
    expect(screen.getByLabelText('パスワード')).toBeInTheDocument()
  })

  it('編集時は既存値が入力済みで、社員コードが無効、パスワード欄が非表示', () => {
    const employee = {
      id: 1,
      employeeCode: 'EMP001',
      name: '田中太郎',
      email: 'tanaka@example.com',
      role: 'EMPLOYEE' as const,
      departmentId: 1,
      departmentName: '開発部',
      active: true,
      version: 0,
    }

    render(<EmployeeForm employee={employee} onSubmit={vi.fn()} onCancel={vi.fn()} />)

    expect(screen.getByLabelText('社員コード')).toHaveValue('EMP001')
    expect(screen.getByLabelText('社員コード')).toBeDisabled()
    expect(screen.getByLabelText('氏名')).toHaveValue('田中太郎')
    expect(screen.queryByLabelText('パスワード')).not.toBeInTheDocument()
  })

  it('キャンセルボタンでonCancelが呼ばれる', () => {
    const onCancel = vi.fn()
    render(<EmployeeForm onSubmit={vi.fn()} onCancel={onCancel} />)

    fireEvent.click(screen.getByText('キャンセル'))
    expect(onCancel).toHaveBeenCalled()
  })

  it('送信失敗時にエラーメッセージが表示される', async () => {
    const onSubmit = vi.fn().mockRejectedValue(new Error('登録に失敗しました'))
    render(<EmployeeForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    fireEvent.change(screen.getByLabelText('社員コード'), { target: { value: 'EMP002' } })
    fireEvent.change(screen.getByLabelText('氏名'), { target: { value: '佐藤' } })
    fireEvent.change(screen.getByLabelText('メールアドレス'), { target: { value: 'sato@example.com' } })
    fireEvent.change(screen.getByLabelText('パスワード'), { target: { value: 'password123' } })
    fireEvent.click(screen.getByText('保存'))

    await waitFor(() => {
      expect(screen.getByText('登録に失敗しました')).toBeInTheDocument()
    })
  })
})
