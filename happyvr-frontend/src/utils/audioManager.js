/**
 * 音频管理器
 * 管理VR场景中的背景音乐和音效
 */

export class AudioManager {
  constructor() {
    // 音频上下文
    this.audioContext = null
    this.masterGain = null
    
    // 音频源
    this.backgroundMusic = null
    this.soundEffects = new Map()
    this.hotspotAudios = new Map()
    
    // 音量控制
    this.masterVolume = 1.0
    this.musicVolume = 0.7
    this.effectsVolume = 0.8
    
    // 状态管理
    this.isInitialized = false
    this.isMuted = false
    
    // 事件监听器
    this.eventListeners = new Map()
    
    this.init()
  }

  /**
   * 初始化音频系统
   */
  async init() {
    try {
      // 创建音频上下文
      this.audioContext = new (window.AudioContext || window.webkitAudioContext)()
      
      // 创建主音量控制节点
      this.masterGain = this.audioContext.createGain()
      this.masterGain.connect(this.audioContext.destination)
      this.masterGain.gain.value = this.masterVolume
      
      this.isInitialized = true
      this.emit('initialized')
    } catch (error) {
      console.error('音频系统初始化失败:', error)
      this.emit('error', error)
    }
  }

  /**
   * 恢复音频上下文（用户交互后）
   */
  async resumeContext() {
    if (this.audioContext && this.audioContext.state === 'suspended') {
      try {
        await this.audioContext.resume()
        this.emit('contextResumed')
      } catch (error) {
        console.error('恢复音频上下文失败:', error)
      }
    }
  }

  /**
   * 加载背景音乐
   * @param {string} url - 音乐文件URL
   * @param {Object} options - 播放选项
   */
  async loadBackgroundMusic(url, options = {}) {
    try {
      this.emit('musicLoadStart', url)
      
      // 停止当前背景音乐
      if (this.backgroundMusic) {
        this.stopBackgroundMusic()
      }

      // 创建音频元素
      const audio = new Audio(url)
      audio.loop = options.loop !== false
      audio.volume = this.musicVolume * this.masterVolume
      audio.crossOrigin = 'anonymous'

      // 等待音频加载
      await new Promise((resolve, reject) => {
        audio.addEventListener('canplaythrough', resolve, { once: true })
        audio.addEventListener('error', reject, { once: true })
        audio.load()
      })

      this.backgroundMusic = {
        audio,
        url,
        options,
        isPlaying: false,
        gainNode: null
      }

      // 如果支持Web Audio API，创建增益节点
      if (this.audioContext && this.isInitialized) {
        try {
          const source = this.audioContext.createMediaElementSource(audio)
          const gainNode = this.audioContext.createGain()
          
          source.connect(gainNode)
          gainNode.connect(this.masterGain)
          gainNode.gain.value = this.musicVolume
          
          this.backgroundMusic.gainNode = gainNode
        } catch (error) {
          console.warn('Web Audio API连接失败，使用基础音频控制:', error)
        }
      }

      this.emit('musicLoaded', this.backgroundMusic)
      
      // 如果设置了自动播放
      if (options.autoPlay) {
        this.playBackgroundMusic()
      }

    } catch (error) {
      console.error('加载背景音乐失败:', error)
      this.emit('musicLoadError', error)
      throw error
    }
  }

  /**
   * 播放背景音乐
   */
  async playBackgroundMusic() {
    if (!this.backgroundMusic) return

    try {
      await this.resumeContext()
      await this.backgroundMusic.audio.play()
      this.backgroundMusic.isPlaying = true
      this.emit('musicPlay')
    } catch (error) {
      console.error('播放背景音乐失败:', error)
      this.emit('musicPlayError', error)
    }
  }

  /**
   * 暂停背景音乐
   */
  pauseBackgroundMusic() {
    if (this.backgroundMusic && this.backgroundMusic.isPlaying) {
      this.backgroundMusic.audio.pause()
      this.backgroundMusic.isPlaying = false
      this.emit('musicPause')
    }
  }

  /**
   * 停止背景音乐
   */
  stopBackgroundMusic() {
    if (this.backgroundMusic) {
      this.backgroundMusic.audio.pause()
      this.backgroundMusic.audio.currentTime = 0
      this.backgroundMusic.isPlaying = false
      this.emit('musicStop')
    }
  }

  /**
   * 设置背景音乐音量
   * @param {number} volume - 音量 (0-1)
   */
  setMusicVolume(volume) {
    this.musicVolume = Math.max(0, Math.min(1, volume))
    
    if (this.backgroundMusic) {
      if (this.backgroundMusic.gainNode) {
        this.backgroundMusic.gainNode.gain.value = this.musicVolume
      } else {
        this.backgroundMusic.audio.volume = this.musicVolume * this.masterVolume
      }
    }
    
    this.emit('musicVolumeChange', this.musicVolume)
  }

  /**
   * 加载音效
   * @param {string} id - 音效ID
   * @param {string} url - 音效文件URL
   */
  async loadSoundEffect(id, url) {
    try {
      const audio = new Audio(url)
      audio.volume = this.effectsVolume * this.masterVolume
      audio.crossOrigin = 'anonymous'

      await new Promise((resolve, reject) => {
        audio.addEventListener('canplaythrough', resolve, { once: true })
        audio.addEventListener('error', reject, { once: true })
        audio.load()
      })

      this.soundEffects.set(id, {
        audio,
        url,
        gainNode: null
      })

      // 如果支持Web Audio API，创建增益节点
      if (this.audioContext && this.isInitialized) {
        try {
          const source = this.audioContext.createMediaElementSource(audio)
          const gainNode = this.audioContext.createGain()
          
          source.connect(gainNode)
          gainNode.connect(this.masterGain)
          gainNode.gain.value = this.effectsVolume
          
          this.soundEffects.get(id).gainNode = gainNode
        } catch (error) {
          console.warn('音效Web Audio API连接失败:', error)
        }
      }

      this.emit('effectLoaded', { id, url })
    } catch (error) {
      console.error(`加载音效失败 (${id}):`, error)
      this.emit('effectLoadError', { id, error })
      throw error
    }
  }

  /**
   * 播放音效
   * @param {string} id - 音效ID
   * @param {Object} options - 播放选项
   */
  async playSoundEffect(id, options = {}) {
    const effect = this.soundEffects.get(id)
    if (!effect) {
      console.warn(`音效不存在: ${id}`)
      return
    }

    try {
      await this.resumeContext()
      
      // 重置播放位置
      effect.audio.currentTime = 0
      
      // 设置音量
      if (options.volume !== undefined) {
        const volume = Math.max(0, Math.min(1, options.volume))
        if (effect.gainNode) {
          effect.gainNode.gain.value = volume
        } else {
          effect.audio.volume = volume * this.masterVolume
        }
      }
      
      await effect.audio.play()
      this.emit('effectPlay', { id, options })
    } catch (error) {
      console.error(`播放音效失败 (${id}):`, error)
      this.emit('effectPlayError', { id, error })
    }
  }

  /**
   * 为热点创建音频
   * @param {string} hotspotId - 热点ID
   * @param {string} url - 音频URL
   * @param {Object} options - 音频选项
   */
  async createHotspotAudio(hotspotId, url, options = {}) {
    try {
      const audio = new Audio(url)
      audio.loop = options.loop || false
      audio.volume = (options.volume || 1.0) * this.masterVolume
      audio.crossOrigin = 'anonymous'

      await new Promise((resolve, reject) => {
        audio.addEventListener('canplaythrough', resolve, { once: true })
        audio.addEventListener('error', reject, { once: true })
        audio.load()
      })

      const hotspotAudio = {
        audio,
        url,
        options,
        isPlaying: false,
        gainNode: null
      }

      // 如果支持Web Audio API，创建增益节点
      if (this.audioContext && this.isInitialized) {
        try {
          const source = this.audioContext.createMediaElementSource(audio)
          const gainNode = this.audioContext.createGain()
          
          source.connect(gainNode)
          gainNode.connect(this.masterGain)
          gainNode.gain.value = options.volume || 1.0
          
          hotspotAudio.gainNode = gainNode
        } catch (error) {
          console.warn('热点音频Web Audio API连接失败:', error)
        }
      }

      this.hotspotAudios.set(hotspotId, hotspotAudio)
      this.emit('hotspotAudioCreated', { hotspotId, url })
      
      return hotspotAudio
    } catch (error) {
      console.error(`创建热点音频失败 (${hotspotId}):`, error)
      this.emit('hotspotAudioError', { hotspotId, error })
      throw error
    }
  }

  /**
   * 播放热点音频
   * @param {string} hotspotId - 热点ID
   */
  async playHotspotAudio(hotspotId) {
    const hotspotAudio = this.hotspotAudios.get(hotspotId)
    if (!hotspotAudio) return

    try {
      await this.resumeContext()
      await hotspotAudio.audio.play()
      hotspotAudio.isPlaying = true
      this.emit('hotspotAudioPlay', hotspotId)
    } catch (error) {
      console.error(`播放热点音频失败 (${hotspotId}):`, error)
      this.emit('hotspotAudioPlayError', { hotspotId, error })
    }
  }

  /**
   * 停止热点音频
   * @param {string} hotspotId - 热点ID
   */
  stopHotspotAudio(hotspotId) {
    const hotspotAudio = this.hotspotAudios.get(hotspotId)
    if (hotspotAudio && hotspotAudio.isPlaying) {
      hotspotAudio.audio.pause()
      hotspotAudio.audio.currentTime = 0
      hotspotAudio.isPlaying = false
      this.emit('hotspotAudioStop', hotspotId)
    }
  }

  /**
   * 设置主音量
   * @param {number} volume - 音量 (0-1)
   */
  setMasterVolume(volume) {
    this.masterVolume = Math.max(0, Math.min(1, volume))
    
    if (this.masterGain) {
      this.masterGain.gain.value = this.isMuted ? 0 : this.masterVolume
    }
    
    // 更新所有音频的音量
    if (this.backgroundMusic && !this.backgroundMusic.gainNode) {
      this.backgroundMusic.audio.volume = this.musicVolume * this.masterVolume
    }
    
    this.soundEffects.forEach((effect) => {
      if (!effect.gainNode) {
        effect.audio.volume = this.effectsVolume * this.masterVolume
      }
    })
    
    this.hotspotAudios.forEach((hotspotAudio) => {
      if (!hotspotAudio.gainNode) {
        hotspotAudio.audio.volume = (hotspotAudio.options.volume || 1.0) * this.masterVolume
      }
    })
    
    this.emit('masterVolumeChange', this.masterVolume)
  }

  /**
   * 静音/取消静音
   */
  toggleMute() {
    this.isMuted = !this.isMuted
    
    if (this.masterGain) {
      this.masterGain.gain.value = this.isMuted ? 0 : this.masterVolume
    }
    
    this.emit('muteToggle', this.isMuted)
  }

  /**
   * 停止所有音频
   */
  stopAll() {
    // 停止背景音乐
    this.stopBackgroundMusic()
    
    // 停止所有热点音频
    this.hotspotAudios.forEach((hotspotAudio, hotspotId) => {
      this.stopHotspotAudio(hotspotId)
    })
    
    this.emit('allStopped')
  }

  /**
   * 暂停所有音频
   */
  pauseAll() {
    // 暂停背景音乐
    this.pauseBackgroundMusic()
    
    // 暂停所有热点音频
    this.hotspotAudios.forEach((hotspotAudio) => {
      if (hotspotAudio.isPlaying) {
        hotspotAudio.audio.pause()
        hotspotAudio.isPlaying = false
      }
    })
    
    this.emit('allPaused')
  }

  /**
   * 获取音频状态
   */
  getStatus() {
    return {
      isInitialized: this.isInitialized,
      isMuted: this.isMuted,
      masterVolume: this.masterVolume,
      musicVolume: this.musicVolume,
      effectsVolume: this.effectsVolume,
      backgroundMusic: this.backgroundMusic ? {
        url: this.backgroundMusic.url,
        isPlaying: this.backgroundMusic.isPlaying,
        currentTime: this.backgroundMusic.audio.currentTime,
        duration: this.backgroundMusic.audio.duration
      } : null,
      hotspotAudios: Array.from(this.hotspotAudios.entries()).map(([id, audio]) => ({
        id,
        url: audio.url,
        isPlaying: audio.isPlaying
      }))
    }
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
        console.error(`音频事件监听器错误 (${eventName}):`, error)
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
   * 销毁音频管理器
   */
  destroy() {
    // 停止所有音频
    this.stopAll()
    
    // 清理音频资源
    if (this.backgroundMusic) {
      this.backgroundMusic.audio.src = ''
      this.backgroundMusic.audio.load()
    }
    
    this.soundEffects.forEach((effect) => {
      effect.audio.src = ''
      effect.audio.load()
    })
    
    this.hotspotAudios.forEach((hotspotAudio) => {
      hotspotAudio.audio.src = ''
      hotspotAudio.audio.load()
    })
    
    // 关闭音频上下文
    if (this.audioContext && this.audioContext.state !== 'closed') {
      this.audioContext.close()
    }
    
    // 清理数据
    this.soundEffects.clear()
    this.hotspotAudios.clear()
    this.eventListeners.clear()
    
    this.isInitialized = false
    this.emit('destroyed')
  }
}

export default AudioManager