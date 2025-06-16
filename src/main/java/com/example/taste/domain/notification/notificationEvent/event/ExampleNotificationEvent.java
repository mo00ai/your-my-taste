package com.example.taste.domain.notification.notificationEvent.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExampleNotificationEvent {
	// 알림 메시지를 만들 때 사용할 데이터들.
	// 이런 식으로 만들면  content("이런" + data + "식의" + want + "알림을" + to + "만들어서" + user + "쓰는 방식")
	private final String data;
	private final Long want;
	private final Long to;
	private final int use;
	// 아니면 그냥 여기서는 string 하나만 넘기고, 저런 데이터 조립을 자신의 서비스 메서드에서 해도 됨.

	// 단 userId는 경우에 따라 넣어서 보내야 하는데,
	// subscribe 알림의 경우 구독 대상 유저 id. 이 유저를 팔로우한 모든 유저에게 알림이 전송됨
	// individual 알림의 경우 그냥 알림을 보낼 대상 유저 id. 이 유저에게'만' 알림이 전송됨.
	// system, marketing 알림을 보내고 싶으면 이 필드는 그냥 안 만들어도 됨. 그냥 알림 보낼 때 user 를 null 로 보낼 것.
	private final Long userId;
	// 모르겠으면 NotificationEventListener 클래스에서 예시 참조.


	//어차피 모든 필드 써야 하니 빌더는 클래스에 붙임
	public ExampleNotificationEvent(String data, Long want, Long to, int use, Long userId) {
		this.data = data;
		this.want = want;
		this.to = to;
		this.use = use;
		this.userId = userId;
	}
}
