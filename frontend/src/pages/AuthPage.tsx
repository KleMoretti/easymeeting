import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, Tabs, message } from 'antd';
import { LockOutlined, MailOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons';
import { loadCheckCode, login, register } from '../api/account';
import { useAuthStore } from '../stores/authStore';
import type { CheckCodeVO } from '../types/api';

export function AuthPage() {
  const setUser = useAuthStore((state) => state.setUser);
  const [captcha, setCaptcha] = useState<CheckCodeVO>();
  const [captchaError, setCaptchaError] = useState<string>();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');

  const refreshCaptcha = async () => {
    try {
      setCaptcha(await loadCheckCode());
      setCaptchaError(undefined);
    } catch {
      setCaptcha(undefined);
      setCaptchaError('验证码加载失败，请确认后端服务可用后重试');
    }
  };

  useEffect(() => {
    void refreshCaptcha();
  }, []);

  const handleLogin = async (values: Record<string, string>) => {
    if (!captcha) {
      return;
    }
    setLoading(true);
    try {
      const user = await login({
        checkCodeKey: captcha.checkCodeKey,
        email: values.email,
        password: values.password,
        checkCode: values.checkCode,
      });
      setUser(user);
      message.success('登录成功');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '登录失败');
      await refreshCaptcha();
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (values: Record<string, string>) => {
    if (!captcha) {
      return;
    }
    setLoading(true);
    try {
      await register({
        checkCodeKey: captcha.checkCodeKey,
        email: values.email,
        nickName: values.nickName,
        password: values.password,
        checkCode: values.checkCode,
      });
      message.success('注册成功，请登录');
      setActiveTab('login');
      await refreshCaptcha();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '注册失败');
      await refreshCaptcha();
    } finally {
      setLoading(false);
    }
  };

  const captchaField = (
    <div className="captcha-row">
      <Form.Item
        name="checkCode"
        rules={[{ required: true, message: '请输入验证码' }]}
        className="captcha-input"
      >
        <Input
          prefix={<SafetyCertificateOutlined />}
          placeholder="验证码"
          autoComplete="off"
        />
      </Form.Item>
      <button className="captcha-image" type="button" onClick={refreshCaptcha}>
        {captcha?.checkCode ? (
          <img src={captcha.checkCode} alt="验证码" />
        ) : (
          <span>重试</span>
        )}
      </button>
      {captchaError && <div className="captcha-error">{captchaError}</div>}
    </div>
  );

  return (
    <main className="auth-page">
      <section className="auth-copy">
        <h1>EasyMeeting</h1>
        <p>基于 WebRTC 和 Netty WebSocket 的在线会议客户端。</p>
      </section>
      <Card className="auth-card">
        <Tabs
          activeKey={activeTab}
          onChange={(key) => {
            setActiveTab(key);
            void refreshCaptcha();
          }}
          items={[
            {
              key: 'login',
              label: '登录',
              children: (
                <Form layout="vertical" onFinish={handleLogin}>
                  <Form.Item
                    label="邮箱"
                    name="email"
                    rules={[{ required: true, type: 'email', message: '请输入正确邮箱' }]}
                  >
                    <Input prefix={<MailOutlined />} placeholder="you@example.com" />
                  </Form.Item>
                  <Form.Item
                    label="密码"
                    name="password"
                    rules={[{ required: true, message: '请输入密码' }]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                  </Form.Item>
                  {captchaField}
                  <Button type="primary" htmlType="submit" block loading={loading}>
                    登录
                  </Button>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '注册',
              children: (
                <Form layout="vertical" onFinish={handleRegister}>
                  <Form.Item
                    label="昵称"
                    name="nickName"
                    rules={[{ required: true, message: '请输入昵称' }]}
                  >
                    <Input prefix={<UserOutlined />} placeholder="会议昵称" maxLength={20} />
                  </Form.Item>
                  <Form.Item
                    label="邮箱"
                    name="email"
                    rules={[{ required: true, type: 'email', message: '请输入正确邮箱' }]}
                  >
                    <Input prefix={<MailOutlined />} placeholder="you@example.com" />
                  </Form.Item>
                  <Form.Item
                    label="密码"
                    name="password"
                    rules={[{ required: true, message: '请输入密码' }]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="最多 20 位" maxLength={20} />
                  </Form.Item>
                  {captchaField}
                  <Button type="primary" htmlType="submit" block loading={loading}>
                    注册
                  </Button>
                </Form>
              ),
            },
          ]}
        />
      </Card>
    </main>
  );
}
