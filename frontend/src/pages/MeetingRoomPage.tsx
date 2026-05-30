import { Button, Tag, Typography, message } from 'antd';
import {
  AudioMutedOutlined,
  AudioOutlined,
  LogoutOutlined,
  VideoCameraAddOutlined,
  VideoCameraOutlined,
} from '@ant-design/icons';
import { MemberList } from '../components/MemberList';
import { VideoTile } from '../components/VideoTile';
import { useAuthStore } from '../stores/authStore';
import { useMeetingStore } from '../stores/meetingStore';

export function MeetingRoomPage() {
  const user = useAuthStore((state) => state.user);
  const {
    meetingId,
    members,
    localStream,
    remoteStreams,
    socketStatus,
    videoOpen,
    audioOpen,
    toggleAudio,
    toggleVideo,
    leaveMeeting,
  } = useMeetingStore();

  const remoteEntries = Object.entries(remoteStreams);
  const primaryRemote = remoteEntries[0];

  const handleLeave = async () => {
    try {
      await leaveMeeting();
      message.success('已退出会议');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '退出会议失败');
    }
  };

  return (
    <main className="meeting-page">
      <header className="meeting-header">
        <div>
          <Typography.Title level={3}>EasyMeeting</Typography.Title>
          <Typography.Text type="secondary">会议 ID：{meetingId}</Typography.Text>
        </div>
        <Tag color={socketStatus === 'open' ? 'green' : 'orange'}>
          WebSocket {socketStatus}
        </Tag>
      </header>

      <section className="meeting-shell">
        <section className="stage-panel">
          <div className="stage-main">
            <VideoTile
              primary
              label={
                primaryRemote
                  ? members.find((item) => item.userId === primaryRemote[0])?.nickName ??
                    '远端成员'
                  : `${user?.nickName ?? '我'}（本地）`
              }
              stream={primaryRemote ? primaryRemote[1] : localStream}
              muted={!primaryRemote}
              videoOpen={
                primaryRemote
                  ? members.find((item) => item.userId === primaryRemote[0])?.openVideo
                  : videoOpen
              }
              audioOpen={
                primaryRemote
                  ? members.find((item) => item.userId === primaryRemote[0])?.openAudio
                  : audioOpen
              }
            />
          </div>

          <div className="filmstrip">
            {primaryRemote && (
              <VideoTile
                label={`${user?.nickName ?? '我'}（本地）`}
                stream={localStream}
                muted
                videoOpen={videoOpen}
                audioOpen={audioOpen}
              />
            )}
            {remoteEntries.slice(primaryRemote ? 1 : 0).map(([userId, stream]) => {
              const member = members.find((item) => item.userId === userId);
              return (
                <VideoTile
                  key={userId}
                  label={member?.nickName ?? '远端成员'}
                  stream={stream}
                  videoOpen={member?.openVideo}
                  audioOpen={member?.openAudio}
                />
              );
            })}
          </div>

          <footer className="meeting-controls">
            <Button
              shape="circle"
              size="large"
              aria-label={audioOpen ? '关闭麦克风' : '开启麦克风'}
              icon={audioOpen ? <AudioOutlined /> : <AudioMutedOutlined />}
              onClick={() => void toggleAudio()}
            />
            <Button
              shape="circle"
              size="large"
              aria-label={videoOpen ? '关闭摄像头' : '开启摄像头'}
              icon={videoOpen ? <VideoCameraOutlined /> : <VideoCameraAddOutlined />}
              onClick={() => void toggleVideo()}
            />
            <Button
              danger
              shape="round"
              size="large"
              icon={<LogoutOutlined />}
              onClick={() => void handleLeave()}
            >
              退出会议
            </Button>
          </footer>
        </section>

        <aside className="member-panel">
          <div className="panel-title">
            <Typography.Title level={4}>成员</Typography.Title>
            <Tag>{members.length} 人</Tag>
          </div>
          <MemberList members={members} currentUserId={user?.userId} />
        </aside>
      </section>
    </main>
  );
}
