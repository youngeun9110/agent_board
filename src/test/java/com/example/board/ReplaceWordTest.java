package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReplaceWordTest {

    @Autowired
    PostService service;
    @Autowired
    PostRepository repository;

    private Post save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p);
    }

    @Test
    void 실제로_바뀐_글만_건수에_포함된다() {
        Post changed = save("kfc 후기", "오늘 kfc 먹음");
        Post sameCaseDiff = save("KFC 후기", "오늘 KFC 먹음"); // 검색은 매칭, 치환은 대소문자 구분이라 불변

        int count = service.replaceWord(List.of(changed.getId(), sameCaseDiff.getId()), "kfc", "bhc");

        assertThat(count).isEqualTo(1);
        assertThat(repository.findById(changed.getId()).orElseThrow().getTitle()).isEqualTo("bhc 후기");
        assertThat(repository.findById(sameCaseDiff.getId()).orElseThrow().getTitle()).isEqualTo("KFC 후기");
    }

    @Test
    void 없는_id는_건너뛰고_제목_또는_내용만_바뀌어도_집계된다() {
        Post titleOnly = save("kfc", "내용 없음");
        Post contentOnly = save("제목", "kfc 내용");

        int count = service.replaceWord(List.of(titleOnly.getId(), contentOnly.getId(), -1L), "kfc", "bhc");

        assertThat(count).isEqualTo(2);
    }
}
