import request from '@/utils/request'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function downloadFile(filename: string) {
  return request.get(`/file/download/${filename}`, {
    responseType: 'blob',
  })
}
