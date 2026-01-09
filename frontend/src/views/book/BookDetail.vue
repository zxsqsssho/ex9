<template>
  <div class="book-detail">
    <el-card>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home/book-query' }">图书查询</el-breadcrumb-item>
        <el-breadcrumb-item>图书详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="book-info" v-if="book">
        <el-image
            :src="book.coverUrl || '/default-book.jpg'"
            class="book-cover"
            fit="contain"
        ></el-image>
        <div class="book-meta">
          <h2>{{ book.title }}</h2>
          <p><strong>作者：</strong>{{ book.author }}</p>
          <p><strong>出版社：</strong>{{ book.publisher }}</p>
          <p><strong>出版日期：</strong>{{ book.publishDate }}</p>
          <p><strong>ISBN：</strong>{{ book.isbn }}</p>
          <p><strong>分类：</strong>{{ book.category }}</p>
          <p><strong>库存：</strong>{{ book.stock > 0 ? '可借阅' : '已借出' }}</p>
          <p><strong>简介：</strong>{{ book.description }}</p>

          <el-button
              type="primary"
              @click="handleBorrow"
              v-if="book.stock > 0 && userStore.userRole !== 'ROLE_SYSTEM_ADMIN'"
          >
            立即借阅
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getBookDetail } from '@/api/book' // 假设的API

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const book = ref(null)

// 获取图书详情
onMounted(async () => {
  const { id } = route.params
  try {
    const res = await getBookDetail(id)
    book.value = res.data
  } catch (err) {
    ElMessage.error('获取图书详情失败')
  }
})

// 借阅图书
const handleBorrow = async () => {
  try {
    // 调用借阅API
    await borrowBook(book.value.id) // 假设的API
    ElMessage.success('借阅成功')
    router.push('/home/my-space')
  } catch (err) {
    ElMessage.error(err.message || '借阅失败')
  }
}
</script>

<style scoped>
.book-detail {
  padding: 20px;
}

.book-info {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

.book-cover {
  width: 200px;
  height: 300px;
  border: 1px solid #eee;
}

.book-meta {
  flex: 1;
  line-height: 1.8;
}

.book-meta h2 {
  margin-bottom: 20px;
  color: #333;
}
</style>