import axios from 'axios'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const token = localStorage.getItem('token')
  return axios.post('/api/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  }).then(res => res.data)
}

export function downloadFile(filename: string) {
  const token = localStorage.getItem('token')
  return axios.get(`/api/file/download/${filename}`, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}
