<template>
  <el-card class="stat-card" :body-style="{ display: 'flex', alignItems: 'center', padding: '20px' }">
    <div class="stat-icon" :style="{ background: iconBackground }">
      <el-icon :size="28"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-content">
      <span class="stat-value">{{ formattedValue }}</span>
      <span class="stat-label">{{ label }}</span>
    </div>
    <div v-if="trend !== undefined" class="stat-trend" :class="trendClass">
      <el-icon><component :is="trendIcon" /></el-icon>
      <span>{{ Math.abs(trend) }}%</span>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  icon: {
    type: [String, Object],
    required: true
  },
  iconBackground: {
    type: String,
    default: 'linear-gradient(135deg, #409eff 0%, #53a8ff 100%)'
  },
  value: {
    type: [Number, String],
    required: true
  },
  label: {
    type: String,
    required: true
  },
  trend: {
    type: Number,
    default: undefined
  },
  format: {
    type: Function,
    default: null
  }
})

const formattedValue = computed(() => {
  if (props.format) {
    return props.format(props.value)
  }
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const trendClass = computed(() => ({
  'trend-up': props.trend > 0,
  'trend-down': props.trend < 0
}))

const trendIcon = computed(() => props.trend >= 0 ? ArrowUp : ArrowDown)
</script>

<style scoped>
.stat-card {
  height: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}
</style>
