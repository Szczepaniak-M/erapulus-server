package com.erapulus.server.mapper.post;

import com.erapulus.server.database.model.PostEntity;
import com.erapulus.server.dto.post.PostResponseDto;
import com.erapulus.server.mapper.ValidatorUtils;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostEntityToResponseDtoMapperTest {

    private static final int ID = 1;
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final LocalDate DATE = LocalDate.now();
    private static final int UNIVERSITY_ID = 2;

    @Test
    void from_shouldMapEntityToDto() {
        // given
        PostEntity entity = PostEntity.builder()
                                      .id(ID)
                                      .title(TITLE)
                                      .content(CONTENT)
                                      .date(DATE)
                                      .universityId(UNIVERSITY_ID)
                                      .build();

        // when
        PostResponseDto result = new PostEntityToResponseDtoMapper().from(entity);

        //then
        assertEquals(ID, result.id());
        assertEquals(TITLE, result.title());
        assertEquals(CONTENT, result.content());
        assertEquals(DATE, result.date());
        assertEquals(UNIVERSITY_ID, result.universityId());
        assertTrue(ValidatorUtils.createValidator().validate(result).isEmpty());
    }
}