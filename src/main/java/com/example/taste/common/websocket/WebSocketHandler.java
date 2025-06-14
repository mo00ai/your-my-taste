package com.example.taste.common.websocket;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class WebSocketHandler extends TextWebSocketHandler {
// 	private final ObjectMapper mapper;
// 	private final Set<WebSocketSession> sessions = new HashSet<>();        // 현재 연결된 소켓 세션 Set
// 	private final Map<Long, Set<WebSocketSession>> partyChatSessionMap = new HashMap<>(); // chatRoomId: {session1, session2}
//
// 	// 소켓 연결 확인
// 	@Override
// 	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
// 		log.info("{} 연결됨", session.getId());
// 		sessions.add(session);
// 	}
//
// 	// 소켓 통신에서 메세지 전송 처리
// 	@Override
// 	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
// 		String payload = message.getPayload();
//
// 		// 페이로드 --> ChatMessageDto 변환
// 		ChatMessageDto chatMessageDto = mapper.readValue(payload, ChatMessageDto.class);
// 		log.info("session {}", chatMessageDto.toString());
//
// 		Long partyId = chatMessageDto.getPartyId();
//
// 		// 메모리 상에 채팅방에 대한 세션 없으면 만들어줌
// 		if (!partyChatSessionMap.containsKey(partyId)) {
// 			partyChatSessionMap.put(partyId, new HashSet<>());
// 		}
// 		Set<WebSocketSession> chatRoomSession = partyChatSessionMap.get(partyId);
// 		sendMessageToChatRoom(chatMessageDto, chatRoomSession);
// 	}
//
// 	// 소켓 연결 종료
// 	@Override
// 	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
// 		log.info("웹소켓 세션: {} 연결 끊김", session.getId());
// 		sessions.remove(session);
// 	}
//
// 	public <T> void sendMessage(WebSocketSession session, T message) {
// 		try {
// 			session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
// 		} catch (IOException e) {
// 			log.error(e.getMessage(), e);
// 		}
// 	}
//
// 	private void sendMessageToChatRoom(ChatMessageDto chatMessageDto, Set<WebSocketSession> chatRoomSession) {
// 		chatRoomSession.parallelStream().forEach(session -> sendMessage(session, chatMessageDto));
// 	}
// }
