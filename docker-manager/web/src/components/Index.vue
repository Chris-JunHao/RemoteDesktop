<template >
  <div>
    <div style="padding: 10px">
      <Button type="primary" @click="openCreateModal" style="width: 120px">新建</Button>
      <Button :disabled="editDisabled" @click="openConfigModal" style="width: 120px">容器配置信息</Button>
      <Dropdown trigger="click" @on-click="controlContainer">
        <Button type="primary" :disabled="selectDisabled" style="width: 120px">
          操作
          <Icon type="ios-arrow-down"></Icon>
        </Button>
        <DropdownMenu slot="list" style="width: 120px">
          <DropdownItem v-for="option in optionList" :key="option.id" :name="option.id" :disabled="option.disabled">{{option.name}}</DropdownItem>
        </DropdownMenu>
      </Dropdown>
      <Button type="text" @click="logout" style="float: right;">注销</Button>
      <Button @click="openLogsModal">查看操作日志</Button>
    </div>
    <Table highlight-row ref="containerTable" :columns="columns" :data="containerList" @on-current-change="selectContainer">
    </Table>

    <!-- 日志弹窗 -->
    <Modal v-model="logsModalVisible" title="操作日志" width="70%">
        <div v-if="logList.length > 0">
          <!-- 日志列表 -->
          <el-table :data="logList" style="width: 100%">
            <el-table-column label="操作人员" prop="accountName"></el-table-column>
            <el-table-column label="操作" prop="action"></el-table-column>
            <el-table-column label="时间" prop="logTime">
            </el-table-column>
          </el-table>
          <!-- 分页组件 -->
          <el-pagination
            :current-page="currentPage"
            :page-size="10"
            :total="totalLogs"
            layout="prev, pager, next, jumper"
            @current-change="handlePageChange"
            style="width: 100%; margin-top: 10px"
          ></el-pagination>
        </div>
        <div v-else>
          <p>没有日志数据。</p>
        </div>
    </Modal>
    <!-- 新建容器 Modal -->
    <Modal id="createModal" title="新建容器" v-model="createModal" :mask-closable="false" width="50%">
      <div>
        <div style="margin-bottom: 10px;">
          <span>容器名称：</span>
          <Input v-model="newContainer.name" placeholder="请输入容器名称" />
        </div>
        <div style="margin-bottom: 10px;">
          <span>访问端口：</span>
          <Input v-model="newContainer.port" placeholder="请输入远程访问端口" />
        </div>
        <div style="margin-bottom: 10px;">
          <span>操作系统：</span>
          <Select v-model="newContainer.os" placeholder="请选择操作系统" style="width: 100%;">
            <Option value="KylinOS">KylinOS（银河麒麟）</Option>
            <Option value="Ubuntu">Ubuntu</Option>
          </Select>
        </div>
        <div v-if="errorMessage" style="color: red; margin-bottom: 10px;">{{ errorMessage }}</div>
      </div>
      <div slot="footer">
        <Button type="text" @click="closeCreateModal">取消</Button>
        <Button type="primary" @click="createContainer" :loading="createModalLoading">确认</Button>
      </div>
    </Modal>
    <!-- 资源利用窗口 -->
    <Modal v-model="isResourceModalVisible" title="资源利用可视化" :mask-closable="false" width="50%">
      <div style="display: flex; flex-direction: column; width: 100%; height: 100%;">
        <!-- CPU 使用率图表 -->
        <div style="flex-grow: 1; margin-bottom: 20px;">
          <h4>容器 CPU 使用率</h4>
          <ChartComponent :data="cpuData" title="CPU Usage" />
        </div>

        <!-- 内存使用率图表 -->
        <div style="flex-grow: 1;">
          <h4>容器内存使用率</h4>
          <ChartComponent :data="memoryData" title="Memory Usage" />
        </div>
      </div>
    </Modal>
    <Modal id="configModal" title="修改配置" v-model="configModal" :mask-closable="false" width="50%">
      <Tabs :value="defaultTab" v-if="container">
        <TabPane label="卷" name="mount">
          <div>
            <Button type="primary" icon="md-add" @click="addMount"></Button>
          </div>
          <div class="table">
            <div class="flex header">
              <div class="column">文件/文件夹</div>
              <div class="column">装载路径</div>
              <div class="half-column">只读</div>
              <div class="half-column">操作</div>
            </div>
            <div class="flex body" v-for="(volume, index) in container.mountPointList" :key="index">
              <div class="column"><Input v-model="volume.source"/></div>
              <div class="column"><Input v-model="volume.target"/></div>
              <div class="half-column"><Checkbox size="large" v-model="volume.readOnly"></Checkbox></div>
              <div class="half-column"><Button type="error" size="small" icon="md-remove" @click="removeMount(index)"></Button></div>
            </div>
          </div>
        </TabPane>
        <TabPane label="端口" name="port">
          <div>
            <Button type="primary" icon="md-add" @click="addPortBinding"></Button>
          </div>
          <div class="table">
            <div class="flex header">
              <div class="column">本地端口</div>
              <div class="column">容器端口</div>
              <div class="half-column">操作</div>
            </div>
            <div class="flex body" v-for="(portBinding, index) in container.portBindingList" :key="index">
              <div class="column"><Input v-model="portBinding.hostPort"/></div>
              <div class="column"><Input v-model="portBinding.port"/></div>
              <div class="half-column"><Button type="error" size="small" icon="md-remove" @click="removePortBinding(index)"></Button></div>
            </div>
          </div>
        </TabPane>
        <TabPane label="环境" name="env">
          <div>
            <Button type="primary" icon="md-add" @click="addEnv"></Button>
          </div>
          <div class="table">
            <div class="flex header">
              <div class="column">本地端口</div>
              <div class="column">容器端口</div>
              <div class="half-column">操作</div>
            </div>
            <div class="flex body" v-for="(env, index) in container.envList" :key="index">
              <div class="column"><Input v-model="env.key"/></div>
              <div class="column"><Input v-model="env.value"/></div>
              <div class="half-column"><Button type="error" size="small" icon="md-remove" @click="removeEnv(index)"></Button></div>
            </div>
          </div>
        </TabPane>
      </Tabs>
      <div slot="footer">
        <Button type="text" @click="configModal = false">取消</Button>
        <Button type="primary" @click="saveContainer" :loading="configModalLoading">确认</Button>
      </div>
    </Modal>
  </div>
</template>


<script>
  import ChartComponent from "./ChartComponent.vue";  // 引入 ChartComponent 组件

  export default {
    name: "Index",
    components: {ChartComponent},
    data() {
      return {
        logList: [
          { label: '账户', prop: 'accountName' },
          { label: '操作', prop: 'action' },
          { label: '时间', prop: 'logTime' }
        ], // 存储日志数据
        totalLogs: 0, // 总日志数
        logsModalVisible: false, // 控制日志弹窗显示
        currentPage: 1, // 当前页，添加这个属性
        isResourceModalVisible: false, // 控制 Modal 显示与隐藏
        cpuData: {},  // 存储 CPU 使用率数据
        memoryData: {}, // 存储内存使用率数据
        createModal: false,
        createModalLoading: false,
        newContainer: {
          name: '',
          port: '',
          os: ''
        },
        errorMessage: '',
        defaultTab: "mount",
        configModal: false,
        configModalLoading: false,
        editDisabled: true,
        selectDisabled: true,
        container: null,
        optionList: [
          {
            id: 1,
            name: "启动",
            disabled: true
          },
          {
            id: 2,
            name: "停止",
            disabled: true
          },
          {
            id: 3,
            name: "重新启动",
            disabled: false
          }
        ],
        containerList: [],
        columns: [
          {
            title: "名称",
            key: "name",
            render: (h, params) => {
              const row = params.row;
              const name = row.name;
              const image = row.image;
              return h("div", [
                h('div', name),
                h('div', image)
              ]);
            }
          },
          {
            title: "状态",
            key: "state",
            render: (h, params) => {
              const row = params.row;
              const state = row.state;
              const startTime = row.startTime;
              let text;
              if (state === "运行中" && startTime) {
                let time = new Date(startTime);
                let now = new Date();
                let last = now.getTime() - time.getTime();
                text = this.parseTime(last);
              }
              return h("div", [
                h('div', state),
                h('div', text)
              ]);
            }
          },
          {
            title: "操作",
            key: "operate",
            render: (h, params) => {
              return h('div', [
                h('i-switch', {
                  props: {
                    value: params.row.state === "运行中"
                  },
                  on: {
                    'on-change': (value) => {
                      this.switchContainer(params.row, value)
                    }
                  }
                })
              ])
            }
          },
          {
            title: "远程连接",
            key: "remote-connect",
            render: (h, params) => {
              const row = params.row;
              const state = row.state;

              return h("div", [
                h("Button", {
                  props: {
                    type: "primary",
                    disabled: state !== "运行中"
                  },
                  on: {
                    click: async () => {
                      const vncPort = await this.getVncPort(row); // 动态获取 VNC 映射端口
                      if (vncPort) {
                        const vncUrl = `http://localhost:${vncPort}/vnc.html`;
                        window.open(vncUrl, "_blank"); // 打开 noVNC 客户端页面
                      }
                    }
                  }
                }, "确定")
              ]);
            }
          },
          {
            title: "资源利用",
            key: "resource",
            render: (h, params) => {
              const row = params.row;

              return h("div", [
                h("Button", {
                  props: {
                    type: "default"
                  },
                  on: {
                    click: async () => {
                      // 通过 row 获取容器 ID，调用 fetchUsageData
                      await this.fetchUsageData(row);
                      this.openResourceModal();
                    }
                  }
                }, "...")
              ]);
            }
          }
        ]
      }
    },
    methods: {

      // 打开日志弹窗
      openLogsModal() {
        this.logsModalVisible = true;
        this.fetchLogs(0, 10); // 获取日志数据，默认从第0条开始，每页10条
      },

// 获取日志数据
      fetchLogs(offset, limit) {
        this.$requests
          .get("/log/getLogs", { offset, limit })  // 传递分页参数 offset 和 limit
          .then((res) => {
            if (res.data.code === 0) {  // 假设返回的code为0表示成功
              const data = res.data.data;
              if (data && Array.isArray(data.items)) {
                this.logList = data.items;  // 获取日志列表
                this.totalLogs = data.total; // 获取日志总数
              } else {
                console.error('日志数据格式错误或没有返回日志项');
                this.logList = [];
                this.totalLogs = 0;
              }
            } else {
              this.$Message.error("获取操作日志失败");
              this.logList = [];  // 出现错误时清空日志列表
              this.totalLogs = 0;
            }
          })
          .catch((error) => {
            console.error("获取操作日志失败:", error);
            this.$Message.error("请求失败，请检查网络");
            this.logList = [];
            this.totalLogs = 0;
          });
      },

// 分页处理
      handlePageChange(page) {
        this.currentPage = page;
        const offset = (page - 1) * 10;  // 每页10条
        this.fetchLogs(offset, 10); // 根据页码和每页条数重新获取数据
      },
      // 打开 Modal
      openResourceModal() {
        this.isResourceModalVisible = true; // 打开弹窗
        this.$nextTick(() => {
          // 确保容器已经渲染完成后调整图表尺寸
          if (this.chartInstance) {
            this.chartInstance.resize();
          }
        });
      },
      // 获取 CPU 使用率数据
      fetchCpuUsage(containerName) {
        this.$requests
          .get("/container/getCpuUsage", {containerName:containerName })
          .then((res) => {
            if (res.data.code === 0) {
              this.cpuData = res.data.data; // 更新 CPU 数据
            } else {
              this.$Message.error("获取 CPU 使用率失败");
            }
          })
          .catch((error) => {
            this.$Message.error("请求失败，请检查网络");
          });
      },

// 获取内存使用率数据
      fetchMemoryUsage(containerName) {
        this.$requests
          .get("/container/getMemoryUsage", {containerName:containerName })
          .then(res => {
            if (res.data.code === 0) {
              this.memoryData = res.data.data;
            } else {
              this.$Message.error("获取内存使用率失败");
            }
          })
          .catch(err => {
            this.$Message.error("请求失败，请检查网络");
          });
      },
// 主方法，调用两个独立的方法
      fetchUsageData(row) {
        if (!row.name) {
          this.$Message.error("容器名称不存在");
          return;
        }

        this.fetchCpuUsage(row.name);
        this.fetchMemoryUsage(row.name);
      },
      openCreateModal() {
        this.createModal = true;
        this.newContainer = { name: '', port: '', os: '' }; // 清空表单
        this.errorMessage = ''; // 清空错误信息
      },
      closeCreateModal() {
        this.createModal = false;
      },
      createContainer() {
        // 验证容器名称是否为空
        if (!this.newContainer.name) {
          this.errorMessage = '请填写容器名称！';
          return;
        }

        // 验证容器名称是否已经存在
        const isNameTaken = this.containerList.some(container => container.name === this.newContainer.name);
        if (isNameTaken) {
          this.errorMessage = '容器名称已存在，请选择其他名称！';
          return;
        }

        // 验证端口是否为空
        if (!this.newContainer.port) {
          this.errorMessage = '请填写端口！';
          return;
        }

        // 验证操作系统类型是否选择
        if (!this.newContainer.os) {
          this.errorMessage = '请选择操作系统！';
          return;
        }

        // 验证容器名称是否符合规范（长度和字符要求）
        if (!/^[a-z0-9-]+$/.test(this.newContainer.name)) {
          this.errorMessage = '容器名称只能包含小写字母、数字和连字符！';
          return;
        }

        // 验证端口是否为数字
        if (isNaN(this.newContainer.port)) {
          this.errorMessage = '端口必须是数字！';
          return;
        }

        // 验证端口范围（例如，1-65535）
        if (this.newContainer.port < 1 || this.newContainer.port > 65535) {
          this.errorMessage = '端口号必须在 1 到 65535 之间！';
          return;
        }

        this.createModalLoading = true;

        // 调用后端接口
        this.$requests.post("/container/createContainer", {
          name: this.newContainer.name,
          hostport: this.newContainer.port,
          type: this.newContainer.os
        }).then(res => {
          if (res.data.code === 0) {
            this.$Message.success('容器创建成功！');
            this.createModal = false;

            // 触发父组件更新容器列表的事件
            this.$emit('container-created', this.newContainer);
          } else {
            this.errorMessage = res.data.message || '容器创建失败，请重试！';
          }
        }).catch(error => {
          this.errorMessage = '接口调用失败，请检查网络或联系管理员！';
          console.error('创建容器失败：', error);
        }).finally(() => {
          this.createModalLoading = false;
        });
      },
      // 用于获取容器对应的 VNC 端口
      getVncPort(row) {
        // 通过容器 ID 来获取 VNC 端口
        // 可以替换为实际的接口调用逻辑
        return this.$requests
          .get("/container/getVncPort", { id: row.id })
          .then(res => {
            if (res.data.code === 0) {
              return res.data.data; // 接口返回的是包含端口号的对象
            } else {
              this.$Message.error("获取 VNC 端口失败");
              return null;
            }
          })
          .catch(() => {
            this.$Message.error("请求失败，请检查网络");
            return null;
          });
      },
      getContainerStatusList() {
        this.$requests.get("/container/getContainerStatusList", {}).then(res => {
          if (res.data.code === 0) {
            this.containerList = res.data.data;
            if (this.container != null) {
              this.containerList.map(ctn => {
                if (ctn.id === this.container.id) {
                  ctn._highlight = true;
                }
                return ctn;
              })
            }
          }
        });
      },
      getContainerConfig() {
        if (this.container) {
          this.$requests.get("/container/getContainerConfig", {id: this.container.id}).then(res => {
            if (res.data.code === 0) {
              this.container = res.data.data;
            }
          });
        }
      },
      selectContainer(currentRow) {
        this.container = currentRow;
        this.selectDisabled = false;
        switch (this.container.state) {
          case "运行中": {
            this.optionList[0].disabled = true;
            this.optionList[1].disabled = false;
            this.optionList[2].disabled = false;
            this.editDisabled = true;
          }
            break;
          case "已停止": {
            this.optionList[0].disabled = false;
            this.optionList[1].disabled = true;
            this.optionList[2].disabled = true;
            this.editDisabled = false;
          }
          break;
          default: {
            this.optionList[0].disabled = true;
            this.optionList[1].disabled = true;
            this.optionList[2].disabled = true;
            this.editDisabled = true;
          }
          break;
        }
      },
      controlContainer(value) {
        switch (value) {
          case 1:
            this.startContainer(this.container.id);
            break;
          case 2:
            this.stopContainer(this.container.id);
            break;
          case 3:
            this.restartContainer(this.container.id);
            break;
        }
      },
      switchContainer(container, value) {
        if (value) {
          this.startContainer(container.id);
        } else {
          this.stopContainer(container.id);
        }
      },
      startContainer(id) {
        this.$requests.post("/container/start", {id: id}).then(res => {
          if (res.data.code === 0) {
            this.$Message.success("启动成功");
            this.getContainerStatusList();
            this.editDisabled = true;
          } else {
            this.$Message.error(res.data.msg);
          }
        })
      },
      stopContainer(id) {
        this.$requests.post("/container/stop", {id: id}).then(res => {
          if (res.data.code === 0) {
            this.$Message.success("停止成功");
            this.getContainerStatusList();
            this.editDisabled = false;
          } else {
            this.$Message.error(res.data.msg);
          }
        })
      },
      restartContainer(id) {
        this.$requests.post("/container/restart", {id: id}).then(res => {
          if (res.data.code === 0) {
            this.$Message.success("重启成功");
            this.getContainerStatusList();
          } else {
            this.$Message.error(res.data.msg);
          }
        })
      },
      openConfigModal() {
        this.defaultTab = "mount";
        this.getContainerConfig();
        this.configModal = true;
      },
      addMount() {
        if (this.container) {
          this.container.mountPointList.push({
            source: "",
            target: "",
            readOnly: false
          })
        }
      },
      removeMount(index) {
        if (this.container && index < this.container.mountPointList.length) {
          this.container.mountPointList.splice(index, 1);
        }
      },
      addPortBinding() {
        if (this.container) {
          this.container.portBindingList.push({
            type: "tcp",
            port: "",
            hostPort: ""
          })
        }
      },
      removePortBinding(index) {
        if (this.container && index < this.container.portBindingList.length) {
          this.container.portBindingList.splice(index, 1);
        }
      },
      addEnv() {
        if (this.container) {
          this.container.envList.push({
            key: "",
            value: ""
          })
        }
      },
      removeEnv(index) {
        if (this.container && index < this.container.envList.length) {
          this.container.envList.splice(index, 1);
        }
      },
      saveContainer() {
        let emptyCount = 0;
        let mountPointList = this.container.mountPointList;
        emptyCount += mountPointList.filter(mountPoint => !mountPoint.source || !mountPoint.target).length;
        let portBindingList = this.container.portBindingList;
        emptyCount += portBindingList.filter(portBinding => !portBinding.type || !portBinding.port || !portBinding.hostPort).length;
        let envList = this.container.envList;
        emptyCount += envList.filter(env => !env.key && !env.value).length;
        if (emptyCount > 0) {
          this.$Message.error("请确认各项参数不能为空");
          return;
        }
        this.configModalLoading = true;
        this.$requests.post("/container/saveContainer", this.container).then(res => {
          if (res.data.code === 0) {
            this.configModal = false;
            this.configModalLoading = false;

            setTimeout(() => {
              this.getContainerStatusList();
              this.$Modal.confirm({
                title: "提示",
                content: "修改成功，需要重启docker服务才能生效，是否现在重启？",
                loading: true,
                onOk: () => {
                  this.$requests.post("/docker/restart", {}).then(res => {
                    if (res.data.code === 0) {
                      this.$Message.success("重启成功");
                      this.$Modal.remove();
                    } else {
                      this.$Message.error(res.data.msg);
                    }
                  })
                }
              });
            }, 300);
          } else {
            this.$Message.error(res.data.msg);
            setTimeout(() => {
              this.configModalLoading = false;
            }, 300);
          }
        })
      },
      logout() {
        this.$Message.success("注销成功");
        localStorage.removeItem("token");
        this.$router.replace({
          path: '/login'
        });
      },
      parseTime(last) {
        let text = "已运行 ";
        const mins = 60 * 1000;
        const hours = 60 * mins;
        const days = 24 * hours;
        if (last < mins) {
          text += " 1 分钟";
        } else if (last < hours) {
          text += parseInt(last / mins) + " 分钟";
        } else if (last < days) {
          text += parseInt(last / hours) + " 小时";
        } else {
          text += parseInt(last / days) + " 天";
        }
        return text;
      }
    },
    created() {
      this.$requests.get("docker/info", {}).then(res => {
        if (res.data.code === 0) {
          this.getContainerStatusList();
          var interval = setInterval(() => {
            this.getContainerStatusList();
          }, 3000)
        } else if (res.data.code !== 10001) {
          this.$Message.error(res.data.msg);
        }
      })
    }
  }
</script>

<style lang="less" >
  #configModal {

    .ivu-modal-body {
      height: 500px;
    }
    .table {
      width: 100%;
      overflow-y: auto;
      height: 360px;
      margin-top: 10px;

      &::-webkit-scrollbar {
        width: 9px;
        height: 9px;
      }

      &::-webkit-scrollbar-track {
        border-radius: 8px;
        background-color: #ffffff;
      }

      &::-webkit-scrollbar-thumb {
        border-radius: 8px;
        background-color: #d6d6d6;
      }

      .flex {
        display: flex;
      }
      .header {
        width: 100%;
        font-weight: bold;
        .column {
          width: 31%;
          position: relative;
          margin-right: 10px;
        }
        .half-column {
          width: 15%;
          position: relative;
          margin-right: 10px;
          text-align: center;
        }
      }
      .body {
        width: 100%;
        margin-top: 10px;
        .column {
          width: 31%;
          position: relative;
          margin: 0 10px 10px 0;
        }
        .half-column {
          width: 15%;
          position: relative;
          margin: 0 10px 10px 0;
          display: flex;
          align-items:center;
          justify-content:center;
        }
      }
    }
  }

  /* 控制Modal弹窗的最大高度 */
  .el-dialog {
    max-height: 80vh;
    overflow-y: auto;
  }

  /* 调整表格样式 */
  .log-table {
    margin-top: 10px;
    border-radius: 8px;
    overflow: hidden;
  }

  /* 表格列的样式 */
  .log-column {
    text-align: center;
    font-size: 14px;
    padding: 8px 12px;
  }

  /* 改变表格头部的背景色 */
  .el-table__header {
    background-color: #f4f4f4;
    color: #333;
    font-weight: bold;
  }

  /* 改变表格行的交替背景色 */
  .el-table__row:nth-child(even) {
    background-color: #f9f9f9;
  }

  /* 改变分页组件的样式 */
  .log-pagination {
    margin-top: 20px;
    text-align: right;
    padding: 10px;
  }

  /* 控制Modal内容的内边距 */
  .el-dialog__body {
    padding: 20px;
  }

  /* 控制分页组件的高度 */
  .el-pagination {
    width: 100%;
    margin-top: 10px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 14px;
    padding: 10px 0;
    background-color: #f9f9f9;
  }

  /* 为表格列添加鼠标悬浮效果 */
  .el-table th,
  .el-table td {
    padding: 12px;
    text-align: center;
  }

  .el-table th {
    background-color: #f2f2f2;
    color: #333;
  }

  .el-table td {
    border-bottom: 1px solid #ddd;
  }

  /* 为表格添加阴影效果 */
  .el-table {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }


</style>
