/**
 * VR场景管理器
 * 基于Three.js实现VR全景渲染
 */
import * as THREE from 'three'

export class VRSceneManager {
  constructor(container, options = {}) {
    this.container = container
    this.options = {
      enableControls: true,
      enableAutoRotate: false,
      autoRotateSpeed: 0.5,
      enableZoom: true,
      minDistance: 1,
      maxDistance: 100,
      enablePan: false,
      enableDamping: true,
      dampingFactor: 0.05,
      ...options
    }

    // Three.js 核心对象
    this.scene = null
    this.camera = null
    this.renderer = null
    this.controls = null
    
    // 场景对象
    this.panoramaMesh = null
    this.hotspots = []
    
    // 状态管理
    this.isInitialized = false
    this.isFullscreen = false
    this.isAutoRotating = false
    this.currentPanorama = null
    
    // 事件监听器
    this.eventListeners = new Map()
    
    // 初始化场景
    this.init()
  }

  /**
   * 初始化Three.js场景
   */
  init() {
    try {
      this.createScene()
      this.createCamera()
      this.createRenderer()
      this.createControls()
      this.setupEventListeners()
      this.startRenderLoop()
      
      this.isInitialized = true
      this.emit('initialized')
    } catch (error) {
      console.error('VR场景初始化失败:', error)
      this.emit('error', error)
    }
  }

  /**
   * 创建场景
   */
  createScene() {
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x000000)
  }

  /**
   * 创建相机
   */
  createCamera() {
    const aspect = this.container.clientWidth / this.container.clientHeight
    this.camera = new THREE.PerspectiveCamera(75, aspect, 0.1, 1000)
    this.camera.position.set(0, 0, 0)
  }

  /**
   * 创建渲染器
   */
  createRenderer() {
    this.renderer = new THREE.WebGLRenderer({ 
      antialias: true,
      alpha: false,
      powerPreference: 'high-performance'
    })
    
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.outputColorSpace = THREE.SRGBColorSpace
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping
    this.renderer.toneMappingExposure = 1
    
    this.container.appendChild(this.renderer.domElement)
  }

  /**
   * 创建控制器
   */
  createControls() {
    if (!this.options.enableControls) return

    // 使用OrbitControls进行相机控制
    import('three/examples/jsm/controls/OrbitControls.js').then(({ OrbitControls }) => {
      this.controls = new OrbitControls(this.camera, this.renderer.domElement)
      
      // 配置控制器
      this.controls.enableDamping = this.options.enableDamping
      this.controls.dampingFactor = this.options.dampingFactor
      this.controls.enableZoom = this.options.enableZoom
      this.controls.enablePan = this.options.enablePan
      this.controls.minDistance = this.options.minDistance
      this.controls.maxDistance = this.options.maxDistance
      this.controls.autoRotate = this.options.enableAutoRotate
      this.controls.autoRotateSpeed = this.options.autoRotateSpeed
      
      // 限制垂直旋转角度
      this.controls.minPolarAngle = 0
      this.controls.maxPolarAngle = Math.PI
      
      // 监听控制器变化
      this.controls.addEventListener('change', () => {
        this.emit('cameraChange', {
          position: this.camera.position.clone(),
          rotation: this.camera.rotation.clone()
        })
      })
    })
  }

  /**
   * 设置事件监听器
   */
  setupEventListeners() {
    // 窗口大小变化
    window.addEventListener('resize', this.handleResize.bind(this))
    
    // 全屏状态变化
    document.addEventListener('fullscreenchange', this.handleFullscreenChange.bind(this))
    
    // 鼠标和触摸事件
    this.renderer.domElement.addEventListener('click', this.handleClick.bind(this))
    this.renderer.domElement.addEventListener('dblclick', this.handleDoubleClick.bind(this))
  }

  /**
   * 开始渲染循环
   */
  startRenderLoop() {
    const animate = () => {
      requestAnimationFrame(animate)
      
      if (this.controls) {
        this.controls.update()
      }
      
      this.renderer.render(this.scene, this.camera)
      this.emit('render')
    }
    
    animate()
  }

  /**
   * 加载全景图片
   * @param {string} imageUrl - 图片URL
   * @param {Object} options - 加载选项
   */
  async loadPanorama(imageUrl, options = {}) {
    try {
      this.emit('loadStart', imageUrl)
      
      // 移除现有的全景图
      if (this.panoramaMesh) {
        this.scene.remove(this.panoramaMesh)
        this.panoramaMesh.geometry.dispose()
        this.panoramaMesh.material.dispose()
      }

      // 创建纹理加载器
      const loader = new THREE.TextureLoader()
      
      // 加载纹理
      const texture = await new Promise((resolve, reject) => {
        loader.load(
          imageUrl,
          resolve,
          (progress) => {
            this.emit('loadProgress', {
              loaded: progress.loaded,
              total: progress.total,
              percentage: progress.total ? (progress.loaded / progress.total) * 100 : 0
            })
          },
          reject
        )
      })

      // 配置纹理
      texture.mapping = THREE.EquirectangularReflectionMapping
      texture.colorSpace = THREE.SRGBColorSpace
      texture.flipY = false

      // 创建球体几何体
      const geometry = new THREE.SphereGeometry(500, 60, 40)
      geometry.scale(-1, 1, 1) // 翻转球体，使纹理显示在内侧

      // 创建材质
      const material = new THREE.MeshBasicMaterial({ 
        map: texture,
        side: THREE.BackSide
      })

      // 创建网格
      this.panoramaMesh = new THREE.Mesh(geometry, material)
      this.scene.add(this.panoramaMesh)

      this.currentPanorama = {
        url: imageUrl,
        texture,
        ...options
      }

      this.emit('loadComplete', this.currentPanorama)
    } catch (error) {
      console.error('加载全景图片失败:', error)
      this.emit('loadError', error)
      throw error
    }
  }

  /**
   * 添加热点
   * @param {Object} hotspotData - 热点数据
   */
  addHotspot(hotspotData) {
    const hotspot = this.createHotspot(hotspotData)
    this.hotspots.push(hotspot)
    this.scene.add(hotspot.mesh)
    this.emit('hotspotAdded', hotspot)
    return hotspot
  }

  /**
   * 创建热点对象
   * @param {Object} data - 热点数据
   */
  createHotspot(data) {
    const { position, type, content, style = {} } = data

    // 创建热点几何体
    const geometry = new THREE.SphereGeometry(2, 16, 16)
    
    // 创建热点材质
    const material = new THREE.MeshBasicMaterial({
      color: style.color || 0xffd700,
      transparent: true,
      opacity: style.opacity || 0.8
    })

    // 创建热点网格
    const mesh = new THREE.Mesh(geometry, material)
    mesh.position.set(position.x, position.y, position.z)

    // 添加用户数据
    mesh.userData = {
      isHotspot: true,
      type,
      content,
      style,
      ...data
    }

    // 创建热点对象
    const hotspot = {
      id: data.id || Math.random().toString(36).substr(2, 9),
      mesh,
      data,
      onClick: data.onClick || (() => {}),
      onHover: data.onHover || (() => {}),
      onLeave: data.onLeave || (() => {})
    }

    return hotspot
  }

  /**
   * 移除热点
   * @param {string} hotspotId - 热点ID
   */
  removeHotspot(hotspotId) {
    const index = this.hotspots.findIndex(h => h.id === hotspotId)
    if (index !== -1) {
      const hotspot = this.hotspots[index]
      this.scene.remove(hotspot.mesh)
      hotspot.mesh.geometry.dispose()
      hotspot.mesh.material.dispose()
      this.hotspots.splice(index, 1)
      this.emit('hotspotRemoved', hotspotId)
    }
  }

  /**
   * 清除所有热点
   */
  clearHotspots() {
    this.hotspots.forEach(hotspot => {
      this.scene.remove(hotspot.mesh)
      hotspot.mesh.geometry.dispose()
      hotspot.mesh.material.dispose()
    })
    this.hotspots = []
    this.emit('hotspotsCleared')
  }

  /**
   * 重置相机视角
   */
  resetView() {
    if (this.controls) {
      this.controls.reset()
      this.emit('viewReset')
    }
  }

  /**
   * 切换自动旋转
   */
  toggleAutoRotate() {
    if (this.controls) {
      this.controls.autoRotate = !this.controls.autoRotate
      this.isAutoRotating = this.controls.autoRotate
      this.emit('autoRotateToggle', this.isAutoRotating)
    }
  }

  /**
   * 设置自动旋转速度
   * @param {number} speed - 旋转速度
   */
  setAutoRotateSpeed(speed) {
    if (this.controls) {
      this.controls.autoRotateSpeed = speed
      this.emit('autoRotateSpeedChange', speed)
    }
  }

  /**
   * 进入全屏模式
   */
  async enterFullscreen() {
    try {
      if (this.container.requestFullscreen) {
        await this.container.requestFullscreen()
      } else if (this.container.webkitRequestFullscreen) {
        await this.container.webkitRequestFullscreen()
      } else if (this.container.msRequestFullscreen) {
        await this.container.msRequestFullscreen()
      }
    } catch (error) {
      console.error('进入全屏失败:', error)
      this.emit('fullscreenError', error)
    }
  }

  /**
   * 退出全屏模式
   */
  async exitFullscreen() {
    try {
      if (document.exitFullscreen) {
        await document.exitFullscreen()
      } else if (document.webkitExitFullscreen) {
        await document.webkitExitFullscreen()
      } else if (document.msExitFullscreen) {
        await document.msExitFullscreen()
      }
    } catch (error) {
      console.error('退出全屏失败:', error)
      this.emit('fullscreenError', error)
    }
  }

  /**
   * 切换全屏模式
   */
  toggleFullscreen() {
    if (this.isFullscreen) {
      this.exitFullscreen()
    } else {
      this.enterFullscreen()
    }
  }

  /**
   * 截图
   * @param {Object} options - 截图选项
   */
  takeScreenshot(options = {}) {
    const { 
      width = this.renderer.domElement.width,
      height = this.renderer.domElement.height,
      format = 'image/png',
      quality = 1
    } = options

    try {
      // 渲染当前帧
      this.renderer.render(this.scene, this.camera)
      
      // 获取canvas数据
      const dataURL = this.renderer.domElement.toDataURL(format, quality)
      
      this.emit('screenshot', {
        dataURL,
        width,
        height,
        format,
        quality
      })
      
      return dataURL
    } catch (error) {
      console.error('截图失败:', error)
      this.emit('screenshotError', error)
      throw error
    }
  }

  /**
   * 处理窗口大小变化
   */
  handleResize() {
    if (!this.camera || !this.renderer) return

    const width = this.container.clientWidth
    const height = this.container.clientHeight

    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(width, height)
    
    this.emit('resize', { width, height })
  }

  /**
   * 处理全屏状态变化
   */
  handleFullscreenChange() {
    this.isFullscreen = !!document.fullscreenElement
    this.emit('fullscreenChange', this.isFullscreen)
    
    // 全屏状态变化时调整渲染器大小
    setTimeout(() => {
      this.handleResize()
    }, 100)
  }

  /**
   * 处理点击事件
   */
  handleClick(event) {
    const mouse = new THREE.Vector2()
    const rect = this.renderer.domElement.getBoundingClientRect()
    
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

    const raycaster = new THREE.Raycaster()
    raycaster.setFromCamera(mouse, this.camera)

    // 检测热点碰撞
    const hotspotMeshes = this.hotspots.map(h => h.mesh)
    const intersects = raycaster.intersectObjects(hotspotMeshes)

    if (intersects.length > 0) {
      const clickedMesh = intersects[0].object
      const hotspot = this.hotspots.find(h => h.mesh === clickedMesh)
      
      if (hotspot) {
        hotspot.onClick(hotspot, event)
        this.emit('hotspotClick', hotspot, event)
      }
    } else {
      // 检测场景碰撞（用于添加热点）
      const sphereIntersects = raycaster.intersectObject(this.panoramaMesh)
      if (sphereIntersects.length > 0) {
        const intersectionPoint = sphereIntersects[0].point
        this.emit('sceneClick', { 
          mouse, 
          event, 
          worldPosition: intersectionPoint,
          intersectionPoint 
        })
      }
    }
  }

  /**
   * 启用热点拖拽
   * @param {Object} hotspot - 热点对象
   */
  enableHotspotDrag(hotspot) {
    if (!hotspot || !hotspot.mesh) return

    let isDragging = false
    let dragStartPosition = new THREE.Vector3()
    let dragOffset = new THREE.Vector3()

    const onMouseDown = (event) => {
      if (event.button !== 0) return // 只响应左键

      const mouse = new THREE.Vector2()
      const rect = this.renderer.domElement.getBoundingClientRect()
      
      mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
      mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

      const raycaster = new THREE.Raycaster()
      raycaster.setFromCamera(mouse, this.camera)

      const intersects = raycaster.intersectObject(hotspot.mesh)
      if (intersects.length > 0) {
        isDragging = true
        dragStartPosition.copy(intersects[0].point)
        dragOffset.subVectors(hotspot.mesh.position, intersects[0].point)
        
        // 禁用相机控制
        if (this.controls) {
          this.controls.enabled = false
        }
        
        this.emit('hotspotDragStart', hotspot)
        event.stopPropagation()
      }
    }

    const onMouseMove = (event) => {
      if (!isDragging) return

      const mouse = new THREE.Vector2()
      const rect = this.renderer.domElement.getBoundingClientRect()
      
      mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
      mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

      const raycaster = new THREE.Raycaster()
      raycaster.setFromCamera(mouse, this.camera)

      // 与球面相交来确定新位置
      const sphereIntersects = raycaster.intersectObject(this.panoramaMesh)
      if (sphereIntersects.length > 0) {
        const newPosition = sphereIntersects[0].point
        hotspot.mesh.position.copy(newPosition)
        
        // 更新热点数据
        hotspot.position = newPosition.clone()
        
        this.emit('hotspotDrag', hotspot, newPosition)
      }
    }

    const onMouseUp = (event) => {
      if (isDragging) {
        isDragging = false
        
        // 重新启用相机控制
        if (this.controls) {
          this.controls.enabled = true
        }
        
        this.emit('hotspotDragEnd', hotspot)
      }
    }

    // 添加事件监听器
    this.renderer.domElement.addEventListener('mousedown', onMouseDown)
    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)

    // 存储清理函数
    hotspot._dragCleanup = () => {
      this.renderer.domElement.removeEventListener('mousedown', onMouseDown)
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
    }
  }

  /**
   * 禁用热点拖拽
   * @param {Object} hotspot - 热点对象
   */
  disableHotspotDrag(hotspot) {
    if (hotspot && hotspot._dragCleanup) {
      hotspot._dragCleanup()
      delete hotspot._dragCleanup
    }
  }

  /**
   * 处理双击事件
   */
  handleDoubleClick(event) {
    this.emit('sceneDoubleClick', event)
  }

  /**
   * 事件发射器
   */
  emit(eventName, ...args) {
    const listeners = this.eventListeners.get(eventName) || []
    listeners.forEach(listener => {
      try {
        listener(...args)
      } catch (error) {
        console.error(`事件监听器错误 (${eventName}):`, error)
      }
    })
  }

  /**
   * 添加事件监听器
   */
  on(eventName, listener) {
    if (!this.eventListeners.has(eventName)) {
      this.eventListeners.set(eventName, [])
    }
    this.eventListeners.get(eventName).push(listener)
  }

  /**
   * 移除事件监听器
   */
  off(eventName, listener) {
    const listeners = this.eventListeners.get(eventName)
    if (listeners) {
      const index = listeners.indexOf(listener)
      if (index !== -1) {
        listeners.splice(index, 1)
      }
    }
  }

  /**
   * 销毁场景管理器
   */
  destroy() {
    // 移除事件监听器
    window.removeEventListener('resize', this.handleResize.bind(this))
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange.bind(this))
    
    // 清理热点
    this.clearHotspots()
    
    // 清理全景图
    if (this.panoramaMesh) {
      this.scene.remove(this.panoramaMesh)
      this.panoramaMesh.geometry.dispose()
      this.panoramaMesh.material.dispose()
    }
    
    // 清理控制器
    if (this.controls) {
      this.controls.dispose()
    }
    
    // 清理渲染器
    if (this.renderer) {
      this.renderer.dispose()
      if (this.renderer.domElement.parentNode) {
        this.renderer.domElement.parentNode.removeChild(this.renderer.domElement)
      }
    }
    
    // 清理场景
    if (this.scene) {
      this.scene.clear()
    }
    
    this.isInitialized = false
    this.emit('destroyed')
  }
}

export default VRSceneManager