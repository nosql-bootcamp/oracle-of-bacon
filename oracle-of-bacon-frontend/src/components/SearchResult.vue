<template>
  <div style="visibility: hidden" class="cytos box" ref="cytos" />
</template>

<script lang="ts">
import cytoscape from "cytoscape";

const drawChart = (
  container: HTMLElement,
  graphElements: any,
  newName: string
) => {
  cytoscape({
    container,
    layout: {
      name: "circle",
    },
    elements: graphElements,
    style: cytoscape
      .stylesheet()
      .selector("node")
      .style({
        "background-color": (node: any) => {
          if (node.data("type") === "Movie") {
            return "#617d57";
          } else if (node.data("type") === "Actor") {
            return "#30514c";
          }
          return "black";
        },
        width: (node: any) => {
          if (
            node.data("value") === "Bacon, Kevin (I)" ||
            node.data("value") === newName
          ) {
            return "200px";
          }
          return "100px";
        },
        height: (node: any) => {
          if (
            node.data("value") === "Bacon, Kevin (I)" ||
            node.data("value") === newName
          ) {
            return "200px";
          }
          return "100px";
        },
        "text-valign": "center",
        "text-halign": "center",
        "text-outline-color": (node: any) => {
          if (node.data("type") === "Movie") {
            return "#617d57";
          } else if (node.data("type") === "Actor") {
            return "#30514c";
          }
          return "black";
        },
        "text-outline-width": 10,
        color: "white",
        "text-wrap": "wrap",
        label: "data(value)",
      })
      .selector("edge")
      .style({
        "curve-style": "bezier",
        "control-point-step-size": 40,
        "target-arrow-shape": "triangle",
      }),
  });
};

import { defineComponent } from "vue";
export default defineComponent({
  name: "search-result",
  props: {
    actorName: String,
  },
  watch: {
    actorName(newName) {
      console.log("actorName changed", newName);
      if (newName) {
        (this.$refs.cytos as HTMLDivElement).style.visibility = "visible";
        fetch(`/api/bacon-to?actor=${newName}`)
          .then((response) => response.json())
          .then((bodyJson) => {
            drawChart(this.$refs.cytos as HTMLDivElement, bodyJson, newName);
          });
      } else {
        (this.$refs.cytos as HTMLDivElement).style.visibility = "hidden";
      }
    },
  },
});
</script>

<style scoped>
.cytos {
  width: 1000px;
  height: 600px;
}
</style>
