package com.lingoquest.wordbook.service;

import com.lingoquest.common.exception.BizException;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import com.lingoquest.wordbook.entity.Word;
import com.lingoquest.wordbook.entity.WordBook;
import com.lingoquest.wordbook.mapper.UserWordProgressMapper;
import com.lingoquest.wordbook.mapper.WordBookMapper;
import com.lingoquest.wordbook.mapper.WordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WordBookService {

    private final WordBookMapper bookMapper;
    private final WordMapper wordMapper;
    private final UserWordProgressMapper progressMapper;
    private final UserMapper userMapper;

    public List<Map<String, Object>> listBooks(Long userId) {
        return bookMapper.listWithProgress(userId);
    }

    public List<Map<String, Object>> listWords(Long userId, Integer bookId, String state) {
        if (bookMapper.selectById(bookId) == null) {
            throw new BizException(40400, "Word book not found");
        }
        // Treat empty string as "no filter"
        String s = (state == null || state.isBlank()) ? null : state;
        return wordMapper.listForUser(userId, bookId, s);
    }

    @Transactional
    public void mark(Long userId, Long wordId, String state) {
        if (wordMapper.selectById(wordId) == null) {
            throw new BizException(40400, "Word not found");
        }
        progressMapper.upsert(userId, wordId, state);
    }

    @Transactional
    public void selectBook(Long userId, Integer bookId) {
        if (bookId != null && bookMapper.selectById(bookId) == null) {
            throw new BizException(40400, "Word book not found");
        }
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(40400, "User not found");
        u.setCurrentBookId(bookId);
        userMapper.updateById(u);
    }

    /** Returns a random word from the user's selected book, or null if none. */
    public Word randomFromCurrent(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getCurrentBookId() == null) return null;
        return wordMapper.pickRandom(u.getCurrentBookId());
    }

    public Word randomFromBook(Integer bookId) {
        return wordMapper.pickRandom(bookId);
    }

    public WordBook getBook(Integer bookId) {
        return bookMapper.selectById(bookId);
    }
}
