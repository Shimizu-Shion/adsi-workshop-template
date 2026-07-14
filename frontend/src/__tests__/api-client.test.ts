import { describe, it, expect, vi, beforeEach } from 'vitest'
import { withBasePath, apiFetch, ApiError } from '@/lib/api-client'

describe('withBasePath', () => {
  beforeEach(() => {
    vi.unstubAllEnvs()
  })

  it('SageMaker環境ではbasePATHを付与する', () => {
    vi.stubEnv('NEXT_PUBLIC_SAGEMAKER', '1')
    expect(withBasePath('/api/health')).toBe(
      '/codeeditor/default/absports/3000/api/health'
    )
  })

  it('通常環境ではパスをそのまま返す', () => {
    vi.stubEnv('NEXT_PUBLIC_SAGEMAKER', '')
    expect(withBasePath('/api/health')).toBe('/api/health')
  })
})

describe('apiFetch', () => {
  beforeEach(() => {
    vi.unstubAllEnvs()
    vi.stubEnv('NEXT_PUBLIC_SAGEMAKER', '')
  })

  it('正常レスポンスをJSONとして返す', async () => {
    const mockData = { status: 'UP' }
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockData),
    })

    const result = await apiFetch('/health')
    expect(result).toEqual(mockData)
    expect(global.fetch).toHaveBeenCalledWith(
      '/api/health',
      expect.objectContaining({ credentials: 'include' })
    )
  })

  it('エラーレスポンスでApiErrorを投げる', async () => {
    const errorBody = { message: 'Not found', code: 'NOT_FOUND' }
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 404,
      json: () => Promise.resolve(errorBody),
    })

    await expect(apiFetch('/employees/999')).rejects.toThrow(ApiError)
    await expect(apiFetch('/employees/999')).rejects.toMatchObject({
      status: 404,
      body: errorBody,
    })
  })
})
