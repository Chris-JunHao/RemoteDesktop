<template>
  <div class="login">
    <div class="logo"></div>
    <div class="login-con">
      <Card>
        <p slot="title">
          <span>{{title}}</span>
        </p>
        <div name="login">
          <Form class="form" ref="loginForm" :rules="loginRules" :model="loginForm" v-show="tab === 'login'">
            <FormItem prop="username">
              <Input type="text" v-model="loginForm.username" placeholder="请输入用户名">
                <Icon type="ios-person-outline" slot="prepend"></Icon>
              </Input>
            </FormItem>
            <FormItem prop="password">
              <Input type="password" v-model="loginForm.password" placeholder="请输入密码">
                <Icon type="ios-lock-outline" slot="prepend"></Icon>
              </Input>
            </FormItem>
            <FormItem>
              <Button type="primary" :loading="loginLoading" :long="true" @click="login">登录</Button>
            </FormItem>
          </Form>
        </div>
      </Card>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      title: "欢迎登录",
      tab: "login",
      // 登录信息
      loginForm: {
        username: "",
        password: "",
      },
      // 登录验证规则
      loginRules: {
        username: [
          {required: true, message: '用户名不能为空', trigger: 'blur'}
        ],
        password: [
          {required: true, message: '密码不能为空', trigger: 'blur'}
        ]
      },
      // 登录加载
      loginLoading: false,
    }
  },
  methods: {
    login() {
      this.$refs["loginForm"].validate((valid) => {
        if (valid) {
          this.loginLoading = true;
          let form = new FormData();
          form.append("username", this.loginForm.username);
          form.append("password", this.loginForm.password);
          this.$requests.post("/login", form).then(res => {
            if (res.data.code === 0) {
              this.$Message.success("登录成功");
              localStorage.setItem("token", res.data.data);
              setTimeout(() => {
                this.$router.push({name: "index"});
              }, 1000)
            } else {
              this.$Message.error(res.data.msg);
            }
            setTimeout(() => {
              this.loginLoading = false;
            }, 500)
          })
        }
      })
    },
  }
}
</script>

<style lang="less">
.login {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #6fa3f7, #2e58b1); /* 更亮的蓝色渐变背景 */
  display: flex;
  justify-content: center;
  align-items: center;

  .logo {
    position: absolute;
    left: 50%;
    top: 15%; /* 修改此值，将logo位置下调 */
    width: 120px;
    height: 120px;
    margin-left: -60px;
    background-image: url("../assets/logo.png");
    background-size: contain; /* 保证图片完整显示且适应容器 */
    background-position: center; /* 居中显示图片 */
    background-repeat: no-repeat; /* 避免重复显示 */
  }

  .login-con {
    width: 380px;
    background-color: #ffffff;
    padding: 30px;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    text-align: center;

    .form {
      .ivu-form-item {
        margin-bottom: 20px;
      }

      .ivu-input {
        border-radius: 20px;
        padding: 10px 15px;
      }

      .ivu-btn {
        border-radius: 20px;
        width: 100%;
        font-size: 16px;
        height: 40px;
      }
    }

    .ivu-tabs-bar {
      display: none;
    }

    a {
      float: right;
      font-size: 14px;
      color: #182848;
    }
  }
}
</style>
