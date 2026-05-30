import { useState } from 'react';
import { Button, Card, Form, Input, Radio, Space, Switch, Typography, message } from 'antd';
import { LoginOutlined, LogoutOutlined, PlusOutlined, VideoCameraOutlined } from '@ant-design/icons';
import { preJoinMeeting, quickMeeting } from '../api/meeting';
import { useAuthStore } from '../stores/authStore';

interface HomePageProps {
  onEnterMeeting: (
    meetingId: string,
    options: { videoOpen: boolean; audioOpen: boolean },
  ) => Promise<void>;
}

export function HomePage({ onEnterMeeting }: HomePageProps) {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const [loading, setLoading] = useState(false);

  const enter = async (
    meetingId: string,
    options: { videoOpen?: boolean; audioOpen?: boolean } = {},
  ) => {
    await onEnterMeeting(meetingId, {
      videoOpen: options.videoOpen ?? true,
      audioOpen: options.audioOpen ?? true,
    });
  };

  const handleQuickMeeting = async (values: {
    meetingName: string;
    meetingNoType: number;
    joinType: number;
    joinPassword?: string;
    videoOpen?: boolean;
    audioOpen?: boolean;
  }) => {
    setLoading(true);
    try {
      const meetingId = await quickMeeting({
        meetingName: values.meetingName,
        meetingNoType: values.meetingNoType,
        joinType: values.joinType,
        joinPassword: values.joinPassword,
      });
      await enter(meetingId, values);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '创建会议失败');
    } finally {
      setLoading(false);
    }
  };

  const handleJoin = async (values: {
    meetingNo: string;
    nickName: string;
    password?: string;
    videoOpen?: boolean;
    audioOpen?: boolean;
  }) => {
    setLoading(true);
    try {
      const meetingId = await preJoinMeeting({
        meetingNo: values.meetingNo,
        nickName: values.nickName,
        password: values.password,
      });
      await enter(meetingId, values);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '加入会议失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="home-page">
      <header className="home-header">
        <div>
          <Typography.Title level={2}>会议工作台</Typography.Title>
          <Typography.Text type="secondary">
            {user?.nickName}，你的会议号：{user?.meetingNo}
          </Typography.Text>
        </div>
        <Button icon={<LogoutOutlined />} onClick={logout}>
          退出登录
        </Button>
      </header>

      <section className="home-grid">
        <Card title="快速发起会议" className="work-card">
          <Form
            layout="vertical"
            initialValues={{
              meetingNoType: 0,
              joinType: 0,
              videoOpen: true,
              audioOpen: true,
            }}
            onFinish={handleQuickMeeting}
          >
            <Form.Item
              label="会议名称"
              name="meetingName"
              rules={[{ required: true, message: '请输入会议名称' }]}
            >
              <Input placeholder="例如：项目同步会" maxLength={100} />
            </Form.Item>
            <Form.Item label="会议号" name="meetingNoType">
              <Radio.Group>
                <Radio value={0}>使用我的固定会议号</Radio>
                <Radio value={1}>生成临时会议号</Radio>
              </Radio.Group>
            </Form.Item>
            <Form.Item label="入会方式" name="joinType">
              <Radio.Group>
                <Radio value={0}>公开加入</Radio>
                <Radio value={1}>密码加入</Radio>
              </Radio.Group>
            </Form.Item>
            <Form.Item label="会议密码" name="joinPassword">
              <Input placeholder="公开会议可留空" maxLength={5} />
            </Form.Item>
            <Space className="media-switches">
              <Form.Item label="摄像头" name="videoOpen" valuePropName="checked">
                <Switch />
              </Form.Item>
              <Form.Item label="麦克风" name="audioOpen" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Space>
            <Button type="primary" htmlType="submit" icon={<PlusOutlined />} loading={loading}>
              创建并进入
            </Button>
          </Form>
        </Card>

        <Card title="加入会议" className="work-card">
          <Form
            layout="vertical"
            initialValues={{
              nickName: user?.nickName,
              videoOpen: true,
              audioOpen: true,
            }}
            onFinish={handleJoin}
          >
            <Form.Item
              label="会议号"
              name="meetingNo"
              rules={[{ required: true, message: '请输入会议号' }]}
            >
              <Input placeholder="输入主持人提供的会议号" />
            </Form.Item>
            <Form.Item
              label="入会昵称"
              name="nickName"
              rules={[{ required: true, message: '请输入入会昵称' }]}
            >
              <Input placeholder="会议中显示的昵称" />
            </Form.Item>
            <Form.Item label="会议密码" name="password">
              <Input placeholder="无密码可留空" />
            </Form.Item>
            <Space className="media-switches">
              <Form.Item label="摄像头" name="videoOpen" valuePropName="checked">
                <Switch />
              </Form.Item>
              <Form.Item label="麦克风" name="audioOpen" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Space>
            <Button type="primary" htmlType="submit" icon={<LoginOutlined />} loading={loading}>
              加入会议
            </Button>
          </Form>
        </Card>

        <Card className="preview-card">
          <div className="preview-icon">
            <VideoCameraOutlined />
          </div>
          <Typography.Title level={4}>首版会议能力</Typography.Title>
          <Typography.Paragraph>
            当前版本聚焦浏览器 Web 会议：WebSocket 信令、2-4 人 Mesh 音视频、
            成员状态和基础媒体控制。Electron 桌面壳后续可复用同一套页面。
          </Typography.Paragraph>
        </Card>
      </section>
    </main>
  );
}
