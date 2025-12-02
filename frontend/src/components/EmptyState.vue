<template>
  <div class="empty-state">
    <el-empty :image-size="imageSize" :description="description">
      <template v-if="$slots.default">
        <slot></slot>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  imageSize: {
    type: Number,
    default: 200
  }
})
</script>

<style scoped>
.empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>
