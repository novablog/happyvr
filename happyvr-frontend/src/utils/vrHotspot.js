/**
 * VR热点类
 * 管理VR场景中的交互热点
 */
import * as THREE from 'three'

export class VRHotspot {
  constructor(data, sceneManager) {
    this.id = data.id || Math.random().toString(36).substr(2, 9)
    this.sceneManager = sceneManager
    this.data = data
    
    // 热点属性
    this.position = new THREE.Vector3(data.position.x, data.position.y, data.position.z)
    this.type = data.type || 'info'
    this.content = data.content || {}
    this.style = { ...this.getDefaultStyle(), ...data.style }
    
    // 3D对象
    this.mesh = null
    this.labelMesh = null
    this.animationMixer = null
    
    // 状态
    this.isVisible = true
    this.isHovered = false
    this.isSelected = false
    
    // 动画
    this.animations = {
      idle: null,
      hover: null,
      click: null
    }
    
    this.createMesh()
    this.setupAnimations()
  }

  /**
   * 获取默认样式
   */
  getDefaultStyle() {
    const styleMap = {
      info: {
        color: 0x4fc3f7,
        size: 2,
        opacity: 0.8,
        glowColor: 0x81d4fa,
        labelColor: '#ffffff',
        labelBackground: 'rgba(79, 195, 247, 0.9)'
      },
      media: {
        color: 0xff7043,
        size: 2.5,
        opacity: 0.8,
        glowColor: 0xffab91,
        labelColor: '#ffffff',
        labelBackground: 'rgba(255, 112, 67, 0.9)'
      },
      link: {
        color: 0x66bb6a,
        size: 2,
        opacity: 0.8,
        glowColor: 0xa5d6a7,
        labelColor: '#ffffff',
        labelBackground: 'rgba(102, 187, 106, 0.9)'
      },
      audio: {
        color: 0xffd54f,
        size: 2.2,
        opacity: 0.8,
        glowColor: 0xfff176,
        labelColor: '#333333',
        labelBackground: 'rgba(255, 213, 79, 0.9)'
      }
    }
    
    return styleMap[this.type] || styleMap.info
  }

  /**
   * 创建热点网格
   */
  createMesh() {
    // 创建热点几何体
    const geometry = new THREE.SphereGeometry(this.style.size, 16, 16)
    
    // 创建热点材质
    const material = new THREE.MeshBasicMaterial({
      color: this.style.color,
      transparent: true,
      opacity: this.style.opacity
    })

    // 创建网格
    this.mesh = new THREE.Mesh(geometry, material)
    this.mesh.position.copy(this.position)
    
    // 添加用户数据
    this.mesh.userData = {
      isHotspot: true,
      hotspotId: this.id,
      type: this.type,
      content: this.content
    }

    // 创建发光效果
    this.createGlowEffect()
    
    // 创建标签
    if (this.content.title) {
      this.createLabel()
    }
  }

  /**
   * 创建发光效果
   */
  createGlowEffect() {
    const glowGeometry = new THREE.SphereGeometry(this.style.size * 1.5, 16, 16)
    const glowMaterial = new THREE.MeshBasicMaterial({
      color: this.style.glowColor,
      transparent: true,
      opacity: 0.3,
      side: THREE.BackSide
    })
    
    const glowMesh = new THREE.Mesh(glowGeometry, glowMaterial)
    this.mesh.add(glowMesh)
    
    this.glowMesh = glowMesh
  }

  /**
   * 创建文字标签
   */
  createLabel() {
    // 创建canvas用于绘制文字
    const canvas = document.createElement('canvas')
    const context = canvas.getContext('2d')
    
    // 设置canvas大小
    canvas.width = 256
    canvas.height = 64
    
    // 绘制背景
    context.fillStyle = this.style.labelBackground
    context.fillRect(0, 0, canvas.width, canvas.height)
    
    // 绘制文字
    context.fillStyle = this.style.labelColor
    context.font = '16px Arial'
    context.textAlign = 'center'
    context.textBaseline = 'middle'
    context.fillText(this.content.title, canvas.width / 2, canvas.height / 2)
    
    // 创建纹理
    const texture = new THREE.CanvasTexture(canvas)
    texture.needsUpdate = true
    
    // 创建标签材质
    const labelMaterial = new THREE.MeshBasicMaterial({
      map: texture,
      transparent: true,
      alphaTest: 0.1
    })
    
    // 创建标签几何体
    const labelGeometry = new THREE.PlaneGeometry(8, 2)
    
    // 创建标签网格
    this.labelMesh = new THREE.Mesh(labelGeometry, labelMaterial)
    this.labelMesh.position.set(0, this.style.size + 3, 0)
    
    // 让标签始终面向相机
    this.labelMesh.lookAt(this.sceneManager.camera.position)
    
    this.mesh.add(this.labelMesh)
  }

  /**
   * 设置动画
   */
  setupAnimations() {
    // 空闲动画 - 缓慢浮动
    this.animations.idle = () => {
      const time = Date.now() * 0.001
      this.mesh.position.y = this.position.y + Math.sin(time * 2) * 0.2
      this.mesh.rotation.y += 0.01
    }
    
    // 悬停动画 - 放大和发光
    this.animations.hover = () => {
      const targetScale = this.isHovered ? 1.2 : 1.0
      this.mesh.scale.lerp(new THREE.Vector3(targetScale, targetScale, targetScale), 0.1)
      
      if (this.glowMesh) {
        this.glowMesh.material.opacity = this.isHovered ? 0.6 : 0.3
      }
    }
    
    // 点击动画 - 脉冲效果
    this.animations.click = () => {
      if (this.isSelected) {
        const time = Date.now() * 0.005
        const pulse = (Math.sin(time) + 1) * 0.5
        this.mesh.material.opacity = this.style.opacity * (0.7 + pulse * 0.3)
      }
    }
  }

  /**
   * 更新动画
   */
  update() {
    if (!this.isVisible) return
    
    // 执行动画
    if (this.animations.idle) this.animations.idle()
    if (this.animations.hover) this.animations.hover()
    if (this.animations.click) this.animations.click()
    
    // 更新标签朝向
    if (this.labelMesh && this.sceneManager.camera) {
      this.labelMesh.lookAt(this.sceneManager.camera.position)
    }
  }

  /**
   * 处理鼠标悬停
   */
  onHover(isHovered) {
    this.isHovered = isHovered
    
    if (isHovered) {
      document.body.style.cursor = 'pointer'
      this.showTooltip()
      
      // 添加悬停高亮效果
      if (this.mesh) {
        this.mesh.material.emissive.setHex(0x444444)
      }
    } else {
      document.body.style.cursor = 'default'
      this.hideTooltip()
      
      // 移除悬停高亮效果
      if (this.mesh) {
        this.mesh.material.emissive.setHex(0x000000)
      }
    }
  }

  /**
   * 设置选中状态
   */
  setSelected(selected) {
    this.isSelected = selected
    
    if (this.mesh) {
      if (selected) {
        // 添加选中边框效果
        if (!this.selectionRing) {
          this.createSelectionRing()
        }
        this.selectionRing.visible = true
      } else {
        if (this.selectionRing) {
          this.selectionRing.visible = false
        }
      }
    }
  }

  /**
   * 创建选中环
   */
  createSelectionRing() {
    const ringGeometry = new THREE.RingGeometry(this.style.size * 1.5, this.style.size * 1.8, 32)
    const ringMaterial = new THREE.MeshBasicMaterial({
      color: 0xffffff,
      transparent: true,
      opacity: 0.8,
      side: THREE.DoubleSide
    })
    
    this.selectionRing = new THREE.Mesh(ringGeometry, ringMaterial)
    this.mesh.add(this.selectionRing)
    
    // 让选中环始终面向相机
    this.selectionRing.lookAt(this.sceneManager.camera.position)
  }

  /**
   * 处理点击事件
   */
  onClick() {
    this.isSelected = !this.isSelected
    
    // 根据热点类型执行不同的操作
    switch (this.type) {
      case 'info':
        this.showInfoModal()
        break
      case 'media':
        this.showMediaModal()
        break
      case 'link':
        this.openLink()
        break
      case 'audio':
        this.playAudio()
        break
    }
    
    // 触发点击事件
    this.sceneManager.emit('hotspotClick', this)
  }

  /**
   * 显示提示框
   */
  showTooltip() {
    if (!this.content.tooltip) return
    
    // TODO: 实现提示框显示
    console.log('显示提示框:', this.content.tooltip)
  }

  /**
   * 隐藏提示框
   */
  hideTooltip() {
    // TODO: 实现提示框隐藏
  }

  /**
   * 显示信息弹窗
   */
  showInfoModal() {
    this.sceneManager.emit('showModal', {
      type: 'info',
      title: this.content.title,
      content: this.content.description,
      image: this.content.image
    })
  }

  /**
   * 显示媒体弹窗
   */
  showMediaModal() {
    this.sceneManager.emit('showModal', {
      type: 'media',
      title: this.content.title,
      mediaUrl: this.content.mediaUrl,
      mediaType: this.content.mediaType
    })
  }

  /**
   * 打开链接
   */
  openLink() {
    if (this.content.url) {
      window.open(this.content.url, this.content.target || '_blank')
    }
  }

  /**
   * 播放音频
   */
  playAudio() {
    if (this.content.audioUrl) {
      this.sceneManager.emit('playAudio', {
        url: this.content.audioUrl,
        loop: this.content.loop || false,
        volume: this.content.volume || 1.0
      })
    }
  }

  /**
   * 设置位置
   */
  setPosition(x, y, z) {
    this.position.set(x, y, z)
    if (this.mesh) {
      this.mesh.position.copy(this.position)
    }
  }

  /**
   * 设置可见性
   */
  setVisible(visible) {
    this.isVisible = visible
    if (this.mesh) {
      this.mesh.visible = visible
    }
  }

  /**
   * 更新内容
   */
  updateContent(newContent) {
    this.content = { ...this.content, ...newContent }
    
    // 重新创建标签
    if (this.labelMesh && this.content.title) {
      this.mesh.remove(this.labelMesh)
      this.labelMesh.geometry.dispose()
      this.labelMesh.material.dispose()
      this.createLabel()
    }
  }

  /**
   * 更新样式
   */
  updateStyle(newStyle) {
    this.style = { ...this.style, ...newStyle }
    
    // 更新材质
    if (this.mesh) {
      this.mesh.material.color.setHex(this.style.color)
      this.mesh.material.opacity = this.style.opacity
    }
    
    // 更新发光效果
    if (this.glowMesh) {
      this.glowMesh.material.color.setHex(this.style.glowColor)
    }
  }

  /**
   * 销毁热点
   */
  destroy() {
    if (this.mesh) {
      // 移除标签
      if (this.labelMesh) {
        this.mesh.remove(this.labelMesh)
        this.labelMesh.geometry.dispose()
        this.labelMesh.material.dispose()
      }
      
      // 移除发光效果
      if (this.glowMesh) {
        this.mesh.remove(this.glowMesh)
        this.glowMesh.geometry.dispose()
        this.glowMesh.material.dispose()
      }
      
      // 清理几何体和材质
      this.mesh.geometry.dispose()
      this.mesh.material.dispose()
    }
  }
}

export default VRHotspot