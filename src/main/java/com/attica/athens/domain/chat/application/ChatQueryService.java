package com.attica.athens.domain.chat.application;

import com.attica.athens.domain.agora.dao.AgoraRepository;
import com.attica.athens.domain.agora.domain.Agora;
import com.attica.athens.domain.agoraUser.dao.AgoraUserRepository;
import com.attica.athens.domain.agoraUser.domain.AgoraUser;
import com.attica.athens.domain.chat.dao.ChatRepository;
import com.attica.athens.domain.chat.domain.Chat;
import com.attica.athens.domain.chat.dto.Cursor;
import com.attica.athens.domain.chat.dto.response.GetChatParticipants;
import com.attica.athens.domain.chat.dto.response.GetChatResponse;
import com.attica.athens.domain.chat.dto.response.GetChatResponse.ChatData;
import com.attica.athens.domain.chat.dto.response.SendMetaResponse;
import com.attica.athens.domain.chat.dto.response.SendMetaResponse.MetaData;
import com.attica.athens.domain.chat.dto.response.SendMetaResponse.ParticipantsInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final AgoraRepository agoraRepository;
    private final AgoraUserRepository agoraUserRepository;
    private final ChatRepository chatRepository;

    public SendMetaResponse sendMeta(Long agoraId) {

        MetaData metaData = new MetaData(
                findAgoraUserByType(agoraId),
                findAgoraById(agoraId)
        );

        return new SendMetaResponse(metaData);
    }

    private List<ParticipantsInfo> findAgoraUserByType(Long agoraId) {
        return agoraUserRepository.countAgoraUsersByType(agoraId);
    }

    public GetChatResponse getChatHistory(Long agoraId, Cursor cursor) {

        List<AgoraUser> agoraUsers = findAgoraUsersByAgoraId(agoraId);

        List<Chat> chats = findChatsByCursor(cursor, extractAgoraUserIds(agoraUsers));

        Map<Long, AgoraUser> agoraUserMap = mapAgoraUserIdToAgoraUser(agoraUsers);
        List<ChatData> chatData = mapChatsToChatData(agoraUserMap, chats);

        Long nextKey = getNextCursor(chats);

        return new GetChatResponse(chatData, cursor.next(nextKey));
    }

    private List<Chat> findChatsByCursor(Cursor cursor, List<Long> agoraUserIds) {

        Pageable pageable = PageRequest.of(0, cursor.getEffectiveSize());

        if (cursor.hasKey()) {
            return chatRepository.findByAgoraUserIdInAndIdLessThanOrderByIdDesc(agoraUserIds, cursor.key(),
                    pageable);
        }
        return chatRepository.findByAgoraUserIdInOrderByIdDesc(agoraUserIds, pageable);
    }

    private Long getNextCursor(List<Chat> chats) {

        if (chats.size() > 0 && chats.get(chats.size() - 1).getId().equals(1L)) {
            return Cursor.NONE_KEY;
        }

        return chats.stream()
                .mapToLong(Chat::getId)
                .min()
                .orElse(Cursor.NONE_KEY);
    }

    private static List<ChatData> mapChatsToChatData(Map<Long, AgoraUser> agoraUserMap, List<Chat> chats) {
        return chats.stream()
                .map(chat -> {
                    AgoraUser agoraUser = agoraUserMap.get(chat.getAgoraUser().getId());
                    return new ChatData(chat, agoraUser);
                })
                .toList();
    }

    private static Map<Long, AgoraUser> mapAgoraUserIdToAgoraUser(List<AgoraUser> agoraUsers) {
        return agoraUsers.stream()
                .collect(Collectors.toMap(AgoraUser::getId, agoraUser -> agoraUser));
    }

    private static List<Long> extractAgoraUserIds(List<AgoraUser> agoraUsers) {
        return agoraUsers.stream()
                .map(AgoraUser::getId)
                .toList();
    }

    private Agora findAgoraById(Long agoraId) {
        return agoraRepository.findById(agoraId)
                .orElseThrow(() -> new IllegalArgumentException("Agora is not exist."));
    }

    public GetChatParticipants getChatParticipants(Long agoraId) {

        return new GetChatParticipants(findAgoraUsersByAgoraId(agoraId), agoraId);
    }

    private List<AgoraUser> findAgoraUsersByAgoraId(Long agoraId) {
        return agoraUserRepository.findByAgoraId(agoraId);
    }
}
