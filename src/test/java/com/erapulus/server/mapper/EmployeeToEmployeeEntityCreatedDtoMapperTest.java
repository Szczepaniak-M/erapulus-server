package com.erapulus.server.mapper;

import com.erapulus.server.database.model.EmployeeEntity;
import com.erapulus.server.database.model.UserType;
import com.erapulus.server.dto.employee.EmployeeResponseDto;
import com.erapulus.server.mapper.employee.EmployeeEntityToResponseDtoMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeToEmployeeEntityCreatedDtoMapperTest {

    private static final String EMAIL = "example@gmail.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "firstName";
    private static final String PASSWORD = "password";
    private static final Integer ID = 1;
    private static final Integer UNIVERSITY_ID = 2;

    @Test
    void from_shouldMapEntityToDto() {
        // given
        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                                                      .type(UserType.EMPLOYEE)
                                                      .password(PASSWORD)
                                                      .id(ID)
                                                      .email(EMAIL)
                                                      .firstName(FIRST_NAME)
                                                      .lastName(LAST_NAME)
                                                      .universityId(UNIVERSITY_ID)
                                                      .build();

        // when
        EmployeeResponseDto result = new EmployeeEntityToResponseDtoMapper().from(employeeEntity);

        //then
        assertEquals(ID, result.id());
        assertEquals(EMAIL, result.email());
        assertEquals(FIRST_NAME, result.firstName());
        assertEquals(LAST_NAME, result.lastName());
        assertEquals(UNIVERSITY_ID, result.universityId());
    }
}
