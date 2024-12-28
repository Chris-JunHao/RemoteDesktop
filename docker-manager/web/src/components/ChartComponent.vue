<template>
  <div ref="chart" class="chart-container" style="width: 100%; height: 500px;"></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  props: {
    data: {
      type: Object,
      required: true
    },
    title: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      chartInstance: null
    };
  },
  watch: {
    data: {
      deep: true,
      handler() {
        this.updateChart();
      }
    }
  },
  mounted() {
    this.chartInstance = echarts.init(this.$refs.chart);
    this.updateChart();
    window.addEventListener('resize', this.resizeChart);
  },
  methods: {
    updateChart() {
      if (!this.chartInstance || !this.data) return;

      // 获取数据并按时间排序
      const xData = Object.keys(this.data).sort((a, b) => new Date(a) - new Date(b)); // 按时间排序
      const yData = xData.map(time => parseFloat(this.data[time])); // 根据排序后的时间获取对应的占用率

      const option = {
        title: {
          text: this.title,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const [{ name, value }] = params;
            return `${name}<br />使用率: ${value}%`;
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xData,
          axisLabel: {
            rotate: 45, // 旋转X轴标签，避免重叠
            interval: 0, // 确保每个标签都显示
            formatter: value => value // 格式化时间轴显示
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%' // 格式化为百分比
          }
        },
        series: [
          {
            name: '使用率',
            type: 'line',
            data: yData,
            smooth: true,
            areaStyle: {},
            lineStyle: {
              width: 2
            },
            itemStyle: {
              color: '#5470C6'
            }
          }
        ]
      };

      this.chartInstance.setOption(option);
    },
    resizeChart() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    }
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
    window.removeEventListener('resize', this.resizeChart);
  }
};
</script>

<style scoped>
.chart-container {
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}
</style>
