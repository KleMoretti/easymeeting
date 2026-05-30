import { useEffect, useRef } from 'react';
import { AudioMutedOutlined, UserOutlined, VideoCameraOutlined } from '@ant-design/icons';

interface VideoTileProps {
  label: string;
  stream?: MediaStream;
  muted?: boolean;
  videoOpen?: boolean;
  audioOpen?: boolean;
  primary?: boolean;
}

export function VideoTile({
  label,
  stream,
  muted,
  videoOpen = true,
  audioOpen = true,
  primary,
}: VideoTileProps) {
  const ref = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    if (ref.current && stream) {
      ref.current.srcObject = stream;
    }
  }, [stream]);

  return (
    <section className={primary ? 'video-tile video-tile-primary' : 'video-tile'}>
      {stream && videoOpen ? (
        <video ref={ref} autoPlay playsInline muted={muted} />
      ) : (
        <div className="video-placeholder" aria-label={`${label} 摄像头关闭`}>
          <UserOutlined />
        </div>
      )}
      <div className="video-meta">
        <span>{label}</span>
        <span className="video-state">
          {!audioOpen && <AudioMutedOutlined aria-label="麦克风关闭" />}
          {videoOpen && <VideoCameraOutlined aria-label="摄像头开启" />}
        </span>
      </div>
    </section>
  );
}
