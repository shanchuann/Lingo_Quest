package com.lingoquest.wordbook.service;

import com.lingoquest.common.exception.BizException;
import com.lingoquest.user.entity.User;
import com.lingoquest.user.mapper.UserMapper;
import com.lingoquest.wordbook.entity.Word;
import com.lingoquest.wordbook.entity.WordBook;
import com.lingoquest.wordbook.mapper.UserWordProgressMapper;
import com.lingoquest.wordbook.mapper.WordBookMapper;
import com.lingoquest.wordbook.mapper.WordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordBookServiceTest {

    @Mock WordBookMapper bookMapper;
    @Mock WordMapper wordMapper;
    @Mock UserWordProgressMapper progressMapper;
    @Mock UserMapper userMapper;

    @InjectMocks WordBookService service;

    @Test
    @DisplayName("mark rejects unknown word")
    void rejectsUnknownWord() {
        when(wordMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.mark(1L, 999L, "MASTERED"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Word not found");
    }

    @Test
    @DisplayName("mark calls upsert with the right key + state")
    void markUpserts() {
        Word w = new Word();
        w.setId(5L);
        when(wordMapper.selectById(5L)).thenReturn(w);

        service.mark(1L, 5L, "LEARNING");

        verify(progressMapper, times(1)).upsert(1L, 5L, "LEARNING");
    }

    @Test
    @DisplayName("selectBook rejects unknown book")
    void selectBookValidates() {
        when(bookMapper.selectById(99)).thenReturn(null);

        assertThatThrownBy(() -> service.selectBook(1L, 99))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Word book not found");
    }

    @Test
    @DisplayName("selectBook(null) clears the user's current book without book lookup")
    void selectNullClears() {
        User u = new User();
        u.setId(1L);
        u.setCurrentBookId(5);
        when(userMapper.selectById(1L)).thenReturn(u);

        service.selectBook(1L, null);

        verify(userMapper, times(1)).updateById(u);
        assertThat(u.getCurrentBookId()).isNull();
        verify(bookMapper, times(0)).selectById(anyInt());
    }

    @Test
    @DisplayName("randomFromCurrent returns null when user has no currentBookId")
    void randomReturnsNullWhenNoCurrent() {
        User u = new User();
        u.setId(1L);
        u.setCurrentBookId(null);
        when(userMapper.selectById(1L)).thenReturn(u);

        assertThat(service.randomFromCurrent(1L)).isNull();
        verify(wordMapper, times(0)).pickRandom(anyInt());
    }

    @Test
    @DisplayName("randomFromCurrent delegates to WordMapper.pickRandom when book is set")
    void randomDelegates() {
        User u = new User();
        u.setId(1L);
        u.setCurrentBookId(7);
        Word picked = new Word();
        picked.setId(100L);
        when(userMapper.selectById(1L)).thenReturn(u);
        when(wordMapper.pickRandom(7)).thenReturn(picked);

        Word result = service.randomFromCurrent(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }
}
