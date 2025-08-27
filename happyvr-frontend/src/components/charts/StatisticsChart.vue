<template>
  <div class="statistics-chart">
    <div class="chart-container" ref="chartContainer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  type: {
    type: String,
    default: 'line' // line, bar, pie
  },
  title: {
    type: String,
    default: ''
  }
})

const chartContainer = ref(null)

// 简单的图表渲染（使用Canvas）
const renderChart = () => {
  if (!chartContainer.value || !props.data.length) return
  
  const canvas = document.createElement('canvas')
  canvas.width = 400
  canvas.height = 200
  canvas.style.width = '100%'
  canvas.style.height = '200px'
  
  const ctx = canvas.getContext('2d')
  
  // 清空容器
  chartContainer.value.innerHTML = ''
  chartContainer.value.appendChild(canvas)
  
  // 绘制简单的柱状图
  if (props.type === 'bar') {
    drawBarChart(ctx, canvas.width, canvas.height)
  } else if (props.type === 'line') {
    drawLineChart(ctx, canvas.width, canvas.height)
  }
}

const drawBarChart = (ctx, width, height) => {
  const padding = 40
  const chartWidth = width - padding * 2
  const chartHeight = height - padding * 2
  
  const maxValue = Math.max(...props.data.map(item => item.value))
  const barWidth = chartWidth / props.data.length * 0.8
  const barSpacing = chartWidth / props.data.length * 0.2
  
  // 绘制背景
  ctx.fillStyle = '#1a1a1a'
  ctx.fillRect(0, 0, width, height)
  
  // 绘制柱子
  props.data.forEach((item, index) => {
    const barHeight = (item.value / maxValue) * chartHeight
    const x = padding + index * (barWidth + barSpacing)
    const y = height - padding - barHeight
    
    // 渐变色
    const gradient = ctx.createLinearGradient(0, y, 0, y + barHeight)
    gradient.addColorStop(0, '#d4af37')
    gradient.addColorStop(1, '#b8860b')
    
    ctx.fillStyle = gradient
    ctx.fillRect(x, y, barWidth, barHeight)
    
    // 绘制数值
    ctx.fillStyle = '#ffffff'
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.fillText(item.value, x + barWidth / 2, y - 5)
    
    // 绘制标签
    ctx.fillText(item.label, x + barWidth / 2, height - 10)
  })
}

const drawLineChart = (ctx, width, height) => {
  const padding = 40
  const chartWidth = width - padding * 2
  const chartHeight = height - padding * 2
  
  const maxValue = Math.max(...props.data.map(item => item.value))
  const minValue = Math.min(...props.data.map(item => item.value))
  const valueRange = maxValue - minValue || 1
  
  // 绘制背景
  ctx.fillStyle = '#1a1a1a'
  ctx.fillRect(0, 0, width, height)
  
  // 绘制网格线
  ctx.strokeStyle = '#333333'
  ctx.lineWidth = 1
  for (let i = 0; i <= 5; i++) {
    const y = padding + (chartHeight / 5) * i
    ctx.beginPath()
    ctx.moveTo(padding, y)
    ctx.lineTo(width - padding, y)
    ctx.stroke()
  }
  
  // 绘制折线
  if (props.data.length > 1) {
    ctx.strokeStyle = '#d4af37'
    ctx.lineWidth = 2
    ctx.beginPath()
    
    props.data.forEach((item, index) => {
      const x = padding + (chartWidth / (props.data.length - 1)) * index
      const y = height - padding - ((item.value - minValue) / valueRange) * chartHeight
      
      if (index === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    
    ctx.stroke()
    
    // 绘制数据点
    props.data.forEach((item, index) => {
      const x = padding + (chartWidth / (props.data.length - 1)) * index
      const y = height - padding - ((item.value - minValue) / valueRange) * chartHeight
      
      ctx.fillStyle = '#d4af37'
      ctx.beginPath()
      ctx.arc(x, y, 4, 0, Math.PI * 2)
      ctx.fill()
      
      // 绘制数值
      ctx.fillStyle = '#ffffff'
      ctx.font = '12px Arial'
      ctx.textAlign = 'center'
      ctx.fillText(item.value, x, y - 10)
    })
  }
}

watch(() => props.data, renderChart, { deep: true })

onMounted(() => {
  renderChart()
})
</script>

<style lang="scss" scoped>
.statistics-chart {
  .chart-container {
    width: 100%;
    height: 200px;
    background: var(--bg-primary);
    border-radius: 8px;
    border: 1px solid var(--border-primary);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>