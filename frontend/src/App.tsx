import { useEffect } from 'react';
import { Spin, message } from 'antd';
import { setTokenProvider, setUnauthorizedHandler } from './api/http';
import { AuthPage } from './pages/AuthPage';
import { HomePage } from './pages/HomePage';
import { MeetingRoomPage } from './pages/MeetingRoomPage';
import { useAuthStore } from './stores/authStore';
import { useMeetingStore } from './stores/meetingStore';

export default function App() {
  const user = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);
  const logout = useAuthStore((state) => state.logout);
  const meetingId = useMeetingStore((state) => state.meetingId);
  const starting = useMeetingStore((state) => state.starting);
  const startMeetingSession = useMeetingStore((state) => state.startMeetingSession);
  const cleanupMeeting = useMeetingStore((state) => state.cleanup);

  useEffect(() => {
    setTokenProvider(() => useAuthStore.getState().token);
    setUnauthorizedHandler(() => {
      cleanupMeeting();
      logout();
      message.warning('登录已过期，请重新登录');
    });
  }, [cleanupMeeting, logout]);

  if (!user || !token) {
    return <AuthPage />;
  }

  if (meetingId) {
    return <MeetingRoomPage />;
  }

  return (
    <Spin spinning={starting} tip="正在进入会议">
      <HomePage
        onEnterMeeting={(nextMeetingId, options) =>
          startMeetingSession({
            token,
            user,
            meetingId: nextMeetingId,
            videoOpen: options.videoOpen,
            audioOpen: options.audioOpen,
          })
        }
      />
    </Spin>
  );
}
