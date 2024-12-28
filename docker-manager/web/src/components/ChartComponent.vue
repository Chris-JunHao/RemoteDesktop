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
      chartInstance: null,
      previousData: null // 用来缓存上次的 data，避免重复更新
    };
  },
  watch: {
    data(newData) {
      if (JSON.stringify(newData) !== JSON.stringify(this.previousData)) {
        this.updateChart(newData);
        this.previousData = newData;
      }
    }
  },
  mounted() {
    this.chartInstance = echarts.init(this.$refs.chart);
    this.updateChart(this.data);
    window.addEventListener('resize', this.resizeChart);
  },
  methods: {
    // 将时间字符串 "hh:mm" 转换为总分钟数
    timeToMinutes(time) {
      const [hours, minutes] = time.split(':').map(num => parseInt(num, 10));
      return hours * 60 + minutes;
    },

    updateChart(data) {
      if (!this.chartInstance || !data) return;

      // 将数据转换为数组并按时间排序
      const entries = Object.entries(data); // 获取数据的键值对
      const sortedEntries = entries.sort((a, b) => this.timeToMinutes(a[0]) - this.timeToMinutes(b[0])); // 按分钟数排序

      // 分别提取排序后的时间和数据
      const xData = sortedEntries.map(entry => entry[0]); // 获取排序后的时间
      const yData = sortedEntries.map(entry => parseFloat(entry[1])); // 获取对应的占用率

      // 确保 X 轴有约 10 个标签
      const totalDataLength = xData.length;
      const interval = Math.max(1, Math.floor(totalDataLength / 10)); // 设置每隔多少个数据点显示一个标签

      // 选择显示 10 个 X 轴标签
      const reducedXData = xData.filter((_, index) => index % interval === 0);

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
          boundaryGap: true, // X轴标签位置
          data: xData, // 使用所有的 X 轴数据
          axisLabel: {
            rotate: 45,
            interval: 0, // 强制显示筛选后的所有标签
            formatter: (value, index) => {
              // 只对 interval 选择的标签显示
              return reducedXData.includes(value) ? value : '';
            }
          },
          axisTick: {
            show: false // 隐藏 X 轴上的刻度线
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '使用率',
            type: 'line',
            data: yData, // 使用所有的数据
            smooth: true, // 保持平滑的线条
            areaStyle: {},
            lineStyle: {
              width: 2
            },
            itemStyle: {
              color: '#5470C6'
            }
          }
        ],
        grid: {
          top: '20%',
          left: '10%',
          right: '10%',
          bottom: '20%' // 适当增加底部的空白
        }
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
  width: 100%; /* 确保容器宽度为100% */
  height: 500px; /* 高度可以根据需要调整 */
}

@media (max-width: 768px) {
  .chart-container {
    height: 400px; /* 在小屏幕下适当减小高度 */
  }
}
</style>
