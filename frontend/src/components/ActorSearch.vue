<template>
  <div>
    <div class="form-container">
      <input
        placeHolder="Type Actor Name Here"
        class="actor-name"
        v-model="searchInput"
        @keypress.enter="search(searchInput)"
        @keyup="suggest(searchInput)"
      />
      <button class="search-button" @click="search(searchInput)">Search</button>
      <div v-show="searchInput" class="suggest">
        <div
          v-for="suggest in suggests"
          :key="suggest"
          class="suggest-item"
          @click="search(suggest)"
        >
          {{ suggest }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "actor-search",
  data() {
    return {
      searchInput: "",
      suggests: [],
    };
  },
  methods: {
    search(toSearch: string) {
      this.$emit("search", toSearch);
      this.searchInput = "";
    },
    suggest(toSuggest: string) {
      fetch(`/api/suggest?q=${toSuggest}`)
        .then((response) => response.json())
        .then((json) => {
          this.suggests = json;
        });
    },
  },
});
</script>

<style scoped>
body {
  box-sizing: border-box;
}

.form-container {
  width: 300px;
  margin: 5px auto;
  position: relative;
  text-align: left;
}
.actor-name {
  border: none;
  text-align: center;
  width: 220px;
  height: 30px;
  font-size: 15px;
  display: inline-block;
  padding: 0px;
  background-color: lightgray;
}
.search-button {
  border: none;
  color: #fbffc3;
  font-size: 15px;
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  display: inline-block;
  background-color: #5a847e;
}

.suggest {
  border: none;
  position: absolute;
  width: 220px;
  text-align: center;
  background-color: white;
  margin-top: 2px;
  z-index: 100;
  font-family: "Avenir", Helvetica, Arial, sans-serif;
}

.suggest-item:hover {
  background-color: #bbcba4;
  cursor: pointer;
}
</style>
