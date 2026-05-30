import { Avatar, List, Tag } from 'antd';
import { AudioMutedOutlined, AudioOutlined, VideoCameraOutlined, VideoCameraAddOutlined } from '@ant-design/icons';
import type { MeetingMemberDto } from '../types/meeting';

interface MemberListProps {
  members: MeetingMemberDto[];
  currentUserId?: string;
}

export function MemberList({ members, currentUserId }: MemberListProps) {
  return (
    <List
      className="member-list"
      dataSource={members}
      locale={{ emptyText: '等待成员加入' }}
      renderItem={(member) => (
        <List.Item>
          <List.Item.Meta
            avatar={<Avatar>{member.nickName?.slice(0, 1) || '会'}</Avatar>}
            title={
              <span className="member-title">
                {member.nickName}
                {member.userId === currentUserId && <Tag color="blue">我</Tag>}
                {member.memberType === 1 && <Tag color="gold">主持</Tag>}
              </span>
            }
            description={
              <span className="member-media">
                {member.openAudio ? <AudioOutlined /> : <AudioMutedOutlined />}
                {member.openVideo ? <VideoCameraOutlined /> : <VideoCameraAddOutlined />}
              </span>
            }
          />
        </List.Item>
      )}
    />
  );
}
