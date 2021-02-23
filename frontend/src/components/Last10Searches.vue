<template>
  <div class="last-10-searches">
    <h3>Last 10 searches</h3>
    <ol>
      <li v-for="search in lastSearches" :key="search">{{ search }}</li>
    </ol>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "last-10-searches",
  data() {
    fetch("/api/last-searches")
      .then((response) => response.json())
      .then((jsonBody) => {
        this.lastSearches = jsonBody;
      });
    return {
      lastSearches: [],
    };
  },
});
</script>

<style scoped>
.last-10-searches {
  padding-right: 25px;
  text-align: left;
}
</style>
