import request from '@/utils/request'

// 上传图片文件
export function uploadImages(files, onProgress) {
  // 默认调用真实后端上传接口，以便文件被保存并返回后端访问 URL。
  // 仅在开发环境并且显式设置 VITE_USE_MOCK_UPLOADS=true 时才返回 mock 数据。
  if (import.meta.env.DEV && import.meta.env.VITE_USE_MOCK_UPLOADS === 'true') {
    return new Promise((resolve) => {
      let progress = 0
      const interval = setInterval(() => {
        progress += 10
        if (onProgress) onProgress(progress)
        if (progress >= 100) {
          clearInterval(interval)
          const mockFiles = Array.isArray(files) ? files : [files]
          resolve({
            code: 200,
            success: true,
            data: {
              files: mockFiles.map((file, index) => ({
                id: Date.now() + index,
                name: file.name,
                size: file.size,
                url: URL.createObjectURL(file),
                status: 'success',
                uid: Date.now() + index
              }))
            }
          })
        }
      }, 200)
    })
  }

  const formData = new FormData()

  // 添加多个文件
  if (Array.isArray(files)) {
    files.forEach(file => {
      formData.append('files', file)
    })
  } else {
    formData.append('files', files)
  }

  return request({
    url: '/upload/images',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress) {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )
        onProgress(percentCompleted)
      }
    }
  })
}

// 上传单个图片
export function uploadImage(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress) {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )
        onProgress(percentCompleted)
      }
    }
  })
}

// 上传媒体文件
export function uploadMedia(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/upload/media',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress) {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )
        onProgress(percentCompleted)
      }
    }
  })
}

// 获取上传进度
export function getUploadProgress(uploadId) {
  return request({
    url: `/upload/progress/${uploadId}`,
    method: 'get'
  })
}

// 取消上传
export function cancelUpload(uploadId) {
  return request({
    url: `/upload/cancel/${uploadId}`,
    method: 'post'
  })
}

// 删除上传的文件
export function deleteUploadedFile(fileId) {
  return request({
    url: `/upload/files/${fileId}`,
    method: 'delete'
  })
}

// 获取文件信息
export function getFileInfo(fileId) {
  return request({
    url: `/upload/files/${fileId}`,
    method: 'get'
  })
}

// 批量删除文件
export function batchDeleteFiles(fileIds) {
  return request({
    url: '/upload/files/batch-delete',
    method: 'delete',
    data: { fileIds }
  })
}

// 获取用户上传统计
export function getUploadStats() {
  return request({
    url: '/upload/stats',
    method: 'get'
  })
}

// 别名导出，保持向后兼容性
export { uploadImage as uploadSingleImage }